package com.trifork.clj_ds;

import java.util.List;

public interface PersistentList<E> extends PersistentStack<E>, List<E> {
	
	PersistentList<E> zero();

	PersistentList<E> plus(E val);

	PersistentList<E> minus();

}
