package com.trifork.clj_ds;

import java.util.Map;

import com.trifork.clj_lang.PersistentArrayMap;
import com.trifork.clj_lang.PersistentHashMap;
import com.trifork.clj_lang.PersistentHashSet;
import com.trifork.clj_lang.PersistentTreeMap;
import com.trifork.clj_lang.PersistentTreeSet;


public final class Persistents {
	
	public static <E> PersistentVector<E> vector() {
		return com.trifork.clj_lang.PersistentVector.emptyVector();
	}
	
	public static <E> PersistentVector<E> vector(E item) {
		return Persistents.<E>vector().cons(item);
	}

	public static <E> PersistentVector<E> vector(E... items) {
		return com.trifork.clj_lang.PersistentVector.create(items);
	}
	
	public static <E> PersistentVector<E> vector(Iterable<? extends E> items) {
		return com.trifork.clj_lang.PersistentVector.create(items);
	}
	
	public static <E> PersistentSet<E> hashSet() {
		return PersistentHashSet.emptySet();
	}
	
	public static <E> PersistentSet<E> hashSet(E item) {
		return Persistents.<E>hashSet().cons(item);
	}
	
	public static <E> PersistentSet<E> hashSet(E... items) {
		return PersistentHashSet.create(items);
	}
	
	public static <E> PersistentSet<E> hashSet(Iterable<? extends E> items) {
		return PersistentHashSet.create(items);
	}
	
	public static <K, V> PersistentMap<K, V> hashMap() {
		return PersistentHashMap.emptyMap();
	}
	
	public static <K, V> PersistentMap<K, V> hashMap(K key, V val) {
		return Persistents.<K, V>hashMap().assoc(key, val);
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
		return Persistents.<K, V>treeMap().assoc(key, val);
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
	
	public static <E> PersistentList<E> linkedList(E item) {
		return Persistents.<E>linkedList().cons(item);
	}
	
	public static <E> PersistentList<E> linkedList(E... items) {
		return com.trifork.clj_lang.PersistentList.create(items);
	}
	
	public static <E> PersistentList<E> linkedList(Iterable<? extends E> items) {
		return com.trifork.clj_lang.PersistentList.create(items);
	}
	
	private Persistents() {

	}
}
