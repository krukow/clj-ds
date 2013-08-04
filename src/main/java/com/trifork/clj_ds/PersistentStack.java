package com.trifork.clj_ds;

import java.util.Collection;

import com.trifork.clj_lang.IPersistentStack;

public interface PersistentStack<E> extends IPersistentStack<E>, Collection<E> {
	
	PersistentStack<E> cons(E o);

	PersistentStack<E> empty();

	PersistentStack<E> pop();
	
}
