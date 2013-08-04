package com.trifork.clj_ds;

import com.trifork.clj_lang.ITransientSet;

public interface TransientSet<E> extends ITransientSet<E> {

	TransientSet<E> conj(E val);
	
	TransientSet<E> disjoin(E key);

	PersistentSet<E> persistent();
	
}
