package net.bpelunit.schemacoverage.model.project;

import java.io.File;

import org.w3c.dom.Element;

public class FileImport {
	public FileImport(File importFile, Element xsdSchemaElement) {
		this.importFile = importFile;
		this.importElement = xsdSchemaElement;
	}

	File importFile;
	Element importElement;
	
	public Element getImportElement() {
		return importElement;
	}
	
	public File getImportFile() {
		return importFile;
	}
}