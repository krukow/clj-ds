/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.trifork.clj_lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import com.trifork.clj_ds.PersistentMap;

/*
 A persistent rendition of Phil Bagwell's Hash Array Mapped Trie

 Uses path copying for persistence
 HashCollision leaves vs. extended hashing
 Node polymorphism vs. conditionals
 No sub-tree pools or root-resizing
 Any errors are my own
 */

public class PersistentHashMap<K,V> extends APersistentMap<K,V> implements IEditableCollection<MapEntry<K, V>>, IObj, PersistentMap<K, V> {

final int count;
final INode root;
final boolean hasNull;
final V nullValue;
final IPersistentMap _meta;

final public static PersistentHashMap EMPTY = new PersistentHashMap(0, null, false, null);
final private static Object NOT_FOUND = new Object();

@SuppressWarnings("unchecked")
final public static <K,V> PersistentHashMap<K, V> emptyMap() {
	return EMPTY;
}

@SuppressWarnings("unchecked")
static public <K,V> PersistentHashMap<K,V> create(Map<? extends K,? extends V> other){
	ITransientMap<K,V> ret = EMPTY.asTransient();
	for(Map.Entry<? extends K, ? extends V> e : other.entrySet())
		{
		ret = ret.assoc(e.getKey(), e.getValue());
		}
	return (PersistentHashMap<K, V>) ret.persistentMap();
}

/*
 * @param init {key1,val1,key2,val2,...}
 */
@SuppressWarnings("unchecked")
public static <K,V> PersistentHashMap<K,V> create(Object... init){
	ITransientMap<K,V> ret = EMPTY.asTransient();
	for(int i = 0; i < init.length; i += 2)
		{
			K k = (K) init[i];
			V v = (V) init[i+1];
		ret = ret.assoc(k, v);
		}
	return (PersistentHashMap<K,V>) ret.persistentMap();
}

public static <K,V> PersistentHashMap<K,V> createWithCheck(Object... init){
	ITransientMap<K, V> ret = EMPTY.asTransient();
	for(int i = 0; i < init.length; i += 2)
		{
		ret = ret.assoc((K) init[i], (V) init[i + 1]);
		if(ret.count() != i/2 + 1)
			throw new IllegalArgumentException("Duplicate key: " + init[i]);
		}
	return (PersistentHashMap<K, V>) ret.persistentMap();
}

static public <K,V> PersistentHashMap<K,V> create(ISeq items){
	ITransientMap<K,V> ret = EMPTY.asTransient();
	for(; items != null; items = items.next().next())
		{
		if(items.next() == null)
			throw new IllegalArgumentException(String.format("No value supplied for key: %s", items.first()));
		ret = ret.assoc((K) items.first(), (V) RT.second(items));
		}
	return (PersistentHashMap<K,V>) ret.persistentMap();
}

static public <K, V> PersistentHashMap<K, V> createWithCheck(ISeq items){
	ITransientMap<K, V> ret = EMPTY.asTransient();
	for(int i=0; items != null; items = items.next().next(), ++i)
		{
		if(items.next() == null)
			throw new IllegalArgumentException(String.format("No value supplied for key: %s", items.first()));
		ret = ret.assoc((K) items.first(), (V) RT.second(items));
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + items.first());
		}
	return (PersistentHashMap<K, V>) ret.persistentMap();
}

/*
 * @param init {key1,val1,key2,val2,...}
 */
@SuppressWarnings("unchecked")
public static <K,V> PersistentHashMap<K,V> create(IPersistentMap meta, Object... init){
	return create(init).withMeta(meta);
}

PersistentHashMap(int count, INode root, boolean hasNull, V nullValue){
	this.count = count;
	this.root = root;
	this.hasNull = hasNull;
	this.nullValue = nullValue;
	this._meta = null;
}

public PersistentHashMap(IPersistentMap meta, int count, INode root, boolean hasNull, V nullValue){
	this._meta = meta;
	this.count = count;
	this.root = root;
	this.hasNull = hasNull;
	this.nullValue = nullValue;
}

static int hash(Object k){
	return Util.hasheq(k);
}

public boolean containsKey(Object key){
	if(key == null)
		return hasNull;
	return (root != null) ? root.find(0, hash(key), key, NOT_FOUND) != NOT_FOUND : false;
}

public IMapEntry<K,V> entryAt(K key){
	if(key == null)
		return hasNull ? new MapEntry<K,V>(null, nullValue) : null;
	return (root != null) ? root.find(0, hash(key), key) : null;
}

public PersistentMap<K,V> assoc(K key, V val){
	if(key == null) {
		if(hasNull && val == nullValue)
			return this;
		return new PersistentHashMap<K,V>(meta(), hasNull ? count : count + 1, root, true, val);
	}
	Box addedLeaf = new Box(null);
	INode newroot = (root == null ? BitmapIndexedNode.EMPTY : root) 
			.assoc(0, hash(key), key, val, addedLeaf);
	if(newroot == root)
		return this;
	return new PersistentHashMap<K,V>(meta(), addedLeaf.val == null ? count : count + 1, newroot, hasNull, nullValue);
}

public V valAt(K key, V notFound){
	if(key == null)
		return hasNull ? nullValue : notFound;
	return (V) (root != null ? root.find(0, hash(key), key, notFound) : notFound);
}

public V valAt(K key){
	return valAt(key, null);
}

public PersistentMap<K,V> assocEx(K key, V val) {
	if(containsKey(key))
		throw Util.runtimeException("Key already present");
	return assoc(key, val);
}

public PersistentMap<K,V> without(K key){
	if(key == null)
		return hasNull ? new PersistentHashMap<K,V>(meta(), count - 1, root, false, null) : this;
	if(root == null)
		return this;
	INode newroot = root.without(0, hash(key), key);
	if(newroot == root)
		return this;
	return new PersistentHashMap<K,V>(meta(), count - 1, newroot, hasNull, nullValue); 
}

