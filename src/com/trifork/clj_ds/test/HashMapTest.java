package com.trifork.clj_ds.test;

import java.util.HashSet;
import java.util.Map;

import com.trifork.clj_ds.PersistentHashMap;

public class HashMapTest {
	public static void main(String[] args) {
		checkMap();
	}

	public static void checkMap() {
		System.out.println("Checking... ");

		PersistentHashMap<Integer, Integer> dsMap = PersistentHashMap.emptyMap();
		System.out.println("Creating clj-ds map...");
		HashSet<Integer> hs = null;
		for (int i = 0; i < 20000; i++) {
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

}
