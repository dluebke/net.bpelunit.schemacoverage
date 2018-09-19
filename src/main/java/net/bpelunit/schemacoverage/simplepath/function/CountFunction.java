package net.bpelunit.schemacoverage.simplepath.function;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;

public class CountFunction extends AbstractNodeFunction {

	public CountFunction(INodeSelector selector) {
		super(selector);
	}

	@Override
	protected List<String> evaluateInternal(List<Node> nodes) {
		return Arrays.asList(Integer.toString(nodes.size()));
	}
	
	@Override
	protected String getFunctionName() {
		return "count";
	}

}
