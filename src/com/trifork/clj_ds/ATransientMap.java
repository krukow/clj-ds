/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.trifork.clj_ds;

import java.util.Map;

import com.trifork.clj_ds.PersistentHashMap.INode;


abstract class ATransientMap<K,V> extends AFn implements ITransientMap<K,V>{
	abstract void ensureEditable();
	abstract ITransientMap<K,V> doAssoc(K key, V val);
	abstract ITransientMap<K,V> doWithout(K key);
	abstract V doValAt(K key, V notFound);
	abstract int doCount();
	abstract IPersistentMap<K,V> doPersistent();

	public ITransientMap<K,V> conj(Map.Entry<K, V> o) {
		ensureEditable();
		return assoc(o.getKey(), o.getValue());
	}

	public final Object invoke(Object arg1) throws Exception{
		return valAt((K) arg1);
	}

	public final Object invoke(Object arg1, Object notFound) throws Exception{
		return valAt((K)arg1, (V) notFound);
	}

	public final V valAt(K key) {
		return valAt(key, null);
	}

	public final ITransientMap<K,V> assoc(K key, V val) {
		ensureEditable();
		return doAssoc(key, val);
	}

	public final ITransientMap<K,V> without(K key) {
		ensureEditable();
		return doWithout(key);
	}

	public final IPersistentMap<K,V> persistentMap() {
		ensureEditable();
		return doPersistent();
	}

	public final V valAt(K key, V notFound) {
		ensureEditable();
		return doValAt(key, notFound);
	}

	public final int count() {
		ensureEditable();
		return doCount();
	}
}
