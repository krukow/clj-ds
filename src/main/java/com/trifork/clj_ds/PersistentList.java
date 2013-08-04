package com.trifork.clj_ds;

import java.util.List;

import com.trifork.clj_lang.IPersistentList;

public interface PersistentList<E> extends PersistentStack<E>, List<E>, IPersistentList<E> {
	
	PersistentList<E> cons(E o);

	PersistentList<E> empty();

	PersistentList<E> pop();
	
	PersistentList<E> consAll(Iterable<? extends E> others);

}
