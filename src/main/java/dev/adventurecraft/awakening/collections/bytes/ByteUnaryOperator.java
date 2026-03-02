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

import java.util.function.UnaryOperator;

/**
 * A type-specific {@link UnaryOperator}; provides methods operating both on objects and on
 * primitives.
 *
 * @see UnaryOperator
 * @since 8.5.0
 */
@FunctionalInterface
public interface ByteUnaryOperator extends UnaryOperator<Byte>, java.util.function.IntUnaryOperator {
	/**
	 * Computes the operator on the given input.
	 *
	 * @param x the input.
	 * @return the output of the operator on the given input.
	 */
	byte apply(byte x);

	/**
	 * Returns a {@code UnaryOperator} that always returns the input unmodified.
	 * 
	 * @see UnaryOperator#identity()
	 */
	public static ByteUnaryOperator identity() {
		// Java is smart enough to see this lambda is stateless and will return the same instance every
		// time.
		return i -> i;
	}

	/**
	 * Returns a {@code UnaryOperator} that always returns the arithmetic negation of the input.
	 * 
	 * @implNote As with all negation, be wary of unexpected behavior near the minimum value of the data
	 *           type. For example, -{@link Integer#MIN_VALUE} will result in {@link Integer#MIN_VALUE}
	 *           (still negative), as the positive value of {@link Integer#MIN_VALUE} is too big for
	 *           {@code int} (it would be 1 greater then {@link Integer#MAX_VALUE}).
	 */
	public static ByteUnaryOperator negation() {
		// Annoyingly, negating a byte (or similar) results in an int.
		return i -> (byte)-i;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This default implementation delegates to {@link #apply} after narrowing down the
	 *           argument to the actual key type, throwing an exception if the argument cannot be
	 *           represented in the restricted domain. This is done for interoperability with the Java 8
	 *           function environment. The use of this method discouraged, as unexpected errors can
	 *           occur.
	 *
	 * @throws IllegalArgumentException If the given operands are not an element of the key domain.
	 * @since 8.5.0
	 * @deprecated Please use {@link #apply}.
	 */
	@Deprecated
	@Override
	default int applyAsInt(final int x) {
		return apply(it.unimi.dsi.fastutil.SafeMath.safeIntToByte(x));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default Byte apply(final Byte x) {
		return apply(x.byteValue());
	}
}
