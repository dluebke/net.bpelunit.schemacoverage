package net.bpelunit.schemacoverage.model.xsd;

import java.io.File;

import org.w3c.dom.Element;

public class XMLSchemaFile {

	private String namespace;
	private Element xmlSchemaRootElement;
	private File xmlSchemaFile;
	
	public XMLSchemaFile(String namespace, Element xmlSchemaRootElement, File xmlSchemaFile) {
		super();
		this.namespace = namespace;
		this.xmlSchemaRootElement = xmlSchemaRootElement;
		this.xmlSchemaFile = xmlSchemaFile;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public File getXmlSchemaFile() {
		return xmlSchemaFile;
	}
	
	public Element getXmlSchemaRootElement() {
		return xmlSchemaRootElement;
	}
	
}
