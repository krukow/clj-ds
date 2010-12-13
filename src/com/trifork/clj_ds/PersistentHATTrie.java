/**
 *   Copyright (c) Karl Krukow. All rights reserved.
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
import java.util.Map;
import java.util.Map.Entry;

/*
 A persistent rendition of Nikolas Askitis' HAT Trie
 Uses path copying for persistence
 Any errors are my own
*/
@SuppressWarnings({"rawtypes","unchecked"})
public class PersistentHATTrie<T> extends APersistentTrie<T> implements IObj {
	private static final long serialVersionUID = -7068824281866890730L;
	final IPersistentMap meta;
	final HATTrieNode<T> root;
	final int count;
	
	public static final PersistentHATTrie EMPTY = new PersistentHATTrie(null, null, 0);
	
	public PersistentHATTrie(HATTrieNode root, IPersistentMap meta, int count) {
		this.root = root;
		this.meta = meta;
		this.count = count;
	}

	public IPersistentMap meta() {
		return meta;
	}

	private static interface HATTrieNode<T> {
		HATTrieNode<T> add(String s, int i, T t);
		T get(String s, int j);
		Iterator<Map.Entry<String, T>> nodeIt(String prefix);
	}
	private static interface ToStringWithPrefix {
		String toStringWithPrefix(String prefix);
	}

	private static final class AccessNode<T> implements HATTrieNode<T>,ToStringWithPrefix {
		private final HATTrieNode<T> children[];
		private final T emptyPtr;
		
		public AccessNode(HATTrieNode[] children, T emptyPtr) {
			this.children = children;
			this.emptyPtr = emptyPtr;
		}
		
		public String toString() {
			return toStringWithPrefix("");
		}

		public String toStringWithPrefix(String prefix) {
			StringBuilder sb = new StringBuilder();
			String nestedPrefix = prefix+"  ";
			sb.append(prefix);
			sb.append("(access-node\n").append(nestedPrefix);
			for (int i=0;i<children.length;i++) {
				HATTrieNode node = children[i];
				if (node != null) {
					sb.append((char) i).append(" -> ").append(((ToStringWithPrefix) node)
							.toStringWithPrefix(nestedPrefix)).append(";\n").append(nestedPrefix);
				}
			}
			if (emptyPtr != null) {
				sb.append("\n").append(prefix).append("**");
			}
			sb.append(prefix).append(")");
			return sb.toString();
		}

		
		public HATTrieNode<T> add(String s, int i, T t) {
			int length = s.length();
			if (i < length) {
				char ichar = s.charAt(i);
				HATTrieNode hatTrieNode = children[ichar];
				if (hatTrieNode != null) {
					HATTrieNode newNode = hatTrieNode.add(s, i+1, t);
					if (newNode == hatTrieNode) {
						return this;
					}
					HATTrieNode[] newArr = new HATTrieNode[children.length];
					System.arraycopy(children, 0, newArr, 0, children.length);
					newArr[ichar] = newNode;
					return new AccessNode(newArr, emptyPtr);
				}
				ContainerNode c = new ContainerNode(PersistentTreeMap.EMPTY.assoc(s.substring(i+1), t));
				HATTrieNode[] newArr = new HATTrieNode[children.length];
				System.arraycopy(children, 0, newArr, 0, children.length);
				newArr[ichar] = c;
				return new AccessNode(newArr, emptyPtr);
			}
			if (i == length && emptyPtr == null) {
				return new AccessNode(children, s); 
			}
			return this;
		}

		public T get(String s, int i) {
			if (i == s.length()) {
				return emptyPtr;
			}
			HATTrieNode<T> c = children[s.charAt(i)];
			if (c == null) {
				return null;
			}
			return c.get(s, i+1);
		}
		
		private static final class AccessNodeIterator<T> implements Iterator<MapEntry<String, T>> {
			private final HATTrieNode children[];
			private final T emptyPtr;
			private int index = -1;
			private final String prefix;
			Iterator<MapEntry<String, T>> current = null;
			
			AccessNodeIterator(AccessNode<T> node, String prefix) {
				children = node.children;
				emptyPtr = node.emptyPtr;
				this.prefix = prefix;
				moveCurIfNeeded();
			}
			
			private void moveCurIfNeeded() {
				if (index == -1){
					if (emptyPtr == null) {
						index = 0;
					} else {
						return;
					}
				}
				
				if (current != null && current.hasNext()) return;
				while (index < children.length && children[index] == null) {index += 1;};
				if (index == children.length) { current = null;}
				else {
					String prefix = new StringBuilder(this.prefix).append((char) index).toString();
					current = children[index++].nodeIt(prefix);
				}
				
			}
			
