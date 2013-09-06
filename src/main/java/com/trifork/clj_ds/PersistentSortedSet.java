package com.trifork.clj_ds;

import com.trifork.clj_lang.Sorted;

public interface PersistentSortedSet<E> extends PersistentSet<E>, Sorted<E> /*, SortedSet<E> */ {

	PersistentSortedSet<E> zero();
	
	PersistentSortedSet<E> plus(E o);

	PersistentSortedSet<E> plusAll(Iterable<? extends E> others);

	PersistentSortedSet<E> minus(E key);

	PersistentSortedSet<E> minusAll(Iterable<? extends E> others);

}
