package com.github.krukow.clj_lang;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({ "rawtypes", "serial" })
public abstract class RRBTree<T> extends APersistentVector<T> {
    /*
	static class Node implements Serializable {
		transient final AtomicReference<Thread> edit;
		final int[] ranges;
		final Object[] array;

		Node(AtomicReference<Thread> edit, Object[] array) {
			this.array = array;
			this.ranges = null;
			this.edit = edit;
		}

		Node(AtomicReference<Thread> edit, Object[] array, int[] ranges) {
			this.edit = edit;
			this.array = array;
			this.ranges = ranges;
		}
	}

	final int cnt;
	final int shift;
	final Node root;
	final Object[] tail;
	final IPersistentMap _meta;

	final static AtomicReference<Thread> NOEDIT = new AtomicReference<Thread>(null);
	final static Node EMPTY_NODE = new Node(NOEDIT, new Object[0]);

	public final static RRBTree EMPTY = new RRBTree(0, 0, EMPTY_NODE,
			new Object[32]);

	RRBTree(int cnt, int shift, Node root, Object[] tail) {
		this._meta = null;
		this.cnt = cnt;
		this.shift = shift;
		this.root = root;
		this.tail = tail;
	}

	//put node's child where i is located in box
	//return the index of 'i' relative to the found child
	private static final int findNode(Node node, int i, int level, Box box) {
		int subidx = (i >>> level) & 0x01f;
		int leftCount = 0;
		if (node.ranges == null) {
			leftCount = subidx * (int) Math.pow(32, level / 5);//optimization possible
		} else {
			leftCount = node.ranges[subidx];
			while (i >= leftCount) {
				subidx++;
				leftCount = node.ranges[subidx];
			}
		}
		i -= leftCount;
		if (subidx < node.array.length) {
			box.val = node.array[subidx];
		} else {
			box.val = null;
		}

		return i;
	}

	@SuppressWarnings("unchecked")
	public T nth(int i) {
		if (i >= 0 && i < cnt) {
			Node node = root;
			Box box = new Box(null);
			for (int level = shift; level > 0; level -= 5) {
				i = findNode(node, i, level, box);
				node = (Node) box.val;
			}
			return (T) node.array[i];
		}
		throw new IndexOutOfBoundsException();
	}

	public RRBTree<T> assocN(int i, T val) {
		if (i >= 0 && i < cnt) {
			return new RRBTree<T>(cnt, shift,
								  doAssoc(new Box(null), shift, root, i, val),
								  tail);
		}
		if (i == cnt)
			return cons(val);
		throw new IndexOutOfBoundsException();
	}

	private static Node cloneNode(Node node) {
		return new Node(node.edit, node.array.clone(),
				(node.ranges == null) ? null : node.ranges.clone());
	}

	private static int findDiff(Node node, int i, int idx) {
		if (node.ranges != null) {
			int cnt = node.ranges[idx];
			while (i >= cnt) {
				idx += 1;
				cnt = node.ranges[idx];
				return 1;
			}
		}
		return 0;
	}

	private static Node doAssoc(Box box, int level, Node parent, int i, Object val) {
		Node ret = cloneNode(parent);
		if (level == 0) {
			ret.array[i] = val;
		} else {
			int subidx = (i >>> level) & 0x01f;
			i = findNode(parent, i, level, box);
			ret.array[subidx] = doAssoc(box, level - 5, (Node) box.val, i, val);
		}
		return ret;
	}

	public RRBTree<T> cons(T val) {
		//note there are optimizations possible here
		Node ret = doCons(new Box(null), root, shift, cnt-1, val);
		// overflow root?
		if (ret == null) {//optimization: this could be cached.
			Object[] rootChildren = new Object[2];

			int newshift = shift + 5;
			rootChildren[0] = root;
			Node tailnode = new Node(root.edit, new Object[] {val});
			rootChildren[1] = newPath(root.edit, shift, tailnode);
			Node newroot = new Node(root.edit, rootChildren,
	                root.ranges == null ? null : new int[] { cnt });

			return new RRBTree<T>(cnt + 1, newshift, newroot, tail);
		}
		return new RRBTree<T>(cnt + 1, shift, ret, tail);
	}

	private static Node newPath(AtomicReference<Thread> edit, int level,
			Node node) {
		if (level == 0)
			return node;
		return new Node(edit, new Object[]{ newPath(edit, level - 5, node) });
	}

	protected Node doCons(Box box, Node node, int level, int i, T val) {
		if (level == 0) {
			if (node.array.length == 32) {
				return null;// no room
			} else {
				Object[] newArr = new Object[node.array.length + 1];
				System.arraycopy(node.array, 0,newArr , 0, node.array.length);
				newArr[node.array.length] = val;
				return new Node(node.edit, newArr, null);
			}
		} else {
			// warning code dup ahead
			int subidx = (i >>> level) & 0x01f;
			subidx += findDiff(node, i, subidx);
			i = findNode(node, i, level, box);

			Node ret = doCons(box, (Node)box.val, level - 5, i, val);
			if (ret == null) {
				if (node.array.length == 32) {
					return null;// no room
				} else {
					Object[] newArr = new Object[node.array.length + 1];
					System.arraycopy(node.array, 0, newArr, 0,
							node.array.length);
					newArr[node.array.length] = newPath(node.edit, level - 5,
							new Node(root.edit, new Object[] { val }));
					int[] newRange = node.ranges;
					if (newArr.length < 32 && node.ranges != null) {
						newRange = new int[node.ranges.length + 1];
						System.arraycopy(node.ranges, 0,newRange, 0,
								node.ranges.length);
						newRange[node.ranges.length] = node.ranges[node.ranges.length - 1] + 1;
					}
					return new Node(node.edit, newArr, newRange);
				}
			}
			Node newNode = cloneNode(node);
			newNode.array[subidx] = ret;
			return newNode;

		}
	}

	public IPersistentVector concatv(IPersistentVector other) {
		if (other instanceof RRBTree) {
			RRBTree t = (RRBTree) other;
			Box box = new Box(null);
			Node left = concatrec(box, ...);
			Node right = (Node)box.val;//left overs
			Node newroot = null;
			int newshift = this.shift;
			if (right == null){
				newroot = left;
			}
			else if (root.array.length < 32) {
				Object[] newRoot = new Object[root.array.length+1];
				System.arraycopy(left.array, 0, newRoot,0, left.array.length);
				newRoot[left.array.length] = right;
				newroot = new Node(root.edit,newRoot);
			} else {
				Object[] rootChildren = new Object[2];

				int newshift = shift + 5;
				rootChildren[0] = root;
				Node tailnode = new Node(root.edit, new Object[] {val});
				rootChildren[1] = newPath(root.edit, shift, tailnode);
				Node newroot = new Node(root.edit, rootChildren,
		                root.ranges == null ? null : new int[] { cnt });

				return new RRBTree<T>(cnt + 1, newshift, newroot, tail);
			}
			return new RRBTree(this.cnt+other.count(),this.shift,left,tail);


		}
		throw new IllegalArgumentException();

	}

	protected static RRBTree concatv(Node lroot, int lcnt, int llevel, Node rroot, int rcnt, int rlevel) {


		//move to bottom
		Node node = lroot;
		Box box = new Box(null);
		int i = lcnt-1;
		for (int level = llevel; level > 0; level -= 5) {
			i = findNode(node, i, level, box);
			node = (Node) box.val;
		}

		Node rnode = rroot;
		box = new Box(null);
		for (int level = rlevel; level > 0; level -= 5) {
			findNode(rnode, 0, level, box);
			rnode = (Node) box.val;
		}



		return r;

	}

	@Override
	public IPersistentStack<T> pop() {
		throw new UnsupportedOperationException();
	}

	public int count() {
		return cnt;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPersistentCollection<T> empty() {
		return EMPTY;
	}

	public static void main(String[] args) {
		RRBTree<Integer> t = RRBTree.EMPTY;
		for (int i=0;i<33;i++) {t = t.cons(i);}

		RRBTree<Integer> s = RRBTree.EMPTY;
		for (int i=0;i<33;i++) {s = s.cons(-i);}

		System.out.println(t.concatv(s));


		System.out.println(t);
	}
    */
}
