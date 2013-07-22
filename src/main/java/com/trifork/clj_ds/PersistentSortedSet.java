package com.trifork.clj_ds;

import com.trifork.clj_lang.Sorted;

public interface PersistentSortedSet<E> extends PersistentSet<E>, Sorted<E> /*, SortedSet<E> */ {

	PersistentSortedSet<E> disjoin(E key);
	
	PersistentSortedSet<E> cons(E o);

	PersistentSortedSet<E> empty();
	
	PersistentSortedSet<E> disjoinAll(Iterable<? extends E> others);
	
	PersistentSortedSet<E> consAll(Iterable<? extends E> others);
	
}
