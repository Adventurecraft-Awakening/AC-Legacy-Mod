package dev.adventurecraft.awakening.common;

public class WorldGenProperties {
    public boolean useImages = true;
    public double mapSize = 250.0D;
    public int waterLevel = 64;
    public double fractureHorizontal = 1.0D;
    public double fractureVertical = 1.0D;
    public double maxAvgDepth = 0.0D;
    public double maxAvgHeight = 0.0D;
    public double volatility1 = 1.0D;
    public double volatility2 = 1.0D;
    public double volatilityWeight1 = 0.0D;
    public double volatilityWeight2 = 1.0D;

    public void copyTo(WorldGenProperties props) {
        props.useImages = this.useImages;
        props.mapSize = this.mapSize;
        props.waterLevel = this.waterLevel;
        props.fractureHorizontal = this.fractureHorizontal;
        props.fractureVertical = this.fractureVertical;
        props.maxAvgDepth = this.maxAvgDepth;
        props.maxAvgHeight = this.maxAvgHeight;
        props.volatility1 = this.volatility1;
        props.volatility2 = this.volatility2;
        props.volatilityWeight1 = this.volatilityWeight1;
        props.volatilityWeight2 = this.volatilityWeight2;
    }
}
