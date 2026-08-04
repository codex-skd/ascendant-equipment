package com.skd.ascendantequipment.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

public class EquipmentComparePositioner {
    protected final int scnWidth;
    protected final int scnHeight;
    protected Rect compareTo;
    protected Rect equipped;

    public EquipmentComparePositioner(int scnWidth, int scnHeight) {
        this.scnWidth = scnWidth;
        this.scnHeight = scnHeight;
    }

    public boolean position(Vector2ic equipPos, int equipW, int equipH, Vector2ic compPos, int compW, int compH) {
        this.equipped = rect(equipPos, equipW, equipH);
        this.compareTo = rect(compPos, compW, compH);
        if (canRenderNow()) {
            return true;
        }

        int padding = 12;
        int totalWidth = equipW + padding + compW;
        if (totalWidth > this.scnWidth) {
            if (totalWidth - padding > this.scnWidth) {
                return false;
            }
            padding = totalWidth - this.scnWidth;
        }

        int maxEquippedX = this.scnWidth - totalWidth - 6;
        int newEquippedX = Math.min(equipPos.x(), maxEquippedX);
        newEquippedX = Math.max(newEquippedX, 6);
        int compareX = newEquippedX + equipW + padding;
        if (compareX + compW >= this.scnWidth) {
            padding = Math.max(2, this.scnWidth - compareX - compW);
            compareX = newEquippedX + equipW + padding;
            if (compareX + compW >= this.scnWidth) {
                return false;
            }
        }

        int maxHeight = Math.max(equipH, compH);
        int maxY = this.scnHeight - maxHeight - 1;
        if (maxY < 1) {
            return false;
        }

        int sharedY = Math.min(Math.min(equipPos.y(), compPos.y()), maxY);
        sharedY = Math.max(sharedY, 1);
        this.equipped = rect(newEquippedX, sharedY, equipW, equipH);
        this.compareTo = rect(compareX, sharedY, compW, compH);
        return !this.equipped.overlaps(this.compareTo);
    }

    public Vector2i getComparePos() {
        return new Vector2i(this.compareTo.x, this.compareTo.y);
    }

    public Vector2i getEquippedPos() {
        return new Vector2i(this.equipped.x, this.equipped.y);
    }

    private Rect rect(Vector2ic pos, int width, int height) {
        return rect(pos.x(), pos.y(), width, height);
    }

    private Rect rect(int x, int y, int width, int height) {
        return new Rect(x, y, width, height, this.scnWidth, this.scnHeight);
    }

    private boolean canRenderNow() {
        return this.compareTo.isOnScreen() && this.equipped.isOnScreen() && !this.compareTo.overlaps(this.equipped);
    }

    protected record Rect(int x, int y, int width, int height, int scnWidth, int scnHeight) {
        boolean overlaps(Rect other) {
            return this.x() < other.x() + other.width
                && other.x() < this.x() + this.width
                && this.y() < other.y() + other.height
                && other.y() < this.y() + this.height;
        }

        boolean isOnScreen() {
            return this.x() >= 0 && this.x() + this.width <= this.scnWidth && this.y() >= 0 && this.y() + this.height <= this.scnHeight;
        }
    }
}
