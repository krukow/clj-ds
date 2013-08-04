package com.trifork.clj_ds;

import java.util.List;

import com.trifork.clj_lang.IEditableCollection;
import com.trifork.clj_lang.IPersistentVector;

public interface PersistentVector<E> extends IPersistentVector<E>, PersistentStack<E>, List<E>, Comparable<E>, IEditableCollection<E> {
	
	PersistentVector<E> cons(E o);

	PersistentVector<E> empty();

	PersistentVector<E> pop();
	
	PersistentVector<E> assocN(int i, E val);
	
	PersistentVector<E> consAll(Iterable<? extends E> others);
	
	TransientVector<E> asTransient();
	
}
