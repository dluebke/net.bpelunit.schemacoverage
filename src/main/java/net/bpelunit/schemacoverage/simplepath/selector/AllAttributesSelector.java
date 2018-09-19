package net.bpelunit.schemacoverage.simplepath.selector;

import org.w3c.dom.Node;

public class AllAttributesSelector extends AbstractNodeSelector {

	@Override
	protected boolean filter(Node n) {
		return n.getNodeType() == Node.ATTRIBUTE_NODE;
	}
	
	@Override
	public void toStringBuilderInternal(StringBuilder sb) {
		sb.append("@*");
	}
	
	
	@Override
	protected AllAttributesSelector cloneInternal() {
		return new AllAttributesSelector();
	}
}
