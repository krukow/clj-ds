package com.trifork.clj_ds;

import java.util.Set;

import com.trifork.clj_lang.IPersistentSet;

public interface PersistentSet<E> extends IPersistentSet<E>, PersistentCollection<E>, Set<E> {

	PersistentSet<E> disjoin(E key);
	
	PersistentSet<E> cons(E o);

	PersistentSet<E> empty();
	
	PersistentSet<E> disjoinAll(Iterable<? extends E> others);
	
	PersistentSet<E> consAll(Iterable<? extends E> others);

}
