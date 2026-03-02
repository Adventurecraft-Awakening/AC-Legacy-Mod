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

import it.unimi.dsi.fastutil.bytes.*;
import it.unimi.dsi.fastutil.bytes.ByteIterable;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.bytes.ByteSpliterators;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import it.unimi.dsi.fastutil.ints.IntSpliterators;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A class providing static methods and objects that do useful things with type-specific
 * collections.
 *
 * @see java.util.Collections
 */
public final class ByteCollections {
	private ByteCollections() {
	}

	/* Only in the EmptyCollection class, where performance is critical, do we override the
	 * deprecated, Object based functional methods. For the rest, we just override the
	 * non-deprecated type-specific method, and let the default method from the interface
	 * filter into that. This is an extra method call and lambda creation, but it isn't worth
	 * complexifying the code generation for a case that is already marked as being inefficient.
	 */
	/**
	 * An immutable class representing an empty type-specific collection.
	 *
	 * <p>
	 * This class may be useful to implement your own in case you subclass a type-specific collection.
	 */
	public abstract static class EmptyCollection extends AbstractByteCollection {
		protected EmptyCollection() {
		}

		@Override
		public boolean contains(byte k) {
			return false;
		}

		@Override
		public Object[] toArray() {
			return ObjectArrays.EMPTY_ARRAY;
		}

		@Override
		public <T> T[] toArray(T[] array) {
			if (array.length > 0) array[0] = null;
			return array;
		}

		@Override

		public ByteBidirectionalIterator iterator() {
			return ByteIterators.EMPTY_ITERATOR;
		}

		@Override

