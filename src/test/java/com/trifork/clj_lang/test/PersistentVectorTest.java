/**
 * 
 */
package com.trifork.clj_lang.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import com.trifork.clj_lang.AFn;
import com.trifork.clj_lang.IFn;
import com.trifork.clj_lang.IPersistentVector;
import com.trifork.clj_lang.ISeq;
import com.trifork.clj_lang.PersistentVector;
import com.trifork.clj_lang.Util;

/**
 * @author krukow
 *
 */
public class PersistentVectorTest {

	@Test
	public final void testEmptyVector() {
		PersistentVector<Integer> vecI = PersistentVector.emptyVector();
		assertEquals(0, vecI.size());
		PersistentVector<String> vecS = PersistentVector.emptyVector();
		assertEquals(0, vecS.size());
		assert(vecI == (PersistentVector) vecS);
	}

	/**
	 *  * NB: this methods takes a long time to run. Be patient.
	 */
	@Test
	public final void testIterator() {
		PersistentVector<Integer> vec = PersistentVector.emptyVector();
		HashSet<Integer> hs = null;
		int N = 32*32*32+33;
		//Checking all states up to: N
		for (int i = 0; i < N; i++) {
			hs = new HashSet<Integer>();
			int expected = 0;
			for (Integer o : vec) {
				assert(expected == o);
				expected += 1;
				hs.add(o);
			}
			assertEquals(i,hs.size());
			Integer o = new Integer(i);
			vec = vec.cons(o);
		}	
	}
	

	/**
	 *  * NB: this methods takes a long time to run. Be patient.
	 */
	@Test
	public final void testVectorMap() {
		PersistentVector<Integer> vec = PersistentVector.emptyVector();
		HashSet<Integer> hs = null;
		int N = 32*32*32+33;
		//Checking all states up to: N
		for (int i = 0; i < N; i++) {
			vec = vec.cons(i);
			
		}
		IPersistentVector vector = PersistentVector.vectormap(new AFn() {
			@Override
			public Object invoke(Object arg1) {
				Integer s = (Integer )arg1;
				return s.intValue()*2;
			}
		}, vec);
		assertEquals(vec.length(), vector.length());
		for (int i = 0; i < N; i++) {
			assertEquals(i*2, vector.nth(i));
			
		}
	}

}