public Iterator<Map.Entry<K, V>> iterator2(){
	return new Iterator<Map.Entry<K, V>>() {
		ISeq<IMapEntry<K, V>> seq = seq();

		public boolean hasNext() {
			return seq != null;
		}

		@Override
		public Map.Entry<K, V> next() {
			Entry<K,V> first = seq.first();
			seq = seq.next();
			return first;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};
}

public Iterator<Map.Entry<K, V>> iterator(){
	final Iterator<Map.Entry<K, V>> s = root != null ? root.nodeIt(false) : new EmptyIterator(); 
	return hasNull ? new Iterator<Map.Entry<K, V>>(){
		Iterator<Map.Entry<K, V>> i = s;
		boolean nullReady = true;
		public boolean hasNext() {
			return nullReady || i.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			if (nullReady) {
				nullReady = false;
				return new MapEntry<K,V>(null, PersistentHashMap.this.nullValue);
			}
			return i.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	} : s;
}

public Iterator<Map.Entry<K, V>> reverseIterator(){
	final Iterator<Map.Entry<K, V>> s = root != null ? root.nodeIt(true) : new EmptyIterator(); 
	return hasNull ? new Iterator<Map.Entry<K, V>>(){
		Iterator<Map.Entry<K, V>> i = s;
		boolean nullReady = true;
		public boolean hasNext() {
			return nullReady || i.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			if (i.hasNext()) {
				return i.next();
			} else if (nullReady) {
				nullReady = false;
				return new MapEntry<K,V>(null, PersistentHashMap.this.nullValue);
			}
			throw new IllegalStateException();
			
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	} : s;
}

public Object kvreduce(IFn f, Object init){
    init = hasNull?f.invoke(init,null,nullValue):init;
	if(RT.isReduced(init))
		return ((IDeref)init).deref();
	if(root != null){
        return root.kvreduce(f,init);
    }
    return init;
}

public Object fold(long n, final IFn combinef, final IFn reducef,
                   IFn fjinvoke, final IFn fjtask, final IFn fjfork, final IFn fjjoin){
	//we are ignoring n for now
	Callable top = new Callable(){
		public Object call() throws Exception{
			Object ret = combinef.invoke();
			if(root != null)
				ret = combinef.invoke(ret, root.fold(combinef,reducef,fjtask,fjfork,fjjoin));
			return hasNull?
			       combinef.invoke(ret,reducef.invoke(combinef.invoke(),null,nullValue))
			       :ret;
		}
	};
	return fjinvoke.invoke(top);
}

public int count(){
	return count;
}

public ISeq<IMapEntry<K, V>> seq(){
	ISeq<IMapEntry<K, V>> s = root != null ? root.nodeSeq() : null; 
	return hasNull ? new Cons<IMapEntry<K, V>>(new MapEntry<K,V>(null, nullValue), s) : s;
}

public Iterator<Map.Entry<K, V>> iteratorFrom(K key){
	if (hasNull) {throw new UnsupportedOperationException("not supported for maps with null entries yet");}
	Iterator<Map.Entry<K, V>> s = root != null ? root.nodeItFrom(0, hash(key), key) : new EmptyIterator(); 
	return s;
}

public IPersistentCollection empty(){
	return EMPTY.withMeta(meta());	
}

static int mask(int hash, int shift){
	//return ((hash << shift) >>> 27);// & 0x01f;
	return (hash >>> shift) & 0x01f;
}

public PersistentHashMap withMeta(IPersistentMap meta){
	return new PersistentHashMap(meta, count, root, hasNull, nullValue);
}

public TransientHashMap asTransient() {
	return new TransientHashMap(this);
}

public IPersistentMap meta(){
	return _meta;
}

static final class TransientHashMap<K,V> extends ATransientMap<K,V> {
	AtomicReference<Thread> edit;
	INode root;
	int count;
	boolean hasNull;
	V nullValue;
	final Box leafFlag = new Box(null);


	TransientHashMap(PersistentHashMap<K,V> m) {
		this(new AtomicReference<Thread>(Thread.currentThread()), m.root, m.count, m.hasNull, m.nullValue);
	}
	
	TransientHashMap(AtomicReference<Thread> edit, INode root, int count, boolean hasNull, V nullValue) {
		this.edit = edit;
		this.root = root; 
		this.count = count; 
		this.hasNull = hasNull;
		this.nullValue = nullValue;
	}

	TransientHashMap<K,V> doAssoc(K key, V val) {
		if (key == null) {
			if (this.nullValue != val)
				this.nullValue = (V) val;
			if (!hasNull) {
				this.count++;
				this.hasNull = true;
			}
			return this;
		}
//		Box leafFlag = new Box(null);
		leafFlag.val = null;
		INode n = (root == null ? BitmapIndexedNode.EMPTY : root)
			.assoc(edit, 0, hash(key), key, val, leafFlag);
		if (n != this.root)
			this.root = n; 
		if(leafFlag.val != null) this.count++;
		return this;
	}

	TransientHashMap<K,V> doWithout(K key) {
		if (key == null) {
			if (!hasNull) return this;
			hasNull = false;
			nullValue = null;
			this.count--;
			return this;
		}
		if (root == null) return this;
//		Box leafFlag = new Box(null);
		leafFlag.val = null;
		INode n = root.without(edit, 0, hash(key), key, leafFlag);
		if (n != root)
			this.root = n;
		if(leafFlag.val != null) this.count--;
		return this;
	}

	PersistentHashMap<K,V> doPersistent() {
		edit.set(null);
		return new PersistentHashMap<K,V>(count, root, hasNull, nullValue);
	}

	V doValAt(K key, V notFound) {
		if (key == null)
			if (hasNull)
				return nullValue;
			else
				return notFound;
		if (root == null)
			return null;
		return (V) root.find(0, hash(key), key, notFound);
	}

	int doCount() {
		return count;
	}
	
	void ensureEditable(){
		Thread owner = edit.get();
		if(owner == Thread.currentThread())
			return;
		if(owner != null)
			throw new IllegalAccessError("Transient used by non-owner thread");
		throw new IllegalAccessError("Transient used after persistent! call");
	}

	public IPersistentCollection persistent() {
		// TODO Auto-generated method stub
		return persistentMap();
	}

}

static interface INode extends Serializable {
	INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf);

	Iterator nodeIt(boolean reverse);
	
	Iterator nodeItFrom(int shift, int hash, Object key);

	INode without(int shift, int hash, Object key);

	IMapEntry find(int shift, int hash, Object key);

	Object find(int shift, int hash, Object key, Object notFound);

	ISeq nodeSeq();

	INode assoc(AtomicReference<Thread> edit, int shift, int hash, Object key, Object val, Box addedLeaf);

	INode without(AtomicReference<Thread> edit, int shift, int hash, Object key, Box removedLeaf);
	
    public Object kvreduce(IFn f, Object init);

	Object fold(IFn combinef, IFn reducef, IFn fjtask, IFn fjfork, IFn fjjoin);
}

final static class ArrayNode implements INode{
	int count;
	final INode[] array;
	final AtomicReference<Thread> edit;

	ArrayNode(AtomicReference<Thread> edit, int count, INode[] array){
		this.array = array;
		this.edit = edit;
		this.count = count;
	}
	
	public Iterator nodeItFrom(int shift, int hash, Object key) {
		return new ArrayNodeIterator(this, shift, hash, key);
	}
	
	static class ArrayNodeIterator implements Iterator {
		int index;
		Iterator current;
		INode[] array;
		int shift, hash;
		Object key;
		public ArrayNodeIterator(ArrayNode an) {
			array = an.array;
			moveCurIfNeeded();
		}
		public ArrayNodeIterator(ArrayNode an, int shift, int hash, Object key) {
			array = an.array;
			this.shift = shift; 
			this.hash = hash;
			this.key = key;
			moveCurTo();
		}
		
		private void moveCurTo() {
			index = mask(hash, shift);
			INode node = array[index];
			if(node == null)
				return;
			current = node.nodeItFrom(shift + 5, hash, key);
			index += 1;
			
		}
		
		public boolean hasNext() {
			while (current != null && !current.hasNext()) {
				moveCurIfNeeded();
			}
			return current != null && current.hasNext(); 
		}

		private void moveCurIfNeeded() {
			if (current != null && current.hasNext()) return;
			while (index < array.length && array[index] == null) {index += 1;};
			current = (index == array.length) ? null : array[index++].nodeIt(false); 
		}

		@Override
		public Object next() {
			return current.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}
	static final class ReverseArrayNodeIterator implements Iterator {
		int index;
		Iterator current;
		INode[] array;
		int shift, hash;
		Object key;
		public ReverseArrayNodeIterator(ArrayNode an) {
			this.array = an.array;
			this.index = array.length-1;
			moveCurIfNeeded();
		}
		private void moveCurIfNeeded() {
			if (current != null && current.hasNext()) return;
			while (index >= 0 && array[index] == null) {index -= 1;};
			current = (index < 0) ? null : array[index--].nodeIt(true); 
		}
		public boolean hasNext() {
			while (current != null && !current.hasNext()) {
				moveCurIfNeeded();
			}
			return current != null && current.hasNext(); 
		}


		@Override
		public Object next() {
			return current.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	public Iterator nodeIt(boolean reverse) {
		return reverse?new ReverseArrayNodeIterator(this): new ArrayNodeIterator(this);
	}

	public INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null)
			return new ArrayNode(null, count + 1, cloneAndSet(array, idx, BitmapIndexedNode.EMPTY.assoc(shift + 5, hash, key, val, addedLeaf)));			
		INode n = node.assoc(shift + 5, hash, key, val, addedLeaf);
		if(n == node)
			return this;
		return new ArrayNode(null, count, cloneAndSet(array, idx, n));
	}

	public INode without(int shift, int hash, Object key){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null)
			return this;
		INode n = node.without(shift + 5, hash, key);
		if(n == node)
			return this;
		if (n == null) {
			if (count <= 8) // shrink
				return pack(null, idx);
			return new ArrayNode(null, count - 1, cloneAndSet(array, idx, n));
		} else 
			return new ArrayNode(null, count, cloneAndSet(array, idx, n));
	}

	public IMapEntry find(int shift, int hash, Object key){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null)
			return null;
		return node.find(shift + 5, hash, key); 
	}

	public Object find(int shift, int hash, Object key, Object notFound){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null)
			return notFound;
		return node.find(shift + 5, hash, key, notFound); 
	}
	
	public ISeq nodeSeq(){
		return Seq.create(array);
	}

    public Object kvreduce(IFn f, Object init){
        for(INode node : array){
            if(node != null){
                init = node.kvreduce(f,init);
	            if(RT.isReduced(init))
		            return ((IDeref)init).deref();
	            }
	        }
        return init;
    }

	public Object fold(final IFn combinef, final IFn reducef,
	                   final IFn fjtask, final IFn fjfork, final IFn fjjoin){
		List<Callable> tasks = new ArrayList();
		for(final INode node : array){
			if(node != null){
				tasks.add(new Callable(){
					public Object call() throws Exception{
						return node.fold(combinef, reducef, fjtask, fjfork, fjjoin);
					}
				});
				}
			}

		return foldTasks(tasks,combinef,fjtask,fjfork,fjjoin);
		}

	static public Object foldTasks(List<Callable> tasks, final IFn combinef,
	                          final IFn fjtask, final IFn fjfork, final IFn fjjoin){

		if(tasks.isEmpty())
			return combinef.invoke();

		if(tasks.size() == 1){
			Object ret = null;
			try
				{
				return tasks.get(0).call();
				}
			catch(Exception e)
				{
				//aargh
				}
			}

		List<Callable> t1 = tasks.subList(0,tasks.size()/2);
		final List<Callable> t2 = tasks.subList(tasks.size()/2, tasks.size());

		Object forked = fjfork.invoke(fjtask.invoke(new Callable() {
			public Object call() throws Exception{
				return foldTasks(t2,combinef,fjtask,fjfork,fjjoin);
			}
		}));

		return combinef.invoke(foldTasks(t1,combinef,fjtask,fjfork,fjjoin),fjjoin.invoke(forked));
	}

	private ArrayNode ensureEditable(AtomicReference<Thread> edit){
		if(this.edit == edit)
			return this;
		return new ArrayNode(edit, count, this.array.clone());
	}
	
	private ArrayNode editAndSet(AtomicReference<Thread> edit, int i, INode n){
		ArrayNode editable = ensureEditable(edit);
		editable.array[i] = n;
		return editable;
	}


	private INode pack(AtomicReference<Thread> edit, int idx) {
		Object[] newArray = new Object[2*(count - 1)];
		int j = 1;
		int bitmap = 0;
		for(int i = 0; i < idx; i++)
			if (array[i] != null) {
				newArray[j] = array[i];
				bitmap |= 1 << i;
				j += 2;
			}
		for(int i = idx + 1; i < array.length; i++)
			if (array[i] != null) {
				newArray[j] = array[i];
				bitmap |= 1 << i;
				j += 2;
			}
		return new BitmapIndexedNode(edit, bitmap, newArray);
	}

	public INode assoc(AtomicReference<Thread> edit, int shift, int hash, Object key, Object val, Box addedLeaf){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null) {
			ArrayNode editable = editAndSet(edit, idx, BitmapIndexedNode.EMPTY.assoc(edit, shift + 5, hash, key, val, addedLeaf));
			editable.count++;
			return editable;			
		}
		INode n = node.assoc(edit, shift + 5, hash, key, val, addedLeaf);
		if(n == node)
			return this;
		return editAndSet(edit, idx, n);
	}	

	public INode without(AtomicReference<Thread> edit, int shift, int hash, Object key, Box removedLeaf){
		int idx = mask(hash, shift);
		INode node = array[idx];
		if(node == null)
			return this;
		INode n = node.without(edit, shift + 5, hash, key, removedLeaf);
		if(n == node)
			return this;
		if(n == null) {
			if (count <= 8) // shrink
				return pack(edit, idx);
			ArrayNode editable = editAndSet(edit, idx, n);
			editable.count--;
			return editable;
		}
		return editAndSet(edit, idx, n);
	}
	
	static class Seq extends ASeq {
		final INode[] nodes;
		final int i;
		final ISeq s; 
		
		static ISeq create(INode[] nodes) {
			return create(null, nodes, 0, null);
		}
		
		private static ISeq create(IPersistentMap meta, INode[] nodes, int i, ISeq s) {
			if (s != null)
				return new Seq(meta, nodes, i, s);
			for(int j = i; j < nodes.length; j++)
				if (nodes[j] != null) {
					ISeq ns = nodes[j].nodeSeq();
					if (ns != null)
						return new Seq(meta, nodes, j + 1, ns);
				}
			return null;
		}
		
		private Seq(IPersistentMap meta, INode[] nodes, int i, ISeq s) {
			super(meta);
			this.nodes = nodes;
			this.i = i;
			this.s = s;
		}

		public Obj withMeta(IPersistentMap meta) {
			return new Seq(meta, nodes, i, s);
		}

		public Object first() {
			return s.first();
		}

		public ISeq next() {
			return create(null, nodes, i, s.next());
		}
		
	}

	
	
}

