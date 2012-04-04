/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 4:28:27 PM */

package com.trifork.clj_ds;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.regex.Matcher;

public class RT {

	static final public Boolean T = Boolean.TRUE;// Keyword.intern(Symbol.create(null,
													// "t"));
	static final public Boolean F = Boolean.FALSE;// Keyword.intern(Symbol.create(null,
													// "t"));
	// single instance of UTF-8 Charset, so as to avoid catching
	// UnsupportedCharsetExceptions everywhere
	static public Charset UTF8 = Charset.forName("UTF-8");

	static volatile boolean readably = true;
	static volatile boolean print_meta = true;

	static public final Object[] EMPTY_ARRAY = new Object[] {};
	static public final Comparator DEFAULT_COMPARATOR = new DefaultComparator();

	private static final class DefaultComparator implements Comparator,
			Serializable {
		public int compare(Object o1, Object o2) {
			return Util.compare(o1, o2);
		}

		private Object readResolve() throws ObjectStreamException {
			// ensures that we aren't hanging onto a new default comparator for
			// every
			// sorted set, etc., we deserialize
			return DEFAULT_COMPARATOR;
		}
	}

	// //////////// Collections support /////////////////////////////////

	static public ISeq seq(Object coll) {
		if (coll instanceof ASeq)
			return (ASeq) coll;
		else if (coll instanceof LazySeq)
			return ((LazySeq) coll).seq();
		else
			return seqFrom(coll);
	}

	static ISeq seqFrom(Object coll) {
		if (coll instanceof Seqable)
			return ((Seqable) coll).seq();
		else if (coll == null)
			return null;
		else if (coll instanceof Iterable)
			return IteratorSeq.create(((Iterable) coll).iterator());
		else if (coll.getClass().isArray())
			return ArraySeq.createFromObject(coll);
		else if (coll instanceof Map)
			return seq(((Map) coll).entrySet());
		else {
			Class c = coll.getClass();
			Class sc = c.getSuperclass();
			throw new IllegalArgumentException(
					"Don't know how to create ISeq from: " + c.getName());
		}
	}

	static public ISeq keys(Object coll) {
		return APersistentMap.KeySeq.create(seq(coll));
	}

	static public ISeq vals(Object coll) {
		return APersistentMap.ValSeq.create(seq(coll));
	}

	static public IPersistentMap meta(Object x) {
		if (x instanceof IMeta)
			return ((IMeta) x).meta();
		return null;
	}

	public static int count(Object o) {
		if (o instanceof Counted)
			return ((Counted) o).count();
		return countFrom(Util.ret1(o, o = null));
	}

	static int countFrom(Object o) {
		if (o == null)
			return 0;
		else if (o instanceof IPersistentCollection) {
			ISeq s = seq(o);
			o = null;
			int i = 0;
			for (; s != null; s = s.next()) {
				if (s instanceof Counted)
					return i + s.count();
				i++;
			}
			return i;
		} else if (o instanceof CharSequence)
			return ((CharSequence) o).length();
		else if (o instanceof Collection)
			return ((Collection) o).size();
		else if (o instanceof Map)
			return ((Map) o).size();
		else if (o.getClass().isArray())
			return Array.getLength(o);

		throw new UnsupportedOperationException(
				"count not supported on this type: "
						+ o.getClass().getSimpleName());
	}

	static public IPersistentCollection conj(IPersistentCollection coll,
			Object x) {
		if (coll == null)
			return new PersistentList(x);
		return coll.cons(x);
	}

	static public ISeq cons(Object x, Object coll) {
		// ISeq y = seq(coll);
		if (coll == null)
			return new PersistentList(x);
		else if (coll instanceof ISeq)
			return new Cons(x, (ISeq) coll);
		else
			return new Cons(x, seq(coll));
	}

	static public Object first(Object x) {
		if (x instanceof ISeq)
			return ((ISeq) x).first();
		ISeq seq = seq(x);
		if (seq == null)
			return null;
		return seq.first();
	}

	static public Object second(Object x) {
		return first(next(x));
	}

	static public Object third(Object x) {
		return first(next(next(x)));
	}

	static public Object fourth(Object x) {
		return first(next(next(next(x))));
	}

