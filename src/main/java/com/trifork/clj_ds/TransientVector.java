package com.trifork.clj_ds;

import com.trifork.clj_lang.ITransientVector;

public interface TransientVector<E> extends ITransientVector<E> {

	TransientVector<E> conj(E val);
	
	TransientVector<E> assocN(int i, E val);

	TransientVector<E> pop();

	PersistentVector<E> persistent();
	
}
