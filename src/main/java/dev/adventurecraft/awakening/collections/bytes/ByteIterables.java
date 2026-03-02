/*
	* Copyright (C) 2020-2024 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*/
package dev.adventurecraft.awakening.collections.bytes;

import it.unimi.dsi.fastutil.bytes.ByteIterable;

/**
 * A class providing static methods and objects that do useful things with type-specific iterables.
 *
 * @see Iterable
 */
public final class ByteIterables {
	private ByteIterables() {
	}

	/**
	 * Counts the number of elements returned by a type-specific iterable.
	 *
	 * @param iterable an iterable.
	 * @return the number of elements returned by {@code iterable}.
	 */
	public static long size(final ByteIterable iterable) {
		long c = 0;
		for (@SuppressWarnings("unused")
		final byte dummy : iterable) c++;
		return c;
	}
}
