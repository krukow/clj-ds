package com.trifork.clj_ds;


public interface TransientSet<E> extends TransientCollection<E> {

	TransientSet<E> plus(E val);
	
	TransientSet<E> minus(E val);

	PersistentSet<E> persist();
	
}
