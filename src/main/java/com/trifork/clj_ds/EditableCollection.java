package com.trifork.clj_ds;


public interface EditableCollection<E> {

	TransientCollection<E> asTransient();
	
}
