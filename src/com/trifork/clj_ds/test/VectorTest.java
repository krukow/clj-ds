package com.trifork.clj_ds.test;

import java.util.HashSet;

import com.trifork.clj_ds.PersistentVector;

public class VectorTest {
	public static void main(String[] args) {
		checkVec();
	}

	public static void checkVec() {
		System.out.println("Checking... ");

		PersistentVector<Integer> vec = PersistentVector.emptyVector();
		System.out.println("Creating clj-ds vec...");
		HashSet<Integer> hs = null;
		int N = 32*32*32+33;
		System.out.println("Checking all states up to: "+N);
		for (int i = 0; i < N; i++) {
			hs = new HashSet<Integer>();
			int expected = 0;
			for (Integer o : vec) {
				if (expected != o) {
					throw new IllegalStateException("Expected: "+expected+" got : "+o);
				}
				expected += 1;
				hs.add(o);
			}
				
			
			if (hs.size() != i) {
				throw new IllegalStateException("error at i="+i);
			}
			if (i % 10000 == 0)
				System.out.println(i);
			Integer o = new Integer(i);
			vec = vec.cons(o);
		}
		System.out.println("Checking map. Size: " + vec.count());

	}

}
