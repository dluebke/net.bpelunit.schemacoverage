package net.bpelunit.schemacoverage.simplepath.selector;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.simplepath.filter.INodeFilter;

public class RootSelector implements INodeSelector {

	private INodeSelector next;
	
	public RootSelector() {
		this(null);
	}
	
	public RootSelector(INodeSelector next) {
		this.next = next;
	}
	
	@Override
	public List<Node> evaluate(Node n) {
		return next.evaluate(n);
	}

	@Override
	public List<Node> evaluate(List<Node> nodes) {
		return next.evaluate(nodes);
	}

	@Override
	public List<Node> evaluate(NodeList nodes) {
		return next.evaluate(nodes);
	}

	@Override
	public RootSelector appendSelector(INodeSelector s) {
		if(next == null) {
			next = s;
		} else {
			if(next.isInSelectorChain(s)) {
				throw new RuntimeException("Cannot add selector because it already is: " + toString());
			}
			next.appendSelector(s);
		}
		
		return this;
	}
	
	public RootSelector appendSelector(INodeSelector s, int maxLength) throws PathTooLongException{
		if(length() >= maxLength) {
			throw new PathTooLongException(this, maxLength, s);
		}

		appendSelector(s);
		
		return this;
	}

	@Override
	public StringBuilder toStringBuilder(StringBuilder sb) {
		if(next != null) {
			next.toStringBuilder(sb);
		}
		return sb;
	}

	@Override
	public RootSelector clone() {
		RootSelector result = new RootSelector();
		if(next != null) {
			result.next = next.clone();
		}
		return result;
	}
	
	@Override
	public String toString() {
		if(next == null) {
			return "";
		} else {
			return next.toString();
		}
	}

	@Override
	public int length() {
		if(next != null) {
			return next.length();
		} else {
			return 0;
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
	
	@Override
	public void setFilter(INodeFilter f) {
		// TODO What to do if called here?
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
}
