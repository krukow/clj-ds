package com.trifork.clj_ds.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	PersistentHashSetTest.class,
	PersistentVectorTest.class, 
	PersistentHashMapTest.class })
public class AllTests {
	
	public static void main(String[] args) {
		String x = "4";
		String y = "2";
		System.out.println(x + y =="42");
	}
}
