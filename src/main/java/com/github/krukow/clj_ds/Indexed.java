package com.github.krukow.clj_ds;

/**
 * This interface add indexed access to elements of a
 * {@link PersistentCollection}. "Random-access" runtime characteristics can be
 * expected for such collection.
 */
public interface Indexed<E> {

	/**
	 * @return The value at index i
	 * @throws IndexOutOfBoundsException
	 *             If i is not in the bounds of the collection.
	 */
	E nth(int i);

	/**
	 * @return The value at index i or the value notFound if i is not in the
	 *         bounds of the collection. This method nether throws an
	 *         {@link IndexOutOfBoundsException}.
	 */
	E nth(int i, E notFound);

}
