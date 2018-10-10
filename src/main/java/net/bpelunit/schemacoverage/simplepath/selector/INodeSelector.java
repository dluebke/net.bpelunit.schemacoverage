package net.bpelunit.schemacoverage.simplepath.selector;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.simplepath.filter.INodeFilter;

public interface INodeSelector extends Cloneable {
	List<Node> evaluate(Node n);
	List<Node> evaluate(List<Node> nodes);
	List<Node> evaluate(NodeList nodes);

	INodeSelector appendSelector(INodeSelector s);

	StringBuilder toStringBuilder(StringBuilder sb);
	
	INodeSelector clone();
	
	int length();
	INodeSelector getLastSelector();
	void setFilter(INodeFilter filter);
	boolean isInSelectorChain(INodeSelector selector);
//	boolean isParentOf(INodeSelector nodeSelector);
	INodeSelector getNext();
}
