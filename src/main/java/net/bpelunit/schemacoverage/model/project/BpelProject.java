package net.bpelunit.schemacoverage.model.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.model.xsd.XMLSchemasContents;
import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.SimpleNamespaceContext;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class BpelProject implements IProject {
	private Document bpelDocument;
	private String bpelNamespace;

	private Map<String, Element> wsdlsForNamespace = new HashMap<>();
	private Map<String, Element> portTypesByQName = new HashMap<>();
	private Map<String, Element> partnerLinkTypesByQName = new HashMap<>();
	private Map<String, Element> allMessagesByQName = new HashMap<>();
	private Set<String> providerRequestMessages = new HashSet<>();
	private Set<String> providerResponseMessages = new HashSet<>();
	private Set<String> consumerRequestMessages = new HashSet<>();
	private Set<String> consumerResponseMessages = new HashSet<>();
	
	private String name;
	
	private XMLSchemasContents xmlSchemasContents = new XMLSchemasContents();

	public static BpelProject readBpelProject(File bpelFile) throws SAXException, IOException {
		BpelProject project = new BpelProject();

		project.name = bpelFile.getName();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder docBuilder;
		try {
			docBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			throw new RuntimeException(e1);
		}
		project.bpelDocument = docBuilder.parse(bpelFile);
		project.bpelNamespace = project.bpelDocument.getDocumentElement().getNamespaceURI();
		List<FileImport> remainingWsdlImports = new ArrayList<>();
		List<FileImport> remainingXsdImports = new ArrayList<>();
		NodeList importElements = project.bpelDocument.getElementsByTagNameNS(project.bpelNamespace, "import");
		for (int i = 0; i < importElements.getLength(); i++) {
			Element importElement = (Element) importElements.item(i);
			File importFile = new File(bpelFile.getParentFile(), importElement.getAttribute("location"));
			String importType = importElement.getAttribute("importType");
			try {
	
				Document importDocument;
				FileImport fileImport;
				switch (importType) {
				case "http://schemas.xmlsoap.org/wsdl/":
					importDocument = docBuilder.parse(importFile);
					fileImport = new FileImport(importFile, importDocument.getDocumentElement());
					remainingWsdlImports.add(fileImport);
					break;
				case "http://www.w3.org/2001/XMLSchema":
					importDocument = docBuilder.parse(importFile);
					fileImport = new FileImport(importFile, importDocument.getDocumentElement());
					remainingXsdImports.add(fileImport);
					break;
				}
			} catch(Exception e) {
				System.err.println("Warning: Cannot resolve reference '" + importFile + "' for namespace " + importElement.getAttribute("importLocation"));
			}
		} 

		while (remainingWsdlImports.size() > 0) {
			FileImport wsdlImport = remainingWsdlImports.remove(0);
			Element wsdlElement = wsdlImport.getImportElement();
			String targetNamespace = wsdlElement.getAttribute("targetNamespace");
			if (!project.wsdlsForNamespace.containsKey(targetNamespace)) {
				project.wsdlsForNamespace.put(targetNamespace, wsdlElement);

				NodeList wsdlImportElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(),
						"import");
				for (int i = 0; i < wsdlImportElements.getLength(); i++) {
					Element importElement = (Element) wsdlImportElements.item(i);
					String importLocation = importElement.getAttribute("location");
					File importFile = new File(wsdlImport.getImportFile().getParentFile(), importLocation);
					remainingWsdlImports
							.add(new FileImport(importFile, docBuilder.parse(importFile).getDocumentElement()));
				}

				// TODO Reuse XMLSchemaReader
				NodeList xsdSchemaElements = wsdlElement.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "schema");
				for (int i = 0; i < xsdSchemaElements.getLength(); i++) {
					Element xsdSchemaElement = (Element) xsdSchemaElements.item(i);
					remainingXsdImports.add(new FileImport(wsdlImport.getImportFile(), xsdSchemaElement));
				}

				NodeList portTypeElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(),
						"portType");
				for (int i = 0; i < portTypeElements.getLength(); i++) {
					Element portTypeElement = (Element) portTypeElements.item(i);
					String portTypeName = portTypeElement.getAttribute("name");
					project.portTypesByQName.put(QNameUtil.format(targetNamespace, portTypeName), portTypeElement);
				}

				NodeList messageElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(), "message");
				for (int i = 0; i < messageElements.getLength(); i++) {
					Element messageElement = (Element) messageElements.item(i);
					String messageName = messageElement.getAttribute("name");
					project.allMessagesByQName.put(QNameUtil.format(targetNamespace, messageName), messageElement);
				}

				NodeList partnerLinkTypeElements = wsdlElement
						.getElementsByTagNameNS("http://docs.oasis-open.org/wsbpel/2.0/plnktype", "partnerLinkType");
				for (int i = 0; i < partnerLinkTypeElements.getLength(); i++) {
					Element partnerLinkTypeElement = (Element) partnerLinkTypeElements.item(i);
					String partnerLinkName = partnerLinkTypeElement.getAttribute("name");
					project.partnerLinkTypesByQName.put(QNameUtil.format(targetNamespace, partnerLinkName),
							partnerLinkTypeElement);
				}
			}
		}

		while (remainingXsdImports.size() > 0) {
			FileImport xsdImport = remainingXsdImports.remove(0);
			Element xsdElement = xsdImport.getImportElement();
			project.xmlSchemasContents.addSchema(xsdElement);

			NodeList xsdImportElements = xsdElement.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "import");
			for (int i = 0; i < xsdImportElements.getLength(); i++) {
				Element importElement = (Element) xsdImportElements.item(i);
				File importFile = new File(xsdImport.getImportFile().getParentFile(),
						importElement.getAttribute("schemaLocation"));

				remainingXsdImports.add(new FileImport(importFile, docBuilder.parse(importFile).getDocumentElement()));
			}
		}
		
		project.resolveUsedMessages();

		return project;
	}

	private List<String> resolveUsedMessages() {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		xpath.setNamespaceContext(new SimpleNamespaceContext("bpel", bpelNamespace, "plnk",
				"http://docs.oasis-open.org/wsbpel/2.0/plnktype"));
		Set<String> usedMessageElements = new HashSet<>();
		try {
			XPathExpression messageActivitiesExpression = xpath.compile("//*[@operation and @partnerLink]");
			NodeList operationNodes = (NodeList) messageActivitiesExpression.evaluate(bpelDocument,
					XPathConstants.NODESET);

			for (int i = 0; i < operationNodes.getLength(); i++) {
				Element e = (Element) operationNodes.item(i);
				String operationname = e.getAttribute("operation");
				String partnerLinkName = e.getAttribute("partnerLink");
				boolean isProcessConsumer = "invoke".equals(e.getLocalName());

				XPathExpression partnerLinkExpression = xpath
						.compile("ancestor::*//bpel:partnerLink[@name='" + partnerLinkName + "']");
				Element partnerLinkElement = (Element) partnerLinkExpression.evaluate(e, XPathConstants.NODE);
				String role = isProcessConsumer ? partnerLinkElement.getAttribute("partnerRole")
						: partnerLinkElement.getAttribute("myRole");

				String partnerLinkTypeQName = partnerLinkElement.getAttribute("partnerLinkType");
				QName partnerLinkType = QNameUtil.resolveQNameFromCName(partnerLinkElement, partnerLinkTypeQName);

				Element partnerLinkTypeElement = partnerLinkTypesByQName.get(QNameUtil.format(partnerLinkType));
				XPathExpression partnerLinkTypeRoleExpression = xpath.compile("plnk:role[@name='" + role + "']");
				Element roleElement = (Element) partnerLinkTypeRoleExpression.evaluate(partnerLinkTypeElement,
						XPathConstants.NODE);
				QName portType = QNameUtil.resolveQNameFromCName(roleElement, roleElement.getAttribute("portType"));
				Element portTypeElement = portTypesByQName.get(QNameUtil.format(portType));

				NodeList operationsInPortType = portTypeElement
						.getElementsByTagNameNS(portTypeElement.getNamespaceURI(), "operation");
				Element operationElement = null;
				for (int j = 0; j < operationsInPortType.getLength(); j++) {
					operationElement = (Element) operationsInPortType.item(j);
					if (operationname.equals(operationElement.getAttribute("name"))) {
						break;
					}
				}
				NodeList operationChildren = operationElement.getChildNodes();
				Set<QName> consumerRequestMessageQNames = new HashSet<>();
				Set<QName> consumerResponseMessageQNames = new HashSet<>();
				Set<QName> providerRequestMessageQNames = new HashSet<>();
				Set<QName> providerResponseMessageQNames = new HashSet<>();
				for (int j = 0; j < operationChildren.getLength(); j++) {
					Node n = operationChildren.item(j);
					if (n.getAttributes() != null) {
						Node messageAttributeNode = n.getAttributes().getNamedItem("message");
						if (messageAttributeNode != null) {
							QName messageQName = QNameUtil.resolveQNameFromCName(n,
									messageAttributeNode.getNodeValue());
							
							if(isProcessConsumer) {
								if("input".equals(n.getLocalName())) {
									consumerRequestMessageQNames.add(messageQName);
								} else {
									consumerResponseMessageQNames.add(messageQName);
								}
							} else {
								if("input".equals(n.getLocalName())) {
									providerRequestMessageQNames.add(messageQName);
								} else {
									providerResponseMessageQNames.add(messageQName);
								}
							}
						}
					}
				}

				resolveMessageQNamesToElements(consumerRequestMessageQNames, consumerRequestMessages);
				resolveMessageQNamesToElements(consumerResponseMessageQNames, consumerResponseMessages);
				resolveMessageQNamesToElements(providerRequestMessageQNames, providerRequestMessages);
				resolveMessageQNamesToElements(providerResponseMessageQNames, providerResponseMessages);
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("This is a programming error!");
		}

		List<String> result = new ArrayList<>(usedMessageElements);
		Collections.sort(result);
		return result;
	}

	private void resolveMessageQNamesToElements(Set<QName> messageQNames, Set<String> messageSet) {
		for (QName messageQName : messageQNames) {
			Element message = allMessagesByQName.get(QNameUtil.format(messageQName));
			NodeList partElements = message.getElementsByTagNameNS(message.getNamespaceURI(), "part");
			for (int j = 0; j < partElements.getLength(); j++) {
				Element partElement = (Element) partElements.item(j);
				messageSet.add(QNameUtil.format(QNameUtil.resolveQNameFromCName(partElement, partElement.getAttribute("element"))));
			}
		}
	}

	@Override
	public Set<String> getProviderRequestMessages() {
		return providerRequestMessages;
	}
	
	@Override
	public Set<String> getProviderResponseMessages() {
		return providerResponseMessages;
	}
	
	@Override
	public Set<String> getConsumerRequestMessages() {
		return consumerRequestMessages;
	}
	
	@Override
	public Set<String> getConsumerResponseMessages() {
		return consumerResponseMessages;
	}
	
	@Override
	public Element getSchemaElementByQName(String qName) {
		return xmlSchemasContents.getSchemaElementsByQName().get(qName);
	}

	@Override
	public Element getSchemaForNamespace(String namespace) {
		return xmlSchemasContents.getSchemasForNamespace().get(namespace);
	}

	@Override
	public Element getSchemaTypeByQName(String qName) {
		return xmlSchemasContents.getSchemaTypesByQName().get(qName);
	}
	
	@Override
	public Set<String> getAllSubstitutionsForElement(String element) {
		return xmlSchemasContents.getAllSubstitutionsByElement().get(element);
	}
	
	@Override
	public Set<String> getAllSubtypesForType(String formattedQName) {
		return xmlSchemasContents.getAllSubtypesByType().get(formattedQName);
	}
	
	@Override
	public XMLSchemasContents getXMLSchemaContents() {
		return xmlSchemasContents;
	}

	@Override
	public Element getAttributeElementByQName(String formattedQName) {
		return xmlSchemasContents.getSchemaAttributesByQName().get(formattedQName);
	}

	@Override
	public String getName() {
		return name;
	}
	
}