package dev.adventurecraft.awakening.text;

import java.text.NumberFormat;

public class BitSizeFormat extends SizeFormat {

    // TODO: add option for returning "bits/Kibits/etc..."

    private static final String[] SUFFIX_BINARY = new String[] {
        "b", "Kib", "Mib", "Gib", "Tib", "Pib", "Eib", "Zib", "Yib"
    };

    private static final String[] SUFFIX_METRIC = new String[] {
        "b", "kb", "Mb", "Gb", "Tb", "Pb", "Eb", "Zb", "Yb"
    };

    public BitSizeFormat(NumberFormat numberFormat, int numberBase) {
        super(numberFormat, numberBase);
    }

    public static BitSizeFormat getBinaryInstance() {
        return new BitSizeFormat(NumberFormat.getInstance(), 1024);
    }

    public static BitSizeFormat getMetricInstance() {
        return new BitSizeFormat(NumberFormat.getInstance(), 1000);
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
