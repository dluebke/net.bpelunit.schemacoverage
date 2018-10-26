package net.bpelunit.schemacoverage.model.project;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.model.xsd.XMLSchemaReader;
import net.bpelunit.schemacoverage.model.xsd.XMLSchemasContents;

public class XsdProject implements IProject {

	private XMLSchemasContents xmlSchemasContents;
	private String name;
	
	public static XsdProject readXsdProject(File mainSchemaFile) throws SAXException, IOException {
		XsdProject result = new XsdProject();
		result.name = mainSchemaFile.getName();
		
		result.xmlSchemasContents = new XMLSchemaReader().readXMLSchemas(mainSchemaFile);
		
		return result;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Set<String> getConsumerRequestMessages() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getConsumerResponseMessages() {
		return Collections.emptySet();
	}
	
	@Override
	public Set<String> getProviderRequestMessages() {
		return xmlSchemasContents.getSchemaElementsByQName().keySet();
	}
	
	@Override
	public Set<String> getProviderResponseMessages() {
		return Collections.emptySet();
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
}
