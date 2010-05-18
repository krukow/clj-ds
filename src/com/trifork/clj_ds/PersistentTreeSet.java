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

package com.trifork.clj_ds;

import java.util.Comparator;

public class PersistentTreeSet<T> extends APersistentSet<T> implements IObj, Reversible<T>, Sorted<T>{
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

public IPersistentSet<T> disjoin(T key) throws Exception{
	if(contains(key))
		return new PersistentTreeSet<T>(meta(),impl.without(key));
	return this;
}

public IPersistentSet<T> cons(T o){
	if(contains(o))
		return this;
	return new PersistentTreeSet<T>(meta(),impl.assoc(o,o));
}

public IPersistentCollection<T> empty(){
	return new PersistentTreeSet<T>(meta(),(PersistentTreeMap)impl.empty());
}

public ISeq<T> rseq() throws Exception{
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
}
