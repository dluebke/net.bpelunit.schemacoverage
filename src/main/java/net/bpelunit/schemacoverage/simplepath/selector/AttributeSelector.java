package net.bpelunit.schemacoverage.simplepath.selector;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.util.StringUtil;

public class AttributeSelector extends AbstractNodeSelector {

	private String namespaceUri;
	private String localName;

	public AttributeSelector(String localName) {
		this(null, localName);
	}
	
	public AttributeSelector(String namespaceUri, String localName) {
		this.namespaceUri = namespaceUri;
		this.localName = localName;
	}
	

	public AttributeSelector(QName attributeQName) {
		this(attributeQName.getNamespaceURI(), attributeQName.getLocalPart());
	}

	@Override
	protected boolean filter(Node n) {
		return n.getNodeType() == Node.ATTRIBUTE_NODE
		&&
		localName.equals(n.getLocalName())
		&&
		StringUtil.isEmpty(namespaceUri) ? StringUtil.isEmpty(n.getNamespaceURI()) : namespaceUri.equals(n.getNamespaceURI());
	}
	
	@Override
	protected void toStringBuilderInternal(StringBuilder sb) {
		sb.append("@");
		if(!StringUtil.isEmpty(namespaceUri)) {
			sb.append("{");
			sb.append(namespaceUri);
			sb.append("}");
		}
		sb.append(localName);
	}

	@Override
	protected AttributeSelector cloneInternal() {
		return new AttributeSelector(namespaceUri, localName);
	}
}
