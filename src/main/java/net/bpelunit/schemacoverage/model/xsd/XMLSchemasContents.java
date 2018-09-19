package net.bpelunit.schemacoverage.model.xsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.util.SetMap;
import net.bpelunit.schemacoverage.util.StringUtil;
import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class XMLSchemasContents {

	private Map<String, Element> schemaTypesByQName = new HashMap<>();
	private Map<String, Set<String>> directSubtypesByType = new HashMap<>();
	private Map<String, Element> schemaElementsByQName = new HashMap<>();
	private Map<String, Element> schemaElementForNamespace = new HashMap<>();
	private SetMap<String, String> allSubtypesByType = new SetMap<>();
	private SetMap<String, String> directSubstitutionsByElement = new SetMap<>();
	private SetMap<String, String> allSubstitutionsByElement = new SetMap<>();
	private SetMap<String, String> allSuperTypesByType = new SetMap<>();

	public Map<String, Element> getSchemaElementsByQName() {
		return schemaElementsByQName;
	}
	
	public Map<String, Element> getSchemaTypesByQName() {
		return schemaTypesByQName;
	}
	
	public SetMap<String, String> getAllSubstitutionsByElement() {
		return allSubstitutionsByElement;
	}
	
	public SetMap<String, String> getAllSubtypesByType() {
		return allSubtypesByType;
	}
	
	public SetMap<String, String> getDirectSubstitutionsByElement() {
		return directSubstitutionsByElement;
	}
	
	public Map<String, Set<String>> getDirectSubtypesByType() {
		return directSubtypesByType;
	}
	
	public SetMap<String, String> getAllSupertypesByType() {
		return allSuperTypesByType;
	}
	
	public void addSchema(Element xsdElement) {
		String targetNamespace = xsdElement.getAttribute("targetNamespace");
		
		schemaElementForNamespace.put(targetNamespace, xsdElement);
		
		NodeList topLevelChildNodes = xsdElement.getChildNodes();
		for (int i = 0; i < topLevelChildNodes.getLength(); i++) {
			Node n = topLevelChildNodes.item(i);

			if (n.getNodeType() == Node.ELEMENT_NODE && XMLUtil.NAMESPACE_XMLSCHEMA.equals(n.getNamespaceURI())) {
				Element e = (Element) n;
				if ("complexType".equals(e.getLocalName()) || "simpleType".equals(e.getLocalName())) {
					String typeName = e.getAttribute("name");
					String typeNameAsFormatedQName = QNameUtil.format(targetNamespace, typeName);
					schemaTypesByQName.put(typeNameAsFormatedQName, e);

					NodeList extensions = e.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "extension");
					if (extensions.getLength() > 0) {
						Element extension = (Element) extensions.item(0);
						String baseAsCName = extension.getAttribute("base");
						String baseAsFormatedQName = QNameUtil.format(QNameUtil.resolveQNameFromCName(extension, baseAsCName));

						Set<String> directExtensions = directSubtypesByType.get(baseAsFormatedQName);
						if(directExtensions == null) {
							directExtensions = new HashSet<>();
							directSubtypesByType.put(baseAsFormatedQName, directExtensions);
						}
						directExtensions.add(typeNameAsFormatedQName);
					}
				}

				if ("element".equals(e.getLocalName())) {
					String elementName = e.getAttribute("name");
					String elementFormatedQName = QNameUtil.format(targetNamespace, elementName);
					schemaElementsByQName.put(elementFormatedQName, e);

					String substitutionGroup = e.getAttribute("substitutionGroup");
					if (!StringUtil.isEmpty(substitutionGroup)) {
						String formatedSubstitutionElementQName = QNameUtil
								.format(QNameUtil.resolveQNameFromCName(e, substitutionGroup));
						directSubstitutionsByElement.get(formatedSubstitutionElementQName).add(elementFormatedQName);
					}
				}
			}
		}

		resolveTransitiveSubtypes();
		resolveTransitiveSupertypes();
		resolveSubstitutionGroups();
	}

	private void resolveTransitiveSupertypes() {
		for(String superType : allSubtypesByType.keySet()) {
			for(String subType : allSubtypesByType.get(superType)) {
				allSuperTypesByType.get(subType).add(superType);
			}
		}
	}

	private void resolveSubstitutionGroups() {
		for (String elementWithSubstitutions : directSubstitutionsByElement.keySet()) {
			if (!StringUtil.isEmpty(elementWithSubstitutions)) {
				Set<String> allSubstitutions = allSubstitutionsByElement.get(elementWithSubstitutions);

				if (directSubstitutionsByElement.containsKey(elementWithSubstitutions)) {
					List<String> substitutionsToProcess = new ArrayList<>(
							directSubstitutionsByElement.get(elementWithSubstitutions));
					while (substitutionsToProcess.size() > 0) {
						String substitution = substitutionsToProcess.remove(0);

						allSubstitutions.add(substitution);
						Set<String> nextSubstitutions = directSubstitutionsByElement.containsKey(substitution)
								? directSubstitutionsByElement.get(substitution) : Collections.emptySet();
						if (nextSubstitutions != null) {
							substitutionsToProcess.addAll(nextSubstitutions);
						}
					}
				}
			}
		}
	}

	private void resolveTransitiveSubtypes() {
		for (String typeWithSubTypes : directSubtypesByType.keySet()) {
			if (!StringUtil.isEmpty(typeWithSubTypes)) {
				Set<String> allSubTypes = allSubtypesByType.get(typeWithSubTypes);

				if (directSubtypesByType.containsKey(typeWithSubTypes)) {
					List<String> subTypesToProcess = new ArrayList<>(directSubtypesByType.get(typeWithSubTypes));
					while (subTypesToProcess.size() > 0) {
						String subtype = subTypesToProcess.remove(0);

						allSubTypes.add(subtype);
						Set<String> nextSubtypes = directSubtypesByType.containsKey(subtype) ? directSubtypesByType.get(subtype) : Collections.emptySet();
						if (nextSubtypes != null) {
							subTypesToProcess.addAll(nextSubtypes);
						}
					}
				}
			}
		}
	}

	public Map<String, Element> getSchemasForNamespace() {
		return schemaElementForNamespace;
	}

}
