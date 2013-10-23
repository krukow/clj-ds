package com.github.krukow.clj_ds;

/**
 * A {@link TransientCollection} is a collection that does not retain older
 * versions in order to gain efficient modifications. To manipulate a
 * {@link TransientCollection}, one must still used the new values produced by
 * the "destructive" operations plus, minus, etc. However, no guarantees are
 * made on the previous values of the collection.
 */
public interface TransientCollection<E> {

	/**
	 * @return A new {@link TransientCollection} consisting of all the elements
	 *         of the current collection together with the value val (no
	 *         guarantees are made on the current collection).
	 */
	TransientCollection<E> plus(E val);

	/**
	 * @return A corresponding {@link PersistentCollection} consisting of the
	 *         elements of the current {@link TransientCollection}.
	 */
	PersistentCollection<E> persist();

}
