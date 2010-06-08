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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PersistentHashSet<T> extends APersistentSet<T> implements IObj, IEditableCollection<T> {

static public final PersistentHashSet EMPTY = new PersistentHashSet(null, PersistentHashMap.EMPTY);

@SuppressWarnings("unchecked")
static public final <T> PersistentHashSet<T> emptySet() {
	return EMPTY;
}
final IPersistentMap _meta;

public static <T> PersistentHashSet<T> create(T... init){
	PersistentHashSet<T> ret = EMPTY;
	for(int i = 0; i < init.length; i++)
		{
		ret = (PersistentHashSet<T>) ret.cons(init[i]);
		}
	return ret;
}

public static <T> PersistentHashSet<T> create(List<? extends T> init){
	PersistentHashSet<T> ret = EMPTY;
	for(T key : init)
		{
		ret = (PersistentHashSet<T>) ret.cons(key);
		}
	return ret;
}

static public <T> PersistentHashSet<T> create(ISeq<? extends T> items){
	PersistentHashSet<T> ret = EMPTY;
	for(; items != null; items = items.next())
		{
		ret = (PersistentHashSet<T>) ret.cons(items.first());
		}
	return ret;
}

public static <T> PersistentHashSet<T> createWithCheck(T ... init){
	PersistentHashSet<T> ret = EMPTY;
	for(int i = 0; i < init.length; i++)
		{
		ret = (PersistentHashSet<T>) ret.cons(init[i]);
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + init[i]);
		}
	return ret;
}

public static <T> PersistentHashSet<T> createWithCheck(List<? extends T> init){
	PersistentHashSet<T> ret = EMPTY;
	int i=0;
	for(T key : init)
		{
		ret = (PersistentHashSet<T>) ret.cons(key);
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + key);		
		++i;
		}
	return ret;
}

static public <T> PersistentHashSet<T> createWithCheck(ISeq<? extends T> items){
	PersistentHashSet<T> ret = EMPTY;
	for(int i=0; items != null; items = items.next(), ++i)
		{
		ret = (PersistentHashSet<T>) ret.cons(items.first());
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + items.first());
		}
	return ret;
}

PersistentHashSet(IPersistentMap meta, IPersistentMap impl){
	super(impl);
	this._meta = meta;
}

public Iterator<T> iterator(){
	return new Iterator<T>() {
		final Iterator<Map.Entry> iterator = impl.iterator();
		
		public boolean hasNext() {
			return iterator.hasNext();
		}

		public T next() {
			Map.Entry n = iterator.next();
			return (T) n.getKey();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		} 
		
	};
}


public IPersistentSet<T> disjoin(T key) throws Exception{
	if(contains(key))
		return new PersistentHashSet<T>(meta(),impl.without(key));
	return this;
}

public IPersistentSet<T> cons(T o){
	if(contains(o))
		return this;
	return new PersistentHashSet<T>(meta(),impl.assoc(o,o));
}

public IPersistentCollection<T> empty(){
	return EMPTY.withMeta(meta());	
}

public PersistentHashSet<T> withMeta(IPersistentMap meta){
	return new PersistentHashSet<T>(meta, impl);
}

public ITransientCollection<T> asTransient() {
	return new TransientHashSet<T>(((PersistentHashMap) impl).asTransient());
}

public IPersistentMap meta(){
	return _meta;
}

static final class TransientHashSet<T> extends ATransientSet<T> {
	TransientHashSet(ITransientMap impl) {
		super(impl);
	}

	public IPersistentCollection<T> persistent() {
		return new PersistentHashSet<T>(null, impl.persistentMap());
	}
}

}
