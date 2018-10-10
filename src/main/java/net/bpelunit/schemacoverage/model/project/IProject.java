package net.bpelunit.schemacoverage.model.project;

import java.util.Set;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.xsd.XMLSchemasContents;

public interface IProject {

	Set<String> getInboundMessageElements();

	Set<String> getOutboundMessageElements();

	Element getSchemaElementByQName(String qName);

	Element getSchemaForNamespace(String namespace);

	Element getSchemaTypeByQName(String qName);

	Set<String> getAllSubstitutionsForElement(String element);

	Set<String> getAllSubtypesForType(String formattedQName);

	XMLSchemasContents getXMLSchemaContents();

	Element getAttributeElementByQName(String formattedQName);

	String getName();
	
}
