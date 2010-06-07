package com.trifork.clj_ds.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;
import com.trifork.clj_ds.PersistentHashSet;
import com.trifork.clj_ds.PersistentTreeSet;
import com.trifork.clj_ds.PersistentVector;

public class Test {
	public static void main(String[] args) {
		checkVec();
	}

	public static void checkMap() {
		System.out.println("Checking... ");

		PersistentHashMap<Integer, Integer> dsMap = PersistentHashMap.EMPTY;
		System.out.println("Creating clj-ds map...");
		HashSet<Integer> hs = null;
		for (int i = 0; i < 500000; i++) {
			hs = new HashSet<Integer>();
			for (Map.Entry<Integer, Integer> o : dsMap) {
				hs.add(o.getKey());
			}
			if (hs.size() != i) {
				throw new IllegalStateException("error at i");
			}
			if (i % 10000 == 0)
				System.out.println(i);
			Integer o = new Integer(i);
			dsMap = (PersistentHashMap<Integer, Integer>) dsMap.assoc(o, o);
		}
		System.out.println("Checking map. Size: " + dsMap.count());

	}

	public static void checkVec() {
		System.out.println("Checking... ");

		PersistentVector<Integer> vec = PersistentVector.EMPTY;
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

	private static void generics() {
		PersistentVector<String> v = PersistentVector.create("Karl", "krukow");
		System.out.println(v.get(1));

		List<Integer> il = new ArrayList<Integer>();
		il.add(42);
		il.add(22);
		il.add(0);
		PersistentVector<Integer> v2 = PersistentVector.create(il);
		System.out.println(v2);

		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		hashMap.put("Karl", 42);
		hashMap.put("Krukow", 99);
		IPersistentMap<String, Integer> phm = PersistentHashMap.create(hashMap);
		System.out.println(phm.valAt("Krukow"));

		System.out.println(phm);
		for (Entry<String, Integer> i : phm) {
			System.out.println(i);
		}

		PersistentHashSet<String> ps = PersistentHashSet.createWithCheck("2",
				"4", "0", "1");
		for (String s : ps) {
			System.out.println(s);
		}

		PersistentTreeSet<String> ps2 = PersistentTreeSet.create(ps.seq());
		for (String s : ps2) {
			System.out.println(s);
		}

	}
}
