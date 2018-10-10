package net.bpelunit.schemacoverage.model;

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

import net.bpelunit.schemacoverage.util.SetMap;
import net.bpelunit.schemacoverage.util.StringUtil;
import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.SimpleNamespaceContext;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class BpelProject {
	public Document bpelDocument;
	public String bpelNamespace;
	
	public Map<String, Element> schemasForNamespace = new HashMap<>();
	public Map<String, Element> schemaTypesByQName = new HashMap<>();
	public Map<String, Element> schemaElementsByQName = new HashMap<>();
	
	public Map<String, Element> wsdlsForNamespace = new HashMap<>();
	public Map<String, Element> portTypesByQName = new HashMap<>();
	public Map<String, Element> partnerLinkTypesByQName = new HashMap<>();
	public Map<String, Element> messageByQName = new HashMap<>();
	public SetMap<String, String> directSubtypesByType = new SetMap<>();
	public SetMap<String, String> allSubtypesByType = new SetMap<>();
	public SetMap<String, String> directSubstitutionsByElement= new SetMap<>();
	public SetMap<String, String> allSubstituionsByElement= new SetMap<>();
	
	public static BpelProject readBpelProject(File bpelFile) throws SAXException, IOException {
		BpelProject project = new BpelProject();
		
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
		for(int i = 0; i < importElements.getLength(); i++) {
			Element importElement = (Element)importElements.item(i);
			File importFile = new File(bpelFile.getParentFile(), importElement.getAttribute("location"));
			
			String importType = importElement.getAttribute("importType");
			
			Document importDocument;
			FileImport fileImport;
			switch(importType) {
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
		}
		
		while(remainingWsdlImports.size() > 0) {
			FileImport wsdlImport = remainingWsdlImports.remove(0);
			Element wsdlElement = wsdlImport.importElement;
			String targetNamespace = wsdlElement.getAttribute("targetNamespace");
			if(!project.wsdlsForNamespace.containsKey(targetNamespace)) {
				project.wsdlsForNamespace.put(targetNamespace, wsdlElement);
				
				NodeList wsdlImportElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(), "import");
				for(int i = 0; i < wsdlImportElements.getLength(); i++) {
					Element importElement = (Element) wsdlImportElements.item(i);
					String importLocation = importElement.getAttribute("location");
					File importFile = new File(wsdlImport.importFile.getParentFile(), importLocation);
					remainingWsdlImports.add(new FileImport(importFile, docBuilder.parse(importFile).getDocumentElement()));
				}
				
				NodeList xsdSchemaElements = wsdlElement.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "schema");
				for(int i = 0; i < xsdSchemaElements.getLength(); i++) {
					Element xsdSchemaElement = (Element)xsdSchemaElements.item(i);
					remainingXsdImports.add(new FileImport(wsdlImport.importFile, xsdSchemaElement));
				}
				
				NodeList portTypeElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(), "portType");
				for(int i = 0; i < portTypeElements.getLength(); i++) {
					Element portTypeElement = (Element)portTypeElements.item(i);
					String portTypeName = portTypeElement.getAttribute("name");
					project.portTypesByQName.put(QNameUtil.format(targetNamespace, portTypeName), portTypeElement);
				}
				
				NodeList messageElements = wsdlElement.getElementsByTagNameNS(wsdlElement.getNamespaceURI(), "message");
				for(int i = 0; i < messageElements.getLength(); i++) {
					Element messageElement = (Element)messageElements.item(i);
					String messageName = messageElement.getAttribute("name");
					project.messageByQName.put(QNameUtil.format(targetNamespace, messageName), messageElement);
				}
				
				NodeList partnerLinkTypeElements = wsdlElement.getElementsByTagNameNS("http://docs.oasis-open.org/wsbpel/2.0/plnktype", "partnerLinkType");
				for(int i = 0; i < partnerLinkTypeElements.getLength(); i++) {
					Element partnerLinkTypeElement = (Element)partnerLinkTypeElements.item(i);
					String partnerLinkName = partnerLinkTypeElement.getAttribute("name");
					project.partnerLinkTypesByQName.put(QNameUtil.format(targetNamespace, partnerLinkName), partnerLinkTypeElement);
				}
			}
		}
		
		while(remainingXsdImports.size() > 0) {
			FileImport xsdImport = remainingXsdImports.remove(0);
			Element xsdElement = xsdImport.importElement;
			String targetNamespace = xsdElement.getAttribute("targetNamespace");
			project.schemasForNamespace.put(targetNamespace, xsdElement);
			
			NodeList xsdImportElements = xsdElement.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "import");
			for(int i = 0; i < xsdImportElements.getLength(); i++) {
				Element importElement = (Element)xsdImportElements.item(i);
				File importFile = new File(xsdImport.importFile.getParentFile(), importElement.getAttribute("schemaLocation"));
				
				remainingXsdImports.add(new FileImport(importFile, docBuilder.parse(importFile).getDocumentElement()));
			}
			
			NodeList topLevelChildNodes = xsdElement.getChildNodes();
			for(int i = 0; i < topLevelChildNodes.getLength(); i++) {
				Node n = topLevelChildNodes.item(i);
				
				if(n.getNodeType() == Node.ELEMENT_NODE && XMLUtil.NAMESPACE_XMLSCHEMA.equals(n.getNamespaceURI())) {
					Element e = (Element)n;
					if("complexType".equals(e.getLocalName()) || "simpleType".equals(e.getLocalName())) {
						String typeName = e.getAttribute("name");
						String typeNameAsFormatedQName = QNameUtil.format(targetNamespace, typeName);
						project.schemaTypesByQName.put(typeNameAsFormatedQName, e);
						
						NodeList extensions = e.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "extension");
						if(extensions.getLength() > 0) {
							Element extension = (Element)extensions.item(0);
							String baseAsCName = extension.getAttribute("base");
							String baseAsFormatedQName = QNameUtil.format(QNameUtil.resolveQNameFromCName(extension, baseAsCName));
							
							Set<String> directExtensions = project.directSubtypesByType.get(baseAsFormatedQName);
							directExtensions.add(typeNameAsFormatedQName);
						}
					}
					
					if("element".equals(e.getLocalName())) {
						String elementName = e.getAttribute("name");
						String elementFormatedQName = QNameUtil.format(targetNamespace, elementName);
						project.schemaElementsByQName.put(elementFormatedQName, e);
						
						String substitutionGroup = e.getAttribute("substitutionGroup");
						if(!StringUtil.isEmpty(substitutionGroup)) {
							String formatedSubstituionElementQName = QNameUtil.format(QNameUtil.resolveQNameFromCName(e, substitutionGroup));
							project.directSubstitutionsByElement.get(formatedSubstituionElementQName).add(elementFormatedQName);
						}
					}
				}
			}
		}
		
		// build list of all subtypes from list of direct subtypes
		for(String typeWithSubTypes : project.directSubtypesByType.keySet()) {
			if(!StringUtil.isEmpty(typeWithSubTypes)) {
				Set<String> allSubTypes = project.allSubtypesByType.get(typeWithSubTypes);
				
				if(project.directSubtypesByType.containsKey(typeWithSubTypes)) {
					List<String> subTypesToProcess = new ArrayList<>(project.directSubtypesByType.get(typeWithSubTypes));
					while(subTypesToProcess.size() > 0) {
						String subtype = subTypesToProcess.remove(0);
						
						allSubTypes.add(subtype);
						Set<String> nextSubtypes = project.directSubtypesByType.containsKey(subtype) ? project.directSubtypesByType.get(subtype) : Collections.emptySet();
						if(nextSubtypes != null) {
							subTypesToProcess.addAll(nextSubtypes);
						}
					}
				}
			}
		}
		
		for(String elementWithSubstitutions : project.directSubstitutionsByElement.keySet()) {
			if(!StringUtil.isEmpty(elementWithSubstitutions)) {
				Set<String> allSubstitutions = project.allSubstituionsByElement.get(elementWithSubstitutions);
				
				if(project.directSubstitutionsByElement.containsKey(elementWithSubstitutions)) {
					List<String> substitutionsToProcess = new ArrayList<>(project.directSubstitutionsByElement.get(elementWithSubstitutions));
					while(substitutionsToProcess.size() > 0) {
						String substitution = substitutionsToProcess.remove(0);
						
						allSubstitutions.add(substitution);
						Set<String> nextSubstitutions = project.directSubstitutionsByElement.containsKey(substitution) ? project.directSubstitutionsByElement.get(substitution) : Collections.emptySet();
						if(nextSubstitutions != null) {
							substitutionsToProcess.addAll(nextSubstitutions);
						}
					}
				}
			}
		}
		
		return project;
	}
	
	public List<String> resolveUsedMessages(boolean calculateTwoWay) {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		xpath.setNamespaceContext(new SimpleNamespaceContext("bpel", bpelNamespace, "plnk", "http://docs.oasis-open.org/wsbpel/2.0/plnktype"));
		Set<String> usedMessageElements = new HashSet<>();
		try {
			XPathExpression messageActivitiesExpression = xpath.compile("//*[@operation and @partnerLink]");
			NodeList operationNodes = (NodeList)messageActivitiesExpression.evaluate(bpelDocument, XPathConstants.NODESET);
			
			for(int i = 0; i < operationNodes.getLength(); i++) {
				Element e = (Element)operationNodes.item(i);
				String operationname = e.getAttribute("operation");
				String partnerLinkName = e.getAttribute("partnerLink");
				boolean isSending = "invoke".equals(e.getLocalName());
				
				XPathExpression partnerLinkExpression = xpath.compile("ancestor::*//bpel:partnerLink[@name='" + partnerLinkName + "']");
				Element partnerLinkElement = (Element) partnerLinkExpression.evaluate(e, XPathConstants.NODE);
				String role = isSending ? partnerLinkElement.getAttribute("partnerRole") : partnerLinkElement.getAttribute("myRole");
				
				String partnerLinkTypeQName = partnerLinkElement.getAttribute("partnerLinkType");
				QName partnerLinkType = QNameUtil.resolveQNameFromCName(partnerLinkElement, partnerLinkTypeQName);

				Element partnerLinkTypeElement = partnerLinkTypesByQName.get(QNameUtil.format(partnerLinkType));
				XPathExpression partnerLinkTypeRoleExpression = xpath.compile("plnk:role[@name='" + role + "']");
				Element roleElement = (Element)partnerLinkTypeRoleExpression.evaluate(partnerLinkTypeElement, XPathConstants.NODE);
				QName portType = QNameUtil.resolveQNameFromCName(roleElement, roleElement.getAttribute("portType"));
				Element portTypeElement = portTypesByQName.get(QNameUtil.format(portType));

				NodeList operationsInPortType = portTypeElement.getElementsByTagNameNS(portTypeElement.getNamespaceURI(), "operation");
				Element operationElement = null;
				for(int j = 0; j < operationsInPortType.getLength(); j++) {
					operationElement = (Element)operationsInPortType.item(j);
					if(operationname.equals(operationElement.getAttribute("name"))) {
						break;
					}
				}
				List<QName> messsagesInOperation = new ArrayList<>();
				NodeList operationChildren = operationElement.getChildNodes();
				for(int j = 0; j < operationChildren.getLength(); j++) {
					Node n = operationChildren.item(j);
					if(
							calculateTwoWay ||
							(!isSending && "input".equals(n.getLocalName())) ||
							(isSending && !"input".equals(n.getLocalName()))
					) {
						if(n.getAttributes() != null) {
							Node messageAttributeNode = n.getAttributes().getNamedItem("message");
							if(messageAttributeNode != null) {
								QName messageQName = QNameUtil.resolveQNameFromCName(n, messageAttributeNode.getNodeValue());
								messsagesInOperation.add(messageQName);
							}
						}
					}
				}
				for(QName messageQName : messsagesInOperation) {
					Element message = messageByQName.get(QNameUtil.format(messageQName));
					NodeList partElements = message.getElementsByTagNameNS(message.getNamespaceURI(), "part");
					for(int j = 0; j < partElements.getLength(); j++) {
						Element partElement = (Element)partElements.item(j);
						usedMessageElements.add(QNameUtil.format(QNameUtil.resolveQNameFromCName(partElement, partElement.getAttribute("element"))));
					}
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("This is a programming error!");
		}
		
		List<String> result = new ArrayList<>(usedMessageElements);
		Collections.sort(result);
		return result;
	}
}