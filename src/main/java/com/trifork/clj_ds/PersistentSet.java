package com.trifork.clj_ds;

import java.util.Set;

public interface PersistentSet<E> extends PersistentCollection<E>, Set<E> {

	PersistentSet<E> zero();

	PersistentSet<E> plus(E val);
	
	PersistentSet<E> plusAll(Iterable<? extends E> vals);

	PersistentSet<E> minus(E val);

	PersistentSet<E> minusAll(Iterable<? extends E> others);

}
