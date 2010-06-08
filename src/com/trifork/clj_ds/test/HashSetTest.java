package com.trifork.clj_ds.test;

import java.util.HashSet;

import com.trifork.clj_ds.PersistentHashSet;

public class HashSetTest {
	public static void main(String[] args) {
		checkSet();
	}

	public static void checkSet() {
		System.out.println("Checking... ");

		PersistentHashSet<Integer> dsSet = PersistentHashSet.emptySet();
		System.out.println("Creating clj-ds map...");
		HashSet<Integer> hs = null;
		for (int i = 0; i < 20000; i++) {
			hs = new HashSet<Integer>();
			for (Integer o : dsSet) {
				hs.add(o);
			}
			if (hs.size() != i) {
				throw new IllegalStateException("error at i");
			}
			if (i % 10000 == 0)
				System.out.println(i);
			Integer o = new Integer(i);
			dsSet = (PersistentHashSet<Integer>) dsSet.cons(o);
		}
		System.out.println("Checking map. Size: " + dsSet.count());

	}

}
