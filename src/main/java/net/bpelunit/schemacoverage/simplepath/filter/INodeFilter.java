package net.bpelunit.schemacoverage.simplepath.filter;

import org.w3c.dom.Node;

public interface INodeFilter {

	public boolean filter(Node n);
	
	public StringBuilder toStringBuilder(StringBuilder sb);

	public INodeFilter clone();
}
