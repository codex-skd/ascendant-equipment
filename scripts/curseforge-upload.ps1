# CurseForge Upload Script
# Generic script that reads project variables and uploads the built JAR.
#
# Usage:
#   Desde la raíz del mod (estructura plana):
#     powershell -File ../codex-docs/scripts/curseforge-upload.ps1
#
#   Desde una subcarpeta de versión (estructura multi-versión):
#     powershell -File ../../codex-docs/scripts/curseforge-upload.ps1
#
#   O forzar ruta manual:
#     $env:MOD_PROJECT_PATH="G:\ruta\al\mod" powershell -File ..\codex-docs\scripts\curseforge-upload.ps1
#
# Requires:
#   - docs/curseforge/project_vars.md with: project_id, api_token, game_versions
#   - gradle.properties with: mod_id, mod_name, minecraft_version, mod_version
#   - docs/curseforge/versions/<version>.md with release notes in HTML

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$modPath = if ($env:MOD_PROJECT_PATH) { $env:MOD_PROJECT_PATH } else { Resolve-Path "$scriptPath/../.." }

# Si no encuentra gradle.properties, intenta con el CWD
if (-not (Test-Path "$modPath/gradle.properties")) {
    $cwd = (Get-Location).Path
    if (Test-Path "$cwd/gradle.properties") {
        $modPath = $cwd
    }
}

# Load project vars from markdown (format: key = value)
function Get-VarFromMd($file, $key) {
    $line = Select-String -Path $file -Pattern "^\s*$key\s*=\s*(.+)" | Select-Object -First 1
    if ($line) { return $line.Matches.Groups[1].Value.Trim() }
    return $null
}

# Load gradle.properties
$gradleFile = "$modPath/gradle.properties"
$modId = Get-VarFromMd $gradleFile "mod_id"
$modName = Get-VarFromMd $gradleFile "mod_name"
$mcVersion = Get-VarFromMd $gradleFile "minecraft_version"
$modVersion = Get-VarFromMd $gradleFile "mod_version"
$modFramework = Get-VarFromMd $gradleFile "mod_framework"
if (-not $modFramework) { $modFramework = "neoforge" }

# Load curseforge vars
$varsFile = "$modPath/docs/curseforge/project_vars.md"
$projectId = Get-VarFromMd $varsFile "project_id"
$gameVersionsStr = Get-VarFromMd $varsFile "game_versions"
$releaseType = Get-VarFromMd $varsFile "release_type"
if (-not $releaseType) { $releaseType = "beta" }

$apiToken = Get-VarFromMd $varsFile "api_token"

# Relations opcionales: formato "slug:type,slug:type".
# type es un enum string del API: embeddedLibrary, optionalDependency, requiredDependency, tool, incompatible, include.
# El API de CurseForge espera relations como { "projects": [ { "slug": ..., "type": ... } ] }.
$relationsStr = Get-VarFromMd $varsFile "relations"
$relationProjects = @()
if ($relationsStr) {
    foreach ($rel in ($relationsStr -split ",")) {
        $parts = $rel.Trim() -split ":"
        if ($parts.Count -ge 2 -and $parts[0].Trim() -ne "" -and $parts[1].Trim() -ne "") {
            $relationProjects += [PSCustomObject]@{ slug = $parts[0].Trim(); type = $parts[1].Trim() }
        }
    }
}
$relations = @{ projects = $relationProjects }

# @(...) forces an array even when there's only one game version, otherwise PowerShell
# unwraps a single-element pipeline result to a scalar and ConvertTo-Json emits an
# integer instead of a JSON array, which the CurseForge API rejects.
$gameVersions = @($gameVersionsStr -split "," | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" } | ForEach-Object { [int]$_ })

# Build paths
$jarName = "${modId}-${mcVersion}-${modFramework}-${modVersion}.jar"
$jarPath = "$modPath/build/libs/$jarName"
$changelogPath = "$modPath/docs/curseforge/versions/${modVersion}.md"

# Validate
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR not found at $jarPath" -ForegroundColor Red
    Write-Host "Run './gradlew.bat clean build' first"
    exit 1
}

if (-not (Test-Path $changelogPath)) {
    Write-Host "ERROR: Changelog not found at $changelogPath" -ForegroundColor Red
    exit 1
}

$changelog = [System.IO.File]::ReadAllText((Resolve-Path $changelogPath))

$metadata = @{
    displayName = "${modName} (${modVersion})"
    gameVersions = $gameVersions
    releaseType = $releaseType
    changelogType = "html"
    changelog = $changelog
}

if ($relationProjects.Count -gt 0) {
    $metadata.relations = $relations
}

$metadataJson = $metadata | ConvertTo-Json -Compress -Depth 5

# Build multipart form using HttpClient/MultipartFormDataContent — binary-safe.
# (A previous version of this script hand-built the multipart body by concatenating
# UTF8-encoded text bytes with the raw file bytes via Invoke-WebRequest -Body; that
# path silently corrupted binary JAR content on Windows PowerShell 5.1, producing an
# archive CurseForge's server-side scanner rejected with "Failed to verify archive".)
Add-Type -AssemblyName System.Net.Http

$fileItem = Get-Item -Path (Resolve-Path $jarPath)
$fileBytes = [System.IO.File]::ReadAllBytes($fileItem.FullName)

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.DefaultRequestHeaders.Add('X-Api-Token', $apiToken)

$multipart = New-Object System.Net.Http.MultipartFormDataContent
$multipart.Add((New-Object System.Net.Http.StringContent($metadataJson, [System.Text.Encoding]::UTF8, "application/json")), "metadata")

$fileContent = New-Object System.Net.Http.ByteArrayContent(,$fileBytes)
$fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/java-archive")
$multipart.Add($fileContent, "file", $fileItem.Name)

Write-Host "Uploading $jarName to CurseForge project $projectId..." -ForegroundColor Cyan

try {
    $response = $httpClient.PostAsync("https://minecraft.curseforge.com/api/projects/$projectId/upload-file", $multipart).GetAwaiter().GetResult()
    $responseBody = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if ($response.IsSuccessStatusCode) {
        Write-Host "Status: $([int]$response.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $responseBody"
        Write-Host ""
        Write-Host "✓ Upload successful!" -ForegroundColor Green
        Write-Host ""
        Write-Host "⚠ IMPORTANT: Manual step required:" -ForegroundColor Yellow
        Write-Host "  1. Go to CurseForge: https://www.curseforge.com/minecraft/mods/$projectId/files" -ForegroundColor Yellow
        Write-Host "  2. Edit the newly uploaded file (v$modVersion)" -ForegroundColor Yellow
        Write-Host "  3. Mark the environment as 'Client & Server'" -ForegroundColor Yellow
        Write-Host "  (The API does not expose this field, so it must be set manually.)" -ForegroundColor Yellow
    } else {
        Write-Host "Status: $([int]$response.StatusCode)" -ForegroundColor Red
        Write-Host "Body: $responseBody"
        exit 1
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    $httpClient.Dispose()
    $multipart.Dispose()
}
