package com.trifork.clj_ds;

import com.trifork.clj_lang.PersistentHashSet;
import com.trifork.clj_lang.PersistentVector;

public class Transients {

	public static final <E> TransientVector<E> transientVector() {
		return PersistentVector.<E>emptyVector().asTransient();
	}
	
	public static final <E> TransientSet<E> transientHashSet() {
		return PersistentHashSet.<E>emptySet().asTransient();
	}
	
}
