package net.bpelunit.schemacoverage.simplepath.filter;

import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class XSITypeFilter implements INodeFilter {

	private String typeQName;
	private boolean allowEmptyType;

	public XSITypeFilter(String typeQName) {
		this(typeQName, false);
	}
	
	public XSITypeFilter(String typeQName, boolean allowEmptyType) {
		this.typeQName = typeQName;
		this.allowEmptyType = allowEmptyType;
	}
	
	@Override
	public boolean filter(Node n) {
		if(n.getNodeType() != Node.ELEMENT_NODE) {
			return false;
		}

		Node typeAttribute = n.getAttributes().getNamedItemNS(XMLUtil.NAMESPACE_SCHEMAINSTANCE, "type");
		if(typeAttribute == null && !allowEmptyType ) {
			return false;
		} else if(typeAttribute == null && allowEmptyType) {
			return true;
		}
		
		String qName = QNameUtil.format(QNameUtil.resolveQNameFromCName(n, typeAttribute.getNodeValue()));
		return qName.equals(typeQName);
	}

	@Override
	public StringBuilder toStringBuilder(StringBuilder sb) {
		sb.append("xsi:type");
		if(allowEmptyType) {
			sb.append("?=");
		} else {
			sb.append("=");
		}
		sb.append(typeQName);
		return sb;
	}
	
	@Override
	public XSITypeFilter clone() {
		return new XSITypeFilter(typeQName, allowEmptyType);
	}

}
