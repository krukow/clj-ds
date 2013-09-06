package com.trifork.clj_ds;

public interface PersistentStack<E> extends PersistentCollection<E> {

	PersistentStack<E> zero();

	PersistentStack<E> plus(E o);

	PersistentStack<E> minus();

	E peek();

}
