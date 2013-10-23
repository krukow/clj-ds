package com.github.krukow.clj_ds;

import java.util.Map;

/**
 * {@link PersistentMap}s
 * 
 * @param <K>
 *            The type of the keys
 * @param <V>
 *            The type of the values
 */
public interface PersistentMap<K, V> extends Map<K, V> {

	/**
	 * @return An empty instance of this kind of {@link PersistentMap}
	 */
	PersistentMap<K, V> zero();

	/**
	 * @return A new {@link PersistentMap} consisting of the content of the
	 *         current {@link PersistentMap} where the given key is associated
	 *         to the value val. The new association may replace a previous
	 *         association.
	 */
	PersistentMap<K, V> plus(K key, V val);

	/**
	 * @return A new {@link PersistentMap} consisting of the content of the
	 *         current {@link PersistentMap} where the given key is associated
	 *         to the value val.
	 * @throws java.lang.RuntimeException
	 *             If the key is already present in the {@link PersistentMap}.
	 */
	PersistentMap<K, V> plusEx(K key, V val);

	/**
	 * @return A new {@link PersistentMap} consisting of the content of the
	 *         current {@link PersistentMap} without the assocation to the given
	 *         key.
	 */
	PersistentMap<K, V> minus(K key);

}
