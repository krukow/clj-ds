package com.trifork.clj_ds.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	PersistentHashSetTest.class,
	PersistentVectorTest.class, 
	PersistentHashMapTest.class })
public class AllTests {}
