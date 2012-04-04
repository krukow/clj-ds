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

import java.util.Collection;
import java.util.Iterator;

/**
 * conses onto rear, peeks/pops from front
 * See Okasaki's Batched Queues
 * This differs in that it uses a PersistentArrayList as the rear, which is in-order,
 * so no reversing or suspensions required for persistent use
 */

public class PersistentQueue<T> extends Obj implements IPersistentList<T>, Collection<T>{

final public static PersistentQueue EMPTY = new PersistentQueue(null, null, null);

//*
final ISeq f;
final PersistentVector r;
//static final int INITIAL_REAR_SIZE = 4;
int _hash = -1;

PersistentQueue(IPersistentMap meta, ISeq f, PersistentVector r){
	super(meta);
	this.f = f;
	this.r = r;
}

public boolean equiv(Object obj){

	if(!(obj instanceof Sequential))
		return false;
	ISeq ms = RT.seq(obj);
	for(ISeq s = seq(); s != null; s = s.next(), ms = ms.next())
		{
		if(ms == null || !Util.equiv(s.first(), ms.first()))
			return false;
		}
	return ms == null;

}

public boolean equals(Object obj){

	if(!(obj instanceof Sequential))
		return false;
	ISeq ms = RT.seq(obj);
	for(ISeq s = seq(); s != null; s = s.next(), ms = ms.next())
		{
		if(ms == null || !Util.equals(s.first(), ms.first()))
			return false;
		}
	return ms == null;

}

public int hashCode(){
	if(_hash == -1)
		{
		int hash = 0;
		for(ISeq s = seq(); s != null; s = s.next())
			{
			hash = Util.hashCombine(hash, Util.hash(s.first()));
			}
		this._hash = hash;
		}
	return _hash;
}

public T peek(){
	return (T) RT.first(f);
}

public PersistentQueue<T> pop(){
	if(f == null)  //hmmm... pop of empty queue -> empty queue?
		return this;
	//throw new IllegalStateException("popping empty queue");
	ISeq f1 = f.next();
	PersistentVector r1 = r;
	if(f1 == null)
		{
		f1 = RT.seq(r);
		r1 = null;
		}
	return new PersistentQueue<T>(meta(), f1, r1);
}

public int count(){
	return RT.count(f) + RT.count(r);
}

public ISeq<T> seq(){
	if(f == null)
		return null;
	return new Seq<T>(f, RT.seq(r));
}

public PersistentQueue<T> cons(T o){
	if(f == null)     //empty
		return new PersistentQueue<T>(meta(), RT.list(o), null);
	else
		return new PersistentQueue<T>(meta(), f, (r != null ? r : PersistentVector.EMPTY).cons(o));
}

public IPersistentCollection<T> empty(){
	return EMPTY.withMeta(meta());	
}

public PersistentQueue<T> withMeta(IPersistentMap meta){
	return new PersistentQueue<T>(meta, f, r);
}

static class Seq<T> extends ASeq<T>{
	final ISeq<T> f;
	final ISeq<T> rseq;

	Seq(ISeq<T> f, ISeq<T> rseq){
		this.f = f;
		this.rseq = rseq;
	}

	Seq(IPersistentMap meta, ISeq<T> f, ISeq<T> rseq){
		super(meta);
		this.f = f;
		this.rseq = rseq;
	}

	public T first(){
		return f.first();
	}

	public ISeq<T> next(){
		ISeq<T> f1 = f.next();
		ISeq<T> r1 = rseq;
		if(f1 == null)
			{
			if(rseq == null)
				return null;
			f1 = rseq;
			r1 = null;
			}
		return new Seq<T>(f1, r1);
	}

	public int count(){
		return RT.count(f) + RT.count(rseq);
	}

	public Seq<T> withMeta(IPersistentMap meta){
		return new Seq<T>(meta, f, rseq);
	}
}

// java.util.Collection implementation

public Object[] toArray(){
	return RT.seqToArray(seq());
}

public boolean add(Object o){
	throw new UnsupportedOperationException();
}

public boolean remove(Object o){
	throw new UnsupportedOperationException();
}

public boolean addAll(Collection<? extends T> c){
	throw new UnsupportedOperationException();
}

public void clear(){
	throw new UnsupportedOperationException();
}

public boolean retainAll(Collection<?> c){
	throw new UnsupportedOperationException();
}

public boolean removeAll(Collection<?> c){
	throw new UnsupportedOperationException();
}

public boolean containsAll(Collection<?> c){
	for(Object o : c)
		{
		if(contains(o))
			return true;
		}
	return false;
}

public Object[] toArray(Object[] a){
	if(a.length >= count())
		{
		ISeq s = seq();
		for(int i = 0; s != null; ++i, s = s.next())
			{
			a[i] = s.first();
			}
		if(a.length >= count())
			a[count()] = null;
		return a;
		}
	else
		return toArray();
}

public int size(){
	return count();
}

public boolean isEmpty(){
	return count() == 0;
}

public boolean contains(Object o){
	for(ISeq<T> s = seq(); s != null; s = s.next())
		{
		if(Util.equiv(s.first(), o))
			return true;
		}
	return false;
}

public Iterator<T> iterator(){
	return new SeqIterator<T>(seq());
}

/*
public static void main(String[] args){
	if(args.length != 1)
		{
		System.err.println("Usage: PersistentQueue n");
		return;
		}
	int n = Integer.parseInt(args[0]);


	long startTime, estimatedTime;

	Queue list = new LinkedList();
	//Queue list = new ConcurrentLinkedQueue();
	System.out.println("Queue");
	startTime = System.nanoTime();
	for(int i = 0; i < n; i++)
		{
		list.add(i);
		list.add(i);
		list.remove();
		}
	for(int i = 0; i < n - 10; i++)
		{
		list.remove();
		}
	estimatedTime = System.nanoTime() - startTime;
	System.out.println("time: " + estimatedTime / 1000000);
	System.out.println("peek: " + list.peek());


	PersistentQueue q = PersistentQueue.EMPTY;
	System.out.println("PersistentQueue");
	startTime = System.nanoTime();
	for(int i = 0; i < n; i++)
		{
		q = q.cons(i);
		q = q.cons(i);
		q = q.pop();
		}
//    IPersistentList lastq = null;
//    IPersistentList lastq2;
	for(int i = 0; i < n - 10; i++)
		{
		//lastq2 = lastq;
		//lastq = q;
		q = q.pop();
		}
	estimatedTime = System.nanoTime() - startTime;
	System.out.println("time: " + estimatedTime / 1000000);
	System.out.println("peek: " + q.peek());

	IPersistentList q2 = q;
	for(int i = 0; i < 10; i++)
		{
		q2 = (IPersistentList) q2.cons(i);
		}
//    for(ISeq s = q.seq();s != null;s = s.rest())
//        System.out.println("q: " + s.first().toString());
//    for(ISeq s = q2.seq();s != null;s = s.rest())
//        System.out.println("q2: " + s.first().toString());
}
*/
}
