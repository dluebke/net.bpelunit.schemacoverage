package net.bpelunit.schemacoverage.report.html;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.ContextType;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointType;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.model.xsd.XMLSchemasContents;
import net.bpelunit.schemacoverage.simplepath.function.INodeFunction;
import net.bpelunit.schemacoverage.simplepath.function.TextValueFunction;
import net.bpelunit.schemacoverage.simplepath.selector.ElementSelector;
import net.bpelunit.schemacoverage.simplepath.selector.RootSelector;

public class HtmlCoverageWriterTest {

	private static final class DummyProject implements IProject {
		@Override
		public XMLSchemasContents getXMLSchemaContents() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Element getSchemaTypeByQName(String qName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Element getSchemaForNamespace(String namespace) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Element getSchemaElementByQName(String qName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return "ProjectName";
		}

		@Override
		public Element getAttributeElementByQName(String formattedQName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getAllSubtypesForType(String formattedQName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getAllSubstitutionsForElement(String element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getProviderRequestMessages() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getProviderResponseMessages() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getConsumerRequestMessages() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getConsumerResponseMessages() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	public void test() throws IOException {
		HtmlCoverageWriter writer = new HtmlCoverageWriter(new File("target"));
		
		Map<String, Context<Element>> allContexts = new HashMap<>();
		Context<Element> c;
		INodeFunction path;
		c = new Context<Element>(ContextType.CONSUMER_REQUEST_MESSAGE, "Request", null);
		allContexts.put(c.getName(), c);
		RootSelector rootSelector;
		rootSelector = new RootSelector();
		rootSelector.appendSelector(new ElementSelector("a"));
		path = new TextValueFunction(rootSelector);
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanFalse, path , "abc"));
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanTrue, path , "abc"));
		
		rootSelector = new RootSelector();
		rootSelector.appendSelector(new ElementSelector("a")).appendSelector(new ElementSelector("b"));
		path = new TextValueFunction(rootSelector);
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanFalse, path , "abc"));
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanTrue, path , "abc"));
		
		rootSelector = new RootSelector();
		rootSelector.appendSelector(new ElementSelector("a")).appendSelector(new ElementSelector("c"));
		path = new TextValueFunction(rootSelector);
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanFalse, path , "abc"));
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanTrue, path , "abc"));
		
		rootSelector = new RootSelector();
		rootSelector.appendSelector(new ElementSelector("a")).appendSelector(new ElementSelector("b")).appendSelector(new ElementSelector("d"));
		path = new TextValueFunction(rootSelector);
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.EnumLiteralUsed, path , "Male"));
		c.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.EnumLiteralUsed, path , "Female"));
		
		IProject project = new DummyProject();
		writer.writeReport(project, allContexts);
	}

}
