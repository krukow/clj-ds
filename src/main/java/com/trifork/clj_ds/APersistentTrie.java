/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 3, 2008 */

package com.trifork.clj_ds;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "rawtypes", "serial" })
public abstract class APersistentTrie<T> extends AFn implements IPersistentTrie<T>, 
									IPersistentSet, Collection<Map.Entry<String, T>>, Set<Map.Entry<String, T>>, Serializable {
int _hash = -1;

public Object invoke(Object arg1) throws Exception{
	return get((String) arg1);
}

public boolean equals(Object obj){
	if(this == obj) return true;
	if(!(obj instanceof Set))
		return false;
	Set m = (Set) obj;

	if(m.size() != count() || m.hashCode() != hashCode())
		return false;

	for(Object aM : m)
		{
		if(!contains(aM))
			return false;
		}
//	for(ISeq s = seq(); s != null; s = s.rest())
//		{
//		if(!m.contains(s.first()))
//			return false;
//		}

	return true;
}

public boolean equiv(Object o){
	return equals(o);
}

public int hashCode(){
	if(_hash == -1)
		{
		int hash = 0;
		for(ISeq s = seq(); s != null; s = s.next())
			{
			Object e = s.first();
			hash +=  Util.hash(e);
			}
		this._hash = hash;
		}
	return _hash;
}

public Object[] toArray(){
	return RT.seqToArray(seq());
}

public Object[] toArray(Object[] a){
	if(a.length >= count())
		{
		ISeq s = seq();
		for(int i = 0; s != null; ++i, s = s.next())
			{
			a[i] = s.first();
			}
		if(a.length > count())
			a[count()] = null;
		return a;
		}
	else
		return toArray();
}

public int size(){
	return count();
}

public boolean isEmpty(){
	return count() == 0;
}


}
