package com.trifork.clj_ds;


/**
 * Copyright (c) Rich Hickey. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
public interface Associative<K, V> extends IPersistentCollection<IMapEntry<K,V>>, ILookup<K,V>{
boolean containsKey(K key);

IMapEntry<K,V> entryAt(K key);

Associative<K,V> assoc(K key, V val);


}
