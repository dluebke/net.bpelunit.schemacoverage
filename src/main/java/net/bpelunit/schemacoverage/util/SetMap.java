package net.bpelunit.schemacoverage.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetMap<K, V> {
	private Map<K, Set<V>> lists = new HashMap<>();

	public Set<V> get(K key) {
		Set<V> result = lists.get(key);
		if(result == null) {
			result = new HashSet<>();
			lists.put(key,  result);
		}
		
		return result;
	}

	public Set<K> keySet() {
		return lists.keySet();
	}

	public boolean containsKey(String key) {
		return lists.containsKey(key);
	}
	
	@Override
	public String toString() {
		return lists.toString();
	}
}
