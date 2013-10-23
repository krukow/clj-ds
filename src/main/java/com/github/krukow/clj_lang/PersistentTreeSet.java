/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 3, 2008 */

package com.github.krukow.clj_lang;

import java.util.Comparator;

import com.github.krukow.clj_ds.PersistentSortedSet;
import com.github.krukow.clj_ds.TransientCollection;

public class PersistentTreeSet<T> extends APersistentSet<T> implements IObj, Reversible<T>, Sorted<T>, PersistentSortedSet<T>{
static public final PersistentTreeSet EMPTY = new PersistentTreeSet(null, PersistentTreeMap.EMPTY);
final IPersistentMap _meta;


static public <T> PersistentTreeSet<T> create(ISeq<? extends T> items){
	PersistentTreeSet<T> ret = EMPTY;
	for(; items != null; items = items.next())
		{
		ret = (PersistentTreeSet<T>) ret.cons(items.first());
		}
	return ret;
}

static public <T> PersistentTreeSet<T> create(Comparator<T> comp, ISeq<? extends T> items){
	PersistentTreeSet<T> ret = new PersistentTreeSet<T>(null, new PersistentTreeMap(null, comp));
	for(; items != null; items = items.next())
		{
		ret = (PersistentTreeSet<T>) ret.cons(items.first());
		}
	return ret;
}

PersistentTreeSet(IPersistentMap meta, IPersistentMap impl){
	super(impl);
	this._meta = meta;
}

public PersistentTreeSet<T> disjoin(T key) {
	if(contains(key))
		return new PersistentTreeSet<T>(meta(),impl.without(key));
	return this;
}

public PersistentTreeSet<T> cons(T o){
	if(contains(o))
		return this;
	return new PersistentTreeSet<T>(meta(),impl.assoc(o,o));
}

public PersistentTreeSet<T> empty(){
	return new PersistentTreeSet<T>(meta(),(PersistentTreeMap)impl.empty());
}

public ISeq<T> rseq() {
	return APersistentMap.KeySeq.create(((Reversible) impl).rseq());
}

public PersistentTreeSet<T> withMeta(IPersistentMap meta){
	return new PersistentTreeSet<T>(meta, impl);
}

public Comparator<T> comparator(){
	return ((Sorted<T>)impl).comparator();
}

public Object entryKey(Object entry){
	return entry;
}

public ISeq<T> seq(boolean ascending){
	PersistentTreeMap m = (PersistentTreeMap) impl;
	return RT.keys(m.seq(ascending));
}

public ISeq<T> seqFrom(T key, boolean ascending){
	PersistentTreeMap m = (PersistentTreeMap) impl;
	return RT.keys(m.seqFrom(key,ascending));
}

public IPersistentMap meta(){
	return _meta;
}
	
	@Override
	public PersistentSortedSet<T> zero() {
		return empty();
	}
	
	@Override
	public PersistentSortedSet<T> plus(T o) {
		return cons(o);
	}
	
	@Override
	public PersistentSortedSet<T> minus(T key) {
		return disjoin(key);
	}
	
	@Override
	public TransientCollection<T> asTransient() {
		throw new UnsupportedOperationException();
	}

}