final static class BitmapIndexedNode implements INode{
	static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(null, 0, new Object[0]);
	
	int bitmap;
	Object[] array;
	final AtomicReference<Thread> edit;

	final int index(int bit){
		return Integer.bitCount(bitmap & (bit - 1));
	}

	BitmapIndexedNode(AtomicReference<Thread> edit, int bitmap, Object[] array){
		this.bitmap = bitmap;
		this.array = array;
		this.edit = edit;
	}
	
	public Iterator nodeItFrom(int shift, int hash, Object key) {
		return new BitmapIndexedNodeIterator(this, shift, hash, key);
	}
	public Iterator nodeIt(boolean reverse) {
		return reverse? new ReverseBitmapIndexedNodeIterator(this) : new BitmapIndexedNodeIterator(this);
	}
	
	static class BitmapIndexedNodeIterator implements Iterator{
		BitmapIndexedNode node;
		
		int index;
		int N;
		Iterator current;
		public BitmapIndexedNodeIterator(BitmapIndexedNode node) {
			this.node = node;
			N = node.array.length;
			moveCurIfNeeded();
		}
		public BitmapIndexedNodeIterator(BitmapIndexedNode bitmapIndexedNode,
				int shift, int hash, Object key) {
			this.node = bitmapIndexedNode;
			N = node.array.length;
			moveCurTo(shift, hash, key);
		}
		private void moveCurTo(int shift, int hash, Object key) {
			int bit = bitpos(hash, shift);
			if((node.bitmap & bit) == 0)
				return;
			index = 2*node.index(bit);
			Object keyOrNull = node.array[index];
			Object valOrNode = node.array[index+1];
			if(keyOrNull == null) {
				index += 2;
				INode val = ((INode) valOrNode);
				if (val != null) {
					Iterator nodeIt  = val.nodeItFrom(shift + 5, hash, key);
					if (nodeIt.hasNext()) {
						current = nodeIt;
						return;
					}
				} 
			} else {
				if(Util.equals(key, keyOrNull)) {
					return;//OK index points to key
				} else {
					throw new IllegalArgumentException("Key not found: "+key);
				}
					
			}
				
		}
		public boolean hasNext() {
			moveCurIfNeeded();
			if (current == null && index >= N) {
				return false;
			}
			return true;
		}
		//current != null => current.hasNext or index points to a valid key
		private void moveCurIfNeeded() {
			if (current != null && current.hasNext()) return;
			current = null;
			while (index < N) {
				Object keyOrNull = node.array[index];
				Object valOrNode = node.array[index+1];
				if (keyOrNull == null) {
					index += 2;
					INode val = ((INode) valOrNode);
					if (val != null) {
						Iterator nodeIt  = val.nodeIt(false);
						if (nodeIt.hasNext()) {
							current = nodeIt;
							return;
						}
					} 
				} else {
					return;
				}
			} 
		}

