package com.trifork.clj_ds;

import com.trifork.clj_lang.Sorted;

public interface PersistentSortedMap<K, V> extends PersistentMap<K, V>, Sorted<K> /*, SortedMap<K, V> */ {

	PersistentSortedMap<K,V> assoc(K key, V val);

	PersistentSortedMap<K,V> assocEx(K key, V val);

	PersistentSortedMap<K,V> without(K key);
	
}
