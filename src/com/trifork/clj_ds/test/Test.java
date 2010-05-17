package com.trifork.clj_ds.test;

import java.util.ArrayList;
import java.util.List;

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
	
	
}
}
