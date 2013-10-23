package com.github.krukow.clj_ds;

public interface TransientMap<K, V> {

	/**
	 * @return A new {@link PersistentMap} consisting of the content of the
	 *         current {@link PersistentMap} where the given key is associated
	 *         to the value val (no guarantees are made on the current map). The
	 *         new association may replace a previous association.
	 */
	TransientMap<K, V> plus(K key, V val);

	/**
	 * @return A new {@link PersistentMap} consisting of the content of the
	 *         current {@link PersistentMap} without the association to the
	 *         given key (no guarantees are made on the current map).
	 */
	TransientMap<K, V> minus(K key);

	PersistentMap<K, V> persist();

}
