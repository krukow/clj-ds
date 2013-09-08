package com.trifork.clj_ds;

import java.util.Set;

public interface PersistentSet<E> extends PersistentCollection<E>, Set<E>, EditableCollection<E> {

	PersistentSet<E> zero();

	PersistentSet<E> plus(E val);

	PersistentSet<E> minus(E val);

}
