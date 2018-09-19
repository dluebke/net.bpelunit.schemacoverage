package net.bpelunit.schemacoverage.messagesource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.xml.XMLUtil;

public class BptsLogFileMessageSource implements IMessageSource {

	private final List<Element> inboundMessageInstances = new ArrayList<>();
	private final List<Element> outboundMessageInstances = new ArrayList<>();

	public static BptsLogFileMessageSource readBptsLogFiles(String... bptsLogFiles) throws ParserConfigurationException, SAXException, IOException {
		BptsLogFileMessageSource result = new BptsLogFileMessageSource();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		for(String fileName : bptsLogFiles) {
			Document d = docBuilder.parse(new File(fileName));
			NodeList tempLiteralDataElements = d.getElementsByTagName("literalData");
			for(int x = 0; x < tempLiteralDataElements.getLength(); x++) {
				Element literalDataElement = XMLUtil.getFirstChildElement(tempLiteralDataElements.item(x));
				String direction = literalDataElement.getParentNode().getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
				if("Send Data Package".equals(direction)) {
					result.inboundMessageInstances.add(literalDataElement);
				} else if("Receive Data Package".equals(direction)) {
					result.outboundMessageInstances.add(literalDataElement);
				} else {
					throw new RuntimeException("Error: direction is unknown: " + direction);
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
		return outboundMessageInstances;
	}
	
}
