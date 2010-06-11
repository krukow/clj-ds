/**
 * 
 */
package com.trifork.clj_ds.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

import com.trifork.clj_ds.IMapEntry;
import com.trifork.clj_ds.IPersistentList;
import com.trifork.clj_ds.IPersistentMap;
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

	
	@Test
	public final void testNullMap() {
		PersistentHashMap<String, Integer> genMap = PersistentHashMap.emptyMap();
		genMap = (PersistentHashMap<String, Integer>) genMap.assoc(null, 42);
		genMap = (PersistentHashMap<String, Integer>) genMap.assoc("43", 43);
		Iterator<Entry<String, Integer>> iterator = genMap.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(42, (int) genMap.get(null));
		int count=0;
		boolean nullKey = false;
		for (Map.Entry<String,Integer> e:genMap) {
			count+=1;
			if (e.getKey()==null) {
				nullKey = true;
				assertEquals(42, (int)e.getValue()); 
			} else {
				assertEquals(43, (int)e.getValue());
			}
			
		}
		 assertEquals(2, count);
		 assertTrue(nullKey);
	}

	@Test
	public final void testFailIteratorFrom() {
		int[] inp = new int[]{
				
				768,290,483,
				869,
				263,
				42,
				878,
				818,
				439,
				152,
				984,
				217,
				761,
				218,
				250,
				219,
				381,
				829,
				574,
				639
		};
		IPersistentMap<Integer, Integer> genMap = PersistentHashMap.emptyMap();
		for (int i=0;i<inp.length;i++) {
			genMap = genMap.assoc(inp[i], inp[i]);
		}
		
		PersistentHashMap<Integer, Integer> hm = (PersistentHashMap<Integer, Integer>) genMap;
		int index = 10;
		int count = 0;
		for (Iterator<Map.Entry<Integer, Integer>> iterator = hm.iteratorFrom(inp[index]); iterator.hasNext();) {
			Entry<Integer, Integer> next = iterator.next();
			assertEquals(inp[index],(int) next.getKey());
			index++;
			count++;
		}
		assertEquals(10, count);
		
	}
	
	@Test
	public final void testIteratorFrom() {
		final int N = 20;
		IPersistentMap<Integer, Integer> genMap = PersistentHashMap.emptyMap();
		for (int i=0;i<N;i++) {
			Integer random = (int) Math.ceil(1000*Math.random());
			while (genMap.containsKey(random)) {
				random = (int) Math.ceil(1000*Math.random());
			}
			genMap = genMap.assoc(random, random);
			
		}
		
		List<Integer> l = new ArrayList<Integer>(20);
		for (Map.Entry<Integer, Integer> e: genMap) {
			l.add(e.getKey());
		}
		
		assertEquals(20, l.size());
		
		PersistentHashMap<Integer, Integer> hm = (PersistentHashMap<Integer, Integer>) genMap;
		
		int index = 10;
		int count = 0;
		for (Iterator<Map.Entry<Integer, Integer>> iterator = hm.iteratorFrom(l.get(index)); iterator.hasNext();) {
			Entry<Integer, Integer> next = iterator.next();
			assertEquals(l.get(index), next.getKey());
			index++;
			count++;
		}
		assertEquals(10, count);
		
		
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

	@Test
	public final void testRandomIterator() {
		final int N = 33000;
		IPersistentMap<Double, Double> genMap = PersistentHashMap.emptyMap();
		for (int i=0;i<N;i++) {
			double random = Math.random();
			genMap = genMap.assoc(random, random);
			
		}
		HashSet<Double> hs = new HashSet<Double>();
		for (Map.Entry<Double, Double> e: genMap) {
			hs.add(e.getKey());
		}
		assertEquals(N, hs.size());
		
	}

}
