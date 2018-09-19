package net.bpelunit.schemacoverage.simplepath.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.simplepath.filter.INodeFilter;

public abstract class AbstractNodeSelector implements INodeSelector {

	private INodeSelector next;
	private INodeFilter filter;

	public final INodeSelector getNext() {
		return next;
	}
	
	public void setFilter(INodeFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public final INodeSelector appendSelector(INodeSelector s) {
		if(this == s) {
			throw new RuntimeException("Cannot append selector to itself!");
		}
		
		if(next != null) {
			next.appendSelector(s);
		} else {
			next = s;
		}
		
		return s;
	}
	
	@Override
	public final List<Node> evaluate(Node n) {
		if(
			n != null && filter(n)
			&&
			(filter == null || filter.filter(n))
			) {
			
			if(getNext() != null) {
				List<Node> result = new ArrayList<>();
				result.addAll(next.evaluate(n.getChildNodes()));
				
				NamedNodeMap attributes = n.getAttributes();
				for(int i = 0; i < attributes.getLength(); i++) {
					result.addAll(next.evaluate(attributes.item(i)));
				}
				
				return result;
			} else {
				return Arrays.asList(n);
			}
		} else {
			return Collections.emptyList();
		}
		
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
	public final String toString() {
		return toStringBuilder(new StringBuilder()).toString();
	}

	
	@Override
	public final StringBuilder toStringBuilder(StringBuilder sb) {
		sb.append("/");
		toStringBuilderInternal(sb);
		
		if(getNext() != null) {
			getNext().toStringBuilder(sb);
		}
		
		if(filter != null) {
			sb.append("[");
			filter.toStringBuilder(sb);
			sb.append("]");
		}
		
		return sb;
	}
	
	protected abstract void toStringBuilderInternal(StringBuilder sb);

	protected abstract boolean filter(Node n);

	@Override
	public AbstractNodeSelector clone() {
		AbstractNodeSelector result = cloneInternal();
		if(next != null) {
			result.next = next.clone();
		}
		if(filter != null) {
			result.filter = filter.clone();
		}
		return result;
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

	protected abstract AbstractNodeSelector cloneInternal();
	
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
}
