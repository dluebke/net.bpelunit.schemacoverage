package net.bpelunit.schemacoverage.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {

	Map<String, String> namespaceByPrefix = new HashMap<>();
	Map<String, String> prefixByNamespace = new HashMap<>();
	
	public SimpleNamespaceContext(String... prefixesAndNamespaces) {
		for(int i = 0; i < prefixesAndNamespaces.length; i += 2) {
			namespaceByPrefix.put(prefixesAndNamespaces[i], prefixesAndNamespaces[i + 1]);
			prefixByNamespace.put(prefixesAndNamespaces[i + 1], prefixesAndNamespaces[i]);
		}
	}
	
	public SimpleNamespaceContext(Map<String, String> prefixForNamespace) {
		for(Entry<String, String>  entry : prefixForNamespace.entrySet()) {
			prefixByNamespace.put(entry.getKey(), entry.getValue());
			namespaceByPrefix.put(entry.getValue(), entry.getKey());
		}
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		return namespaceByPrefix.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return prefixByNamespace.get(namespaceURI);
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return Arrays.asList(getPrefix(namespaceURI)).iterator();
	}

}
