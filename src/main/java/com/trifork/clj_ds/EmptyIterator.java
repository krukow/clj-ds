package com.trifork.clj_ds;

import java.util.Iterator;

public final class EmptyIterator implements Iterator {

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Object next() {
		throw new IllegalStateException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
