package com.trifork.clj_ds;

import com.trifork.clj_lang.ITransientMap;

public interface TransientMap<K, V> extends ITransientMap<K, V> {

	TransientMap<K,V> assoc(K key, V val);

	TransientMap<K,V> without(K key);

	PersistentMap<K,V> persistentMap();
	
}
