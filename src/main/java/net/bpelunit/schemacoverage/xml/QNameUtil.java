package net.bpelunit.schemacoverage.xml;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

public class QNameUtil {

	public static String format(QName qName) {
		if(qName != null) {
			return QNameUtil.format(qName.getNamespaceURI(), qName.getLocalPart());
		} else {
			return null;
		}
	}

	public static String format(String namespaceURI, String localPart) {
		return "{" + namespaceURI + "}" + localPart;
	}

	public static QName resolveQNameFromCName(Node nodeForNamespaceContext, String cname) {
		if(cname == null || nodeForNamespaceContext == null) {
			return null;
		}
		String[] cNameComponents = cname.split(":");
		String localName;
		String namespacePrefix;
		if(cNameComponents.length == 2) {
			namespacePrefix = cNameComponents[0];
			localName = cNameComponents[1];
		} else {
			namespacePrefix = null;
			localName = cNameComponents[0];
		}
		String namespaceURI = nodeForNamespaceContext.lookupNamespaceURI(namespacePrefix);
		QName qName = new QName(namespaceURI, localName);
		return qName;
	}
	
	public static QName resolveQName(String type) {
		String namespaceURI = type.substring(1, type.indexOf('}'));
		String localName = type.substring(type.indexOf('}') + 1);
		return new QName(namespaceURI, localName);
	}

}
