/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.adventurecraft.awakening.text;

import java.util.Arrays;
import java.util.Objects;

/**
 * A similarity algorithm indicating the percentage of matched characters between two character sequences.
 *
 * <p>
 * The Jaro measure is the weighted sum of percentage of matched characters from each file and transposed characters.
 * Winkler increased this measure for matching initial characters.
 * </p>
 * <p>
 * This implementation is based on the Jaro Winkler similarity algorithm from <a href="https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.
 * </p>
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 */
public class JaroWinklerSimilarity {

    private int leftLen, rightLen;
    private int matches, halfTranspositions, prefix;

    private int[] matchIndexes;
    private boolean[] matchFlags;

    public JaroWinklerSimilarity() {
    }

    /**
     * Computes the Jaro-Winkler string matches, half transpositions, prefix array.
     *
     * @param first the first input to be matched.
     * @param second the second input to be matched.
     */
    public void match(CharSequence first, CharSequence second) {
        this.leftLen = first.length();
        this.rightLen = second.length();

        final CharSequence max;
        final CharSequence min;
        if (first.length() > second.length()) {
            max = first;
            min = second;
        }
        else {
            max = second;
            min = first;
        }

        final int range = Math.max(max.length() / 2 - 1, 0);
        final int[] matchIndexes = new int[min.length()];
        Arrays.fill(matchIndexes, -1);
        final boolean[] matchFlags = new boolean[max.length()];
        int matches = 0;
        for (int mi = 0; mi < min.length(); mi++) {
            final char c1 = min.charAt(mi);
            for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
                if (!matchFlags[xi] && c1 == max.charAt(xi)) {
                    matchIndexes[mi] = xi;
                    matchFlags[xi] = true;
                    matches++;
                    break;
                }
            }
        }
        this.matches = matches;
        this.matchIndexes = matchIndexes;
        this.matchFlags = matchFlags;

        char[] ms1 = new char[matches];
        char[] ms2 = new char[matches];
        for (int i = 0, si = 0; i < min.length(); i++) {
            if (matchIndexes[i] != -1) {
                ms1[si] = min.charAt(i);
                si++;
            }
        }
        for (int i = 0, si = 0; i < max.length(); i++) {
            if (matchFlags[i]) {
                ms2[si] = max.charAt(i);
                si++;
            }
        }

        int halfTranspositions = 0;
        for (int mi = 0; mi < ms1.length; mi++) {
            if (ms1[mi] != ms2[mi]) {
                halfTranspositions++;
            }
        }
        this.halfTranspositions = halfTranspositions;

        int prefix = 0;
        for (int mi = 0; mi < Math.min(4, min.length()); mi++) {
            if (first.charAt(mi) != second.charAt(mi)) {
                break;
            }
            prefix++;
        }
        this.prefix = prefix;
    }

    public double getScore(double scalingFactor) {
        if (this.matches == 0) {
            return 0d;
        }
        final double m = this.matches;
        final double j = (m / this.leftLen + m / this.rightLen + (m - (double) this.halfTranspositions / 2) / m) / 3;
        return j < 0.7d ? j : j + scalingFactor * this.prefix * (1d - j);
    }

    public double getScore() {
        return this.getScore(0.1);
    }

    public double matchScore(CharSequence first, CharSequence second) {
        if (Objects.equals(first, second)) {
            return 1.0;
        }
        this.match(first, second);
        return this.getScore();
    }

    public int matches() {
        return this.matches;
    }

    public int prefix() {
        return this.prefix;
    }

    public int halfTranspositions() {
        return this.halfTranspositions;
    }

    public int[] matchIndices() {
        return this.matchIndexes;
    }

    public boolean[] matchFlags() {
        return this.matchFlags;
    }
}
