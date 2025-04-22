package dev.adventurecraft.awakening.util;

/*
The xxHash32 implementation is based on the code published by Yann Collet:
https://raw.githubusercontent.com/Cyan4973/xxHash/5c174cfa4e45a42f94082dc0d4539b39696afea1/xxhash.c

  xxHash - Fast Hash algorithm
  Copyright (C) 2012-2016, Yann Collet

  BSD 2-Clause License (http://www.opensource.org/licenses/bsd-license.php)

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the following disclaimer
  in the documentation and/or other materials provided with the
  distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  You can contact the author at :
  - xxHash homepage: http://www.xxhash.com
  - xxHash source repository : https://github.com/Cyan4973/xxHash
*/

/**
 * Provide a way of diffusing bits from a limited input hash space.
 * <p>
 * For example, many enums only have a few possible hashes, only using the bottom few bits of the code.
 * Some collections are built on the assumption that hashes are spread over a larger space,
 * so diffusing the bits may help the collection work more efficiently.
 * </p>
 */
public final class HashCode {

    private static final int Prime1 = 0x9E3779B1;
    private static final int Prime2 = 0x85EBCA77;
    private static final int Prime3 = 0xC2B2AE3D;
    private static final int Prime4 = 0x27D4EB2F;
    private static final int Prime5 = 0x165667B1;

    private static final int seed = (int) RandomUtil.secureNextInt64();

    private static int mixEmptyState() {
        return seed + Prime5;
    }

    private static int mixState(int v1, int v2, int v3, int v4) {
        return Integer.rotateLeft(v1, 1) +
            Integer.rotateLeft(v2, 7) +
            Integer.rotateLeft(v3, 12) +
            Integer.rotateLeft(v4, 18);
    }

    private static int round(int hash, int input) {
        return Integer.rotateLeft(hash + (input * Prime2), 13) * Prime1;
    }

    private static int queueRound(int hash, int queuedValue) {
        return Integer.rotateLeft(hash + queuedValue * Prime3, 17) * Prime4;
    }

    private static int mixFinal(int hash) {
        hash ^= hash >>> 15;
        hash *= Prime2;
        hash ^= hash >>> 13;
        hash *= Prime3;
        hash ^= hash >>> 16;
        return hash;
    }

    public static int combine(int hc1) {
        int hash = mixEmptyState();
        hash += 4;

        hash = queueRound(hash, hc1);

        hash = mixFinal(hash);
        return hash;
    }

    public static int combine(int hc1, int hc2) {
        int hash = mixEmptyState();
        hash += 8;

        hash = queueRound(hash, hc1);
        hash = queueRound(hash, hc2);

        hash = mixFinal(hash);
        return hash;
    }

    public static int combine(int hc1, int hc2, int hc3) {
        int hash = mixEmptyState();
        hash += 12;

        hash = queueRound(hash, hc1);
        hash = queueRound(hash, hc2);
        hash = queueRound(hash, hc3);

        hash = mixFinal(hash);
        return hash;
    }

    public static int combine(int hc1, int hc2, int hc3, int hc4) {
        int v1 = seed + Prime1 + Prime2;
        int v2 = seed + Prime2;
        int v3 = seed;
        int v4 = seed - Prime1;

        v1 = round(v1, hc1);
        v2 = round(v2, hc2);
        v3 = round(v3, hc3);
        v4 = round(v4, hc4);

        int hash = mixState(v1, v2, v3, v4);
        hash += 16;

        hash = mixFinal(hash);
        return hash;
    }
}
