package net.bpelunit.schemacoverage.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	public static final String NAMESPACE_SCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String NAMESPACE_XMLSCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	public static Element getFirstChildElement(Node item) {
		NodeList children = item.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE) {
				return (Element)n;
			}
		}
		return null;
	}

}
