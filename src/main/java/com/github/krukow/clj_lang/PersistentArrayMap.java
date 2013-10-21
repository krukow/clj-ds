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

import java.util.Iterator;
import java.util.Map;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.TransientMap;

/**
 * Simple implementation of persistent map on an array
 * <p/>
 * Note that instances of this class are constant values
 * i.e. add/remove etc return new values
 * <p/>
 * Copies array on every change, so only appropriate for _very_small_ maps
 * <p/>
 * null keys and values are ok, but you won't be able to distinguish a null value via valAt - use contains/entryAt
 */

public class PersistentArrayMap<K,V> extends APersistentMap<K,V> implements IObj, IEditableCollection<MapEntry<K, V>>, PersistentMap<K,V> {

final Object[] array;
static final int HASHTABLE_THRESHOLD = 16;

public static final PersistentArrayMap EMPTY = new PersistentArrayMap();
private final IPersistentMap _meta;

@SuppressWarnings("unchecked")
static public <K,V> PersistentMap<K,V> create(Map<? extends K, ? extends V> other){
	ITransientMap<K,V> ret = EMPTY.asTransient();
	for(Map.Entry<? extends K, ? extends V> e : other.entrySet())
		{
		ret = ret.assoc(e.getKey(), e.getValue());
		}
	return (PersistentMap<K, V>) ret.persistentMap();
}

protected PersistentArrayMap(){
	this.array = new Object[]{};
	this._meta = null;
}

public PersistentArrayMap<K,V> withMeta(IPersistentMap meta){
	return new PersistentArrayMap<K,V>(meta, array);
}

PersistentArrayMap<K,V> create(Object... init){
	return new PersistentArrayMap<K,V>(meta(), init);
}


PersistentHashMap<K,V> createHT(Object[] init){
	return PersistentHashMap.create(meta(), init);
}

static public <K,V> PersistentArrayMap<K,V> createWithCheck(Object[] init){
	for(int i=0;i< init.length;i += 2)
		{
		K k = (K) init[i];
		if (i+1 >= init.length) {throw new IllegalArgumentException("Must provide an even number of params...");}
		V v = (V) init[i+1];
		for(int j=i+2;j<init.length;j += 2)
			{
			if(equalKey(init[i],init[j]))
				throw new IllegalArgumentException("Duplicate key: " + init[i]);
			}
		}
	return new PersistentArrayMap<K,V>(init);
}

static public <K, V> PersistentArrayMap<K, V> createAsIfByAssoc(Object[] init){
	// If this looks like it is doing busy-work, it is because it
	// is achieving these goals: O(n^2) run time like
	// createWithCheck(), never modify init arg, and only
	// allocate memory if there are duplicate keys.
	int n = 0;
	for(int i=0;i< init.length;i += 2)
		{
		boolean duplicateKey = false;
		for(int j=0;j<i;j += 2)
			{
			if(equalKey(init[i],init[j]))
				{
				duplicateKey = true;
				break;
				}
			}
		if(!duplicateKey)
			n += 2;
		}
	if(n < init.length)
		{
		// Create a new shorter array with unique keys, and
		// the last value associated with each key.  To behave
		// like assoc, the first occurrence of each key must
		// be used, since its metadata may be different than
		// later equal keys.
		Object[] nodups = new Object[n];
		int m = 0;
		for(int i=0;i< init.length;i += 2)
			{
			boolean duplicateKey = false;
			for(int j=0;j<m;j += 2)
				{
				if(equalKey(init[i],nodups[j]))
					{
					duplicateKey = true;
					break;
					}
				}
			if(!duplicateKey)
				{
				int j;
				for (j=init.length-2; j>=i; j -= 2)
					{
					if(equalKey(init[i],init[j]))
						{
						break;
						}
					}
				nodups[m] = init[i];
				nodups[m+1] = init[j+1];
				m += 2;
				}
			}
		if (m != n)
			throw new IllegalArgumentException("Internal error: m=" + m);
		init = nodups;
		}
	return new PersistentArrayMap<K, V>(init);
}

/**
 * This ctor captures/aliases the passed array, so do not modify later
 *
 * @param init {key1,val1,key2,val2,...}
 */
public PersistentArrayMap(Object[] init){
	this.array = init;
	this._meta = null;
}


public PersistentArrayMap(IPersistentMap meta, Object[] init){
	this._meta = meta;
	this.array = init;
}

public int count(){
	return array.length / 2;
}

public boolean containsKey(Object key){
	return indexOf(key) >= 0;
}

public IMapEntry<K,V> entryAt(K key){
	int i = indexOf(key);
	if(i >= 0)
		return new MapEntry<K,V>((K) array[i],(V)array[i+1]);
	return null;
}

public IPersistentMap<K,V> assocEx(K key, V val) {
	int i = indexOf(key);
	Object[] newArray;
	if(i >= 0)
		{
		throw Util.runtimeException("Key already present");
		}
	else //didn't have key, grow
		{
		if(array.length > HASHTABLE_THRESHOLD)
			return createHT(array).assocEx(key, val);
		newArray = new Object[array.length + 2];
		if(array.length > 0)
			System.arraycopy(array, 0, newArray, 2, array.length);
		newArray[0] = key;
		newArray[1] = val;
		}
	return create(newArray);
}

public IPersistentMap<K,V> assoc(K key, V val){
	int i = indexOf(key);
	Object[] newArray;
	if(i >= 0) //already have key, same-sized replacement
		{
		if(array[i + 1] == val) //no change, no op
			return this;
		newArray = array.clone();
		newArray[i + 1] = val;
		}
	else //didn't have key, grow
		{
		if(array.length > HASHTABLE_THRESHOLD)
			return createHT(array).assoc(key, val);
		newArray = new Object[array.length + 2];
		if(array.length > 0)
			System.arraycopy(array, 0, newArray, 2, array.length);
		newArray[0] = key;
		newArray[1] = val;
		}
	return create(newArray);
}

public PersistentArrayMap<K,V> without(K key){
	int i = indexOf(key);
	if(i >= 0) //have key, will remove
		{
		int newlen = array.length - 2;
		if(newlen == 0)
			return empty();
		Object[] newArray = new Object[newlen];
		for(int s = 0, d = 0; s < array.length; s += 2)
			{
			if(!equalKey(array[s], key)) //skip removal key
				{
				newArray[d] = array[s];
				newArray[d + 1] = array[s + 1];
				d += 2;
				}
			}
		return create(newArray);
		}
	//don't have key, no op
	return this;
}

public PersistentArrayMap<K,V> empty(){
	return (PersistentArrayMap<K,V>) EMPTY.withMeta(meta());
}

final public V valAt(K key, V notFound){
	int i = indexOf(key);
	if(i >= 0)
		return (V) array[i + 1];
	return notFound;
}

public V valAt(K key){
	return valAt(key, null);
}

public int capacity(){
	return count();
}

private int indexOfObject(Object key){
    Util.EquivPred ep = Util.equivPred(key);
    for(int i = 0; i < array.length; i += 2)
        {
        if(ep.equiv(key, array[i]))
            return i;
        }
	return -1;
}

private int indexOf(Object key){
    return indexOfObject(key);
}

static boolean equalKey(Object k1, Object k2){
	return Util.equiv(k1, k2);
}

public Iterator<Map.Entry<K, V>> iterator(){
	return new Iter(array);
}

public Iterator<Map.Entry<K, V>> reverseIterator(){
	return new RevIter(array);
}

public Iterator<Map.Entry<K, V>> iteratorFrom(K key){
	int i = indexOf(key);
	return new Iter(array,i);
}

public ISeq seq(){
	if(array.length > 0)
		return new Seq(array, 0);
	return null;
}

public IPersistentMap meta(){
	return _meta;
}

static class Seq extends ASeq implements Counted{
	final Object[] array;
	final int i;

