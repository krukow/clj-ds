package com.github.krukow.clj_ds;

import com.github.krukow.clj_lang.Sorted;

public interface PersistentSortedSet<E> extends PersistentSet<E>, Sorted<E> /*, SortedSet<E> */ {

	PersistentSortedSet<E> zero();
	
	PersistentSortedSet<E> plus(E o);

	PersistentSortedSet<E> minus(E key);

}
