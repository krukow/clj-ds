/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jun 19, 2007 */

package com.trifork.clj_ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SeqIterator<T> implements Iterator<T>{

ISeq<T> seq;

public SeqIterator(ISeq<T> seq){
	this.seq = seq;
}


public boolean hasNext(){
	return seq != null;
}

public T next() throws NoSuchElementException {
	if(seq == null)
		throw new NoSuchElementException();
	T ret = (T) RT.first(seq);
	seq = RT.next(seq);
	return ret;
}

public void remove(){
throw new UnsupportedOperationException();
}
}
