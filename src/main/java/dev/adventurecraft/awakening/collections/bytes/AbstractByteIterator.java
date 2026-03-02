/*
	* Copyright (C) 2002-2024 Sebastiano Vigna
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

import it.unimi.dsi.fastutil.bytes.ByteIterator;

/**
 * An abstract class facilitating the creation of type-specific iterators.
 *
 * @apiNote Up to version 8.5.0, this class was deprecated as abstract methods were turned into
 *          default methods of the type-specific interface. Now the class hosts finalized versions
 *          of default delegating methods such as {@link #forEachRemaining}.
 */
public abstract class AbstractByteIterator implements ByteIterator {
	protected AbstractByteIterator() {
	}
}
