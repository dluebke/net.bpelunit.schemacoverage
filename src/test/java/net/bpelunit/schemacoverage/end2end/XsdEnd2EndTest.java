package net.bpelunit.schemacoverage.end2end;

import org.junit.Test;

import net.bpelunit.schemacoverage.SchemaCoverageCalculatorMain;

public class XsdEnd2EndTest {

	@Test
	public void testSchema1_XML() throws Exception {
		String[] args = new String[]{
			"-xmlschema",
			"src/test/resources/end2end/schema1.xsd",
			"-d",
			"src/test/resources/end2end/schema1",
			"-csv",
			"target/schema1_xml.csv"
		};
		
		SchemaCoverageCalculatorMain.main(args);
	}
	
	@Test
	public void testSchema1_BPELUnit() throws Exception {
		String[] args = new String[]{
				"-xmlschema",
				"src/test/resources/end2end/schema1.xsd",
				"-bpelunitxmllog",
				"src/test/resources/end2end/schema1.log.xml",
				"-csv",
				"target/schema1_bpts.csv"
		};
		
		SchemaCoverageCalculatorMain.main(args);
	}
	
	@Test
	public void testSchema2_XML() throws Exception {
		String[] args = new String[]{
				"-xmlschema",
				"src/test/resources/end2end/schema2.xsd",
				"-d",
				"src/test/resources/end2end/schema2",
				"-csv",
				"target/schema2_xml.csv"
		};
		
		SchemaCoverageCalculatorMain.main(args);
	}

}
