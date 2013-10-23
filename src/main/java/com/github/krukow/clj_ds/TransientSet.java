package com.github.krukow.clj_ds;

public interface TransientSet<E> extends TransientCollection<E> {

	TransientSet<E> plus(E val);

	/**
	 * @return A new {@link TransientSet} consisting of all the elements of the
	 *         current collection without the value val (no guarantees are made
	 *         on the current set).
	 */
	TransientSet<E> minus(E val);

	PersistentSet<E> persist();

}
