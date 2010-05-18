package com.trifork.clj_ds.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;
import com.trifork.clj_ds.PersistentHashSet;
import com.trifork.clj_ds.PersistentTreeSet;
import com.trifork.clj_ds.PersistentVector;

public class Test {
/**
 * @param args
 */
/**
 * @param args
 */
public static void main(String[] args) {
	PersistentVector<String> v = PersistentVector.create("Karl", "krukow");
	System.out.println(v.get(1));
	
	List<Integer> il = new ArrayList<Integer>();
	il.add(42);
	il.add(22);
	il.add(0);
	PersistentVector<Integer> v2 = PersistentVector.create(il);
	System.out.println(v2);
	
	
	HashMap<String,Integer> hashMap = new HashMap<String,Integer>();
	hashMap.put("Karl", 42);
	hashMap.put("Krukow", 99);
	IPersistentMap<String, Integer> phm = PersistentHashMap.create(hashMap);
	System.out.println(phm.valAt("Krukow"));
	
	System.out.println(phm);
	for (Entry<String,Integer> i: phm) {
		System.out.println(i);
	}
	
	PersistentHashSet<String> ps = PersistentHashSet.createWithCheck("2","4","0","1");
	for(String s : ps) {
		System.out.println(s);
	}
	
	PersistentTreeSet<String> ps2 = PersistentTreeSet.create(ps.seq());
	for(String s : ps2) {
		System.out.println(s);
	}
	
	
}
}
