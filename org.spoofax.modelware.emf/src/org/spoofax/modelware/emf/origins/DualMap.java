/**
 * Created on Oct 16, 2008
 * @author Richard
 * @license This code is copyrighted by Brigham Young University. It can be
 *          freely used or modified for educational and commercial research
 *          purposes provided this notice remains in the code.
 */
package org.spoofax.modelware.emf.origins;

import java.util.Set;

/**
 * Consistent interface for the {@link DualHashMap} and {@link WeakDualMap} classes.
 * @author Richard
 * @param <K> The key type to use.
 * @param <V> The value type to use.
 */
public interface DualMap<K, V>
{
	/**
	 * Store the specified key and value in this map.
	 * @param key The key to store.
	 * @param value The value to store.
	 */
	void put(K key, V value);

	/**
	 * Gets the key for the specified value.
	 * @param value The value to find the key for.
	 * @return The key for the specified value. <code>null</code> if it is not in this map.
	 */
	K getKey(V value);

	/**
	 * Gets the value for the specified key.
	 * @param key The key to get the value for.
	 * @return The value for the specified key. <code>null</code> if it is not in this map.
	 */
	V getValue(K key);

	/**
	 * Removes the key associated with the specified value. This pairing is no longer
	 * available after this call.
	 * @param value The value to remove the key and value for.
	 * @return The removed key.
	 */
	K removeKey(V value);

	/**
	 * Removes the value associated with the specified key. This pairing is no longer
	 * available after this call.
	 * @param key The key to remove the key and value for.
	 * @return The value that was removed.
	 */
	V removeValue(K key);

	/**
	 * Wipe out all the maps.
	 */
	void clear();

	/**
	 * Gets the number of items in this map.
	 * @return The number of items in this map.
	 */
	int size();

	/**
	 * Get whether or not the key is stored in this map.
	 * @param key The key to check if it is in this map.
	 * @return <code>true</code> if it is in the map. <code>false</code> otherwise.
	 */
	boolean containsKey(K key);

	/**
	 * Gets whether or not the value is stored in this map.
	 * @param value The value to check for containment in this map. 
	 * @return <code>true</code> if the value is contained. <code>false</code> otherwise.
	 */
	boolean containsValue(V value);
	
	/**
	 * Gets all the keys and returns them as a set.
	 * @return set of keys
	 */
	Set<K> keySet();
	/**
	 * Gets all the values and returns them as a set.
	 * @return set of values
	 */
	Set<V> valueSet();
}