	Seq(Object[] array, int i){
		this.array = array;
		this.i = i;
	}

	public Seq(IPersistentMap meta, Object[] array, int i){
		super(meta);
		this.array = array;
		this.i = i;
	}

	public Object first(){
		return new MapEntry(array[i],array[i+1]);
	}

	public ISeq next(){
		if(i + 2 < array.length)
			return new Seq(array, i + 2);
		return null;
	}

	public int count(){
		return (array.length - i) / 2;
	}

	public Obj withMeta(IPersistentMap meta){
		return new Seq(meta, array, i);
	}
}

static class Iter implements Iterator{
	Object[] array;
	int i;

	//for iterator
	Iter(Object[] array){
		this(array, -2);
	}

	//for entryAt
	Iter(Object[] array, int i){
		this.array = array;
		this.i = i;
	}

	public boolean hasNext(){
		return i < array.length - 2;
	}

	public Object next(){
		i += 2;
		return new MapEntry(array[i],array[i+1]);
	}

	public void remove(){
		throw new UnsupportedOperationException();
	}

}

public Object kvreduce(IFn f, Object init){
    for(int i=0;i < array.length;i+=2){
        init = f.invoke(init, array[i], array[i+1]);
	    if(RT.isReduced(init))
		    return ((IDeref)init).deref();
        }
    return init;
}

static class RevIter implements Iterator{
	Object[] array;
	int i;

