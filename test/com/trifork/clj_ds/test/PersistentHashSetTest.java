/**
 * 
 */
package com.trifork.clj_ds.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import com.trifork.clj_ds.IPersistentCollection;
import com.trifork.clj_ds.PersistentHashSet;
import com.trifork.clj_ds.PersistentVector;
import com.trifork.clj_ds.RT;

/**
 * @author krukow
 *
 */
public class PersistentHashSetTest {


	@Test
	public final void testEmptyVector() {
		PersistentHashSet<Integer> vecI = PersistentHashSet.emptySet();
		assertEquals(0, vecI.size());
		PersistentHashSet<String> vecS = PersistentHashSet.emptySet();
		assertEquals(0, vecS.size());
		assert(vecI == (PersistentHashSet) vecS);
	}

	/**
	 *  * NB: this methods takes a long time to run. Be patient.
	 */
	@Test
	public final void testIterator() {
		PersistentHashSet<Integer> dsSet = PersistentHashSet.emptySet();
		HashSet<Integer> hs = null;
		for (int i = 0; i < 20000; i++) {
			hs = new HashSet<Integer>();
			for (Integer o : dsSet) {
				hs.add(o);
			}
			assertEquals(i,hs.size());
			Integer o = new Integer(i);
			dsSet = (PersistentHashSet<Integer>) dsSet.cons(o);

		}
		
	}

}
