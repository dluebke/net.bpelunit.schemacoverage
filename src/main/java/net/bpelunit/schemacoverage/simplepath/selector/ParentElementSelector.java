package net.bpelunit.schemacoverage.simplepath.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.simplepath.filter.INodeFilter;

public class ParentElementSelector implements INodeSelector {

	private INodeSelector next;
	private INodeFilter filter;
	
	@Override
	public List<Node> evaluate(Node n) {
		Node parent = n.getParentNode();
		if(parent.getNodeType() == Node.ELEMENT_NODE) {
			if(filter == null || filter.filter(parent)) {
				return Arrays.asList(parent);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public final List<Node> evaluate(List<Node> nodes) {
		List<Node> result = new ArrayList<>();
		
		for(Node n : nodes) {
			result.addAll(evaluate(n));
		}
		
		return result;
	}
	
	@Override
	public final List<Node> evaluate(NodeList nodes) {
		List<Node> result = new ArrayList<>();
		
		for(int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			result.addAll(evaluate(n));
		}
		
		return result;
	}

	@Override
	public INodeSelector appendSelector(INodeSelector s) {
		if(next == null) {
			next = s;
		} else {
			next.appendSelector(s);
		}
		
		return s;
	}

	@Override
	public StringBuilder toStringBuilder(StringBuilder sb) {
		sb.append("/..");
		
		if(filter != null) {
			sb.append("[");
			filter.toStringBuilder(sb);
			sb.append("]");
		}
		
		if(getNext() != null) {
			getNext().toStringBuilder(sb);
		}
		
		return sb;
	}

	@Override
	public INodeSelector clone() {
		ParentElementSelector clone = new ParentElementSelector();
		if(next != null) {
			clone.next = next.clone();
		}
		if(filter != null) {
			clone.filter = filter.clone();
		}
		return clone;
	}

	@Override
	public int length() {
		if(next != null) {
			return next.length() + 1;
		} else {
			return 1;
		}
	}

	@Override
	public INodeSelector getLastSelector() {
		if(next != null) {
			return next.getLastSelector();
		} else {
			return this;
		}
	}

	public void setFilter(INodeFilter filter) {
		this.filter = filter;
	}

	@Override
	public boolean isInSelectorChain(INodeSelector selector) {
		if(this == selector) {
			return true;
		}
		if(next == null) {
			return false;
		}
		return next.isInSelectorChain(selector);
	}

	@Override
	public INodeSelector getNext() {
		return next;
	}

}
