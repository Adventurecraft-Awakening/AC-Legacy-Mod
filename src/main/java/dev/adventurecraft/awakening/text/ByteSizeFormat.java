package dev.adventurecraft.awakening.text;

import java.text.NumberFormat;

public class ByteSizeFormat extends SizeFormat {

    private static final String[] SUFFIX_BINARY = new String[] {
        "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"
    };

    private static final String[] SUFFIX_METRIC = new String[] {
        "B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
    };

    public ByteSizeFormat(NumberFormat numberFormat, int numberBase) {
        super(numberFormat, numberBase);
    }

    public static ByteSizeFormat getBinaryInstance() {
        return new ByteSizeFormat(NumberFormat.getInstance(), 1024);
    }

    public static ByteSizeFormat getMetricInstance() {
        return new ByteSizeFormat(NumberFormat.getInstance(), 1000);
    }

    @Override
    protected String[] getBinarySuffixes() {
        return SUFFIX_BINARY;
    }

    @Override
    protected String[] getMetricSuffixes() {
        return SUFFIX_METRIC;
    }
}
