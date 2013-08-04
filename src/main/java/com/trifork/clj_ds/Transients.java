package com.trifork.clj_ds;

import com.trifork.clj_lang.PersistentVector;

public class Transients {

	public static final <E> TransientVector<E> transientVector() {
		return PersistentVector.<E>emptyVector().asTransient();
	}
	
}
