package com.trifork.clj_ds;

import com.trifork.clj_lang.Sorted;

public interface PersistentSortedMap<K, V> extends PersistentMap<K, V>, Sorted<K> /*																			 */{

	PersistentSortedMap<K, V> zero();

	PersistentSortedMap<K, V> plus(K key, V val);

	PersistentSortedMap<K, V> plusEx(K key, V val);

	PersistentSortedMap<K, V> minus(K key);

}
