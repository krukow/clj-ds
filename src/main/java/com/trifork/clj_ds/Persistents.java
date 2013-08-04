package com.trifork.clj_ds;

import java.util.List;
import java.util.Map;

import com.trifork.clj_lang.PersistentArrayMap;
import com.trifork.clj_lang.PersistentHashMap;
import com.trifork.clj_lang.PersistentHashSet;
import com.trifork.clj_lang.PersistentTreeMap;
import com.trifork.clj_lang.PersistentTreeSet;


public final class Persistents {
	
	public static <E> PersistentVector<E> emptyVector() {
		return com.trifork.clj_lang.PersistentVector.emptyVector();
	}

	public static <E> PersistentVector<E> newVector(E... items) {
		return com.trifork.clj_lang.PersistentVector.create(items);
	}
	
	public static <E> PersistentVector<E> newVector(Iterable<? extends E> items) {
		return com.trifork.clj_lang.PersistentVector.create(items);
	}
	
	public static <E> PersistentSet<E> emptyHashSet() {
		return PersistentHashSet.emptySet();
	}
	
	public static <E> PersistentSet<E> newHashSet(E... items) {
		return PersistentHashSet.create(items);
	}
	
	public static <E> PersistentSet<E> newHashSet(Iterable<? extends E> items) {
		return PersistentHashSet.create(items);
	}
	
	public static <K, V> PersistentMap<K, V> emptyHashMap() {
		return PersistentHashMap.emptyMap();
	}
	
	public static <K, V> PersistentMap<K, V> newHashMap(Object... keyValues) {
		return PersistentHashMap.create(keyValues);
	}
	
	public static <K, V> PersistentMap<K, V> newHashMap(Map<? extends K, ? extends V> init) {
		return PersistentHashMap.create(init);
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> PersistentSortedMap<K, V> emptyTreeMap() {
		return PersistentTreeMap.EMPTY;
	}
	
	public static <K, V> PersistentSortedMap<K, V> newTreeMap(Map<? extends K, ? extends V> init) {
		return PersistentTreeMap.create(init);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> PersistentSortedSet<E> emptyTreeSet() {
		return PersistentTreeSet.EMPTY;
	}
	
	/*
	public static <E> PersistentSortedSet<E> newTreeSet(E... items) {
		return PersistentTreeSet.create(items);
	}
	
	public static <E> PersistentSortedSet<E> newTreeSet(Iterable<? extends E> items) {
		return PersistentTreeSet.create(items);
	}*/
	
	@SuppressWarnings("unchecked")
	public static <K, V> PersistentMap<K, V> emptyArrayMap() {
		return PersistentArrayMap.EMPTY;
	}
	
	public static <K, V> PersistentMap<K, V> newArrayMap(Map<? extends K, ? extends V> init) {
		return PersistentArrayMap.create(init);
	}
	
	public static <E> PersistentList<E> emptyLinkedList() {
		return com.trifork.clj_lang.PersistentList.emptyList();
	}
	
	public static <E> PersistentList<E> newLinkedList(E... items) {
		return com.trifork.clj_lang.PersistentList.create(items);
	}
	
	public static <E> PersistentList<E> newLinkedList(Iterable<? extends E> items) {
		return com.trifork.clj_lang.PersistentList.create(items);
	}
	
	public static <E> PersistentList<E> newLinkedList(List<? extends E> items) {
		return com.trifork.clj_lang.PersistentList.create(items);
	}
	
	private Persistents() {

	}
}
