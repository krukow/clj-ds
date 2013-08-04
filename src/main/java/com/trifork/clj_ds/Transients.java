package com.trifork.clj_ds;

import com.trifork.clj_lang.PersistentArrayMap;
import com.trifork.clj_lang.PersistentHashMap;
import com.trifork.clj_lang.PersistentHashSet;
import com.trifork.clj_lang.PersistentVector;

public final class Transients {

	public static final <E> TransientVector<E> transientVector() {
		return PersistentVector.<E>emptyVector().asTransient();
	}
	
	public static final <E> TransientSet<E> transientHashSet() {
		return PersistentHashSet.<E>emptySet().asTransient();
	}
	
	@SuppressWarnings("unchecked")
	public static final <K, V> TransientMap<K, V> transientHashMap() {
		return PersistentHashMap.<K, V>emptyMap().asTransient();
	}
	
	@SuppressWarnings("unchecked")
	public static final <K, V> TransientMap<K, V> transientArrayMap() {
		return PersistentArrayMap.EMPTY.asTransient();
	}
	
	private Transients() {
	}
	
}
