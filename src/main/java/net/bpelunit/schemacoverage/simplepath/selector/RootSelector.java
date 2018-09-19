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
		if(next != null) {
			next.toStringBuilder(sb);
		}
		return sb;
	}

	@Override
	public INodeSelector clone() {
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
}