		@Override
		public Object next() {
			if (current != null) {
				return current.next();
			} else {
				Object keyOrNull = node.array[index++];
				Object valOrNode = node.array[index++];
				return new MapEntry(keyOrNull, valOrNode);
			}
				
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}		
		
	}
	
	static final class ReverseBitmapIndexedNodeIterator implements Iterator {
		BitmapIndexedNode node;
		
		int index;
		Iterator current;
		public ReverseBitmapIndexedNodeIterator(BitmapIndexedNode node) {
			this.node = node;
			index = node.array.length-1;
			moveCurIfNeeded();
		}

		public boolean hasNext() {
			moveCurIfNeeded();
			if (current == null && index < 0) {
				return false;
			}
			return true;
		}
		//current != null => current.hasNext or index points to a valid key
		private void moveCurIfNeeded() {
			if (current != null && current.hasNext()) return;
			current = null;
			while (index >= 0) {
				Object valOrNode = node.array[index];
				Object keyOrNull = node.array[index-1];
				if (keyOrNull == null) {
					index -= 2;
					INode val = ((INode) valOrNode);
					if (val != null) {
						Iterator nodeIt  = val.nodeIt(true);
						if (nodeIt.hasNext()) {
							current = nodeIt;
							return;
						}
					} 
				} else {
					return;
				}
			} 
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}		
		@Override
		public Object next() {
			if (current != null) {
				return current.next();
			} else {
				Object valOrNode = node.array[index--];
				Object keyOrNull = node.array[index--];
				return new MapEntry(keyOrNull, valOrNode);
			}
				
		}
	}
	
