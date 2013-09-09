package com.trifork.clj_ds;


public interface TransientVector<E> extends TransientCollection<E>, Indexed<E> {

	TransientVector<E> plus(E val);
	
	TransientVector<E> plusN(int i, E val);

	TransientVector<E> minus();
	
	TransientVector<E> pop();

	PersistentVector<E> persist();
	
}
