package com.github.krukow.clj_ds;

import com.github.krukow.clj_lang.Sorted;

public interface PersistentSortedMap<K, V> extends PersistentMap<K, V>, Sorted<K> /* , SortedMap<K, V> */{

	PersistentSortedMap<K, V> zero();

	PersistentSortedMap<K, V> plus(K key, V val);

	PersistentSortedMap<K, V> plusEx(K key, V val);

	PersistentSortedMap<K, V> minus(K key);

}
