package net.bpelunit.schemacoverage.simplepath.function;

import java.util.List;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;


public interface INodeFunction {
	List<String> evaluate(Node n);

	INodeSelector getNodeSelector();
}
