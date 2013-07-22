package com.trifork.clj_ds;

import java.util.Collection;

import com.trifork.clj_lang.IPersistentCollection;

public interface PersistentCollection<E> extends IPersistentCollection<E>, Collection<E> {
	
	PersistentCollection<E> cons(E o);

	PersistentCollection<E> empty();
	
	PersistentCollection<E> consAll(Iterable<? extends E> others);

}
