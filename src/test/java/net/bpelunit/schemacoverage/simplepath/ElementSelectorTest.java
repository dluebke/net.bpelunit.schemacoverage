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

import net.bpelunit.schemacoverage.simplepath.selector.ElementSelector;

public class ElementSelectorTest {

private static Element sampleXml;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		sampleXml = db.parse(new ByteArrayInputStream("<a><b><c/><d/></b><e><f/></e></a>".getBytes("UTF-8"))).getDocumentElement();
	}
	
	@Test
	public void testToString_ElementWithoutNamespace() {
		ElementSelector s1 = new ElementSelector("a");
		assertEquals("/a", s1.toString());
	}
	
	@Test
	public void testToString_ElementWithNamespace() {
		ElementSelector s1 = new ElementSelector("urn", "a");
		assertEquals("/{urn}a", s1.toString());
	}

	@Test
	public void testToString_ElementWithNext() {
		ElementSelector s1 = new ElementSelector("a");
		ElementSelector s2 = new ElementSelector("b");
		s1.appendSelector(s2);
		assertEquals("/a/b", s1.toString());
	}
	
	@Test(expected=RuntimeException.class)
	public void testAppend_CannotAppendItself() {
		ElementSelector s1 = new ElementSelector("a");
		s1.appendSelector(s1);
	}
	
	@Test
	public void testEvaluate_RootElement() throws Exception {
		ElementSelector s1 = new ElementSelector("a");
		ElementSelector s2 = new ElementSelector("b");
		
		List<Node> result = s1.evaluate(sampleXml);
		assertEquals(1, result.size());
		assertEquals("a", result.get(0).getLocalName());
		
		result = s2.evaluate(sampleXml);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testEvaluate_ChildElement() throws Exception {
		ElementSelector s1 = new ElementSelector("a");
		s1.appendSelector(new ElementSelector("b"));
		
		ElementSelector s2 = new ElementSelector("a");
		s2.appendSelector(new ElementSelector("c"));
		
		List<Node> result = s1.evaluate(sampleXml);
		assertEquals(1, result.size());
		assertEquals("b", result.get(0).getLocalName());
		
		result = s2.evaluate(sampleXml);
		assertEquals(0, result.size());
	}
}
