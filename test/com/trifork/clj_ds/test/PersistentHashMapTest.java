/**
 * 
 */
package com.trifork.clj_ds.test;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

import com.trifork.clj_ds.IMapEntry;
import com.trifork.clj_ds.IPersistentList;
import com.trifork.clj_ds.ISeq;
import com.trifork.clj_ds.MapEntry;
import com.trifork.clj_ds.PersistentHashMap;
import com.trifork.clj_ds.PersistentList;
import com.trifork.clj_ds.PersistentVector;
import com.trifork.clj_ds.RT;

/**
 * @author krukow
 *
 */
public class PersistentHashMapTest {


	/**
	 * Test method for {@link com.trifork.clj_ds.PersistentHashMap#emptyMap()}.
	 */
	@Test
	public final void testEmptyMap() {
		PersistentHashMap<String, Integer> genMap = PersistentHashMap.emptyMap();
		assertEquals(0, genMap.count());
		PersistentHashMap<Number, Boolean> genMap2 = PersistentHashMap.emptyMap();
		assertEquals(0, genMap2.count());
		assert(genMap == (PersistentHashMap) genMap2);
	}

	/**
	 * Test method for {@link com.trifork.clj_ds.PersistentHashMap#create(java.util.Map)}.
	 */
	@Test
	public final void testCreateMapOfQextendsKQextendsV() {
		Map<String, Integer> input = new TreeMap<String, Integer>();
		int N = 10;
		for (int i=0;i<N;i++) {
			input.put(String.valueOf(('A'+i)), i);
		}
		PersistentHashMap<String,Integer> output = PersistentHashMap.create(input);
		
		for (int i=0;i<N;i++) {
			assertEquals(i,  (int) output.get(String.valueOf(('A'+i))));
		}
		assertEquals(N, output.count());
		
		input = Collections.EMPTY_MAP;
		output = PersistentHashMap.create(input);
		assertEquals(0, output.count());
		
	}

	/**
	 * Test method for {@link com.trifork.clj_ds.PersistentHashMap#create(java.lang.Object[])}.
	 */
	@Test
	public final void testCreateObjectArray() {
		PersistentHashMap<Integer, Boolean> ib = PersistentHashMap.create(1,false,2,true,3,false);
		assertEquals(false, ib.get(1));
		assertEquals(true, ib.get(2));
		assertEquals(false, ib.get(3));
		
	}

	/**
	 * Test method for {@link com.trifork.clj_ds.PersistentHashMap#create(java.lang.Object[])}.
	 */
	@Test(expected=ClassCastException.class)
	public final void testBadInvocCreateObjectArray() {
		PersistentHashMap<Integer, Boolean> bad = PersistentHashMap.create(1,false,2,"true",3,false);
		Boolean b = bad.get(2);
	}

	/**
	 * NB: this methods takes a long time to run. Be patient.
	 * Test method for {@link com.trifork.clj_ds.PersistentHashMap#iterator()}.
	 */
	@Test
	public final void testIterator() {
		PersistentHashMap<Integer, Integer> dsMap = PersistentHashMap.emptyMap();
		HashSet<Integer> hs = null;
		for (int i = 0; i < 33000; i++) {
			hs = new HashSet<Integer>();
			for (Map.Entry<Integer, Integer> o : dsMap) {
				hs.add(o.getKey());
				assertEquals(o.getKey(), o.getValue());
			}
			assertEquals(i, hs.size());
			Integer o = new Integer(i);
			dsMap = (PersistentHashMap<Integer, Integer>) dsMap.assoc(o, o);
		}
		
	}

}
