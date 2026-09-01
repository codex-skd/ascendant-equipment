package com.skd.ascendantequipment.util;

import net.minecraft.util.Mth;
import net.minecraft.world.inventory.SimpleContainerData;

public class FloatReferenceHolder {
    boolean updating = false;
    float internal = 0.0F;
    final float min;
    final float max;

    SimpleContainerData array = new SimpleContainerData(3) {
        @Override
        public void set(int index, int value) {
            super.set(index, value);
            if (!FloatReferenceHolder.this.updating) {
                FloatReferenceHolder.this.updateFromArray();
            }
        }
    };

    public FloatReferenceHolder(float def, float min, float max) {
        this.min = min;
        this.max = max;
        this.set(def);
    }

    public SimpleContainerData getArray() {
        return this.array;
    }

    public float get() {
        return this.internal;
    }

    public void set(float f) {
        f = Mth.clamp(f, this.min, this.max);
        this.internal = f;
        this.updating = true;
        this.array.set(0, (int) f);
        this.array.set(1, (int) (f * 10.0F) % 10);
        this.array.set(2, (int) (f * 100.0F) % 10);
        this.updating = false;
    }

    private void updateFromArray() {
        this.internal = this.array.get(0) + this.array.get(1) / 10.0F + this.array.get(2) / 100.0F;
    }

    public float getMax() {
        return this.max;
    }

    public float getMin() {
        return this.min;
    }
}
