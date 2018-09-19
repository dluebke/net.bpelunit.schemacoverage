package net.bpelunit.schemacoverage.util;

import java.util.HashMap;

@SuppressWarnings("serial")
public class CounterMap<K> extends HashMap<K, Integer>{

	@Override
	public Integer get(Object key) {
		Integer v = super.get(key);
		if(v == null) {
			return 0;
		}
		return v;
	}
	
	public int inc(K key) {
		int newValue = get(key) + 1;
		put(key, newValue);
		return newValue;
	}
}
