package com.github.krukow.clj_ds;

import java.util.Collection;

/**
 * The base interface for all persistent data structures. A persistent data
 * structure combines immutability with efficient runtime properties. For more
 * information read the <a
 * href="http://en.wikipedia.org/wiki/Persistent_data_structure">Persistent Data
 * Structure</a> article on Wikipedia.
 * 
 * @param <E>
 *            The type of objects held in the collection.
 */
public interface PersistentCollection<E> extends Collection<E> {

	/**
	 * @return An empty instance of this kind of collection.
	 */
	PersistentCollection<E> zero();

	/**
	 * @return A new collection consisting of all elements of the current
	 *         collection together with the value val.
	 */
	PersistentCollection<E> plus(E val);

}
