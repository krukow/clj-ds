package com.github.krukow.clj_ds;

import java.util.List;

/**
 * A persistent linked list. It implements the {@link List} interface without
 * the optional destructive operations.
 */
public interface PersistentList<E> extends PersistentStack<E>, List<E> {

	PersistentList<E> zero();

	/**
	 * @return A new {@link PersistentList} consisting of the value val followed
	 * by the element of the current {@link PersistentList}.
	 */
	PersistentList<E> plus(E val);

	/**
	 * @return A new {@link PersistentList} consisting of the elements of the current
	 * {@link PersistentList} without its first element.
	 */
	PersistentList<E> minus();

}
