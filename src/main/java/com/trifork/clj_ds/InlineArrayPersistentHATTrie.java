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
import java.util.Random;

/*
 A persistent rendition of Nikolas Askitis' HAT Trie
 Uses path copying for persistence
 Any errors are my own
*/
@SuppressWarnings({"rawtypes","unchecked"})
public class InlineArrayPersistentHATTrie<T> extends APersistentTrie<T> implements IObj {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6864541653381702688L;
	final IPersistentMap meta;
	final HATTrieNode<T> root;
	final int count;
	private static final int seed = new Random().nextInt();
	private static final int slotPageSize = 64;
	
	/**
	 * getHashCode() differs from the standard Java hashcode algorithm. It's an
	 * shift-add-xor class algorithm, as tested in <em>"Performance
	 * in Practise of String Hashing Functions"</em>, Jobel & Ramakrishna, 1997;
	 * and is implemented as efficiently as possible.
	 */
	public static int getHashCode(String inputString) {
		int result = InlineArrayPersistentHATTrie.seed;
		int length = inputString.length();
		for (int i = 0; i < length; i++) {
			result ^= ((result << 5) + inputString.charAt(i) + (result >>> 2));
		}
		return ((result & 0x7fffffff) & 0x1ff);
	}
	
	public static final InlineArrayPersistentHATTrie EMPTY = new InlineArrayPersistentHATTrie(null, null, 0);
	
	public InlineArrayPersistentHATTrie(HATTrieNode root, IPersistentMap meta, int count) {
		this.root = root;
		this.meta = meta;
		this.count = count;
	}

	public IPersistentMap meta() {
		return meta;
	}

	private static interface HATTrieNode<T> {
		T get(String s, int j);
		Iterator<Map.Entry<String, T>> nodeIt(String prefix);
		HATTrieNode add(RandomAccessChars s, int start, int end, T t);
	}
	private static interface ToStringWithPrefix {
		String toStringWithPrefix(String prefix);
	}
	private static interface RandomAccessChars {
		public char charAt(int index);
		public int length();
	}
	
	private static class StringRandomAccessChars implements RandomAccessChars {
		private final String s;

		public StringRandomAccessChars(String s) {
			this.s = s;
		}

		@Override
		public char charAt(int index) {
			return s.charAt(index);
		}

		@Override
		public int length() {
			return s.length();
		}
		
		
	}
	private static class CharArrayRandomAccessChars implements RandomAccessChars {
		private final char[] s;

		public CharArrayRandomAccessChars(char[] s) {
			this.s = s;
		}

		@Override
		public char charAt(int index) {
			return s[index];
		}

		@Override
		public int length() {
			return s.length;
		}
		
		
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

