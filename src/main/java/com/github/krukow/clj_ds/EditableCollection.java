package com.github.krukow.clj_ds;

/**
 * A {@link PersistentCollection} implementing this interface indicates that it
 * can produce a {@link TransientCollection} for efficient modifications.
 */
public interface EditableCollection<E> {

	TransientCollection<E> asTransient();

}