	public INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf){
		int bit = bitpos(hash, shift);
		int idx = index(bit);
		if((bitmap & bit) != 0) {
			Object keyOrNull = array[2*idx];
			Object valOrNode = array[2*idx+1];
			if(keyOrNull == null) {
				INode n = ((INode) valOrNode).assoc(shift + 5, hash, key, val, addedLeaf);
				if(n == valOrNode)
					return this;
				return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2*idx+1, n));
			} 
			if(Util.equiv(key, keyOrNull)) {
				if(val == valOrNode)
					return this;
				return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2*idx+1, val));
			} 
			addedLeaf.val = addedLeaf;
			return new BitmapIndexedNode(null, bitmap, 
					cloneAndSet(array, 
							2*idx, null, 
							2*idx+1, createNode(shift + 5, keyOrNull, valOrNode, hash, key, val)));
		} else {
			int n = Integer.bitCount(bitmap);
			if(n >= 16) {
				INode[] nodes = new INode[32];
				int jdx = mask(hash, shift);
				nodes[jdx] = EMPTY.assoc(shift + 5, hash, key, val, addedLeaf);  
				int j = 0;
				for(int i = 0; i < 32; i++)
					if(((bitmap >>> i) & 1) != 0) {
						if (array[j] == null)
							nodes[i] = (INode) array[j+1];
						else
							nodes[i] = EMPTY.assoc(shift + 5, hash(array[j]), array[j], array[j+1], addedLeaf);
						j += 2;
					}
				return new ArrayNode(null, n + 1, nodes);
			} else {
				Object[] newArray = new Object[2*(n+1)];
				System.arraycopy(array, 0, newArray, 0, 2*idx);
				newArray[2*idx] = key;
				addedLeaf.val = addedLeaf; 
				newArray[2*idx+1] = val;
				System.arraycopy(array, 2*idx, newArray, 2*(idx+1), 2*(n-idx));
				return new BitmapIndexedNode(null, bitmap | bit, newArray);
			}
		}
	}

	public INode without(int shift, int hash, Object key){
		int bit = bitpos(hash, shift);
		if((bitmap & bit) == 0)
			return this;
		int idx = index(bit);
		Object keyOrNull = array[2*idx];
		Object valOrNode = array[2*idx+1];
		if(keyOrNull == null) {
			INode n = ((INode) valOrNode).without(shift + 5, hash, key);
			if (n == valOrNode)
				return this;
			if (n != null)
				return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2*idx+1, n));
			if (bitmap == bit) 
				return null;
			return new BitmapIndexedNode(null, bitmap ^ bit, removePair(array, idx));
		}
		if(Util.equiv(key, keyOrNull))
			// TODO: collapse
			return new BitmapIndexedNode(null, bitmap ^ bit, removePair(array, idx));
		return this;
	}
	
	public IMapEntry find(int shift, int hash, Object key){
		int bit = bitpos(hash, shift);
		if((bitmap & bit) == 0)
			return null;
		int idx = index(bit);
		Object keyOrNull = array[2*idx];
		Object valOrNode = array[2*idx+1];
		if(keyOrNull == null)
			return ((INode) valOrNode).find(shift + 5, hash, key);
		if(Util.equiv(key, keyOrNull))
			return new MapEntry(keyOrNull, valOrNode);
		return null;
	}

	public Object find(int shift, int hash, Object key, Object notFound){
		int bit = bitpos(hash, shift);
		if((bitmap & bit) == 0)
			return notFound;
		int idx = index(bit);
		Object keyOrNull = array[2*idx];
		Object valOrNode = array[2*idx+1];
		if(keyOrNull == null)
			return ((INode) valOrNode).find(shift + 5, hash, key, notFound);
		if(Util.equiv(key, keyOrNull))
			return valOrNode;
		return notFound;
	}

	public ISeq nodeSeq(){
		return NodeSeq.create(array);
	}

   public Object kvreduce(IFn f, Object init){
        return NodeSeq.kvreduce(array,f,init);
   }

	public Object fold(IFn combinef, IFn reducef, IFn fjtask, IFn fjfork, IFn fjjoin){
		return NodeSeq.kvreduce(array, reducef, combinef.invoke());
	}

	private BitmapIndexedNode ensureEditable(AtomicReference<Thread> edit){
		if(this.edit == edit)
			return this;
		int n = Integer.bitCount(bitmap);
		Object[] newArray = new Object[n >= 0 ? 2*(n+1) : 4]; // make room for next assoc
		System.arraycopy(array, 0, newArray, 0, 2*n);
		return new BitmapIndexedNode(edit, bitmap, newArray);
	}
	
	private BitmapIndexedNode editAndSet(AtomicReference<Thread> edit, int i, Object a) {
		BitmapIndexedNode editable = ensureEditable(edit);
		editable.array[i] = a;
		return editable;
	}

	private BitmapIndexedNode editAndSet(AtomicReference<Thread> edit, int i, Object a, int j, Object b) {
		BitmapIndexedNode editable = ensureEditable(edit);
		editable.array[i] = a;
		editable.array[j] = b;
		return editable;
	}

	private BitmapIndexedNode editAndRemovePair(AtomicReference<Thread> edit, int bit, int i) {
		if (bitmap == bit) 
			return null;
		BitmapIndexedNode editable = ensureEditable(edit);
		editable.bitmap ^= bit;
		System.arraycopy(editable.array, 2*(i+1), editable.array, 2*i, editable.array.length - 2*(i+1));
		editable.array[editable.array.length - 2] = null;
		editable.array[editable.array.length - 1] = null;
		return editable;
	}

	public INode assoc(AtomicReference<Thread> edit, int shift, int hash, Object key, Object val, Box addedLeaf){
		int bit = bitpos(hash, shift);
		int idx = index(bit);
		if((bitmap & bit) != 0) {
			Object keyOrNull = array[2*idx];
			Object valOrNode = array[2*idx+1];
			if(keyOrNull == null) {
				INode n = ((INode) valOrNode).assoc(edit, shift + 5, hash, key, val, addedLeaf);
				if(n == valOrNode)
					return this;
				return editAndSet(edit, 2*idx+1, n);
			} 
			if(Util.equiv(key, keyOrNull)) {
				if(val == valOrNode)
					return this;
				return editAndSet(edit, 2*idx+1, val);
			} 
			addedLeaf.val = addedLeaf;
			return editAndSet(edit, 2*idx, null, 2*idx+1, 
					createNode(edit, shift + 5, keyOrNull, valOrNode, hash, key, val)); 
		} else {
			int n = Integer.bitCount(bitmap);
			if(n*2 < array.length) {
				addedLeaf.val = addedLeaf;
				BitmapIndexedNode editable = ensureEditable(edit);
				System.arraycopy(editable.array, 2*idx, editable.array, 2*(idx+1), 2*(n-idx));
				editable.array[2*idx] = key;
				editable.array[2*idx+1] = val;
				editable.bitmap |= bit;
				return editable;
			}
			if(n >= 16) {
				INode[] nodes = new INode[32];
				int jdx = mask(hash, shift);
				nodes[jdx] = EMPTY.assoc(edit, shift + 5, hash, key, val, addedLeaf);  
				int j = 0;
				for(int i = 0; i < 32; i++)
					if(((bitmap >>> i) & 1) != 0) {
						if (array[j] == null)
							nodes[i] = (INode) array[j+1];
						else
							nodes[i] = EMPTY.assoc(edit, shift + 5, hash(array[j]), array[j], array[j+1], addedLeaf);
						j += 2;
					}
				return new ArrayNode(edit, n + 1, nodes);
			} else {
				Object[] newArray = new Object[2*(n+4)];
				System.arraycopy(array, 0, newArray, 0, 2*idx);
				newArray[2*idx] = key;
				addedLeaf.val = addedLeaf; 
				newArray[2*idx+1] = val;
				System.arraycopy(array, 2*idx, newArray, 2*(idx+1), 2*(n-idx));
				BitmapIndexedNode editable = ensureEditable(edit);
				editable.array = newArray;
				editable.bitmap |= bit;
				return editable;
			}
		}
	}

	public INode without(AtomicReference<Thread> edit, int shift, int hash, Object key, Box removedLeaf){
		int bit = bitpos(hash, shift);
		if((bitmap & bit) == 0)
			return this;
		int idx = index(bit);
		Object keyOrNull = array[2*idx];
		Object valOrNode = array[2*idx+1];
		if(keyOrNull == null) {
			INode n = ((INode) valOrNode).without(edit, shift + 5, hash, key, removedLeaf);
			if (n == valOrNode)
				return this;
			if (n != null)
				return editAndSet(edit, 2*idx+1, n); 
			if (bitmap == bit) 
				return null;
			return editAndRemovePair(edit, bit, idx); 
		}
		if(Util.equiv(key, keyOrNull)) {
			removedLeaf.val = removedLeaf;
			// TODO: collapse
			return editAndRemovePair(edit, bit, idx); 			
		}
		return this;
	}
}

