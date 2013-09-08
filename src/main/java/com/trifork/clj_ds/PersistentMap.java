package com.trifork.clj_ds;

import java.util.Map;

public interface PersistentMap<K, V> extends Map<K, V> {

	PersistentMap<K, V> zero();

	PersistentMap<K, V> plus(K key, V val);

	PersistentMap<K, V> plusEx(K key, V val);

	PersistentMap<K, V> minus(K key);

}