			public boolean hasNext() {
				if (index == -1 && emptyPtr != null)  {
					return true;
				}
				while (current != null && !current.hasNext()) {
					moveCurIfNeeded();
				}
				return current != null && current.hasNext(); 
			}
			
			@Override
			public MapEntry<String, T> next() {
				if (index == -1 && emptyPtr != null)  {
					index = 0;
					moveCurIfNeeded();
					return new MapEntry<String,T>(prefix, emptyPtr);
				}
				return current.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();			
			}
			
		}

		@Override
		public Iterator<Map.Entry<String, T>> nodeIt(String prefix) {
			return new AccessNodeIterator(this,prefix);
		}
		
	}

	private static final class ContainerNode<T> implements HATTrieNode<T>,ToStringWithPrefix {
		private PersistentTreeMap<String, T> strings;
		
		public ContainerNode(PersistentTreeMap<String, T> strings) {
			this.strings = strings;
		}
		
		public String toString() {
			return strings.toString();
		}

		public HATTrieNode<T> add(String s, int i, T t) {
			String ss = s.substring(i);
			T et = strings.get(ss);
			if (Util.equals(et, t)) {
				return this;
			}
		    if (shouldBurst()) {
			   return burst(s,i,t);
		    }
			return new ContainerNode<T>(this.strings.assoc(s.substring(i),t));
		}

		public T get(String s, int i) {
			return strings.get(s.substring(i));
		}
		
		private HATTrieNode burst(String s, int i, T t) {
			HATTrieNode[] children = new HATTrieNode[256];
			T empty = s.length() == i ? t : null;
			for (Iterator<Map.Entry<String, T>> iterator = strings.iterator(); iterator.hasNext();) {
				Entry<String, T> next = iterator.next();
				String old = next.getKey();
				T value = next.getValue();
				if (empty == null && "".equals(old)) {//can only happen once
					empty = value;
				} else {
					char f = old.charAt(0);
					children[f] = addToNode(children[f],old,value,1);
				}
			}
			if (empty != t) {//i < s.length()
				char f = s.charAt(i);
				children[f] = addToNode(children[f],s,t,i+1);
			}
			return new AccessNode(children, empty);			
		}

		private static final <T> HATTrieNode addToNode(HATTrieNode hatTrieNode, String s, T v, int i) {
			if (hatTrieNode == null) {
				return new ContainerNode(PersistentTreeMap.EMPTY.assoc(s.substring(i),v));
			} else {
				return hatTrieNode.add(s, i,v);
			}
			
		}

		private boolean shouldBurst() {
			return strings.count() == 4;
		}

		@Override
		public Iterator<Map.Entry<String, T>> nodeIt(final String prefix) {
			return new Iterator<Map.Entry<String, T>>() {
				Iterator<Map.Entry<String, T>> it  = strings.iterator();
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public Map.Entry<String, T> next() {
					Entry<String, T> next = it.next();
					return new MapEntry(prefix+next.getKey(), next.getValue());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public String toStringWithPrefix(String prefix) {
			return prefix+toString();
			
		}

	}

	@Override
	public T getMember(String s) {
		if (root == null || s == null) return null;
		return root.get(s,0);
	}

	@Override
	public IPersistentTrie<T> addMember(String s, T t) {
		if (root == null) {
			return new PersistentHATTrie(new ContainerNode(PersistentTreeMap.EMPTY.assoc(s, t)),null,1);
		}
		HATTrieNode<T> newRoot = root.add(s, 0,t);
		if (root == newRoot) {
			return this;
		}
		return new PersistentHATTrie(newRoot,meta,count+1);
	}

	@Override
	public IPersistentSet disjoin(Object key) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object key) {
		return (key instanceof String) && getMember((String) key) != null;
	}

	@Override
	public Boolean get(Object key) {
		return contains(key);
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public IPersistentCollection cons(Object o) {
		if (!(o instanceof Map.Entry)) {
			throw new IllegalArgumentException("Only adding strings is supported");
		}
		Map.Entry<String, T> e = (Entry<String, T>) o;
		return (IPersistentCollection) this.addMember(e.getKey(),e.getValue());
	}

	@Override
	public IPersistentCollection empty() {
		return EMPTY;
	}

	@Override
	public Iterator<Map.Entry<String, T>> iterator() {
		return root != null ? root.nodeIt("") : new EmptyIterator(); 
	}
	
	@Override
	public ISeq<String> seq() {
		throw new UnsupportedOperationException();
	}
		
		
		

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObj withMeta(IPersistentMap meta) {
		return new PersistentHATTrie(root,meta,count);
	}
	
	@Override
	public String toString() {
		if (root == null) {return "{}";}
		return root.toString();
	}


	@Override
	public boolean add(Entry<String, T> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Entry<String, T>> c) {
		throw new UnsupportedOperationException();
	}

}