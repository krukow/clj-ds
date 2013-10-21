package com.github.krukow.clj_ds;

public interface TransientVector<E> extends TransientCollection<E>, Indexed<E> {

	/**
	 * @return A new {@link TransientVector} consisting of the elements of the
	 *         current {@link TransientVector} followed by the value val. (no
	 *         guarantees are made on the current collection).
	 */
	TransientVector<E> plus(E val);

	/**
	 * @return A new {@link TransientVector} consisting of the elements of
	 *         current {@link TransientSet} where the element at index i has
	 *         been replaced by the value val (no guarantees are made on the
	 *         current collection).
	 * @throw {@link IndexOutOfBoundsException} if the index i is greater that
	 *        the current maximum index.
	 */
	TransientVector<E> plusN(int i, E val);

	/**
	 * @return A new {@link TransientVector} consisting of the elements of the
	 *         current collection together with its last element (no guarantees
	 *         are made on the current collection).
	 */
	TransientVector<E> minus();

	TransientVector<E> pop();

	PersistentVector<E> persist();

}