	static public ISeq next(Object x) {
		if (x instanceof ISeq)
			return ((ISeq) x).next();
		ISeq seq = seq(x);
		if (seq == null)
			return null;
		return seq.next();
	}

	static public ISeq more(Object x) {
		if (x instanceof ISeq)
			return ((ISeq) x).more();
		ISeq seq = seq(x);
		if (seq == null)
			return PersistentList.EMPTY;
		return seq.more();
	}

	// static public Seqable more(Object x){
	// Seqable ret = null;
	// if(x instanceof ISeq)
	// ret = ((ISeq) x).more();
	// else
	// {
	// ISeq seq = seq(x);
	// if(seq == null)
	// ret = PersistentList.EMPTY;
	// else
	// ret = seq.more();
	// }
	// if(ret == null)
	// ret = PersistentList.EMPTY;
	// return ret;
	// }

	static public Object peek(Object x) {
		if (x == null)
			return null;
		return ((IPersistentStack) x).peek();
	}

	static public Object pop(Object x) {
		if (x == null)
			return null;
		return ((IPersistentStack) x).pop();
	}

	static public Object get(Object coll, Object key) {
		if (coll instanceof ILookup)
			return ((ILookup) coll).valAt(key);
		return getFrom(coll, key);
	}

	static Object getFrom(Object coll, Object key) {
		if (coll == null)
			return null;
		else if (coll instanceof Map) {
			Map m = (Map) coll;
			return m.get(key);
		} else if (coll instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) coll;
			return set.get(key);
		} else if (key instanceof Number
				&& (coll instanceof String || coll.getClass().isArray())) {
			int n = ((Number) key).intValue();
			if (n >= 0 && n < count(coll))
				return nth(coll, n);
			return null;
		}

