/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 11:01:29 AM */

package com.trifork.clj_ds;

import java.io.Serializable;
import java.util.Collection;

final public class Cons<T> extends ASeq<T> implements Serializable {

private final T _first;
private final ISeq<T> _more;

public Cons(T first, ISeq<T> _more){
	this._first = first;
	this._more = _more;
}


public Cons(IPersistentMap meta, T _first, ISeq<T> _more){
	super(meta);
	this._first = _first;
	this._more = _more;
}

public T first(){
	return _first;
}

public ISeq<T> next(){
	return more().seq();
}

public ISeq<T> more(){
	if(_more == null)
		return (ISeq<T>) PersistentList.emptyList();
	return _more;
}

public int count(){
	return 1 + RT.count(_more);
}

public Cons<T> withMeta(IPersistentMap meta){
	return new Cons<T>(meta, _first, _more);
}


}
