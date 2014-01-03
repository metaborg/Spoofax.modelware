/**
 * Created on Oct 26, 2007
 * @author Richard
 * @license This code is copyrighted by Brigham Young University. It can be
 *          freely used or modified for educational and commercial research
 *          purposes provided this notice remains in the code.
 */
package org.spoofax.modelware.emf.origins;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This is a bi-directional weak hash map. However, a true bi-directional one would
 * cause the objects to never be collected because the "value" part of each map is a
 * strong reference. The <i>K</i> reference is treated as the truly weak part of this
 * map, and the <i>V</i> part of this map is allowed to stay alive.
 * <p>
 * This is a 1-to-1 mapping of keys and values, whereas a normal {@link WeakHashMap}
 * can be a many-to-1 mapping.
 * </p>
 * <p>
 * This class is not thread safe. You must wrap it up in your own thread safety 
 * mechanisms if you use this in a threaded environment.
 * </p>
 * @author Richard
 * @see DualHashMap
 */
public class WeakDualMap<K,V> implements DualMap<K, V> 
{
	private WeakHashMap<K, V> forward = new WeakHashMap<K,V>();
	private WeakHashMap<V, WeakReference<K>> reverse = new WeakHashMap<V, WeakReference<K>>();
	/**
	 * Put the specified key and value pair into the map.
	 * @param key The key to put into the mapping.
	 * @param value The value to put into the mapping.
	 */
	public void put(K key, V value)
	{
		forward.put(key, value);
		reverse.put(value, new WeakReference<K>(key));
	}
	
	/**
	 * Gets the value associated with the specified key.
	 * @param key The key to get the value for.
	 * @return The value associated with that key.
	 */
	public V getValue(K key)
	{
		return forward.get(key);
	}
	/**
	 * Gets the key associated with the specified value.
	 * @param value The value to get the key for.
	 * @return The key associated with that value. <code>null</code> if it doesn't 
	 *         exist or was collected.
	 */
	public K getKey(V value)
	{
		WeakReference<K> result = reverse.get(value);
		return (result == null) ? null : result.get();
	}

	/**
	 * Clears all the items from these maps.
	 */
	public void clear()
	{
		forward.clear();
		reverse.clear();
	}

	/**
	 * Removes the key and value for the specified key.
	 * @param key The key to remove the value for.
	 * @return The value removed from the map. null if it doesn't exist or was collected.
	 */
	public V removeValue(K key)
	{
		V value = forward.remove(key);
		if(value != null)
		{
			reverse.remove(value);
		}
		return value;
	}
	
	/**
	 * Removes the key and value for the specified value.
	 * @param value The value to remove the key for.
	 * @return The key that was removed from the mapping.
	 */
	public K removeKey(V value)
	{
		WeakReference<K> key = reverse.get(value);
		if(key != null)
		{
			K keyRef = key.get();
			if(keyRef != null)
			{
				forward.remove(keyRef);
			}
			return keyRef;
		}
		return null;
	}

	@Override
	public boolean containsKey(K key)
	{
		return forward.containsKey(key);
	}

	@Override
	public boolean containsValue(V value)
	{
		return reverse.containsKey(value);
	}

	@Override
	public int size()
	{
		return Math.min(reverse.size(), forward.size());
	}

	/**
	 * Gets all the keys and returns them as a set.
	 * @return set of keys
	 * @see edu.byu.cs.ice.utilities.DualMap#keySet()
	 */
	@Override
	public Set<K> keySet()
	{
		return forward.keySet();
	}

	/**
	 * Gets all the values and returns them as a set.
	 * @return set of values
	 * @see edu.byu.cs.ice.utilities.DualMap#valueSet()
	 */
	@Override
	public Set<V> valueSet()
	{
		return reverse.keySet();
	}
}