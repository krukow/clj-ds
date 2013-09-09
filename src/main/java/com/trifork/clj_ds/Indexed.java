package com.trifork.clj_ds;

public interface Indexed<E> {
	
	E nth(int i);

	E nth(int i, E notFound);

}