		return null;
	}

	static public Object get(Object coll, Object key, Object notFound) {
		if (coll instanceof ILookup)
			return ((ILookup) coll).valAt(key, notFound);
		return getFrom(coll, key, notFound);
	}

	static Object getFrom(Object coll, Object key, Object notFound) {
		if (coll == null)
			return notFound;
		else if (coll instanceof Map) {
			Map m = (Map) coll;
			if (m.containsKey(key))
				return m.get(key);
			return notFound;
		} else if (coll instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) coll;
			if (set.contains(key))
				return set.get(key);
			return notFound;
		} else if (key instanceof Number
				&& (coll instanceof String || coll.getClass().isArray())) {
			int n = ((Number) key).intValue();
			return n >= 0 && n < count(coll) ? nth(coll, n) : notFound;
		}
		return notFound;

	}

	static public Associative assoc(Object coll, Object key, Object val) {
		if (coll == null)
			return new PersistentArrayMap(new Object[] { key, val });
		return ((Associative) coll).assoc(key, val);
	}

	static public Object contains(Object coll, Object key) {
		if (coll == null)
			return F;
		else if (coll instanceof Associative)
			return ((Associative) coll).containsKey(key) ? T : F;
		else if (coll instanceof IPersistentSet)
			return ((IPersistentSet) coll).contains(key) ? T : F;
		else if (coll instanceof Map) {
			Map m = (Map) coll;
			return m.containsKey(key) ? T : F;
		} else if (key instanceof Number
				&& (coll instanceof String || coll.getClass().isArray())) {
			int n = ((Number) key).intValue();
			return n >= 0 && n < count(coll);
		}
		return F;
	}

	static public Object find(Object coll, Object key) {
		if (coll == null)
			return null;
		else if (coll instanceof Associative)
			return ((Associative) coll).entryAt(key);
		else {
			Map m = (Map) coll;
			if (m.containsKey(key))
				return new MapEntry(key, m.get(key));
			return null;
		}
	}


	static public Object dissoc(Object coll, Object key) throws Exception {
		if (coll == null)
			return null;
		return ((IPersistentMap) coll).without(key);
	}

	static public Object nth(Object coll, int n) {
		if (coll instanceof Indexed)
			return ((Indexed) coll).nth(n);
		return nthFrom(Util.ret1(coll, coll = null), n);
	}

	static Object nthFrom(Object coll, int n) {
		if (coll == null)
			return null;
		else if (coll instanceof CharSequence)
			return Character.valueOf(((CharSequence) coll).charAt(n));
		else if (coll.getClass().isArray())
			return Reflector.prepRet(Array.get(coll, n));
		else if (coll instanceof RandomAccess)
			return ((List) coll).get(n);
		else if (coll instanceof Matcher)
			return ((Matcher) coll).group(n);

		else if (coll instanceof Map.Entry) {
			Map.Entry e = (Map.Entry) coll;
			if (n == 0)
				return e.getKey();
			else if (n == 1)
				return e.getValue();
			throw new IndexOutOfBoundsException();
		}

		else if (coll instanceof Sequential) {
			ISeq seq = RT.seq(coll);
			coll = null;
			for (int i = 0; i <= n && seq != null; ++i, seq = seq.next()) {
				if (i == n)
					return seq.first();
			}
			throw new IndexOutOfBoundsException();
		} else
			throw new UnsupportedOperationException(
					"nth not supported on this type: "
							+ coll.getClass().getSimpleName());
	}

	static public Object nth(Object coll, int n, Object notFound) {
		if (coll instanceof Indexed) {
			Indexed v = (Indexed) coll;
			return v.nth(n, notFound);
		}
		return nthFrom(coll, n, notFound);
	}

	static Object nthFrom(Object coll, int n, Object notFound) {
		if (coll == null)
			return notFound;
		else if (n < 0)
			return notFound;

		else if (coll instanceof CharSequence) {
			CharSequence s = (CharSequence) coll;
			if (n < s.length())
				return Character.valueOf(s.charAt(n));
			return notFound;
		} else if (coll.getClass().isArray()) {
			if (n < Array.getLength(coll))
				return Reflector.prepRet(Array.get(coll, n));
			return notFound;
		} else if (coll instanceof RandomAccess) {
			List list = (List) coll;
			if (n < list.size())
				return list.get(n);
			return notFound;
		} else if (coll instanceof Matcher) {
			Matcher m = (Matcher) coll;
			if (n < m.groupCount())
				return m.group(n);
			return notFound;
		} else if (coll instanceof Map.Entry) {
			Map.Entry e = (Map.Entry) coll;
			if (n == 0)
				return e.getKey();
			else if (n == 1)
				return e.getValue();
			return notFound;
		} else if (coll instanceof Sequential) {
			ISeq seq = RT.seq(coll);
			coll = null;
			for (int i = 0; i <= n && seq != null; ++i, seq = seq.next()) {
				if (i == n)
					return seq.first();
			}
			return notFound;
		} else
			throw new UnsupportedOperationException(
					"nth not supported on this type: "
							+ coll.getClass().getSimpleName());
	}

	static public Object assocN(int n, Object val, Object coll) {
		if (coll == null)
			return null;
		else if (coll instanceof IPersistentVector)
			return ((IPersistentVector) coll).assocN(n, val);
		else if (coll instanceof Object[]) {
			// hmm... this is not persistent
			Object[] array = ((Object[]) coll);
			array[n] = val;
			return array;
		} else
			return null;
	}

	/**
	 * ********************* Boxing/casts ******************************
	 */
	static public Object box(Object x) {
		return x;
	}

	static public Character box(char x) {
		return Character.valueOf(x);
	}

	static public Object box(boolean x) {
		return x ? T : F;
	}

	static public Object box(Boolean x) {
		return x;// ? T : null;
	}

	static public Number box(byte x) {
		return x;// Num.from(x);
	}

	static public Number box(short x) {
		return x;// Num.from(x);
	}

	static public Number box(int x) {
		return x;// Num.from(x);
	}

	static public Number box(long x) {
		return x;// Num.from(x);
	}

	static public Number box(float x) {
		return x;// Num.from(x);
	}

	static public Number box(double x) {
		return x;// Num.from(x);
	}

	static public char charCast(Object x) {
		if (x instanceof Character)
			return ((Character) x).charValue();

		long n = ((Number) x).longValue();
		if (n < Character.MIN_VALUE || n > Character.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for char: "
					+ x);

		return (char) n;
	}

	static public boolean booleanCast(Object x) {
		if (x instanceof Boolean)
			return ((Boolean) x).booleanValue();
		return x != null;
	}

	static public byte byteCast(Object x) {
		long n = ((Number) x).longValue();
		if (n < Byte.MIN_VALUE || n > Byte.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for byte: "
					+ x);

		return (byte) n;
	}

	static public short shortCast(Object x) {
		long n = ((Number) x).longValue();
		if (n < Short.MIN_VALUE || n > Short.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for short: "
					+ x);

		return (short) n;
	}

	static public int intCast(Object x) {
		if (x instanceof Integer)
			return ((Integer) x).intValue();
		if (x instanceof Number)
			return intCast(((Number) x).longValue());
		return ((Character) x).charValue();
	}

	static public int intCast(char x) {
		return x;
	}

	static public int intCast(byte x) {
		return x;
	}

	static public int intCast(short x) {
		return x;
	}

	static public int intCast(int x) {
		return x;
	}

	static public int intCast(float x) {
		if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for int: "
					+ x);
		return (int) x;
	}

	static public int intCast(long x) {
		if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for int: "
					+ x);
		return (int) x;
	}

	static public int intCast(double x) {
		if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for int: "
					+ x);
		return (int) x;
	}

	static public long longCast(Object x) {
		return ((Number) x).longValue();
	}

	static public long longCast(int x) {
		return x;
	}

	static public long longCast(float x) {
		if (x < Long.MIN_VALUE || x > Long.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for long: "
					+ x);
		return (long) x;
	}

	static public long longCast(long x) {
		return x;
	}

	static public long longCast(double x) {
		if (x < Long.MIN_VALUE || x > Long.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for long: "
					+ x);
		return (long) x;
	}

	static public float floatCast(Object x) {
		if (x instanceof Float)
			return ((Float) x).floatValue();

		double n = ((Number) x).doubleValue();
		if (n < -Float.MAX_VALUE || n > Float.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for float: "
					+ x);

		return (float) n;

	}

	static public float floatCast(int x) {
		return x;
	}

	static public float floatCast(float x) {
		return x;
	}

	static public float floatCast(long x) {
		return x;
	}

	static public float floatCast(double x) {
		if (x < -Float.MAX_VALUE || x > Float.MAX_VALUE)
			throw new IllegalArgumentException("Value out of range for float: "
					+ x);

		return (float) x;
	}

	static public double doubleCast(Object x) {
		return ((Number) x).doubleValue();
	}

	static public double doubleCast(int x) {
		return x;
	}

	static public double doubleCast(float x) {
		return x;
	}

	static public double doubleCast(long x) {
		return x;
	}

	static public double doubleCast(double x) {
		return x;
	}

	static public IPersistentSet set(Object... init) {
		return PersistentHashSet.createWithCheck(init);
	}

	static public IPersistentVector vector(Object... init) {
		return LazilyPersistentVector.createOwning(init);
	}

	static public IPersistentVector subvec(IPersistentVector v, int start,
			int end) {
		if (end < start || start < 0 || end > v.count())
			throw new IndexOutOfBoundsException();
		if (start == end)
			return PersistentVector.EMPTY;
		return new APersistentVector.SubVector(null, v, start, end);
	}

	/**
	 * **************************************** list support
	 * *******************************
	 */

	static public ISeq list() {
		return null;
	}

	static public ISeq list(Object arg1) {
		return new PersistentList(arg1);
	}

	static public ISeq list(Object arg1, Object arg2) {
		return listStar(arg1, arg2, null);
	}

	static public ISeq list(Object arg1, Object arg2, Object arg3) {
		return listStar(arg1, arg2, arg3, null);
	}

	static public ISeq list(Object arg1, Object arg2, Object arg3, Object arg4) {
		return listStar(arg1, arg2, arg3, arg4, null);
	}

	static public ISeq list(Object arg1, Object arg2, Object arg3, Object arg4,
			Object arg5) {
		return listStar(arg1, arg2, arg3, arg4, arg5, null);
	}

	static public ISeq listStar(Object arg1, ISeq rest) {
		return (ISeq) cons(arg1, rest);
	}

	static public ISeq listStar(Object arg1, Object arg2, ISeq rest) {
		return (ISeq) cons(arg1, cons(arg2, rest));
	}

	static public ISeq listStar(Object arg1, Object arg2, Object arg3, ISeq rest) {
		return (ISeq) cons(arg1, cons(arg2, cons(arg3, rest)));
	}

	static public ISeq listStar(Object arg1, Object arg2, Object arg3,
			Object arg4, ISeq rest) {
		return (ISeq) cons(arg1, cons(arg2, cons(arg3, cons(arg4, rest))));
	}

	static public ISeq listStar(Object arg1, Object arg2, Object arg3,
			Object arg4, Object arg5, ISeq rest) {
		return (ISeq) cons(arg1, cons(arg2, cons(arg3, cons(arg4, cons(arg5,
				rest)))));
	}

	static public ISeq arrayToList(Object[] a) throws Exception {
		ISeq ret = null;
		for (int i = a.length - 1; i >= 0; --i)
			ret = (ISeq) cons(a[i], ret);
		return ret;
	}

	static public Object[] object_array(Object sizeOrSeq) {
		if (sizeOrSeq instanceof Number)
			return new Object[((Number) sizeOrSeq).intValue()];
		else {
			ISeq s = RT.seq(sizeOrSeq);
			int size = RT.count(s);
			Object[] ret = new Object[size];
			for (int i = 0; i < size && s != null; i++, s = s.next())
				ret[i] = s.first();
			return ret;
		}
	}

	static public Object[] toArray(Object coll) throws Exception {
		if (coll == null)
			return EMPTY_ARRAY;
		else if (coll instanceof Object[])
			return (Object[]) coll;
		else if (coll instanceof Collection)
			return ((Collection) coll).toArray();
		else if (coll instanceof Map)
			return ((Map) coll).entrySet().toArray();
		else if (coll instanceof String) {
			char[] chars = ((String) coll).toCharArray();
			Object[] ret = new Object[chars.length];
			for (int i = 0; i < chars.length; i++)
				ret[i] = chars[i];
			return ret;
		} else if (coll.getClass().isArray()) {
			ISeq s = (seq(coll));
			Object[] ret = new Object[count(s)];
			for (int i = 0; i < ret.length; i++, s = s.next())
				ret[i] = s.first();
			return ret;
		} else
			throw new Exception("Unable to convert: " + coll.getClass()
					+ " to Object[]");
	}

	static public Object[] seqToArray(ISeq seq) {
		int len = length(seq);
		Object[] ret = new Object[len];
		for (int i = 0; seq != null; ++i, seq = seq.next())
			ret[i] = seq.first();
		return ret;
	}

	static public Object seqToTypedArray(ISeq seq) throws Exception {
		Class type = (seq != null) ? seq.first().getClass() : Object.class;
		return seqToTypedArray(type, seq);
	}

	static public Object seqToTypedArray(Class type, ISeq seq) throws Exception {
		Object ret = Array.newInstance(type, length(seq));
		for (int i = 0; seq != null; ++i, seq = seq.next())
			Array.set(ret, i, seq.first());
		return ret;
	}

	static public int length(ISeq list) {
		int i = 0;
		for (ISeq c = list; c != null; c = c.next()) {
			i++;
		}
		return i;
	}

	static public int boundedLength(ISeq list, int limit) throws Exception {
		int i = 0;
		for (ISeq c = list; c != null && i <= limit; c = c.next()) {
			i++;
		}
		return i;
	}

	// /////////////////////////////// reader support
	// ////////////////////////////////

	static Character readRet(int ret) {
		if (ret == -1)
			return null;
		return box((char) ret);
	}

	static public Character readChar(Reader r) throws Exception {
		int ret = r.read();
		return readRet(ret);
	}

	static public Character peekChar(Reader r) throws Exception {
		int ret;
		if (r instanceof PushbackReader) {
			ret = r.read();
			((PushbackReader) r).unread(ret);
		} else {
			r.mark(1);
			ret = r.read();
			r.reset();
		}

		return readRet(ret);
	}

	static public String printString(Object x) {
		try {
			StringWriter sw = new StringWriter();
			print(x, sw);
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public void print(Object x, Writer w) throws Exception {

		if (x instanceof Obj) {
			Obj o = (Obj) x;
			if (RT.count(o.meta()) > 0) {
				IPersistentMap meta = o.meta();
				w.write("#^");
				print(meta, w);
				w.write(' ');
			}
		}
		if (x == null)
			w.write("null");
		else if (x instanceof ISeq || x instanceof IPersistentList) {
			w.write('(');
			printInnerSeq(seq(x), w);
			w.write(')');
		} else if (x instanceof String) {
			String s = (String) x;
			if (!readably)
				w.write(s);
			else {
				w.write('"');
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					switch (c) {
					case '\n':
						w.write("\\n");
						break;
					case '\t':
						w.write("\\t");
						break;
					case '\r':
						w.write("\\r");
						break;
					case '"':
						w.write("\\\"");
						break;
					case '\\':
						w.write("\\\\");
						break;
					case '\f':
						w.write("\\f");
						break;
					case '\b':
						w.write("\\b");
						break;
					default:
						w.write(c);
					}
				}
				w.write('"');
			}
		} else if (x instanceof IPersistentMap) {
			w.write('{');
			for (ISeq s = seq(x); s != null; s = s.next()) {
				IMapEntry e = (IMapEntry) s.first();
				print(e.key(), w);
				w.write(' ');
				print(e.val(), w);
				if (s.next() != null)
					w.write(", ");
			}
			w.write('}');
		} else if (x instanceof IPersistentVector) {
			IPersistentVector a = (IPersistentVector) x;
			w.write('[');
			for (int i = 0; i < a.count(); i++) {
				print(a.nth(i), w);
				if (i < a.count() - 1)
					w.write(' ');
			}
			w.write(']');
		} else if (x instanceof IPersistentSet) {
			w.write("#{");
			for (ISeq s = seq(x); s != null; s = s.next()) {
				print(s.first(), w);
				if (s.next() != null)
					w.write(" ");
			}
			w.write('}');
		} else if (x instanceof Character) {
			char c = ((Character) x).charValue();
			if (!readably)
				w.write(c);
			else {
				w.write('\\');
				switch (c) {
				case '\n':
					w.write("newline");
					break;
				case '\t':
					w.write("tab");
					break;
				case ' ':
					w.write("space");
					break;
				case '\b':
					w.write("backspace");
					break;
				case '\f':
					w.write("formfeed");
					break;
				case '\r':
					w.write("return");
					break;
				default:
					w.write(c);
				}
			}
		} else if (x instanceof Class) {
			w.write("#=");
			w.write(((Class) x).getName());
		} else if (x instanceof BigDecimal && readably) {
			w.write(x.toString());
			w.write('M');
		} else
			w.write(x.toString());
	}

	// */

	private static void printInnerSeq(ISeq x, Writer w) throws Exception {
		for (ISeq s = x; s != null; s = s.next()) {
			print(s.first(), w);
			if (s.next() != null)
				w.write(' ');
		}
	}

	static public void formatAesthetic(Writer w, Object obj) throws IOException {
		if (obj == null)
			w.write("null");
		else
			w.write(obj.toString());
	}

	static public void formatStandard(Writer w, Object obj) throws IOException {
		if (obj == null)
			w.write("null");
		else if (obj instanceof String) {
			w.write('"');
			w.write((String) obj);
			w.write('"');
		} else if (obj instanceof Character) {
			w.write('\\');
			char c = ((Character) obj).charValue();
			switch (c) {
			case '\n':
				w.write("newline");
				break;
			case '\t':
				w.write("tab");
				break;
			case ' ':
				w.write("space");
				break;
			case '\b':
				w.write("backspace");
				break;
			case '\f':
				w.write("formfeed");
				break;
			default:
				w.write(c);
			}
		} else
			w.write(obj.toString());
	}

	static public Object format(Object o, String s, Object... args)
			throws Exception {
		Writer w;
		if (o == null)
			w = new StringWriter();
		else if (Util.equals(o, T))
			w = (Writer) new OutputStreamWriter(System.out);
		else
			w = (Writer) o;
		doFormat(w, s, ArraySeq.create(args));
		if (o == null)
			return w.toString();
		return null;
	}

	static public ISeq doFormat(Writer w, String s, ISeq args) throws Exception {
		for (int i = 0; i < s.length();) {
			char c = s.charAt(i++);
			switch (Character.toLowerCase(c)) {
			case '~':
				char d = s.charAt(i++);
				switch (Character.toLowerCase(d)) {
				case '%':
					w.write('\n');
					break;
				case 't':
					w.write('\t');
					break;
				case 'a':
					if (args == null)
						throw new IllegalArgumentException("Missing argument");
					RT.formatAesthetic(w, RT.first(args));
					args = RT.next(args);
					break;
				case 's':
					if (args == null)
						throw new IllegalArgumentException("Missing argument");
					RT.formatStandard(w, RT.first(args));
					args = RT.next(args);
					break;
				case '{':
					int j = s.indexOf("~}", i); // note - does not nest
					if (j == -1)
						throw new IllegalArgumentException("Missing ~}");
					String subs = s.substring(i, j);
					for (ISeq sargs = RT.seq(RT.first(args)); sargs != null;)
						sargs = doFormat(w, subs, sargs);
					args = RT.next(args);
					i = j + 2; // skip ~}
					break;
				case '^':
					if (args == null)
						return null;
					break;
				case '~':
					w.write('~');
					break;
				default:
					throw new IllegalArgumentException(
							"Unsupported ~ directive: " + d);
				}
				break;
			default:
				w.write(c);
			}
		}
		return args;
	}

	// /////////////////////////////// values //////////////////////////

	static public Object[] setValues(Object... vals) {
		// ThreadLocalData.setValues(vals);
		if (vals.length > 0)
			return vals;// [0];
		return null;
	}

	static public float aget(float[] xs, int i) {
		return xs[i];
	}

	static public float aset(float[] xs, int i, float v) {
		xs[i] = v;
		return v;
	}

	static public int alength(float[] xs) {
		return xs.length;
	}

	static public float[] aclone(float[] xs) {
		return xs.clone();
	}

	static public double aget(double[] xs, int i) {
		return xs[i];
	}

	static public double aset(double[] xs, int i, double v) {
		xs[i] = v;
		return v;
	}

	static public int alength(double[] xs) {
		return xs.length;
	}

	static public double[] aclone(double[] xs) {
		return xs.clone();
	}

	static public int aget(int[] xs, int i) {
		return xs[i];
	}

	static public int aset(int[] xs, int i, int v) {
		xs[i] = v;
		return v;
	}

	static public int alength(int[] xs) {
		return xs.length;
	}

	static public int[] aclone(int[] xs) {
		return xs.clone();
	}

	static public long aget(long[] xs, int i) {
		return xs[i];
	}

	static public long aset(long[] xs, int i, long v) {
		xs[i] = v;
		return v;
	}

	static public int alength(long[] xs) {
		return xs.length;
	}

	static public long[] aclone(long[] xs) {
		return xs.clone();
	}

	static public char aget(char[] xs, int i) {
		return xs[i];
	}

	static public char aset(char[] xs, int i, char v) {
		xs[i] = v;
		return v;
	}

	static public int alength(char[] xs) {
		return xs.length;
	}

	static public char[] aclone(char[] xs) {
		return xs.clone();
	}

	static public byte aget(byte[] xs, int i) {
		return xs[i];
	}

	static public byte aset(byte[] xs, int i, byte v) {
		xs[i] = v;
		return v;
	}

	static public int alength(byte[] xs) {
		return xs.length;
	}

	static public byte[] aclone(byte[] xs) {
		return xs.clone();
	}

	static public short aget(short[] xs, int i) {
		return xs[i];
	}

	static public short aset(short[] xs, int i, short v) {
		xs[i] = v;
		return v;
	}

	static public int alength(short[] xs) {
		return xs.length;
	}

	static public short[] aclone(short[] xs) {
		return xs.clone();
	}

	static public boolean aget(boolean[] xs, int i) {
		return xs[i];
	}

	static public boolean aset(boolean[] xs, int i, boolean v) {
		xs[i] = v;
		return v;
	}

	static public int alength(boolean[] xs) {
		return xs.length;
	}

	static public boolean[] aclone(boolean[] xs) {
		return xs.clone();
	}

	static public Object aget(Object[] xs, int i) {
		return xs[i];
	}

	static public Object aset(Object[] xs, int i, Object v) {
		xs[i] = v;
		return v;
	}

	static public int alength(Object[] xs) {
		return xs.length;
	}

	static public Object[] aclone(Object[] xs) {
		return xs.clone();
	}

}
