package net.bpelunit.schemacoverage.simplepath.function;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;

public class TextValueFunction extends AbstractNodeFunction {

	public TextValueFunction(INodeSelector selector) {
		super(selector);
	}

	@Override
	protected List<String> evaluateInternal(List<Node> nodes) {
		List<String> result = new ArrayList<>();
		
		for(Node n : nodes) {
			result.add(n.getTextContent());
		}
		
		return result;
	}

	@Override
	protected String getFunctionName() {
		return "text";
	}
}
