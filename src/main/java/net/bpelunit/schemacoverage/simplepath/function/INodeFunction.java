package net.bpelunit.schemacoverage.simplepath.function;

import java.util.List;

import org.w3c.dom.Node;


public interface INodeFunction {
	List<String> evaluate(Node n);
}