final static class HashCollisionNode implements INode{

	final int hash;
	int count;
	Object[] array;
	final AtomicReference<Thread> edit;

	HashCollisionNode(AtomicReference<Thread> edit, int hash, int count, Object... array){
		this.edit = edit;
		this.hash = hash;
		this.count = count;
		this.array = array;
	}
	
	static final class HashCollisionNodeIterator implements Iterator {
		Object[] array;
		int index;
		int count;
		public HashCollisionNodeIterator(HashCollisionNode node) {
			
			this.array = node.array;
			this.count = node.count;
		}
		public HashCollisionNodeIterator(HashCollisionNode hashCollisionNode,
				int shift, int hash, Object key) {
			this.array = hashCollisionNode.array;
			this.count = hashCollisionNode.count;
			int idx = hashCollisionNode.findIndex(key);
			index = idx == -1 ? count * 2 : idx;
			
		}
		public boolean hasNext() {
			 return index < count * 2;
		}
		
		public Object next() {
			Object k = array[index++];
			Object v = array[index++];
			return new MapEntry(k, v);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}
	static final class ReverseHashCollisionNodeIterator implements Iterator {
		Object[] array;
		int index;
		int count;
		public ReverseHashCollisionNodeIterator(HashCollisionNode node) {
			this.array = node.array;
			this.count = node.count;
			this.index = count*2 - 1;
		}
		public boolean hasNext() {
			 return index >= 0;
		}
		
