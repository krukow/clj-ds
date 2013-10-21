package com.github.krukow.clj_ds;

import com.github.krukow.clj_lang.PersistentArrayMap;
import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentHashSet;
import com.github.krukow.clj_lang.PersistentVector;

public final class Transients {
	
	// Factory Methods

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
	
	// Utilities
	
	public static <E> TransientVector<E> plusAll(TransientVector<E> vec, Iterable<? extends E> others) {
		TransientVector<E> tv = vec;
		for (E other : others) {
			tv = tv.plus(other);
		}
		return tv;
	}
	
	public static <E> TransientVector<E> plusAll(TransientVector<E> vec, E... others) {
		TransientVector<E> tv = vec;
		for (E other : others) {
			tv = tv.plus(other);
		}
		return tv;
	}
	
	public static <E> TransientSet<E> plusAll(TransientSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> tv = set;
		for (E other : others) {
			tv = tv.plus(other);
		}
		return tv;
	}
	
	public static <E> TransientSet<E> plusAll(TransientSet<E> set, E... others) {
		TransientSet<E> tv = set;
		for (E other : others) {
			tv = tv.plus(other);
		}
		return tv;
	}
	
	public static <E> TransientSet<E> minusAll(TransientSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> tv = set;
		for (E other : others) {
			tv = tv.minus(other);
		}
		return tv;
	}
	
	private Transients() {
	}
	
}
