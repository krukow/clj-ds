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

public interface ITransientSet<T> extends ITransientCollection<T>, Counted{
	public ITransientSet<T> disjoin(T key) throws Exception;
	public boolean contains(T key);
	public Boolean get(T key);
}
