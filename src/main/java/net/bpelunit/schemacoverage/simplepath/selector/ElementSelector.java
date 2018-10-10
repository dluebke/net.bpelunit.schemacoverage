package net.bpelunit.schemacoverage.simplepath.selector;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.filter.INodeFilter;
import net.bpelunit.schemacoverage.util.StringUtil;

public class ElementSelector extends AbstractNodeSelector {

	private String namespaceUri;
	private String localName;

	public ElementSelector(String localName) {
		this(null, localName);
	}
	
	public ElementSelector(String namespaceUri, String localName) {
		this(namespaceUri, localName, null);
	}
	
	public ElementSelector(String namespaceUri, String localName, INodeFilter filter) {
		this.namespaceUri = namespaceUri;
		this.localName = localName;
		setFilter(filter);
	}
	
	public ElementSelector(QName qName) {
		this(qName.getNamespaceURI(), qName.getLocalPart());
	}
	
	public ElementSelector(QName qName, INodeFilter filter) {
		this(qName.getNamespaceURI(), qName.getLocalPart(), filter);
	}

	
	
	@Override
	protected boolean filter(Node n) {
		return 
			n.getNodeType() == Node.ELEMENT_NODE
			&&
			localName.equals(n.getLocalName())
			&&
			(StringUtil.isEmpty(namespaceUri) ? StringUtil.isEmpty(n.getNamespaceURI()) : namespaceUri.equals(n.getNamespaceURI()))
		;
	}
	
	@Override
	protected void toStringBuilderInternal(StringBuilder sb) {
		if(!StringUtil.isEmpty(namespaceUri)) {
			sb.append("{");
			sb.append(namespaceUri);
			sb.append("}");
		}
		sb.append(localName);
	}
	
	@Override
	protected ElementSelector cloneInternal() {
		return new ElementSelector(namespaceUri, localName);
	}
}