		public Object next() {
			Object v = array[index--];
			Object k = array[index--];
			return new MapEntry(k, v);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}
	
	public Iterator nodeItFrom(int shift, int hash, Object key) {
		return new HashCollisionNodeIterator(this,shift,hash,key);
	}
	public Iterator nodeIt(boolean reverse) {
		return reverse?new ReverseHashCollisionNodeIterator(this) : new HashCollisionNodeIterator(this);
	}

	
	public INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf){
		if(hash == this.hash) {
			int idx = findIndex(key);
			if(idx != -1) {
				if(array[idx + 1] == val)
					return this;
				return new HashCollisionNode(null, hash, count, cloneAndSet(array, idx + 1, val));
			}
			Object[] newArray = new Object[array.length + 2];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = key;
			newArray[array.length + 1] = val;
			addedLeaf.val = addedLeaf;
			return new HashCollisionNode(edit, hash, count + 1, newArray);
		}
		// nest it in a bitmap node
		return new BitmapIndexedNode(null, bitpos(this.hash, shift), new Object[] {null, this})
			.assoc(shift, hash, key, val, addedLeaf);
	}

	public INode without(int shift, int hash, Object key){
		int idx = findIndex(key);
		if(idx == -1)
			return this;
		if(count == 1)
			return null;
		return new HashCollisionNode(null, hash, count - 1, removePair(array, idx/2));
	}

	public IMapEntry find(int shift, int hash, Object key){
		int idx = findIndex(key);
		if(idx < 0)
			return null;
		if(Util.equiv(key, array[idx]))
			return new MapEntry(array[idx], array[idx+1]);
		return null;
	}

	public Object find(int shift, int hash, Object key, Object notFound){
		int idx = findIndex(key);
		if(idx < 0)
			return notFound;
		if(Util.equiv(key, array[idx]))
			return array[idx+1];
		return notFound;
	}

	public ISeq nodeSeq(){
		return NodeSeq.create(array);
	}

   public Object kvreduce(IFn f, Object init){
        return NodeSeq.kvreduce(array,f,init);
   }

	public Object fold(IFn combinef, IFn reducef, IFn fjtask, IFn fjfork, IFn fjjoin){
		return NodeSeq.kvreduce(array, reducef, combinef.invoke());
	}

	public int findIndex(Object key){
		for(int i = 0; i < 2*count; i+=2)
			{
			if(Util.equiv(key, array[i]))
				return i;
			}
		return -1;
	}

	private HashCollisionNode ensureEditable(AtomicReference<Thread> edit){
		if(this.edit == edit)
			return this;
		Object[] newArray = new Object[2*(count+1)]; // make room for next assoc
		System.arraycopy(array, 0, newArray, 0, 2*count);
		return new HashCollisionNode(edit, hash, count, newArray);
	}

	private HashCollisionNode ensureEditable(AtomicReference<Thread> edit, int count, Object[] array){
		if(this.edit == edit) {
			this.array = array;
			this.count = count;
			return this;
		}
		return new HashCollisionNode(edit, hash, count, array);
	}

	private HashCollisionNode editAndSet(AtomicReference<Thread> edit, int i, Object a) {
		HashCollisionNode editable = ensureEditable(edit);
		editable.array[i] = a;
		return editable;
	}

	private HashCollisionNode editAndSet(AtomicReference<Thread> edit, int i, Object a, int j, Object b) {
		HashCollisionNode editable = ensureEditable(edit);
		editable.array[i] = a;
		editable.array[j] = b;
		return editable;
	}


	public INode assoc(AtomicReference<Thread> edit, int shift, int hash, Object key, Object val, Box addedLeaf){
		if(hash == this.hash) {
			int idx = findIndex(key);
			if(idx != -1) {
				if(array[idx + 1] == val)
					return this;
				return editAndSet(edit, idx+1, val); 
			}
			if (array.length > 2*count) {
				addedLeaf.val = addedLeaf;
				HashCollisionNode editable = editAndSet(edit, 2*count, key, 2*count+1, val);
				editable.count++;
				return editable;
			}
			Object[] newArray = new Object[array.length + 2];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = key;
			newArray[array.length + 1] = val;
			addedLeaf.val = addedLeaf;
			return ensureEditable(edit, count + 1, newArray);
		}
		// nest it in a bitmap node
		return new BitmapIndexedNode(edit, bitpos(this.hash, shift), new Object[] {null, this, null, null})
			.assoc(edit, shift, hash, key, val, addedLeaf);
	}	

