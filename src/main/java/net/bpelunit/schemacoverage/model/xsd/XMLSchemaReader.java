package net.bpelunit.schemacoverage.model.xsd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.xml.XMLUtil;

public class XMLSchemaReader {

	private DocumentBuilder docBuilder;

	public XMLSchemaReader() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			docBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized XMLSchemasContents readXMLSchemas(File... xmlSchemaFile) throws SAXException, IOException {
		
		XMLSchemasContents result = new XMLSchemasContents();
		
		List<XMLSchemaFile> schemaFiles = new ArrayList<>();
		for(File f : xmlSchemaFile) {
			schemaFiles.add(createXMLSchemaFileFromFile(f));
		}
		
		while(schemaFiles.size() > 0) {
			XMLSchemaFile currentSchema = schemaFiles.remove(0);
			Element xsdElement = currentSchema.getXmlSchemaRootElement();
			NodeList xsdImportElements = xsdElement.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "import");
			for (int i = 0; i < xsdImportElements.getLength(); i++) {
				Element importElement = (Element) xsdImportElements.item(i);
				File importFile = new File(currentSchema.getXmlSchemaFile().getParentFile(),
						importElement.getAttribute("schemaLocation"));

				schemaFiles.add(createXMLSchemaFileFromFile(importFile));
				
			}
			result.addSchema(xsdElement);
		}
		
		return result;
	}
	
	private XMLSchemaFile createXMLSchemaFileFromFile(File f) throws SAXException, IOException {
		Element schemaElement = docBuilder.parse(f).getDocumentElement();
		return new XMLSchemaFile(schemaElement.getAttribute("targetNamespace"), schemaElement, f);
	}
	
}