		public HATTrieNode<T> add(RandomAccessChars s, int start, int end, T t) {
			int length = s.length();
			if (start < length) {
				char ichar = s.charAt(start);
				HATTrieNode hatTrieNode = children[ichar];
				if (hatTrieNode != null) {
					HATTrieNode newNode = hatTrieNode.add(s, start+1, end, t);
					if (newNode == hatTrieNode) {
						return this;
					}
					HATTrieNode[] newArr = new HATTrieNode[children.length];
					System.arraycopy(children, 0, newArr, 0, children.length);
					newArr[ichar] = newNode;
					return new AccessNode(newArr, emptyPtr);
				}
				ContainerNode c = singletonContainer(s,start+1, end, t);
				HATTrieNode[] newArr = new HATTrieNode[children.length];
				System.arraycopy(children, 0, newArr, 0, children.length);
				newArr[ichar] = c;
				return new AccessNode(newArr, emptyPtr);
			}
			if (start == length && emptyPtr == null) {
				return new AccessNode(children, t); 
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
	
	private static final <T> ContainerNode<T> singletonContainer(RandomAccessChars s, int start,int end, T t) {
		char[] strings = new char[slotPageSize];
		int slen = end-start;
		int k = encodeNumber(0, slen, strings);
		for (int j=start;j<end;j++) {
			strings[k++] = s.charAt(j);
		}
		strings[k++] = 0;
		k = encodeNumber(k,0,strings);
		return new ContainerNode<T>(strings, new Object[]{t});
	}
	
	private static final int encodeNumber(int j, int inputlength, char[] tmp) {
		// Add String to end of contents array.
		   // First encode the length of the string (this allows us to
		   // skip forward rapidly in the array when string matching if we
		   // get an early negative)
		   tmp[j++] = (char)(inputlength & 0xffff);
		   tmp[j++] = (char)((inputlength >> 16) & 0xffff);
		return j;
	}

	private static final class ContainerNode<T> implements HATTrieNode<T>,ToStringWithPrefix {
		
		private final char[] contents;
		private final Object[] values;
		
		
		public ContainerNode(char[] strings, Object[] values) {
			this.contents = strings;
			this.values = values;
		}
		
		public String toString() {
			return "NOTIMPLE";
		}

		@Override
		public HATTrieNode add(RandomAccessChars s, int start, int end, T t) {
			int id = getIndex(s,start,end);
			if (Util.equals(values[id], t)) {
				return this;
			}
		    if (shouldBurst()) {
			   return burst(contents,start,end,t);
		    }
			return addString(contents,start,end,t);
		}
		
		
		public T get(String s, int i) {
			int idx = getIndex(new StringRandomAccessChars(s),i, s.length());
			if (idx != -1) {
				return (T) values[idx];
			}
			return null;
		}
		
		private HATTrieNode burst(RandomAccessChars s, int start, int end, T t) {
			HATTrieNode[] children = new HATTrieNode[256];
			int length = end-start;
			T empty = s.length() == start ? t : null;
			int j = 0;
		    length = 0;
		    int index = -1;
		    int slotlength = this.contents.length;
	
			while (j < slotlength) {
				  // if there's nothing left in the slot, jump out
				  if (this.contents[j] == 0) { break; }
				  
				  // first decode the length of the string
				  length = (int) (this.contents[j++] | (this.contents[j++] << 16));
				  if (empty == null && length == 0) {//can only happen once
					  j += 1;//skip 0
					  index = (int) (this.contents[j++] | (this.contents[j++] << 16));
					  empty = (T) this.values[index];
				  }  else {
					char f = this.contents[j++];
					children[f] = addToNode(children[f],this.contents,j,length,this.values);
				  }
		    	  j += length + 3;
			}
			if (empty != t) {//i < s.length()
				char f = s.charAt(start);
				children[f] = addToNode(children[f],s,start+1,end,t);
			}
			return new AccessNode(children, empty);			
		}
		private HATTrieNode burst(char[] s, int start, int end, T t) {
			HATTrieNode[] children = new HATTrieNode[256];
			T empty = (end == start) ? t : null;
			int j = 0;
		    int length = 0;
		    int index = -1;
		    int slotlength = this.contents.length;
	
			while (j < slotlength) {
				  // if there's nothing left in the slot, jump out
				  if (this.contents[j] == 0) { break; }
				  
				  // first decode the length of the string
				  length = (int) (this.contents[j++] | (this.contents[j++] << 16));
				  if (empty == null && length == 0) {//can only happen once
					  j += 1;//skip 0
					  index = (int) (this.contents[j++] | (this.contents[j++] << 16));
					  empty = (T) this.values[index];
				  }  else {
					char f = this.contents[j++];
					children[f] = addToNode(children[f],this.contents,j,length,this.values);
				  }
		    	  j += length + 3;
			}
			if (empty != t) {//i < s.length()
				char f = s[start];
				children[f] = addToNode(children[f],s,start+1,(end-start-1), values);
			}
			return new AccessNode(children, empty);			
		}

		private static final <T> HATTrieNode addToNode(HATTrieNode hatTrieNode, char[] contents, int j, int length, Object[] values) {
			int idIndex = j + length + 1;
			int id = (int) (contents[idIndex++] | (contents[idIndex++] << 16));;
			if (hatTrieNode == null) {
				char[] newcontents = new char[InlineArrayPersistentHATTrie.slotPageSize];
				int nextindex = encodeNumber(0, length, newcontents);
				System.arraycopy(contents, j, newcontents, nextindex++, length);
				newcontents[nextindex++] = 0;
				return new ContainerNode<T>(newcontents, new Object[]{values[id]});
			} else {
				return hatTrieNode.add(new CharArrayRandomAccessChars(contents), j,j+length, (T) values[id]);
			}
			
		}
		private static final <T> HATTrieNode addToNode(HATTrieNode hatTrieNode, RandomAccessChars s, int start, int end, T t) {
			if (hatTrieNode == null) {
				return singletonContainer(s, start,end, t);
			} else {
				return hatTrieNode.add(s, start,end,t);
			}
			
		}
		
		private ContainerNode<T> addString(String inputString, int index, T t)
		{
		   int j = 0;
		   int k = 0;
		   int length = 0;
		   int slotlength = this.contents.length;
		   int inputlength = inputString.length()-index;
		   boolean match = false;

		   while (j < slotlength) {
			      // if there's nothing left in the slot, jump out and return -1
			      if (this.contents[j] == 0) { break; }
			      match = true;

			      // first decode the length of the string
			      length = (int) (this.contents[j++] | (this.contents[j++] << 16));

			      // if the length does not match, then don't bother checking the characters
			      if (inputlength != length) {
			         match = false;
			      } else {
			         // then start checking, character by character until we hit
			         // the null terminator to see if we have a match.
			    	 int strIdx = index;
			         for (k = j; this.contents[k] != 0; k++) {
			            if (this.contents[k] != inputString.charAt(strIdx++)) {
			               // if we find we do not have a match, it'll happen
			               // earlier than finding we do, so jump right out of
			               // the loop.
			               match = false;
			               break;
			            }
			         }
			         k++;
			      }

			      // If we found the string, we don't bother to add it, we
			      // just return back.
			      if (match) return this;

			      // skip ahead to the next string in the array and do it again.
			      j += length + 3;
			   }
		   // Do we need to grow this slot in the array? If so, do so
		   // now...
		   char tmp[] = this.contents;
		   int newslotlength = slotlength;
		   while ((newslotlength - inputlength) < (j+5)) {
		      tmp = new char[newslotlength + InlineArrayPersistentHATTrie.slotPageSize];
		      System.arraycopy(this.contents, 0, tmp, 0, newslotlength);
		      newslotlength = tmp.length;
		   }

		   j = encodeNumber(j, inputlength, tmp);

		   // Now copy over the inputString characters
		   System.arraycopy(inputString, 0, tmp, j, inputlength);
		   j += inputlength;

		   
		   // null-terminate
		   tmp[j++] = 0;

		   // Now encode the id int after the null termination.
		   encodeNumber(j, values.length, tmp);
		   
		   Object[] newValues = new Object[values.length+1];
		   System.arraycopy(values, 0, newValues, 0, values.length);
		   newValues[values.length] = t;
		   return new ContainerNode<T>(tmp, values);
		}
		private ContainerNode<T> addString(char[] inputString, int start, int end, T t)
		{
		   int j = 0;
		   int k = 0;
		   int length = 0;
		   int slotlength = this.contents.length;
		   int inputlength = end-start;
		   boolean match = false;

		   while (j < slotlength) {
			      // if there's nothing left in the slot, jump out and return -1
			      if (this.contents[j] == 0) { break; }
			      match = true;

			      // first decode the length of the string
			      length = (int) (this.contents[j++] | (this.contents[j++] << 16));

			      // if the length does not match, then don't bother checking the characters
			      if (inputlength != length) {
			         match = false;
			      } else {
			         // then start checking, character by character until we hit
			         // the null terminator to see if we have a match.
			    	 int strIdx = start;
			         for (k = j; this.contents[k] != 0; k++) {
			            if (this.contents[k] != inputString[strIdx++]) {
			               // if we find we do not have a match, it'll happen
			               // earlier than finding we do, so jump right out of
			               // the loop.
			               match = false;
			               break;
			            }
			         }
			         k++;
			      }

			      // If we found the string, we don't bother to add it, we
			      // just return back.
			      if (match) return this;

			      // skip ahead to the next string in the array and do it again.
			      j += length + 3;
			   }
		   // Do we need to grow this slot in the array? If so, do so
		   // now...
		   char tmp[] = this.contents;
		   int newslotlength = slotlength;
		   while ((newslotlength - inputlength) < (j+5)) {
		      tmp = new char[newslotlength + InlineArrayPersistentHATTrie.slotPageSize];
		      System.arraycopy(this.contents, 0, tmp, 0, newslotlength);
		      newslotlength = tmp.length;
		   }

		   j = encodeNumber(j, inputlength, tmp);

		   // Now copy over the inputString characters
		   System.arraycopy(inputString, 0, tmp, j, inputlength);
		   j += inputlength;

		   
		   // null-terminate
		   tmp[j++] = 0;

		   // Now encode the id int after the null termination.
		   encodeNumber(j, values.length, tmp);
		   
		   Object[] newValues = new Object[values.length+1];
		   System.arraycopy(values, 0, newValues, 0, values.length);
		   newValues[values.length] = t;
		   return new ContainerNode<T>(tmp, values);
		}
		
		
		public int getIndex(RandomAccessChars input, int start, int end)
		{
		   int j = 0;
		   int k = 0;
		   int length = 0;
		   int inputlength = end-start;
		   int slotlength = this.contents.length;
		   boolean match = false;

		   while (j < slotlength) {
		      // if there's nothing left in the slot, jump out and return -1
		      if (this.contents[j] == 0) { break; }
		      match = true;

		      // first decode the length of the string
		      length = (int) (this.contents[j++] | (this.contents[j++] << 16));

		      // if the length does not match, then don't bother checking the characters
		      if (inputlength != length) {
		         match = false;
		      } else {
		    	  // then start checking, character by character until we hit
		         // the null terminator to see if we have a match.
		    	 int strIdx = start;
		         for (k = j; this.contents[k] != 0; k++) {
		            if (this.contents[k] != input.charAt(strIdx++)) {
		               // if we find we do not have a match, it'll happen
		               // earlier than finding we do, so jump right out of
		               // the loop.
		               match = false;
		               break;
		            }
		         }
		         k++;
		      }

		      // If we did find a match, return the id int.
		      if (match) {
		         return (int) (this.contents[k++] | (this.contents[k] << 16));
		      }

		      // skip ahead to the next string in the array and do it again.
		      j += length + 3;
		   }

		   return -1;
		}


		private boolean shouldBurst() {
			return values.length == 4;
		}

		@Override
		public Iterator<Map.Entry<String, T>> nodeIt(final String prefix) {
			return new Iterator<Map.Entry<String, T>>() {
				int j = 0;
				@Override
				public boolean hasNext() {
					   return ContainerNode.this.contents[j] == 0;
				}

				@Override
				public Map.Entry<String, T> next() {
						int length = (int) (ContainerNode.this.contents[j++] | (ContainerNode.this.contents[j++] << 16));
						char[] str = new char[length];
						int i = 0;
						while (i<length) {
							str[i++] = ContainerNode.this.contents[j++]; 
						}
						j++;//skip 0 terminating char
						String key = new String(str);
						
						int idx = (int) (ContainerNode.this.contents[j++] | (ContainerNode.this.contents[j++] << 16));
						return new MapEntry(key, ContainerNode.this.values[idx]);
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
			return new InlineArrayPersistentHATTrie(singletonContainer(new StringRandomAccessChars(s), 0,s.length(), t),null,1);
		}
		HATTrieNode<T> newRoot = root.add(new StringRandomAccessChars(s), 0,s.length(),t);
		if (root == newRoot) {
			return this;
		}
		return new InlineArrayPersistentHATTrie(newRoot,meta,count+1);
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
		return new InlineArrayPersistentHATTrie(root,meta,count);
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