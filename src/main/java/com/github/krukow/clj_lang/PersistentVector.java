/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jul 5, 2007 */

package com.github.krukow.clj_lang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class PersistentVector<T> extends APersistentVector<T> implements IObj, IEditableCollection<T>, com.github.krukow.clj_ds.PersistentVector<T>{

static class Node implements Serializable {
	transient final AtomicReference<Thread> edit;
	final Object[] array;

	Node(AtomicReference<Thread> edit, Object[] array){
		this.edit = edit;
		this.array = array;
	}

	Node(AtomicReference<Thread> edit){
		this.edit = edit;
		this.array = new Object[32];
	}
}

final static AtomicReference<Thread> NOEDIT = new AtomicReference<Thread>(null);
final static Node EMPTY_NODE = new Node(NOEDIT, new Object[32]);

final int cnt;
final int shift;
final Node root;
final Object[] tail;
final IPersistentMap _meta;


@SuppressWarnings("unchecked")
public final static PersistentVector EMPTY = new PersistentVector(0, 5, EMPTY_NODE, new Object[]{});

@SuppressWarnings("unchecked")
static public <T> PersistentVector<T> emptyVector() {
	return EMPTY;
}

@SuppressWarnings("unchecked")
static public <T> PersistentVector<T> create(ISeq<? extends T> items){
	TransientVector<T> ret = EMPTY.asTransient();
	for(; items != null; items = items.next())
		ret = ret.conj(items.first());
	return ret.persistentMap();
}

@SuppressWarnings("unchecked")
static public <T> PersistentVector<T> create(Iterable<? extends T> items){
	TransientVector<T> ret = EMPTY.asTransient();
	for(T item : items)
		ret = ret.conj(item);
	return ret.persistentMap();
}

@SuppressWarnings("unchecked")
static public <T> PersistentVector<T> create(T ... items){
	TransientVector<T> ret = EMPTY.asTransient();
	for(T item : items)
		ret = ret.conj(item);
	return ret.persistentMap();
}

PersistentVector(int cnt, int shift, Node root, Object[] tail){
	this._meta = null;
	this.cnt = cnt;
	this.shift = shift;
	this.root = root;
	this.tail = tail;
}


PersistentVector(IPersistentMap meta, int cnt, int shift, Node root, Object[] tail){
	this._meta = meta;
	this.cnt = cnt;
	this.shift = shift;
	this.root = root;
	this.tail = tail;
}

public PersistentVector(IPersistentMap meta, int cnt, int shift, Node root, Object[] tail, IFn f) {
	this._meta = meta();
	this.cnt = cnt;
	this.shift = shift;
	this.tail = mapArray(f,Util.ret1(tail,tail=null));
	this.root = mapNode(f,Util.ret1(root, root=null), this.shift);
}


public TransientVector<T> asTransient(){
	return new TransientVector<T>(this);
}

final int tailoff(){
	if(cnt < 32)
		return 0;
	return ((cnt - 1) >>> 5) << 5;
}

public Object[] arrayFor(int i){
	if(i >= 0 && i < cnt)
		{
		if(i >= tailoff())
			return tail;
		Node node = root;
		for(int level = shift; level > 0; level -= 5)
			node = (Node) node.array[(i >>> level) & 0x01f];
		return node.array;
		}
	throw new IndexOutOfBoundsException();
}

public T nth(int i){
	Object[] node = arrayFor(i);
	return (T) node[i & 0x01f];
}

public T nth(int i, T notFound){
	if(i >= 0 && i < cnt)
		return nth(i);
	return notFound;
}

public PersistentVector<T> assocN(int i, T val){
	if(i >= 0 && i < cnt)
		{
		if(i >= tailoff())
			{
			Object[] newTail = new Object[tail.length];
			System.arraycopy(tail, 0, newTail, 0, tail.length);
			newTail[i & 0x01f] = val;

			return new PersistentVector<T>(meta(), cnt, shift, root, newTail);
			}

		return new PersistentVector<T>(meta(), cnt, shift, doAssoc(shift, root, i, val), tail);
		}
	if(i == cnt)
		return cons(val);	
	throw new IndexOutOfBoundsException();
}

private static Node doAssoc(int level, Node node, int i, Object val){
	Node ret = new Node(node.edit,node.array.clone());
	if(level == 0)
		{
		ret.array[i & 0x01f] = val;
		}
	else
		{
		int subidx = (i >>> level) & 0x01f;
		ret.array[subidx] = doAssoc(level - 5, (Node) node.array[subidx], i, val);
		}
	return ret;
}

public int count(){
	return cnt;
}

public PersistentVector<T> withMeta(IPersistentMap meta){
	return new PersistentVector<T>(meta, cnt, shift, root, tail);
}

public IPersistentMap meta(){
	return _meta;
}


public PersistentVector<T> cons(T val){
	int i = cnt;
	//room in tail?
//	if(tail.length < 32)
	if(cnt - tailoff() < 32)
		{
		Object[] newTail = new Object[tail.length + 1];
		System.arraycopy(tail, 0, newTail, 0, tail.length);
		newTail[tail.length] = val;
		return new PersistentVector<T>(meta(), cnt + 1, shift, root, newTail);
		}
	//full tail, push into tree
	Node newroot;
	Node tailnode = new Node(root.edit,tail);
	int newshift = shift;
	//overflow root?
	if((cnt >>> 5) > (1 << shift))
		{
		newroot = new Node(root.edit);
		newroot.array[0] = root;
		newroot.array[1] = newPath(root.edit,shift, tailnode);
		newshift += 5;
		}
	else
		newroot = pushTail(shift, root, tailnode);
	return new PersistentVector<T>(meta(), cnt + 1, newshift, newroot, new Object[]{val});
}

private Node pushTail(int level, Node parent, Node tailnode){
	//if parent is leaf, insert node,
	// else does it map to an existing child? -> nodeToInsert = pushNode one more level
	// else alloc new path
	//return  nodeToInsert placed in copy of parent
	int subidx = ((cnt - 1) >>> level) & 0x01f;
	Node ret = new Node(parent.edit, parent.array.clone());
	Node nodeToInsert;
	if(level == 5)
		{
		nodeToInsert = tailnode;
		}
	else
		{
		Node child = (Node) parent.array[subidx];
		nodeToInsert = (child != null)?
		                pushTail(level-5,child, tailnode)
		                :newPath(root.edit,level-5, tailnode);
		}
	ret.array[subidx] = nodeToInsert;
	return ret;
}

private static Node newPath(AtomicReference<Thread> edit,int level, Node node){
	if(level == 0)
		return node;
	Node ret = new Node(edit);
	ret.array[0] = newPath(edit, level - 5, node);
	return ret;
}

public IChunkedSeq<T> chunkedSeq(){
	if(count() == 0)
		return null;
	return new ChunkedSeq<T>(this,0,0);
}

public ISeq<T> seq(){
	return chunkedSeq();
}

Iterator<T> rangedIterator(final int start, final int end){
	return new Iterator<T>(){
		int i = start;
		int base = i - (i%32);
		Object[] array = (start < count())?arrayFor(i):null;

		public boolean hasNext(){
			return i < end;
			}

		public T next(){
			if(i-base == 32){
				array = arrayFor(i);
				base += 32;
				}
			return (T) array[i++ & 0x01f];
			}

		public void remove(){
			throw new UnsupportedOperationException();
		}
	};
}

public Iterator iterator151(){return rangedIterator(0,count());}

public Object kvreduce(IFn f, Object init){
    int step = 0;
    for(int i=0;i<cnt;i+=step){
        Object[] array = arrayFor(i);
        for(int j =0;j<array.length;++j){
            init = f.invoke(init,j+i,array[j]);
            if(RT.isReduced(init))
	            return ((IDeref)init).deref();
            }
        step = array.length;
    }
    return init;
}


final static class PersistentVectorIterator<T> implements Iterator<T> {
	PersistentVector<T> vec;
	int sft;
	Stack<MapEntry<Integer, Object[]>> path;
	Object[] current;
	int currentIndex;
	
	public PersistentVectorIterator(PersistentVector<T> vec) {
		this.vec = vec;
		sft = vec.shift;
		path = initialPath();
		MapEntry<Integer,Object[]> el = path.peek();
		if (el.key() == -1) {
			current = el.val();
		} else {
			current = ((Node) el.val()[el.key()]).array;
		}
	}
	@Override
	public boolean hasNext() {
		ensureCurrentReady();
		return (current != null && currentIndex < current.length);
		
	}
	
	private void ensureCurrentReady() {
		if (current != null && currentIndex < current.length) {
			return;
		}//else current is null or exhausted. Find next
		Object[] last = current;
		current = findNextArray();
		if (current != last) {
			currentIndex = 0;
		}
	}
	private Object[] findNextArray() {
		if (path.isEmpty()) {return null;}
		while(path.peek().getKey() != -1) {
			MapEntry<Integer, Object[]> loc = path.pop();
			int idx = loc.key();
			Object[] arr = loc.val();
			idx += 1;
			if (idx < arr.length) {
				Node next = (Node) arr[idx];
				if (next == null) {
					continue;
				}
				path.push(new MapEntry(idx, arr));
				for(int level = sft - (path.size() - 1) * 5; level > 0; level -= 5) {
					path.push(new MapEntry<Integer, Object[]>(0,next.array));
					next = (Node) next.array[0];
				}
				return next.array;
				
			}	
		}
		return path.pop().val();
	}
	private Stack<MapEntry<Integer, Object[]>> initialPath() {
		Stack<MapEntry<Integer, Object[]>>  res = new Stack<MapEntry<Integer, Object[]>>();
		res.push(new MapEntry<Integer, Object[]>(-1, vec.tail));	
		Node node = vec.root;
		for(int level = sft; level > 0; level -= 5) {
			res.push(new MapEntry<Integer, Object[]>(0,node.array));
			node = (Node) node.array[0];
			if (node == null) {
				res.pop();
				break;
			}
		}
		return res;	
	}
	@Override
	public T next() {
		return (T) current[currentIndex++];
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

public Iterator<T> iterator(){
	return new PersistentVectorIterator(this);
}


static public final class ChunkedSeq<T> extends ASeq<T> implements IChunkedSeq<T>{

	public final PersistentVector<T> vec;
	final Object[] node;
	final int i;
	public final int offset;

	public ChunkedSeq(PersistentVector<T> vec, int i, int offset){
		this.vec = vec;
		this.i = i;
		this.offset = offset;
		this.node = vec.arrayFor(i);
	}

	ChunkedSeq(IPersistentMap meta, PersistentVector<T> vec, Object[] node, int i, int offset){
		super(meta);
		this.vec = vec;
		this.node = node;
		this.i = i;
		this.offset = offset;
	}

	ChunkedSeq(PersistentVector<T> vec, Object[] node, int i, int offset){
		this.vec = vec;
		this.node = node;
		this.i = i;
		this.offset = offset;
	}

	public IChunk<T> chunkedFirst() {
		return new ArrayChunk<T>(node, offset);
		}

	public ISeq<T> chunkedNext(){
		if(i + node.length < vec.cnt)
			return new ChunkedSeq<T>(vec,i+ node.length,0);
		return null;
		}

	public ISeq<T> chunkedMore(){
		ISeq<T> s = chunkedNext();
		if(s == null)
			return (ISeq<T>) PersistentList.emptyList();
		return s;
	}

	public Obj withMeta(IPersistentMap meta){
		if(meta == this._meta)
			return this;
		return new ChunkedSeq(meta, vec, node, i, offset);
	}

	public T first(){
		return (T) node[offset];
	}

	public ISeq<T> next(){
		if(offset + 1 < node.length)
			return new ChunkedSeq<T>(vec, node, i, offset + 1);
		return chunkedNext();
	}
	
	public int count(){
		return vec.cnt - (i + offset);
	}
}

public PersistentVector<T> empty(){
	return EMPTY.withMeta(meta());
}

//private Node pushTail(int level, Node node, Object[] tailNode, Box expansion){
//	Object newchild;
//	if(level == 0)
//		{
//		newchild = tailNode;
//		}
//	else
//		{
//		newchild = pushTail(level - 5, (Object[]) arr[arr.length - 1], tailNode, expansion);
//		if(expansion.val == null)
//			{
//			Object[] ret = arr.clone();
//			ret[arr.length - 1] = newchild;
//			return ret;
//			}
//		else
//			newchild = expansion.val;
//		}
//	//expansion
//	if(arr.length == 32)
//		{
//		expansion.val = new Object[]{newchild};
//		return arr;
//		}
//	Object[] ret = new Object[arr.length + 1];
//	System.arraycopy(arr, 0, ret, 0, arr.length);
//	ret[arr.length] = newchild;
//	expansion.val = null;
//	return ret;
//}

public PersistentVector<T> pop(){
	if(cnt == 0)
		throw new IllegalStateException("Can't pop empty vector");
	if(cnt == 1)
		return EMPTY.withMeta(meta());
	//if(tail.length > 1)
	if(cnt-tailoff() > 1)
		{
		Object[] newTail = new Object[tail.length - 1];
		System.arraycopy(tail, 0, newTail, 0, newTail.length);
		return new PersistentVector<T>(meta(), cnt - 1, shift, root, newTail);
		}
	Object[] newtail = arrayFor(cnt - 2);

	Node newroot = popTail(shift, root);
	int newshift = shift;
	if(newroot == null)
		{
		newroot = EMPTY_NODE;
		}
	if(shift > 5 && newroot.array[1] == null)
		{
		newroot = (Node) newroot.array[0];
		newshift -= 5;
		}
	return new PersistentVector<T>(meta(), cnt - 1, newshift, newroot, newtail);
}

private Node popTail(int level, Node node){
	int subidx = ((cnt-2) >>> level) & 0x01f;
	if(level > 5)
		{
		Node newchild = popTail(level - 5, (Node) node.array[subidx]);
		if(newchild == null && subidx == 0)
			return null;
		else
			{
			Node ret = new Node(root.edit, node.array.clone());
			ret.array[subidx] = newchild;
			return ret;
			}
		}
	else if(subidx == 0)
		return null;
	else
		{
		Node ret = new Node(root.edit, node.array.clone());
		ret.array[subidx] = null;
		return ret;
		}
}

static final class TransientVector<T> extends AFn implements ITransientVector<T>, Counted, com.github.krukow.clj_ds.TransientVector<T> {
	int cnt;
	int shift;
	Node root;
	Object[] tail;

	TransientVector(int cnt, int shift, Node root, Object[] tail){
		this.cnt = cnt;
		this.shift = shift;
		this.root = root;
		this.tail = tail;
	}

	TransientVector(PersistentVector<? extends T> v){
		this(v.cnt, v.shift, editableRoot(v.root), editableTail(v.tail));
	}

	public int count(){
		ensureEditable();
		return cnt;
	}
	
	Node ensureEditable(Node node){
		if(node.edit == root.edit)
			return node;
		return new Node(root.edit, node.array.clone());
	}

	void ensureEditable(){
		Thread owner = root.edit.get();
		if(owner == Thread.currentThread())
			return;
		if(owner != null)
			throw new IllegalAccessError("Transient used by non-owner thread");
		throw new IllegalAccessError("Transient used after persistent! call");

//		root = editableRoot(root);
//		tail = editableTail(tail);
	}

	static Node editableRoot(Node node){
		return new Node(new AtomicReference<Thread>(Thread.currentThread()), node.array.clone());
	}

	public PersistentVector<T> persistentMap(){
		ensureEditable();
//		Thread owner = root.edit.get();
//		if(owner != null && owner != Thread.currentThread())
//			{
//			throw new IllegalAccessError("Mutation release by non-owner thread");
//			}
		root.edit.set(null);
		Object[] trimmedTail = new Object[cnt-tailoff()];
		System.arraycopy(tail,0,trimmedTail,0,trimmedTail.length);
		return new PersistentVector<T>(cnt, shift, root, trimmedTail);
	}

	static Object[] editableTail(Object[] tl){
		Object[] ret = new Object[32];
		System.arraycopy(tl,0,ret,0,tl.length);
		return ret;
	}

	public TransientVector<T> conj(Object val){
		T t = (T) val;
		ensureEditable();
		int i = cnt;
		//room in tail?
		if(i - tailoff() < 32)
			{
			tail[i & 0x01f] = val;
			++cnt;
			return this;
			}
		//full tail, push into tree
		Node newroot;
		Node tailnode = new Node(root.edit, tail);
		tail = new Object[32];
		tail[0] = val;
		int newshift = shift;
		//overflow root?
		if((cnt >>> 5) > (1 << shift))
			{
			newroot = new Node(root.edit);
			newroot.array[0] = root;
			newroot.array[1] = newPath(root.edit,shift, tailnode);
			newshift += 5;
			}
		else
			newroot = pushTail(shift, root, tailnode);
		root = newroot;
		shift = newshift;
		++cnt;
		return this;
	}

	private Node pushTail(int level, Node parent, Node tailnode){
		//if parent is leaf, insert node,
		// else does it map to an existing child? -> nodeToInsert = pushNode one more level
		// else alloc new path
		//return  nodeToInsert placed in parent
		parent = ensureEditable(parent);
		int subidx = ((cnt - 1) >>> level) & 0x01f;
		Node ret = parent;
		Node nodeToInsert;
		if(level == 5)
			{
			nodeToInsert = tailnode;
			}
		else
			{
			Node child = (Node) parent.array[subidx];
			nodeToInsert = (child != null) ?
			               pushTail(level - 5, child, tailnode)
			                               : newPath(root.edit, level - 5, tailnode);
			}
		ret.array[subidx] = nodeToInsert;
		return ret;
	}

	final private int tailoff(){
		if(cnt < 32)
			return 0;
		return ((cnt-1) >>> 5) << 5;
	}

	private Object[] arrayFor(int i){
		if(i >= 0 && i < cnt)
			{
			if(i >= tailoff())
				return tail;
			Node node = root;
			for(int level = shift; level > 0; level -= 5)
				node = (Node) node.array[(i >>> level) & 0x01f];
			return node.array;
			}
		throw new IndexOutOfBoundsException();
	}

	private Object[] editableArrayFor(int i){
		if(i >= 0 && i < cnt)
			{
			if(i >= tailoff())
				return tail;
			Node node = root;
			for(int level = shift; level > 0; level -= 5)
				node = ensureEditable((Node) node.array[(i >>> level) & 0x01f]);
			return node.array;
			}
		throw new IndexOutOfBoundsException();
	}

	public Object valAt(Object key){
		//note - relies on ensureEditable in 2-arg valAt
		return valAt(key, null);
	}

	public Object valAt(Object key, Object notFound){
		ensureEditable();
		if(Util.isInteger(key))
			{
			int i = ((Number) key).intValue();
			if(i >= 0 && i < cnt)
				return nth(i);
			}
		return notFound;
	}

	public Object invoke(Object arg1) {
		//note - relies on ensureEditable in nth
		if(Util.isInteger(arg1))
			return nth(((Number) arg1).intValue());
		throw new IllegalArgumentException("Key must be integer");
	}

	public T nth(int i){
		ensureEditable();
		Object[] node = arrayFor(i);
		return (T) node[i & 0x01f];
	}

	public T nth(int i, T notFound){
		if(i >= 0 && i < count())
			return nth(i);
		return notFound;
	}

	public TransientVector<T> assocN(int i, T val){
		ensureEditable();
		if(i >= 0 && i < cnt)
			{
			if(i >= tailoff())
				{
				tail[i & 0x01f] = val;
				return this;
				}

			root = doAssoc(shift, root, i, val);
			return this;
			}
		if(i == cnt)
			return conj(val);
		throw new IndexOutOfBoundsException();
	}

	public TransientVector<T> assoc(Object key, Object val){
		//note - relies on ensureEditable in assocN
		if(Util.isInteger(key))
			{
			int i = ((Number) key).intValue();
			return assocN(i, (T) val);
			}
		throw new IllegalArgumentException("Key must be integer");
	}

	private Node doAssoc(int level, Node node, int i, Object val){
		node = ensureEditable(node);
		Node ret = node;
		if(level == 0)
			{
			ret.array[i & 0x01f] = val;
			}
		else
			{
			int subidx = (i >>> level) & 0x01f;
			ret.array[subidx] = doAssoc(level - 5, (Node) node.array[subidx], i, val);
			}
		return ret;
	}

	public TransientVector<T> pop(){
		ensureEditable();
		if(cnt == 0)
			throw new IllegalStateException("Can't pop empty vector");
		if(cnt == 1)
			{
			cnt = 0;
			return this;
			}
		int i = cnt - 1;
		//pop in tail?
		if((i & 0x01f) > 0)
			{
			--cnt;
			return this;
			}

		Object[] newtail = editableArrayFor(cnt - 2);

		Node newroot = popTail(shift, root);
		int newshift = shift;
		if(newroot == null)
			{
			newroot = new Node(root.edit);
			}
		if(shift > 5 && newroot.array[1] == null)
			{
			newroot = ensureEditable((Node) newroot.array[0]);
			newshift -= 5;
			}
		root = newroot;
		shift = newshift;
		--cnt;
		tail = newtail;
		return this;
	}

	private Node popTail(int level, Node node){
		node = ensureEditable(node);
		int subidx = ((cnt - 2) >>> level) & 0x01f;
		if(level > 5)
			{
			Node newchild = popTail(level - 5, (Node) node.array[subidx]);
			if(newchild == null && subidx == 0)
				return null;
			else
				{
				Node ret = node;
				ret.array[subidx] = newchild;
				return ret;
				}
			}
		else if(subidx == 0)
			return null;
		else
			{
			Node ret = node;
			ret.array[subidx] = null;
			return ret;
			}
	}

	@Override
	public PersistentVector<T> persistent() {
		return persistentMap();
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentVector<T> persist() {
		return persistent();
	}
	
	@Override
	public com.github.krukow.clj_ds.TransientVector<T> plus(T val) {
		return conj(val);
	}
	
	@Override
	public com.github.krukow.clj_ds.TransientVector<T> plusN(int i, T val) {
		return assocN(i, val);
	}
	
	@Override
	public com.github.krukow.clj_ds.TransientVector<T> minus() {
		return pop();
	}
	
}
	
	@Override
	public com.github.krukow.clj_ds.PersistentVector<T> zero() {
		return empty();
	}
	
	@Override
	public com.github.krukow.clj_ds.PersistentVector<T> plus(T o) {
		return cons(o);
	}

	@Override
	public com.github.krukow.clj_ds.PersistentVector<T> plusN(int i, T val) {
		return assocN(i, val);
	}

	@Override
	public com.github.krukow.clj_ds.PersistentVector<T> minus() {
		return pop();
	}

public static IPersistentVector vectormap(IFn f, PersistentVector v) {
	return new PersistentVector(v._meta,v.cnt,v.shift,v.root,Util.ret1(v.tail,v=null),f);
}

private static Object[] mapArray(IFn f, Object[] arr) {
	Object[] res = new Object[arr.length];
	System.arraycopy(arr, 0, res, 0, arr.length);
	arr = null;
	try {
		for(int i=0;i<res.length;i++) {	
				res[i] = f.invoke(res[i]);
		}
		return res;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

private static Node mapNode(IFn f, Node node, int level) {
	if (node == null) {return null;}
	if (level == 0) {
		return new Node(null,mapArray(f, Util.ret1(node.array, node=null)));
	}
	Object[] newArr = new Object[node.array.length];
	System.arraycopy(node.array, 0, newArr, 0, node.array.length);
	node=null;
	level -= 5;
	for (int i=0;i<newArr.length;i++) {
		newArr[i] = mapNode(f,Util.ret1((Node) newArr[i], newArr[i]=null),level);
	}
	return new Node(null,newArr);
}

}
