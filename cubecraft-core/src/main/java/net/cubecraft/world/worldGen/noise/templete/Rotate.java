package net.cubecraft.world.worldGen.noise.templete;

import net.cubecraft.world.worldGen.noise.Noise;

public class Rotate
extends Noise {
    private final Noise synth;
    private final double sin;
    private final double cos;

    public Rotate(Noise synth, double angle) {
        this.synth = synth;
        this.sin = Math.sin(angle);
        this.cos = Math.cos(angle);
    }

    @Override
    public double getValue(double x, double y) {
        return this.synth.getValue(x * this.cos + y * this.sin, y * this.cos - x * this.sin);
    }
}
