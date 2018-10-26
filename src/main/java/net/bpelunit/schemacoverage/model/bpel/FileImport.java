package net.bpelunit.schemacoverage.model.bpel;

import java.io.File;

import org.w3c.dom.Element;

public class FileImport {
	public FileImport(File importFile, Element xsdSchemaElement) {
		this.importFile = importFile;
		this.importElement = xsdSchemaElement;
	}

	File importFile;
	Element importElement;
}