package com.trifork.clj_ds;

import java.util.List;

public interface PersistentVector<E> extends PersistentStack<E>, List<E>, Indexed<E>, Comparable<E>, EditableCollection<E> {
	
	PersistentVector<E> zero();

	PersistentVector<E> plus(E o);
	
	PersistentVector<E> plusN(int i, E val);

	PersistentVector<E> minus();
	
	TransientVector<E> asTransient();
	
}
