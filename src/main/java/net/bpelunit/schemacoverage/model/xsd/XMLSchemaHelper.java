package net.bpelunit.schemacoverage.model.xsd;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.xml.QNameUtil;

public class XMLSchemaHelper {

	
	
	public static boolean isAbstract(String formattedQName, IProject project) {
		Element typeElement = project.getSchemaTypeByQName(formattedQName);
		return typeElement != null && "true".equals(typeElement.getAttribute("abstract"));
	}

	public static List<Element> collectAttributes(Element type) {
		List<Element> result = new ArrayList<>();
		
		if(type != null) {
			NodeList children = type.getChildNodes();
			for(int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE) {
					if("attribute".equals(n.getLocalName())) {
						result.add((Element)n);
					} else {
						result.addAll(collectAttributes((Element)n));
					}
				}
			}
		}
		
		return result;
	}

	public static List<Element> collectElements(Element type, XMLSchemasContents xmlSchemaContents) {
		List<Element> result = new ArrayList<>();
		
		if(type != null) {
			
			String typeQName = QNameUtil.format(getTargetNamespace(type), type.getAttribute("name"));
			List<Element> allTypeElements = new ArrayList<>();
			allTypeElements.add(type);
			for(String qName : xmlSchemaContents.getAllSupertypesByType().get(typeQName)) {
				allTypeElements.add(xmlSchemaContents.getSchemaTypesByQName().get(qName));
			}
			
			for(Element typeElement : allTypeElements) {
				if(typeElement != null) {
					NodeList children = typeElement.getChildNodes();
					for(int i = 0; i < children.getLength(); i++) {
						Node n = children.item(i);
						if(n.getNodeType() == Node.ELEMENT_NODE) {
							if("element".equals(n.getLocalName())) {
								result.add((Element)n);
							} else {
								result.addAll(collectElements((Element)n, xmlSchemaContents));
							}
						}
					}
				}
			}
		}
		
		return result;
	}

	private static String getTargetNamespace(Element e) {
		return e.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
	}

	public static Element getAnonymousType(Element element) {
		NodeList childNodes = element.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node n = childNodes.item(i);
			if("complexType".equals(n.getLocalName()) || "simpleType".equals(n.getLocalName())) {
				return (Element)n;
			}
		}
		return null;
	}

	public static int getMinOccurs(Element element) {
		String minOccurs = element.getAttribute("minOccurs");
		if(minOccurs != null && !"".equals(minOccurs)) {
			return Integer.parseInt(minOccurs);
		} else {
			return 1;
		}
	}
	
	public static int getMaxOccurs(Element element) {
		String maxOccurs = element.getAttribute("maxOccurs");
		if(maxOccurs != null && !"".equals(maxOccurs)) {
			if("unbounded".equals(maxOccurs)) {
				return -1;
			} else {
				return Integer.parseInt(maxOccurs);
			}
		} else {
			return 1;
		}
	}

	public static QName resolveQNameForElement(Element e) {
		String targetNamespace = getTargetNamespaceNamespaceForElement(e);
		String elementName = e.getAttribute("name");
		return new QName(targetNamespace, elementName);
	}
	
	public static QName resolveQNameForAttribute(Element attributeElement) {
		String targetNamespace = getTargetNamespaceForAttribute(attributeElement);
		String elementName = attributeElement.getAttribute("name");
		return new QName(targetNamespace, elementName);
	}
	
	public static String getTargetNamespaceNamespaceForElement(Element element) {
		Element schemaElement = element.getOwnerDocument().getDocumentElement();
		if(element.getParentNode().getLocalName().equals("schema") || "qualified".equals(schemaElement.getAttribute("elementFormDefault"))) {
			String targetNamespace = schemaElement.getAttribute("targetNamespace");
			return targetNamespace;
		} else {
			return null;
		}
	}
	
	public static String getTargetNamespaceForAttribute(Element element) {
		Element schemaElement = element.getOwnerDocument().getDocumentElement();
		if(element.getParentNode().getLocalName().equals("schema") || "qualified".equals(schemaElement.getAttribute("attributeFormDefault"))) {
			String targetNamespace = schemaElement.getAttribute("targetNamespace");
			return targetNamespace;
		} else {
			return null;
		}
	}

	public static Element resolveElementRefIfNecessary(Element element, IProject project) {
		String ref = element.getAttribute("ref");
		if(ref != null && !"".equals(ref)) {
			QName elementName = QNameUtil.resolveQNameFromCName(element, ref);
			Element resolvedElement = project.getSchemaElementByQName(QNameUtil.format(elementName));
			return resolvedElement;
		} else {
			return element;
		}
	}

	public static String getName(Element xsdElementOrAttribute) {
		return xsdElementOrAttribute.getAttribute("name");
	}
}
