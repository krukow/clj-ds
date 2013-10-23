/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Apr 19, 2006 */

package com.github.krukow.clj_lang;

public class Reflector {

	public static Object prepRet(Class c, Object x){
		if (!(c.isPrimitive() || c == Boolean.class))
			return x;
		if(x instanceof Boolean)
			return ((Boolean) x)?Boolean.TRUE:Boolean.FALSE;
//		else if(x instanceof Integer)
//			{
//			return ((Integer)x).longValue();
//			}
//		else if(x instanceof Float)
//				return Double.valueOf(((Float) x).doubleValue());
		return x;
	}
}
