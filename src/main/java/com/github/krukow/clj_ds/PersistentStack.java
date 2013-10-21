package com.github.krukow.clj_ds;

public interface PersistentStack<E> extends PersistentCollection<E> {

	PersistentStack<E> zero();

	PersistentStack<E> plus(E o);

	/**
	 * @return A new stack consisting of the current stack without its top
	 *         element.
	 */
	PersistentStack<E> minus();

	/**
	 * @return The top element of this stack.
	 */
	E peek();

}
