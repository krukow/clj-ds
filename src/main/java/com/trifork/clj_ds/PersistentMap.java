package com.trifork.clj_ds;

import java.util.Map;

import com.trifork.clj_lang.IPersistentMap;

public interface PersistentMap<K, V> extends IPersistentMap<K, V>, Map<K, V> {
	
	PersistentMap<K,V> assoc(K key, V val);

	PersistentMap<K,V> assocEx(K key, V val);

	PersistentMap<K,V> without(K key);

}
