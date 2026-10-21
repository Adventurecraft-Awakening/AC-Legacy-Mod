package dev.adventurecraft.awakening.earlyrisers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reviewed compatibility contract; public native helpers never become implicit GL entry points. */
public final class LegacyGlAbi {
    public static final String RESOURCE = "assets/adventurecraft/legacy-gl-abi.txt";
    public enum Support { EMULATED, FALLBACK, UNSUPPORTED }
    public record Entry(String signature, String target, Support support) {
        public String descriptor() { return signature.substring(signature.indexOf('(')); }
    }
    private static final java.util.regex.Pattern SIGNATURE = java.util.regex.Pattern.compile(
        "gl[A-Za-z_$][A-Za-z0-9_$]*\\((?:\\[*[ZBCSIJFD]|\\[*L[^;()]+;)*\\)(?:V|\\[*[ZBCSIJFD]|\\[*L[^;()]+;)");
    private LegacyGlAbi() { }

    public static Map<String, Entry> load() throws IOException {
        try (InputStream source = LegacyGlAbi.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (source == null) throw new IOException("Missing legacy GL ABI contract: " + RESOURCE);
            return read(source);
        }
    }

    public static Map<String, Entry> read(InputStream source) throws IOException {
        Map<String, Entry> entries = new LinkedHashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            int lineNumber = 0;
            for (String line; (line = reader.readLine()) != null;) {
                lineNumber++;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\s+");
                if (fields.length != 3 || !SIGNATURE.matcher(fields[1]).matches()) {
                    throw new IOException("Invalid legacy GL ABI entry at line " + lineNumber);
                }
                Support support;
                try { support = Support.valueOf(fields[0].toUpperCase(java.util.Locale.ROOT)); }
                catch (IllegalArgumentException error) { throw new IOException("Invalid legacy GL ABI support at line " + lineNumber, error); }
                if ((support == Support.UNSUPPORTED) != fields[2].equals("-")) {
                    throw new IOException("Legacy GL ABI target/support mismatch at line " + lineNumber);
                }
                Entry entry = new Entry(fields[1], fields[2], support);
                if (entries.putIfAbsent(entry.signature(), entry) != null) throw new IOException("Duplicate legacy GL ABI signature: " + entry.signature());
            }
        }
        return Collections.unmodifiableMap(entries);
    }
}
