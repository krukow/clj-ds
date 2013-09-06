package com.trifork.clj_ds;

import java.util.Collection;

public interface PersistentCollection<E> extends Collection<E> {
	
	PersistentCollection<E> zero();
	
	PersistentCollection<E> plus(E val);
	
}
