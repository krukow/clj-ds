package com.trifork.clj_ds;

public interface TransientCollection<E> {

	TransientCollection<E> plus(E val);

	PersistentCollection<E> persist();

}
