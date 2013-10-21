package com.github.krukow.clj_ds;

import java.util.Set;

public interface PersistentSet<E> extends PersistentCollection<E>, Set<E>, EditableCollection<E> {

	PersistentSet<E> zero();

	PersistentSet<E> plus(E val);

	/**
	 * @return A new {@link PersistentSet} that consists of the elements of the
	 *         current {@link PersistentSet} without the value val.
	 */
	PersistentSet<E> minus(E val);

}
