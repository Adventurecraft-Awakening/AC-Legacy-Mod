package dev.adventurecraft.awakening.dom;

import java.util.Locale;

public class LocaleFormatException extends IllegalArgumentException {

    public LocaleFormatException(LocaleNode contents, String value) {
        super(String.format(Locale.ROOT, "Error parsing %s: %s", contents, value));
    }

    public LocaleFormatException(LocaleNode contents, int i) {
        super(String.format(Locale.ROOT, "Index %d out of range for %s", i, contents));
    }

    public LocaleFormatException(LocaleNode contents, Throwable throwable) {
        super(String.format(Locale.ROOT, "Error while parsing: %s", contents), throwable);
    }
}

