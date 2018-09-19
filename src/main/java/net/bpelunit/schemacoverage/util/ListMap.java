package net.bpelunit.schemacoverage.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListMap<K, V> {
	private Map<K, List<V>> lists = new HashMap<>();

	public List<V> get(K key) {
		List<V> result = lists.get(key);
		if(result == null) {
			result = new ArrayList<>();
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
