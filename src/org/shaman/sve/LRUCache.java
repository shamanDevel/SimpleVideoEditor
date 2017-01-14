/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LRU Cache
 * @author Sebastian Weiss
 * @param <K> the key type
 * @param <V> the value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	
	private int maxSize;

	public LRUCache(int maxSize) {
		super(maxSize, 0.75f, true);
		this.maxSize = maxSize;
	}
	
	public static interface Factory<K, V> {
		V create(K key);
	}
	public synchronized V getCreate(K key, Factory<K, V> factory) {
		V v = get(key);
		if (v == null && factory != null) {
			v = factory.create(key);
			put(key, v);
		}
		return v;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() >= maxSize;
	}
}
