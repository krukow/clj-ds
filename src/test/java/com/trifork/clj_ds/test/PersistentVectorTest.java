/**
 * 
 */
package com.trifork.clj_ds.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import com.trifork.clj_ds.AFn;
import com.trifork.clj_ds.IFn;
import com.trifork.clj_ds.IPersistentVector;
import com.trifork.clj_ds.ISeq;
import com.trifork.clj_ds.PersistentVector;
import com.trifork.clj_ds.Util;

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
			public Object invoke(Object arg1) throws Exception {
				Integer s = (Integer )arg1;
				return s.intValue()*2;
			}
		}, vec);
		assertEquals(vec.length(), vector.length());
		for (int i = 0; i < N; i++) {
			assertEquals(i*2, vector.nth(i));
			
		}
	}
	
	static final int NUM_ITERS = 5;
	/**
	 *  * NB: this methods takes a long time to run. Be patient.
	 */
	
	public static void main(String[] args) {
		AFn mapfn = new AFn() {
			@Override
			public Object invoke(Object arg1) throws Exception {
				Integer s = (Integer )arg1;
				double pow = Math.pow(s,2);
				long round = Math.round(pow);
				Thread.sleep(1);
				return ((Long) round).toString();
			}
		};
		//IPersistentVector vec2 = mapArray();
		PersistentVector<Integer> vec = PersistentVector.emptyVector();

		int N = 10000;
		//Checking all states up to: N
		for (int i = 0; i < N; i++) {
			vec = vec.cons(i);
		}
		//System.out.println("YAY:"+vec2.length());
		IPersistentVector mapped;
		int count =0;
		while (count < NUM_ITERS) {
			long start = System.nanoTime();
			mapped = doSerialMap(mapfn,vec);
			long end = System.nanoTime();
			System.out.println("Serial TIME ( "+count+") "+mapped.length()+": "+(end-start)/1000);
			count++;
			mapped = null;
			System.gc();
		}
		mapped = null;
		System.gc();
		count =0;
		while (count < NUM_ITERS) {
			long start = System.nanoTime();
			mapped = doPMap(mapfn,vec);
			long end = System.nanoTime();
			System.out.println("Parallel TIME ( "+count+") "+mapped.length()+": "+(end-start)/1000);
			count++;
			mapped = null;
			System.gc();
		}

	}

	private static IPersistentVector doPMap(AFn mapfn,
			PersistentVector<Integer> vec) {
		return PersistentVector.pvectormap(mapfn, vec);
	}

	private static IPersistentVector doSerialMap(AFn mapfn,PersistentVector vec) {
		return PersistentVector.vectormap(mapfn, (PersistentVector)Util.ret1(vec, vec=null));
	}

	private static IPersistentVector mapArray(PersistentVector vec) {
		/*PersistentVector<Integer> vec = PersistentVector.emptyVector();
		int N = 3*32*32*32*32+33;
		//Checking all states up to: N
		for (int i = 0; i < N; i++) {
			vec = vec.cons(i);
		}*/
		
		/*PersistentVector<Integer> vec2 = PersistentVector.emptyVector();
		int N2 = 3*32*32*32*32+33;
		//Checking all states up to: N
		for (int i = 0; i < N2; i++) {
			vec2 = vec2.cons(i);
		}
		*/
		return null;
		
	}
	
	
	

}
