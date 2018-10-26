package net.bpelunit.schemacoverage.model.measurement;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.model.project.XsdProject;

public class MeasurementBuilderTest {

	private XsdProject project;
	
	@Before
	public void setUp() throws SAXException, IOException {
		project = XsdProject.readXsdProject(new File("src/test/resources/MeasurementBuilderTest.xsd"));
	}
	
	@Test
	public void testSimpleElement() {
		String qName = "{http://www.example.org/MeasurementBuilderTest}StringElement";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(2, measurementPoints.size());
		List<Object> mpAsStrings = Arrays.asList(measurementPoints.stream().map(x -> x.toString()).toArray());
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MessageUsed:count(/{http://www.example.org/MeasurementBuilderTest}StringElement)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MultipleValues:text(/{http://www.example.org/MeasurementBuilderTest}StringElement)"));
	}
	
	@Test
	public void testBooleanElement() {
		String qName = "{http://www.example.org/MeasurementBuilderTest}BooleanElement";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(3, measurementPoints.size());
		List<Object> mpAsStrings = Arrays.asList(measurementPoints.stream().map(x -> x.toString()).toArray());
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MessageUsed:count(/{http://www.example.org/MeasurementBuilderTest}BooleanElement)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanTrue:text(/{http://www.example.org/MeasurementBuilderTest}BooleanElement)"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanFalse:text(/{http://www.example.org/MeasurementBuilderTest}BooleanElement)"));
	}
	
	@Test
	public void testEnumElement() {
		String qName = "{http://www.example.org/MeasurementBuilderTest}EnumElement";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(measurementPoints.toString(), 3, measurementPoints.size());
		List<Object> mpAsStrings = Arrays.asList(measurementPoints.stream().map(x -> x.toString()).toArray());
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MessageUsed:count(/{http://www.example.org/MeasurementBuilderTest}EnumElement)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("EnumLiteralUsed:text(/{http://www.example.org/MeasurementBuilderTest}EnumElement)[=A]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("EnumLiteralUsed:text(/{http://www.example.org/MeasurementBuilderTest}EnumElement)[=B]"));
	}
	
	@Test
	public void testSubstitutionElement() {
		String qName = "{http://www.example.org/MeasurementBuilderTest}SubstitutionElement";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(measurementPoints.toString(), 7, measurementPoints.size());
		List<Object> mpAsStrings = Arrays.asList(measurementPoints.stream().map(x -> x.toString()).toArray());
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MessageUsed:count(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MandatoryNodeUsed:count(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Parent)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanTrue:text(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Parent)"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanFalse:text(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Parent)"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MandatoryNodeUsed:count(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Substitute)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanTrue:text(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Substitute)"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("BooleanFalse:text(/{http://www.example.org/MeasurementBuilderTest}SubstitutionElement/{http://www.example.org/MeasurementBuilderTest}Substitute)"));
	}
	
	@Test
	public void testInheritanceElement() {
		String qName = "{http://www.example.org/MeasurementBuilderTest}InheritanceElement";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(measurementPoints.toString(), 3, measurementPoints.size());
		List<Object> mpAsStrings = Arrays.asList(measurementPoints.stream().map(x -> x.toString()).toArray());
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MessageUsed:count(/{http://www.example.org/MeasurementBuilderTest}InheritanceElement)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MandatoryNodeUsed:count(/{http://www.example.org/MeasurementBuilderTest}InheritanceElement/{http://www.example.org/MeasurementBuilderTest}X)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MultipleValues:text(/{http://www.example.org/MeasurementBuilderTest}InheritanceElement/{http://www.example.org/MeasurementBuilderTest}X)"));
	}

	@Test
	public void testNestedInlineTypes() throws Exception {
		project = XsdProject.readXsdProject(new File("src/test/resources/SchemaWithInlineType.xsd"));
		
		String qName = "{http://www.example.org/SchemaWithNestedInlineType}A";
		Context<Element> ctx = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, qName, project.getSchemaElementByQName(qName));
		new MeasurementPointBuilder().buildMeasurements(ctx, project);
		
		List<MeasurementPoint> measurementPoints = ctx.getMeasurementPoints();
		assertEquals(measurementPoints.toString(), 4, measurementPoints.size());
		
		
		String mpAsStrings = measurementPoints.toString();
		assertTrue(measurementPoints.toString(), mpAsStrings .contains("MessageUsed:count(/{http://www.example.org/SchemaWithNestedInlineType}A)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MandatoryNodeUsed:count(/{http://www.example.org/SchemaWithNestedInlineType}A/{http://www.example.org/SchemaWithNestedInlineType}B)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MandatoryNodeUsed:count(/{http://www.example.org/SchemaWithNestedInlineType}A/{http://www.example.org/SchemaWithNestedInlineType}B/{http://www.example.org/SchemaWithNestedInlineType}B)[=1]"));
		assertTrue(measurementPoints.toString(), mpAsStrings.contains("MultipleValues:text(/{http://www.example.org/SchemaWithNestedInlineType}A/{http://www.example.org/SchemaWithNestedInlineType}B/{http://www.example.org/SchemaWithNestedInlineType}B)"));
	}
}
