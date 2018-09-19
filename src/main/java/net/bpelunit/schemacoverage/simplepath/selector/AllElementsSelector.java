package net.bpelunit.schemacoverage.simplepath.selector;

import org.w3c.dom.Node;

public class AllElementsSelector extends AbstractNodeSelector {

	@Override
	protected boolean filter(Node n) {
		return n.getNodeType() == Node.ELEMENT_NODE;
	}
	
	@Override
	public void toStringBuilderInternal(StringBuilder sb) {
		sb.append("*");
	}
	
	@Override
	protected AllElementsSelector cloneInternal() {
		return new AllElementsSelector();
	}
}
