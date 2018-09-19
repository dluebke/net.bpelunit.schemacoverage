package net.bpelunit.schemacoverage.simplepath.function;

import java.util.List;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;

public abstract class AbstractNodeFunction implements INodeFunction {

	private INodeSelector nodeSelector;

	public AbstractNodeFunction(INodeSelector selector) {
		this.nodeSelector = selector;
	}

	@Override
	public List<String> evaluate(Node n) {
		return evaluateInternal(nodeSelector.evaluate(n));
	}

	protected abstract List<String> evaluateInternal(List<Node> nodes);
	protected abstract String getFunctionName();
	
	@Override
	public String toString() {
		return getFunctionName() + "(" + nodeSelector.toString() + ")";
	}
}