		public it.unimi.dsi.fastutil.bytes.ByteSpliterator spliterator() {
			return ByteSpliterators.EMPTY_SPLITERATOR;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof Collection)) return false;
			return ((Collection<?>)o).isEmpty();
		}

		@Deprecated
		@Override
		public void forEach(final Consumer<? super Byte> action) {
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			return c.isEmpty();
		}

		@Override
		public boolean addAll(final Collection<? extends Byte> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Deprecated
		@Override
		public boolean removeIf(final java.util.function.Predicate<? super Byte> filter) {
			Objects.requireNonNull(filter);
			return false;
		}

		@Override
		public byte[] toByteArray() {
			return ByteArrays.EMPTY_ARRAY;
		}

		/* {@inheritDoc}
		 * @deprecated Please use {@code toArray()} instead&mdash;this method is redundant and will be removed in the future.
		 */
		@Deprecated
		@Override
		public byte[] toByteArray(byte[] a) {
			return a;
		}

		@Override
		public void forEach(final ByteConsumer action) {
		}

		@Override
		public boolean containsAll(final ByteCollection c) {
			return c.isEmpty();
		}

		@Override
		public boolean addAll(final ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(final ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeIf(final BytePredicate filter) {
			Objects.requireNonNull(filter);
			return false;
		}

		@Override
		public IntIterator intIterator() {
			return IntIterators.EMPTY_ITERATOR;
		}

		@Override
		public IntSpliterator intSpliterator() {
			return IntSpliterators.EMPTY_SPLITERATOR;
		}
	}

	/** A synchronized wrapper class for collections. */
	static class SynchronizedCollection implements ByteCollection, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final ByteCollection collection;
		protected final Object sync;

		protected SynchronizedCollection(final ByteCollection c, final Object sync) {
			this.collection = Objects.requireNonNull(c);
			this.sync = sync;
		}

		protected SynchronizedCollection(final ByteCollection c) {
			this.collection = Objects.requireNonNull(c);
			this.sync = this;
		}

		@Override
		public boolean add(final byte k) {
			synchronized (sync) {
				return collection.add(k);
			}
		}

		@Override
		public boolean contains(final byte k) {
			synchronized (sync) {
				return collection.contains(k);
			}
		}

		@Override
		public boolean rem(final byte k) {
			synchronized (sync) {
				return collection.rem(k);
			}
		}

		@Override
		public int size() {
			synchronized (sync) {
				return collection.size();
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (sync) {
				return collection.isEmpty();
			}
		}

		@Override
		public byte[] toByteArray() {
			synchronized (sync) {
				return collection.toByteArray();
			}
		}

		@Override
		public Object[] toArray() {
			synchronized (sync) {
				return collection.toArray();
			}
		}

		/* {@inheritDoc}
		 * @deprecated Please use {@code toArray()} instead&mdash;this method is redundant and will be removed in the future.
		 */
		@Deprecated
		@Override
		public byte[] toByteArray(final byte[] a) {
			return toArray(a);
		}

		@Override
		public byte[] toArray(final byte[] a) {
			synchronized (sync) {
				return collection.toArray(a);
			}
		}

		@Override
		public boolean addAll(final ByteCollection c) {
			synchronized (sync) {
				return collection.addAll(c);
			}
		}

		@Override
		public boolean containsAll(final ByteCollection c) {
			synchronized (sync) {
				return collection.containsAll(c);
			}
		}

		@Override
		public boolean removeAll(final ByteCollection c) {
			synchronized (sync) {
				return collection.removeAll(c);
			}
		}

		@Override
		public boolean retainAll(final ByteCollection c) {
			synchronized (sync) {
				return collection.retainAll(c);
			}
		}

		@Override
		@Deprecated
		public boolean add(final Byte k) {
			synchronized (sync) {
				return collection.add(k);
			}
		}

		@Override
		@Deprecated
		public boolean contains(final Object k) {
			synchronized (sync) {
				return collection.contains(k);
			}
		}

		@Override
		@Deprecated
		public boolean remove(final Object k) {
			synchronized (sync) {
				return collection.remove(k);
			}
		}

		@Override
		public IntIterator intIterator() {
			return collection.intIterator();
		}

		@Override
		public IntSpliterator intSpliterator() {
			return collection.intSpliterator();
		}

		@Override
		public java.util.stream.IntStream intStream() {
			return collection.intStream();
		}

		@Override
		public java.util.stream.IntStream intParallelStream() {
			return collection.intParallelStream();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			synchronized (sync) {
				return collection.toArray(a);
			}
		}

		@Override
		public it.unimi.dsi.fastutil.bytes.ByteIterator iterator() {
			return collection.iterator();
		}

		@Override
		public it.unimi.dsi.fastutil.bytes.ByteSpliterator spliterator() {
			return collection.spliterator();
		}

		@Deprecated
		@Override
		public java.util.stream.Stream<Byte> stream() {
			return collection.stream();
		}

		@Deprecated
		@Override
		public java.util.stream.Stream<Byte> parallelStream() {
			return collection.parallelStream();
		}

		@Override
		public void forEach(final ByteConsumer action) {
			synchronized (sync) {
				collection.forEach(action);
			}
		}

		@Override
		public boolean addAll(final Collection<? extends Byte> c) {
			synchronized (sync) {
				return collection.addAll(c);
			}
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			synchronized (sync) {
				return collection.containsAll(c);
			}
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			synchronized (sync) {
				return collection.removeAll(c);
			}
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			synchronized (sync) {
				return collection.retainAll(c);
			}
		}

		@Override
		public boolean removeIf(final BytePredicate filter) {
			synchronized (sync) {
				return collection.removeIf(filter);
			}
		}

		@Override
		public void clear() {
			synchronized (sync) {
				collection.clear();
			}
		}

		@Override
		public String toString() {
			synchronized (sync) {
				return collection.toString();
			}
		}

		@Override
		public int hashCode() {
			synchronized (sync) {
				return collection.hashCode();
			}
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			synchronized (sync) {
				return collection.equals(o);
			}
		}

		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			synchronized (sync) {
				s.defaultWriteObject();
			}
		}
	}

	/**
	 * Returns a synchronized collection backed by the specified collection.
	 *
	 * @param c the collection to be wrapped in a synchronized collection.
	 * @return a synchronized view of the specified collection.
	 * @see java.util.Collections#synchronizedCollection(Collection)
	 */
	public static ByteCollection synchronize(final ByteCollection c) {
		return new SynchronizedCollection(c);
	}

	/**
	 * Returns a synchronized collection backed by the specified collection, using an assigned object to
	 * synchronize.
	 *
	 * @param c the collection to be wrapped in a synchronized collection.
	 * @param sync an object that will be used to synchronize the list access.
	 * @return a synchronized view of the specified collection.
	 * @see java.util.Collections#synchronizedCollection(Collection)
	 */
	public static ByteCollection synchronize(final ByteCollection c, final Object sync) {
		return new SynchronizedCollection(c, sync);
	}

	/** An unmodifiable wrapper class for collections. */
	static class UnmodifiableCollection implements ByteCollection, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final ByteCollection collection;

		protected UnmodifiableCollection(final ByteCollection c) {
			this.collection = Objects.requireNonNull(c);
		}

		@Override
		public boolean add(final byte k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean rem(final byte k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return collection.size();
		}

		@Override
		public boolean isEmpty() {
			return collection.isEmpty();
		}

		@Override
		public boolean contains(final byte o) {
			return collection.contains(o);
		}

		@Override
		public it.unimi.dsi.fastutil.bytes.ByteIterator iterator() {
			return ByteIterators.unmodifiable(collection.iterator());
		}

		@Override
		public it.unimi.dsi.fastutil.bytes.ByteSpliterator spliterator() {
			return collection.spliterator();
		}

		@Deprecated
		@Override
		public java.util.stream.Stream<Byte> stream() {
			return collection.stream();
		}

		@Deprecated
		@Override
		public java.util.stream.Stream<Byte> parallelStream() {
			return collection.parallelStream();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			return collection.toArray(a);
		}

		@Override
		public Object[] toArray() {
			return collection.toArray();
		}

		@Override
		public void forEach(final ByteConsumer action) {
			collection.forEach(action);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return collection.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Byte> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeIf(final BytePredicate filter) {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated
		public boolean add(final Byte k) {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated
		public boolean contains(final Object k) {
			return collection.contains(k);
		}

		@Override
		@Deprecated
		public boolean remove(final Object k) {
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] toByteArray() {
			return collection.toByteArray();
		}

		/* {@inheritDoc}
		 * @deprecated Please use {@code toArray()} instead&mdash;this method is redundant.
		 */
		@Deprecated
		@Override
		public byte[] toByteArray(final byte[] a) {
			return toArray(a);
		}

		@Override
		public byte[] toArray(final byte[] a) {
			return collection.toArray(a);
		}

		@Override
		public boolean containsAll(ByteCollection c) {
			return collection.containsAll(c);
		}

		@Override
		public boolean addAll(ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(ByteCollection c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IntIterator intIterator() {
			return collection.intIterator();
		}

		@Override
		public IntSpliterator intSpliterator() {
			return collection.intSpliterator();
		}

		@Override
		public java.util.stream.IntStream intStream() {
			return collection.intStream();
		}

		@Override
		public java.util.stream.IntStream intParallelStream() {
			return collection.intParallelStream();
		}

		@Override
		public String toString() {
			return collection.toString();
		}

		@Override
		public int hashCode() {
			return collection.hashCode();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) return true;
			return collection.equals(o);
		}
	}

	/**
	 * Returns an unmodifiable collection backed by the specified collection.
	 *
	 * @param c the collection to be wrapped in an unmodifiable collection.
	 * @return an unmodifiable view of the specified collection.
	 * @see java.util.Collections#unmodifiableCollection(Collection)
	 */
	public static ByteCollection unmodifiable(final ByteCollection c) {
		return new UnmodifiableCollection(c);
	}

	/** A collection wrapper class for iterables. */
	public static class IterableCollection extends AbstractByteCollection implements java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;
		protected final it.unimi.dsi.fastutil.bytes.ByteIterable iterable;

		protected IterableCollection(final it.unimi.dsi.fastutil.bytes.ByteIterable iterable) {
			this.iterable = Objects.requireNonNull(iterable);
		}

		@Override
		public int size() {
			long size = iterable.spliterator().getExactSizeIfKnown();
			if (size >= 0) return (int)Math.min(Integer.MAX_VALUE, size);
			int c = 0;
			final it.unimi.dsi.fastutil.bytes.ByteIterator iterator = iterator();
			while (iterator.hasNext()) {
				iterator.nextByte();
				c++;
			}
			return c;
		}

		@Override
		public boolean isEmpty() {
			return !iterable.iterator().hasNext();
		}

		@Override
		public ByteIterator iterator() {
			return iterable.iterator();
		}

		@Override
		public ByteSpliterator spliterator() {
			return iterable.spliterator();
		}

		@Override
		public IntIterator intIterator() {
			return iterable.intIterator();
		}

		@Override
		public IntSpliterator intSpliterator() {
			return iterable.intSpliterator();
		}
	}

	/**
	 * Returns an unmodifiable collection backed by the specified iterable.
	 *
	 * @param iterable the iterable object to be wrapped in an unmodifiable collection.
	 * @return an unmodifiable collection view of the specified iterable.
	 */
	public static ByteCollection asCollection(final ByteIterable iterable) {
		if (iterable instanceof ByteCollection) return (ByteCollection)iterable;
		// TODO test for Collection but not our collection, and make a wrapper for it.
		return new IterableCollection(iterable);
	}
}
