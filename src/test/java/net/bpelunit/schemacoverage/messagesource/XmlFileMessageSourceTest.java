package net.bpelunit.schemacoverage.messagesource;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Element;

public class XmlFileMessageSourceTest {

	@Test
	public void testReadXmlFiles() throws Exception {
		XmlFileMessageSource xmlFileMessageSource = XmlFileMessageSource.readXmlFiles("src/test/resources/net/bpelunit/schemacoverage/messagesource/xmlfiles/subdirectory");
		assertEquals(0, xmlFileMessageSource.getOutboundMessageInstances().size());
		
		List<Element> inboundMessageInstances = xmlFileMessageSource.getInboundMessageInstances();
		assertEquals(1, inboundMessageInstances.size());
		
		assertTrue(containsElement(inboundMessageInstances, "c"));
	}
	
	@Test
	public void testReadXmlFilesRecursively() throws Exception {
		XmlFileMessageSource xmlFileMessageSource = XmlFileMessageSource.readXmlFiles("src/test/resources/net/bpelunit/schemacoverage/messagesource/xmlfiles");
		assertEquals(0, xmlFileMessageSource.getOutboundMessageInstances().size());
		
		List<Element> inboundMessageInstances = xmlFileMessageSource.getInboundMessageInstances();
		assertEquals(3, inboundMessageInstances.size());
		
		assertTrue(containsElement(inboundMessageInstances, "a"));
		assertTrue(containsElement(inboundMessageInstances, "b"));
		assertTrue(containsElement(inboundMessageInstances, "c"));
	}

	private boolean containsElement(List<Element> elements, String localName) {
		for(Element e : elements) {
			if(e.getLocalName().equals(localName)) {
				return true;
			}
		}
		return false;
	}

}
