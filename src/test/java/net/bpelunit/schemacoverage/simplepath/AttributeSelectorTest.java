package net.bpelunit.schemacoverage.simplepath;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.AttributeSelector;
import net.bpelunit.schemacoverage.simplepath.selector.ElementSelector;

public class AttributeSelectorTest {

	private static Element sampleXml;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		sampleXml = db.parse(new ByteArrayInputStream("<a c=\"c\"><b><c/><d/></b><e><f/></e></a>".getBytes("UTF-8"))).getDocumentElement();
	}
	
	@Test
	public void testToString_ElementWithoutNamespace() {
		ElementSelector s1 = new ElementSelector("a");
		s1.appendSelector(new AttributeSelector("c"));
		assertEquals("/a/@c", s1.toString());
	}
	
	@Test
	public void testToString_ElementWithNamespace() {
		ElementSelector s1 = new ElementSelector("a");
		s1.appendSelector(new AttributeSelector("urn", "c"));
		assertEquals("/a/@{urn}c", s1.toString());
	}

	@Test(expected=RuntimeException.class)
	public void testAppend_CannotAppendItself() {
		AttributeSelector s1 = new AttributeSelector("a");
		s1.appendSelector(s1);
	}
	
	@Test
	public void testEvaluate() throws Exception {
		ElementSelector s1 = new ElementSelector("a");
		s1.appendSelector(new AttributeSelector("c"));
		
		ElementSelector s2 = new ElementSelector("a");
		s2.appendSelector(new AttributeSelector("b"));
		
		List<Node> result = s1.evaluate(sampleXml);
		assertEquals(1, result.size());
		assertEquals("c", result.get(0).getLocalName());
		
		result = s2.evaluate(sampleXml);
		assertEquals(0, result.size());
	}
}