	//for iterator
	RevIter(Object[] array){
		this(array, array.length);
	}

	//for entryAt
	RevIter(Object[] array, int i){
		this.array = array;
		this.i = i;
	}

	public boolean hasNext(){
		return i > 0;
	}

	public Object next(){
		i -= 2;
		return new MapEntry(array[i],array[i+1]);
	}

	public void remove(){
		throw new UnsupportedOperationException();
	}

}


public TransientArrayMap asTransient(){
	return new TransientArrayMap(array);
}

static final class TransientArrayMap<K,V> extends ATransientMap<K,V> implements TransientMap<K, V> {
	int len;
	final Object[] array;
	Thread owner;

	public TransientArrayMap(Object[] array){
		this.owner = Thread.currentThread();
		this.array = new Object[Math.max(HASHTABLE_THRESHOLD, array.length)];
		System.arraycopy(array, 0, this.array, 0, array.length);
		this.len = array.length;
	}
	
	private int indexOf(Object key){
		for(int i = 0; i < len; i += 2)
			{
			if(equalKey(array[i], key))
				return i;
			}
		return -1;
	}

	ITransientMap<K,V> doAssoc(K key, V val){
		int i = indexOf(key);
		if(i >= 0) //already have key,
			{
			if(array[i + 1] != val) //no change, no op
				array[i + 1] = val;
			}
		else //didn't have key, grow
			{
			if(len >= array.length)
				return PersistentHashMap.create(array).asTransient().assoc(key, val);
			array[len++] = key;
			array[len++] = val;
			}
		return this;
	}

	TransientArrayMap<K,V> doWithout(K key) {
		int i = indexOf(key);
		if(i >= 0) //have key, will remove
			{
			if (len >= 2)
				{
					array[i] = array[len - 2];
					array[i + 1] = array[len - 1];
				}
			len -= 2;
			}
		return this;
	}

	V doValAt(K key, V notFound) {
		int i = indexOf(key);
		if (i >= 0)
			return (V) array[i + 1];
		return notFound;
	}

	int doCount() {
		return len / 2;
	}
	
	PersistentArrayMap<K,V> doPersistent(){
		ensureEditable();
		owner = null;
		Object[] a = new Object[len];
		System.arraycopy(array,0,a,0,len);
		return new PersistentArrayMap<K,V>(a);
	}

	void ensureEditable(){
		if(owner == Thread.currentThread())
			return;
		if(owner != null)
			throw new IllegalAccessError("Transient used by non-owner thread");
		throw new IllegalAccessError("Transient used after persistent! call");
	}

	@Override
	public IPersistentCollection persistent() {
		return persistentMap();
	}
	
	@Override
	public PersistentMap<K, V> persist() {
		return (PersistentMap<K, V>) persistent();
	}
	
	@Override
	public TransientMap<K, V> plus(K key, V val) {
		return (TransientMap<K, V>) assoc(key, val);
	}
	
	@Override
	public TransientMap<K, V> minus(K key) {
		return (TransientMap<K, V>) without(key);
	}
	
}
	@Override
	public PersistentMap<K, V> zero() {
		return empty();
	}
	
	@Override
	public PersistentMap<K, V> plus(K key, V val) {
		return (PersistentMap<K, V>) assoc(key, val);
	}
	
	@Override
	public PersistentMap<K, V> plusEx(K key, V val) {
		return (PersistentMap<K, V>) assocEx(key, val);
	}
	
	@Override
	public PersistentMap<K, V> minus(K key) {
		return without(key);
	}
}