	public INode without(AtomicReference<Thread> edit, int shift, int hash, Object key, Box removedLeaf){
		int idx = findIndex(key);
		if(idx == -1)
			return this;
		removedLeaf.val = removedLeaf;
		if(count == 1)
			return null;
		HashCollisionNode editable = ensureEditable(edit);
		editable.array[idx] = editable.array[2*count-2];
		editable.array[idx+1] = editable.array[2*count-1];
		editable.array[2*count-2] = editable.array[2*count-1] = null;
		editable.count--;
		return editable;
	}
}

/*
public static void main(String[] args){
	try
		{
		ArrayList words = new ArrayList();
		Scanner s = new Scanner(new File(args[0]));
		s.useDelimiter(Pattern.compile("\\W"));
		while(s.hasNext())
			{
			String word = s.next();
			words.add(word);
			}
		System.out.println("words: " + words.size());
		IPersistentMap map = PersistentHashMap.EMPTY;
		//IPersistentMap map = new PersistentTreeMap();
		//Map ht = new Hashtable();
		Map ht = new HashMap();
		Random rand;

		System.out.println("Building map");
		long startTime = System.nanoTime();
		for(Object word5 : words)
			{
			map = map.assoc(word5, word5);
			}
		rand = new Random(42);
		IPersistentMap snapshotMap = map;
		for(int i = 0; i < words.size() / 200; i++)
			{
			map = map.without(words.get(rand.nextInt(words.size() / 2)));
			}
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("count = " + map.count() + ", time: " + estimatedTime / 1000000);

		System.out.println("Building ht");
		startTime = System.nanoTime();
		for(Object word1 : words)
			{
			ht.put(word1, word1);
			}
		rand = new Random(42);
		for(int i = 0; i < words.size() / 200; i++)
			{
			ht.remove(words.get(rand.nextInt(words.size() / 2)));
			}
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("count = " + ht.size() + ", time: " + estimatedTime / 1000000);

		System.out.println("map lookup");
		startTime = System.nanoTime();
		int c = 0;
		for(Object word2 : words)
			{
			if(!map.contains(word2))
				++c;
			}
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("notfound = " + c + ", time: " + estimatedTime / 1000000);
		System.out.println("ht lookup");
		startTime = System.nanoTime();
		c = 0;
		for(Object word3 : words)
			{
			if(!ht.containsKey(word3))
				++c;
			}
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("notfound = " + c + ", time: " + estimatedTime / 1000000);
		System.out.println("snapshotMap lookup");
		startTime = System.nanoTime();
		c = 0;
		for(Object word4 : words)
			{
			if(!snapshotMap.contains(word4))
				++c;
			}
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("notfound = " + c + ", time: " + estimatedTime / 1000000);
		}
	catch(FileNotFoundException e)
		{
		e.printStackTrace();
		}

}
*/

private static INode[] cloneAndSet(INode[] array, int i, INode a) {
	INode[] clone = array.clone();
	clone[i] = a;
	return clone;
}

private static Object[] cloneAndSet(Object[] array, int i, Object a) {
	Object[] clone = array.clone();
	clone[i] = a;
	return clone;
}

private static Object[] cloneAndSet(Object[] array, int i, Object a, int j, Object b) {
	Object[] clone = array.clone();
	clone[i] = a;
	clone[j] = b;
	return clone;
}

private static Object[] removePair(Object[] array, int i) {
	Object[] newArray = new Object[array.length - 2];
	System.arraycopy(array, 0, newArray, 0, 2*i);
	System.arraycopy(array, 2*(i+1), newArray, 2*i, newArray.length - 2*i);
	return newArray;
}

private static INode createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
	int key1hash = hash(key1);
	if(key1hash == key2hash)
		return new HashCollisionNode(null, key1hash, 2, new Object[] {key1, val1, key2, val2});
	Box _ = new Box(null);
	AtomicReference<Thread> edit = new AtomicReference<Thread>();
	return BitmapIndexedNode.EMPTY
		.assoc(edit, shift, key1hash, key1, val1, _)
		.assoc(edit, shift, key2hash, key2, val2, _);
}

private static INode createNode(AtomicReference<Thread> edit, int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
	int key1hash = hash(key1);
	if(key1hash == key2hash)
		return new HashCollisionNode(null, key1hash, 2, new Object[] {key1, val1, key2, val2});
	Box _ = new Box(null);
	return BitmapIndexedNode.EMPTY
		.assoc(edit, shift, key1hash, key1, val1, _)
		.assoc(edit, shift, key2hash, key2, val2, _);
}

private static int bitpos(int hash, int shift){
	return 1 << mask(hash, shift);
}

static final class NodeSeq extends ASeq {
	final Object[] array;
	final int i;
	final ISeq s;
	
	NodeSeq(Object[] array, int i) {
		this(null, array, i, null);
	}

	static ISeq create(Object[] array) {
		return create(array, 0, null);
	}

    static public Object kvreduce(Object[] array, IFn f, Object init){
        for(int i=0;i<array.length;i+=2)
            {
            if(array[i] != null)
                init = f.invoke(init, array[i], array[i+1]);
            else
                {
                INode node = (INode) array[i+1];
                if(node != null)
                    init = node.kvreduce(f,init);
                }
            if(RT.isReduced(init))
	             return ((IDeref)init).deref();
            }
       return init;
   }

	private static ISeq create(Object[] array, int i, ISeq s) {
		if(s != null)
			return new NodeSeq(null, array, i, s);
		for(int j = i; j < array.length; j+=2) {
			if(array[j] != null)
				return new NodeSeq(null, array, j, null);
			INode node = (INode) array[j+1];
			if (node != null) {
				ISeq nodeSeq = node.nodeSeq();
				if(nodeSeq != null)
					return new NodeSeq(null, array, j + 2, nodeSeq);
			}
		}
		return null;
	}
	
	NodeSeq(IPersistentMap meta, Object[] array, int i, ISeq s) {
		super(meta);
		this.array = array;
		this.i = i;
		this.s = s;
	}

	public Obj withMeta(IPersistentMap meta) {
		return new NodeSeq(meta, array, i, s);
	}

	public Object first() {
		if(s != null)
			return s.first();
		return new MapEntry(array[i], array[i+1]);
	}

	public ISeq next() {
		if(s != null)
			return create(array, i, s.next());
		return create(array, i + 2, null);
	}
}

}
