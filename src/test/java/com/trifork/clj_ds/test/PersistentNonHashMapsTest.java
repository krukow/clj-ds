package com.trifork.clj_ds.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.trifork.clj_ds.Cons;
import com.trifork.clj_ds.MapEntry;
import com.trifork.clj_ds.PersistentArrayMap;
import com.trifork.clj_ds.PersistentStructMap;
import com.trifork.clj_ds.PersistentStructMap.Def;

public class PersistentNonHashMapsTest {

/*

PersistentArrayMap.java
PersistentStructMap.java
PersistentTreeMap.java

*/
	@Test
	public final void testArrayMap() {
		PersistentArrayMap<String, Integer> am = PersistentArrayMap.createWithCheck(new Object[]{
				"1",1,"2",2,"3",3,"4",4,"5",5});
		List<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String,Integer>>();
		int i=1;
		for (Map.Entry<String, Integer> e:am) {
			l.add(e);
			assertEquals(i, (int) e.getValue());
			assertEquals(i+"", e.getKey());
			i += 1;
		}
		
		i=3;
		for (Iterator<Entry<String, Integer>> it = am.iteratorFrom("3");it.hasNext();) {
			System.out.println(l.get(i));
			assertEquals(l.get(i++), it.next());
		}
		assertEquals(5, i);
		
		for(Iterator<Entry<String, Integer>> rit = am.reverseIterator();rit.hasNext();) {
			assertEquals(l.get(--i), rit.next());
		}
		assertEquals(0,i);
		
	}
	
	@Test
	public final void testStructMap() {
		 Def def = PersistentStructMap.createSlotMap(
				new Cons("1", new Cons("2", new Cons("3",null))));
		 PersistentStructMap<String, Integer> sm = PersistentStructMap.construct(def, 
				 new Cons(1,new Cons(2,new Cons(3,null))));
		 
		for (int i=4;i<10000;i++) {
			sm = (PersistentStructMap<String, Integer>) sm.assoc(i+"", i);
		}
		List<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String,Integer>>();
		for (Map.Entry<String, Integer> e: sm) {l.add(e);}
		assertEquals(new MapEntry("1",1),l.get(0));
		assertEquals(new MapEntry("2",2),l.get(1));
		assertEquals(new MapEntry("3",3),l.get(2));
		
		assertEquals(9999,l.size());
		
		int i=9999;
		for (Iterator<Entry<String, Integer>> rit = sm.reverseIterator();rit.hasNext();) {
			assertEquals(l.get(--i),rit.next());
		}
		assertEquals(0,i);
		
				
	}
}
