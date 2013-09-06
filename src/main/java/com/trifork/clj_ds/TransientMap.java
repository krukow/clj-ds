package com.trifork.clj_ds;


public interface TransientMap<K, V> {

	TransientMap<K,V> plus(K key, V val);

	TransientMap<K,V> minus(K key);

	PersistentMap<K,V> persist();
	
}
