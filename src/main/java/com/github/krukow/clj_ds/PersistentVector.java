package com.github.krukow.clj_ds;

import java.util.List;

/**
 * A {@link PersistentCollection} that has array-like runtime characteritics.
 * New elements are "added" at the end of the {@link PersistentVector}.
 */
public interface PersistentVector<E> extends PersistentStack<E>, List<E>, Indexed<E>, Comparable<E>, EditableCollection<E> {

	PersistentVector<E> zero();

	/**
	 * @return A new {@link PersistentVector} consisting of the elements of the
	 *         current {@link PersistentVector} followed by the value val.
	 */
	PersistentVector<E> plus(E val);

	/**
	 * @return A new {@link PersistentVector} consisting of the elements of
	 *         current {@link PersistentVector} where the element at index i has
	 *         been replaced by the value val.
	 * @throw {@link IndexOutOfBoundsException} if the index i is greater that
	 *        the current maximum index.
	 */
	PersistentVector<E> plusN(int i, E val);

	/**
	 * @return A new {@link PersistentVector} consisting of the elements of
	 *         the current {@link PersistentVector} without its last element.
	 */
	PersistentVector<E> minus();

	TransientVector<E> asTransient();

}
