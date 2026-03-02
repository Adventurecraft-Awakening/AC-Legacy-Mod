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

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteIterator;

import java.util.AbstractCollection;
import java.util.Collection;

/**
 * An abstract class providing basic methods for collections implementing a type-specific interface.
 *
 * <p>
 * In particular, this class provide {@link #iterator()}, {@code add()}, {@link #remove(Object)} and
 * {@link #contains(Object)} methods that just call the type-specific counterpart.
 *
 * <p>
 * <strong>Warning</strong>: Because of a name clash between the list and collection interfaces the
 * type-specific deletion method of a type-specific abstract collection is {@code rem()}, rather
 * then {@code remove()}. A subclass must thus override {@code rem()}, rather than {@code remove()},
 * to make all inherited methods work properly.
 */
public abstract class AbstractByteCollection extends AbstractCollection<Byte> implements ByteCollection {
	protected AbstractByteCollection() {
	}

	@Override
	public abstract ByteIterator iterator();

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final byte k) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation iterates over the elements in the collection, looking for the
	 *           specified element.
	 */
	@Override
	public boolean contains(final byte k) {
		final ByteIterator iterator = iterator();
		while (iterator.hasNext()) if (k == iterator.nextByte()) return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation iterates over the elements in the collection, looking for the
	 *           specified element and tries to remove it.
	 */
	@Override
	public boolean rem(final byte k) {
		final ByteIterator iterator = iterator();
		while (iterator.hasNext()) if (k == iterator.nextByte()) {
			iterator.remove();
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean add(final Byte key) {
		return ByteCollection.super.add(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
		return ByteCollection.super.contains(key);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
		return ByteCollection.super.remove(key);
	}

	@Override
	public byte[] toArray(byte[] a) {
		final int size = size();
		if (a == null) {
			a = new byte[size];
		} else if (a.length < size) {
			a = java.util.Arrays.copyOf(a, size);
		}
		ByteIterators.unwrap(iterator(), a);
		return a;
	}

	@Override
	public byte[] toByteArray() {
		final int size = size();
		if (size == 0) return ByteArrays.EMPTY_ARRAY;
		final byte a[] = new byte[size];
		ByteIterators.unwrap(iterator(), a);
		return a;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Please use {@code toArray()} instead&mdash;this method is redundant and will be
	 *             removed in the future.
	 */
	@Deprecated
	@Override
	public byte[] toByteArray(final byte[] a) {
		return toArray(a);
	}

	@Override
	public boolean addAll(final ByteCollection c) {
		boolean retVal = false;
		for (final ByteIterator i = c.iterator(); i.hasNext();) if (add(i.nextByte())) retVal = true;
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Byte> c) {
		if (c instanceof ByteCollection) {
			return addAll((ByteCollection)c);
		}
		return super.addAll(c);
	}

	@Override
	public boolean containsAll(final ByteCollection c) {
		for (final ByteIterator i = c.iterator(); i.hasNext();) if (!contains(i.nextByte())) return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		if (c instanceof ByteCollection) {
			return containsAll((ByteCollection)c);
		}
		return super.containsAll(c);
	}

	@Override
	public boolean removeAll(final ByteCollection c) {
		boolean retVal = false;
		for (final ByteIterator i = c.iterator(); i.hasNext();) if (rem(i.nextByte())) retVal = true;
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		if (c instanceof ByteCollection) {
			return removeAll((ByteCollection)c);
		}
		return super.removeAll(c);
	}

	@Override
	public boolean retainAll(final ByteCollection c) {
		boolean retVal = false;
		for (final ByteIterator i = iterator(); i.hasNext();) if (!c.contains(i.nextByte())) {
			i.remove();
			retVal = true;
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 *           collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		if (c instanceof ByteCollection) {
			return retainAll((ByteCollection)c);
		}
		return super.retainAll(c);
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ByteIterator i = iterator();
		int n = size();
		byte k;
		boolean first = true;
		s.append("{");
		while (n-- != 0) {
			if (first) first = false;
			else s.append(", ");
			k = i.nextByte();
			s.append(String.valueOf(k));
		}
		s.append("}");
		return s.toString();
	}
}
