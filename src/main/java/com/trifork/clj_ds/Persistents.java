package com.trifork.clj_ds;

import java.util.Map;

import com.trifork.clj_lang.PersistentArrayMap;
import com.trifork.clj_lang.PersistentHashMap;
import com.trifork.clj_lang.PersistentHashSet;
import com.trifork.clj_lang.PersistentTreeMap;
import com.trifork.clj_lang.PersistentTreeSet;


public final class Persistents {
	
	// Factory Methods
	
	public static <E> PersistentVector<E> vector() {
		return com.trifork.clj_lang.PersistentVector.emptyVector();
	}
	
	public static <E> PersistentVector<E> vector(E val) {
		return Persistents.<E>vector().plus(val);
	}

	public static <E> PersistentVector<E> vector(E... vals) {
		return com.trifork.clj_lang.PersistentVector.create(vals);
	}
	
	public static <E> PersistentVector<E> vector(Iterable<? extends E> vals) {
		return com.trifork.clj_lang.PersistentVector.create(vals);
	}
	
	public static <E> PersistentSet<E> hashSet() {
		return PersistentHashSet.emptySet();
	}
	
	public static <E> PersistentSet<E> hashSet(E val) {
		return Persistents.<E>hashSet().plus(val);
	}
	
	public static <E> PersistentSet<E> hashSet(E... vals) {
		return PersistentHashSet.create(vals);
	}
	
	public static <E> PersistentSet<E> hashSet(Iterable<? extends E> vals) {
		return PersistentHashSet.create(vals);
	}
	
	public static <K, V> PersistentMap<K, V> hashMap() {
		return PersistentHashMap.emptyMap();
	}
	
	public static <K, V> PersistentMap<K, V> hashMap(K key, V val) {
		return Persistents.<K, V>hashMap().plus(key, val);
	}
	
	public static <K, V> PersistentMap<K, V> hashMap(Object... keyValues) {
		return PersistentHashMap.create(keyValues);
	}
	
	public static <K, V> PersistentMap<K, V> hashMap(Map<? extends K, ? extends V> init) {
		return PersistentHashMap.create(init);
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> PersistentSortedMap<K, V> treeMap() {
		return PersistentTreeMap.EMPTY;
	}
	
	public static <K, V> PersistentSortedMap<K, V> treeMap(K key, V val) {
		return Persistents.<K, V>treeMap().plus(key, val);
	}
	
	public static <K, V> PersistentSortedMap<K, V> treeMap(Map<? extends K, ? extends V> init) {
		return PersistentTreeMap.create(init);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> PersistentSortedSet<E> treeSet() {
		return PersistentTreeSet.EMPTY;
	}
	
	/*
	public static <E> PersistentSortedSet<E> treeSet(E... items) {
		return PersistentTreeSet.create(items);
	}
	
	public static <E> PersistentSortedSet<E> treeSet(Iterable<? extends E> items) {
		return PersistentTreeSet.create(items);
	}*/
	
	@SuppressWarnings("unchecked")
	public static <K, V> PersistentMap<K, V> arrayMap() {
		return PersistentArrayMap.EMPTY;
	}
	
	public static <K, V> PersistentMap<K, V> arrayMap(Map<? extends K, ? extends V> init) {
		return PersistentArrayMap.create(init);
	}
	
	public static <E> PersistentList<E> linkedList() {
		return com.trifork.clj_lang.PersistentList.emptyList();
	}
	
	public static <E> PersistentList<E> linkedList(E val) {
		return Persistents.<E>linkedList().plus(val);
	}
	
	public static <E> PersistentList<E> linkedList(E... vals) {
		return com.trifork.clj_lang.PersistentList.create(vals);
	}
	
	public static <E> PersistentList<E> linkedList(Iterable<? extends E> vals) {
		return com.trifork.clj_lang.PersistentList.create(vals);
	}
	
	// Utilities
	
	public static <E> PersistentList<E> plusAll(PersistentList<E> list, Iterable<? extends E> others) {
		return com.trifork.clj_lang.PersistentList.consAll(list, others);
	}
	
	public static <E> PersistentList<E> plusAll(PersistentList<E> list, E...others) {
		PersistentList<E> result = list;
		for (E other : others) {
			result = result.plus(other);
		}
		return result;
	}
	
	public static <E> PersistentVector<E> plusAll(PersistentVector<E> vec, Iterable<? extends E> others) {
		TransientVector<E> result = vec.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}
	
	public static <E> PersistentVector<E> plusAll(PersistentVector<E> vec, E... others) {
		TransientVector<E> result = vec.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}

	public static <E> PersistentSet<E> plusAll(PersistentSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result =  result.plus(other);
		}
		return result.persist();
	}
	
	public static <E> PersistentSet<E> plusAll(PersistentSet<E> set, E... others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result =  result.plus(other);
		}
		return result.persist();
	}
	
	public static <E> PersistentSet<E> minusAll(PersistentSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result =  result.minus(other);
		}
		return result.persist();
	}
	
	public static <E> PersistentSet<E> minusAll(PersistentSet<E> set, E... others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result =  result.minus(other);
		}
		return result.persist();
	}
	
	public static <E> PersistentSortedSet<E> plusAll(PersistentSortedSet<E> set, Iterable<? extends E> others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.plus(other);
		}
		return result;
	}
	
	public static <E> PersistentSortedSet<E> plusAll(PersistentSortedSet<E> set, E... others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.plus(other);
		}
		return result;
	}
	
	public static <E> PersistentSortedSet<E> minusAll(PersistentSortedSet<E> set, Iterable<? extends E> others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.minus(other);
		}
		return result;
	}

	public static <E> PersistentSortedSet<E> minusAll(PersistentSortedSet<E> set, E... others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.minus(other);
		}
		return result;
	}
	
	// Empty Constructor
	
	private Persistents() {

	}
}
