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

public abstract class ATransientSet<T> extends AFn implements ITransientSet<T>{
	ITransientMap impl;

	ATransientSet(ITransientMap impl) {
		this.impl = impl;
	}
	
	public int count() {
		return impl.count();
	}

	public ITransientSet<T> conj(T val) {
		ITransientMap m = impl.assoc(val, val);
		if (m != impl) this.impl = m;
		return this;
	}

	public boolean contains(T key) {
		return this != impl.valAt(key, this);
	}

	public ITransientSet<T> disjoin(T key) throws Exception {
		ITransientMap m = impl.without(key);
		if (m != impl) this.impl = m;
		return this;
	}

	public Boolean get(T key) {
		return (Boolean) impl.valAt(key);
	}

	public Object invoke(Object key, Object notFound) throws Exception {
		return impl.valAt(key, notFound);
	}

	public Object invoke(Object key) throws Exception {
		return impl.valAt(key);	
	}
	
}
