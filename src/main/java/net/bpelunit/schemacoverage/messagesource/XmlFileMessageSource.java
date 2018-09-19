package net.bpelunit.schemacoverage.messagesource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlFileMessageSource implements IMessageSource {

	private final List<Element> inboundMessageInstances = new ArrayList<>();

	public static XmlFileMessageSource readXmlFiles(String... directories) throws ParserConfigurationException, SAXException, IOException {
		XmlFileMessageSource result = new XmlFileMessageSource();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();

		List<String> allXmlFiles = new ArrayList<>();
		for(String dir : directories) {
			allXmlFiles.addAll(gatherAllXmlFiles(dir));
		}
		
		for(String fileName : allXmlFiles) {
			Document d = docBuilder.parse(new File(fileName));
			result.inboundMessageInstances.add(d.getDocumentElement());
		}
		return result;
	}
	
	private static Collection<? extends String> gatherAllXmlFiles(String directory) {
		List<String> result = new ArrayList<>();
		if(directory != null) {
			File dir = new File(directory);
			File[] children = dir.listFiles();

			if(children != null) {
				for(File f : children) {
					if(f.isFile() && f.getName().endsWith(".xml")) {
						result.add(f.getAbsolutePath());
					} else if(f.isDirectory()) {
						result.addAll(gatherAllXmlFiles(f.getAbsolutePath()));
					}
				}
			}
		}
		
		return result;
	}

	@Override
	public List<Element> getInboundMessageInstances() {
		return inboundMessageInstances;
	}
	
	@Override
	public List<Element> getOutboundMessageInstances() {
		return Collections.emptyList();
	}

}
