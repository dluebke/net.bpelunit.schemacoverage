package net.bpelunit.schemacoverage.model.project;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.Test;

public class XsdProjectTest {

	@Test
	public void testSimpleSchema() throws Exception {
		File f = new File("src/test/resources/SimpleSchema.xsd");
		XsdProject project = XsdProject.readXsdProject(f);
		
		assertNotNull(project);
		assertNotNull(project.getSchemaForNamespace("http://www.example.org/SimpleSchema"));
		
		Set<String> inboundMessageElements = project.getProviderRequestMessages();
		assertEquals(1, inboundMessageElements.size());
		assertTrue(inboundMessageElements.contains("{http://www.example.org/SimpleSchema}A"));
		
		assertEquals(0, project.getProviderResponseMessages().size());
		assertEquals(0, project.getConsumerRequestMessages().size());
		assertEquals(0, project.getConsumerResponseMessages().size());
		
		assertNotNull(project.getSchemaElementByQName("{http://www.example.org/SimpleSchema}A"));
		assertNotNull(project.getSchemaTypeByQName("{http://www.example.org/SimpleSchema}tComplexType"));
		assertNotNull(project.getSchemaTypeByQName("{http://www.example.org/SimpleSchema}tSimpleType"));
	}
	
	@Test
	public void testSchemaWithSubstitutionGroup() throws Exception {
		File f = new File("src/test/resources/SchemaWithSubstitutionGroup.xsd");
		XsdProject project = XsdProject.readXsdProject(f);
		
		assertNotNull(project);
		assertNotNull(project.getSchemaForNamespace("http://www.example.org/SchemaWithSubstitutionGroup"));
		
		Set<String> inboundMessageElements = project.getProviderRequestMessages();
		assertEquals(3, inboundMessageElements.size());
		assertTrue(inboundMessageElements.contains("{http://www.example.org/SchemaWithSubstitutionGroup}A"));
		assertTrue(inboundMessageElements.contains("{http://www.example.org/SchemaWithSubstitutionGroup}B"));
		assertTrue(inboundMessageElements.contains("{http://www.example.org/SchemaWithSubstitutionGroup}C"));
		
		assertEquals(0, project.getProviderResponseMessages().size());
		assertEquals(0, project.getConsumerRequestMessages().size());
		assertEquals(0, project.getConsumerResponseMessages().size());
		
		assertNotNull(project.getSchemaElementByQName("{http://www.example.org/SchemaWithSubstitutionGroup}A"));
		assertNotNull(project.getSchemaElementByQName("{http://www.example.org/SchemaWithSubstitutionGroup}B"));
		assertNotNull(project.getSchemaElementByQName("{http://www.example.org/SchemaWithSubstitutionGroup}C"));
		
		Set<String> substitutionsForB = project.getAllSubstitutionsForElement("{http://www.example.org/SchemaWithSubstitutionGroup}B");
		assertEquals(1, substitutionsForB.size());
		assertEquals("{http://www.example.org/SchemaWithSubstitutionGroup}C", substitutionsForB.iterator().next());
	}
	
	@Test
	public void testSchemaWithTypeInheritance() throws Exception {
		File f = new File("src/test/resources/SchemaWithTypeInheritance.xsd");
		XsdProject project = XsdProject.readXsdProject(f);
		
		assertNotNull(project);
		assertNotNull(project.getSchemaForNamespace("http://www.example.org/SchemaWithTypeInheritance"));
		
		Set<String> inboundMessageElements = project.getProviderRequestMessages();
		assertEquals(1, inboundMessageElements.size());
		assertTrue(inboundMessageElements.contains("{http://www.example.org/SchemaWithTypeInheritance}A"));
		
		assertEquals(0, project.getProviderResponseMessages().size());
		assertEquals(0, project.getConsumerRequestMessages().size());
		assertEquals(0, project.getConsumerResponseMessages().size());
		
		assertNotNull(project.getSchemaElementByQName("{http://www.example.org/SchemaWithTypeInheritance}A"));

		assertNotNull(project.getSchemaTypeByQName("{http://www.example.org/SchemaWithTypeInheritance}tC"));
		assertNotNull(project.getSchemaTypeByQName("{http://www.example.org/SchemaWithTypeInheritance}tD"));

		assertEquals(1, project.getAllSubtypesForType("{http://www.example.org/SchemaWithTypeInheritance}tC").size());
		assertTrue(project.getAllSubtypesForType("{http://www.example.org/SchemaWithTypeInheritance}tC").contains("{http://www.example.org/SchemaWithTypeInheritance}tD"));
	}
	
}
