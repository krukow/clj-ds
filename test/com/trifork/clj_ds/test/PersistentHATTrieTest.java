/**
 * 
 */
package com.trifork.clj_ds.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import com.trifork.clj_ds.PersistentHATTrie;

/**
 * @author krukow
 *
 */
public class PersistentHATTrieTest {


	PersistentHATTrie<Integer> createTrie() {
		PersistentHATTrie<Integer> trie = (PersistentHATTrie<Integer>) PersistentHATTrie.EMPTY.addMember("we",0);
		trie = (PersistentHATTrie<Integer>) trie.addMember("were",1);
		trie = (PersistentHATTrie<Integer>) trie.addMember("went",2);
		trie = (PersistentHATTrie<Integer>) trie.addMember("west",3);
		trie = (PersistentHATTrie<Integer>) trie.addMember("water",4);
		trie = (PersistentHATTrie<Integer>) trie.addMember("western",5);
		trie = (PersistentHATTrie<Integer>) trie.addMember("tri",6);
		trie = (PersistentHATTrie<Integer>) trie.addMember("fork",7);
		trie = (PersistentHATTrie<Integer>) trie.addMember("trifork",8);
		trie = (PersistentHATTrie<Integer>) trie.addMember("forks",9);
		return trie;
	}
	@Test
	public final void testSmallTrie() {
		PersistentHATTrie trie = createTrie();
		assertEquals(10, trie.count());
		
		assertEquals(0, trie.getMember("we"));
		assertEquals(1, trie.getMember("were"));
		assertEquals(2, trie.getMember("went"));
		assertEquals(3, trie.getMember("west"));
		assertEquals(4, trie.getMember("water"));
		assertEquals(5, trie.getMember("western"));
		assertEquals(6, trie.getMember("tri"));
		assertEquals(7, trie.getMember("fork"));
		assertEquals(8, trie.getMember("trifork"));
		assertEquals(9, trie.getMember("forks"));		
	}
	
	@Test
	public final void doesNotContainNotAdded() {
		PersistentHATTrie trie = createTrie();
		assertNull(trie.getMember("wes"));
		assertNull(trie.getMember("w"));
		assertNull(trie.getMember("wents"));
		assertNull(trie.getMember("westee"));
		assertNull(trie.getMember("tris"));
		assertNull(trie.getMember("sfork"));
		assertNull(trie.getMember("tri2fork"));
		assertNull(trie.getMember("forksa"));		
	}
	
	@Test
	public final void testIterator() {
		PersistentHATTrie<Integer> trie = createTrie();
		int count =0;
		System.out.println(trie.toString());
		for (Map.Entry<String, Integer> s:trie) {
			System.out.println(s);
			count++;
		}
		assertEquals(10,count);
	}
}
