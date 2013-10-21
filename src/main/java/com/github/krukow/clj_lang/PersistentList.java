/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.github.krukow.clj_lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class PersistentList<T> extends ASeq<T> implements IPersistentList<T>, IReduce, List<T>, Counted, com.github.krukow.clj_ds.PersistentList<T> {

private final T _first;
private final PersistentList<T> _rest;
private final int _count;

final public static EmptyList EMPTY = new EmptyList(null);

public static final <T> EmptyList<T> emptyList() {
return EMPTY;
}

public PersistentList(T first){
	this._first = first;
	this._rest = null;

	this._count = 1;
}

PersistentList(IPersistentMap meta, T _first, PersistentList<T> _rest, int _count){
	super(meta);
	this._first = _first;
	this._rest = _rest;
	this._count = _count;
}

public static <T> com.github.krukow.clj_ds.PersistentList<T> create(T... init){
	IPersistentList<T> ret = emptyList();
	for(int i = init.length-1; i>=0; i--)
		{
		ret = (IPersistentList<T>) ret.cons(init[i]);
		}
	return (com.github.krukow.clj_ds.PersistentList<T>) ret;
}

public static <T> com.github.krukow.clj_ds.PersistentList<T> create(Iterable<? extends T> init) {
	PersistentVector<T> initVector = PersistentVector.create(init);
	return (com.github.krukow.clj_ds.PersistentList<T>) create(initVector);
}

public static <T> IPersistentList<T> create(List<? extends T> init){
	IPersistentList<T> ret = emptyList();
	for(ListIterator<? extends T> i = init.listIterator(init.size()); i.hasPrevious();)
		{
		ret = (IPersistentList<T>) ret.cons(i.previous());
		}
	return ret;
}

public T first(){
	return _first;
}

public ISeq<T> next(){
	if(_count == 1)
		return null;
	return (ISeq) _rest;
}

public T peek(){
	return first();
}

public IPersistentList<T> pop(){
	if(_rest == null)
		return EMPTY.withMeta(_meta);
	return _rest;
}

public int count(){
	return _count;
}

public PersistentList<T> cons(T o){
	return new PersistentList<T>(meta(), o, this, _count + 1);
}


public EmptyList<T> empty(){
	return EMPTY.withMeta(meta());
}

public PersistentList<T> withMeta(IPersistentMap meta){
	if(meta != _meta)
		return new PersistentList<T>(meta, _first, _rest, _count);
	return this;
}

public Object reduce(IFn f) {
	Object ret = first();
	for(ISeq s = next(); s != null; s = s.next())
		ret = f.invoke(ret, s.first());
	return ret;
}

public Object reduce(IFn f, Object start) {
	Object ret = f.invoke(start, first());
	for(ISeq s = next(); s != null; s = s.next())
		ret = f.invoke(ret, s.first());
	return ret;
}


    static class EmptyList<T> extends Obj implements IPersistentList<T>, List<T>, ISeq<T>, Counted, com.github.krukow.clj_ds.PersistentList<T>{

	public int hashCode(){
		return 1;
	}

    public boolean equals(Object o) {
        return (o instanceof Sequential || o instanceof List) && RT.seq(o) == null;
    }

	public boolean equiv(Object o){
		return equals(o);
	}
	
    EmptyList(IPersistentMap meta){
		super(meta);
	}

        public T first() {
            return null;
        }

        public ISeq<T> next() {
            return null;
        }

        public ISeq<T> more() {
            return this;
        }

        public PersistentList<T> cons(T o){
		return new PersistentList<T>(meta(), o, null, 1);
	}

	public EmptyList<T> empty(){
		return this;
	}

	public EmptyList<T> withMeta(IPersistentMap meta){
		if(meta != meta())
			return new EmptyList<T>(meta);
		return this;
	}

	public T peek(){
		return null;
	}

	public PersistentList<T> pop(){
		throw new IllegalStateException("Can't pop empty list");
	}

	public int count(){
		return 0;
	}

	public ISeq<T> seq(){
		return null;
	}


	public int size(){
		return 0;
	}

	public boolean isEmpty(){
		return true;
	}

	public boolean contains(Object o){
		return false;
	}

	public Iterator<T> iterator(){
		return new Iterator<T>(){

			public boolean hasNext(){
				return false;
			}

			public T next(){
				throw new NoSuchElementException();
			}

			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}

	public Object[] toArray(){
		return RT.EMPTY_ARRAY;
	}

	public boolean add(T o){
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o){
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends T> collection){
		throw new UnsupportedOperationException();
	}

	public void clear(){
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection collection){
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection collection){
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection collection){
		return collection.isEmpty();
	}

	public Object[] toArray(Object[] objects){
		if(objects.length > 0)
			objects[0] = null;
		return objects;
	}

	//////////// List stuff /////////////////
	private List<T> reify(){
		return Collections.unmodifiableList(new ArrayList<T>(this));
	}

	public List<T> subList(int fromIndex, int toIndex){
		return reify().subList(fromIndex, toIndex);
	}

	public T set(int index, T element){
		throw new UnsupportedOperationException();
	}

	public T remove(int index){
		throw new UnsupportedOperationException();
	}

	public int indexOf(Object o){
		ISeq s = seq();
		for(int i = 0; s != null; s = s.next(), i++)
			{
			if(Util.equiv(s.first(), o))
				return i;
			}
		return -1;
	}

	public int lastIndexOf(Object o){
		return reify().lastIndexOf(o);
	}

	public ListIterator<T> listIterator(){
		return reify().listIterator();
	}

	public ListIterator<T> listIterator(int index){
		return reify().listIterator(index);
	}

	public T get(int index){
		return (T) RT.nth(this, index);
	}

	public void add(int index, Object element){
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection c){
		throw new UnsupportedOperationException();
	}
		
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> zero() {
		return this;
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> plus(T val) {
		return cons(val);
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> minus() {
		return pop();
	}

}
	
	public static <T> com.github.krukow.clj_ds.PersistentList<T> consAll(com.github.krukow.clj_ds.PersistentList<T> list, Iterable<? extends T> others) {
		for (T other : others) {
			list = list.plus(other);
		}
		return list;
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> zero() {
		return empty();
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> plus(T val) {
		return cons(val);
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentList<T> minus() {
		return (com.github.krukow.clj_ds.PersistentList<T>) pop();
	}

}
