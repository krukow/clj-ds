package com.trifork.clj_ds;

import java.util.Set;

import com.trifork.clj_lang.IPersistentSet;

public interface PersistentSet<E> extends IPersistentSet<E>, Set<E> {

	IPersistentSet<E> disjoin(E key);
	
	IPersistentSet<E> cons(E o);

	IPersistentSet<E> empty();

}
