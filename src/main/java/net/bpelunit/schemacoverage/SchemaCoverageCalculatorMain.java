package net.bpelunit.schemacoverage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.bpelunit.schemacoverage.messagesource.BptsLogFileMessageSource;
import net.bpelunit.schemacoverage.messagesource.IMessageSource;
import net.bpelunit.schemacoverage.messagesource.XmlFileMessageSource;
import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.ContextType;
import net.bpelunit.schemacoverage.model.measurement.MeasurementBuilder;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointType;
import net.bpelunit.schemacoverage.model.project.BpelProject;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.model.project.XsdProject;
import net.bpelunit.schemacoverage.util.CounterMap;
import net.bpelunit.schemacoverage.xml.QNameUtil;

/*
 * Lessons Learned:
 * Only follow complex type inheritance
 * Good schema design helps here
 * Indirect indicator of schema precision
 * How to handle recursion
 * How to handle root of substitution groups
 */

/* TODOs
 * Substitution Groups
 * Type Inheritance
 * 
 */

public class SchemaCoverageCalculatorMain {

	private static final String COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA = "xmlschema";
	private static final String COMMANDLINEOPTION_PROJECTTYPE_BPEL = "bpel";
	private static final String COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG = "bpelunitxmllog";
	private static final String COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES = "d";
	private static final String COMMANDLINEOPTION_OUTPUT_CSV = "csv";
	private static final String COMMANDLINEOPTION_IGNORE_INBOUND = "ignoreinbound";
	private static final String COMMANDLINEOPTION_IGNORE_OUTBOUND = "ignoreoutbound";
	private static final String COMMANDLINEOPTION_IGNORE_MESSAGES = "ignoremessage";
	
	private static final String DEFAULT_OUTPUT_FILENAME = "report.csv";
	
	public static void main(String[] args) throws Exception {
		new SchemaCoverageCalculatorMain().run(args);
	}

	private void run(String[] args) throws ParserConfigurationException, SAXException, IOException, ParseException {
		Options options = createOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		if(cmd.hasOption(COMMANDLINEOPTION_PROJECTTYPE_BPEL) && cmd.hasOption(COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA)) {
			System.err.println(String.format("-%s and -%s are mutually exclusive!", COMMANDLINEOPTION_PROJECTTYPE_BPEL, COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA));
			printHelpAndExit(options);
		}
		
		if(cmd.hasOption(COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG) && cmd.hasOption(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES)) {
			System.err.println(String.format("-%s and -%s are mutually exclusive!", COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG, COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES));
			printHelpAndExit(options);
		}
		
		IProject project = null;
		if(cmd.hasOption(COMMANDLINEOPTION_PROJECTTYPE_BPEL)) {
			String bpelFileName = cmd.getOptionValue(COMMANDLINEOPTION_PROJECTTYPE_BPEL);
			File bpelFile = new File(bpelFileName);
			
			// read BPEL process and all associated WSDLs and XSDs
			project = BpelProject.readBpelProject(bpelFile);
		} else if(cmd.hasOption(COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA)){
			String xsdFileName = cmd.getOptionValue(COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA);
			File xsdFile = new File(xsdFileName);
			
			// read BPEL process and all associated WSDLs and XSDs
			project = XsdProject.readXsdProject(xsdFile);
		} else {
			System.err.println(String.format("You must specify a project type with -%s or -%s!", COMMANDLINEOPTION_PROJECTTYPE_BPEL, COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA));
			printHelpAndExit(options);
		}
		
		IMessageSource messageSource = null;
		if(cmd.hasOption(COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG)) {
			messageSource = BptsLogFileMessageSource.readBptsLogFiles(cmd.getOptionValues(COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG));
		} else if(cmd.hasOption(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES)) {
			messageSource = XmlFileMessageSource.readXmlFiles(cmd.getOptionValues(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES));
		} else {
			System.err.println(String.format("You must specify a message source with -%s or -%s!", COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG, COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES));
			printHelpAndExit(options);
		}
		
		String csvFileName = DEFAULT_OUTPUT_FILENAME;
		if(cmd.hasOption(COMMANDLINEOPTION_OUTPUT_CSV)) {
			csvFileName = cmd.getOptionValue(COMMANDLINEOPTION_OUTPUT_CSV);
		}
		
		// resolve used messages
		Set<String> inboundMessages = project.getInboundMessageElements();
		Set<String> outboundMessages = project.getOutboundMessageElements();
		System.out.println(
				"Found " + 
						inboundMessages.size() + 
						" inbound message type(s) and " +
						outboundMessages.size() + 
						" outbound message type(s)"
				);

		// Ignore messages specified by user
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_INBOUND)) {
			String[] ignore = cmd.getOptionValues(COMMANDLINEOPTION_IGNORE_INBOUND);
			inboundMessages.removeAll(Arrays.asList(ignore));
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_OUTBOUND)) {
			String[] ignore = cmd.getOptionValues(COMMANDLINEOPTION_IGNORE_OUTBOUND);
			outboundMessages.removeAll(Arrays.asList(ignore));
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_MESSAGES)) {
			String[] ignore = cmd.getOptionValues(COMMANDLINEOPTION_IGNORE_MESSAGES);
			inboundMessages.removeAll(Arrays.asList(ignore));
			outboundMessages.removeAll(Arrays.asList(ignore));
		}
		System.out.println(
				"Using " + 
						inboundMessages.size() + 
						" inbound message type(s) and " +
						outboundMessages.size() + 
						" outbound message type(s)"
				);

		
		
		Map<String, Context<Element>> inboundContexts = new HashMap<>();
		List<MeasurementPoint> measurementPoints = new ArrayList<>();
		MeasurementBuilder mb = new MeasurementBuilder();
		for(String m : inboundMessages) {
			Context<Element> ctx = new Context<Element>(ContextType.INBOUND_MESSAGE, m, project.getSchemaElementByQName(m));
			inboundContexts.put(m, ctx);
			mb.buildMeasurements(ctx, project);
			measurementPoints.addAll(ctx.getMeasurementPoints());
		}
		Map<String, Context<Element>> outboundContexts = new HashMap<>();
		for(String m : outboundMessages) {
			Context<Element> ctx = new Context<Element>(ContextType.OUTBOUND_MESSAGE, m, project.getSchemaElementByQName(m));
			outboundContexts.put(m, ctx);
			mb.buildMeasurements(ctx, project);
			measurementPoints.addAll(ctx.getMeasurementPoints());
		}
		
		// TODO read customizing for coverage criteria
		
		
		System.out.println("Found " + measurementPoints.size() + " measurement points");
		CounterMap<MeasurementPointType> counters = new CounterMap<>();
		for(MeasurementPoint p : measurementPoints) {
			counters.inc(p.getMeasurementPointType());
		}
		for(Entry<MeasurementPointType, Integer> entry : counters.entrySet()) {
			System.out.println(" | " + entry.getKey() + " := " + entry.getValue());
		}
		
		// read BPELUnit log
		System.out.println(
			"Found " + 
			messageSource.getInboundMessageInstances().size() + 
			" inbound message instance(s) and " +
			messageSource.getOutboundMessageInstances().size() + 
			" outbound message instance(s)"
		);
		
		// calculate coverage
		for(Element e : messageSource.getInboundMessageInstances()) {
			processMeasurementPoints(inboundContexts, e);
		}
		for(Element e : messageSource.getOutboundMessageInstances()) {
			processMeasurementPoints(outboundContexts, e);
		}
		
		// write report
		Map<String, Context<Element>> allContexts = new HashMap<>();
		allContexts.putAll(inboundContexts);
		allContexts.putAll(outboundContexts);
		try (FileWriter out = new FileWriter(csvFileName)) {
			
			int fulfilled = 0;
			String headerLine = String.join("\t",
					"Context Type", 
					"Context Name",
					"Measurement Point", 
					"Path Expression", 
					"Expected Value", 
					"Encountered Values", 
					"Statisfied"
					);
			out.write(headerLine); 
			out.write("\n");
			for(Context<Element> ctx : allContexts.values()) {
				for(MeasurementPoint m : ctx.getMeasurementPoints()) {
					boolean measurementFulfilled = m.isFulfilled();
					String resultLine = String.join("\t",
							ctx.getType().toString(), 
							ctx.getName(),
							m.getMeasurementPointType().toString(), 
							m.getPath().toString(), 
							m.getExpectedValue() != null ? m.getExpectedValue() : "", 
							String.join(",", m.getExtractedValues()), 
							measurementFulfilled+ ""
						);
					out.write(resultLine); 
					out.write("\n");
					if(measurementFulfilled) {
						fulfilled++;
					}
				}
			}
			System.out.println("Coverage: " + fulfilled + "/" + measurementPoints.size() + " = " + (fulfilled * 100 / measurementPoints.size()) + "%");
		}
	}

	private void printHelpAndExit(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("schemacoveragecalculator", options);
		System.exit(1);
	}

	private Options createOptions() {
		Options options = new Options();
		options.addOption(COMMANDLINEOPTION_PROJECTTYPE_BPEL, true, "Read and evaluate a specified BPEL File");
		options.addOption(COMMANDLINEOPTION_PROJECTTYPE_XMLSCHEMA, true, "Read and evaluate a specified XML Schema");
		options.addOption(COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG, true, "Read test data from BPELUnit XML log file (use bpelunit -x log.xml)");
		options.addOption(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES, true, "Read test data from XML files located in a specified directory");
		options.addOption(COMMANDLINEOPTION_OUTPUT_CSV, true, "CSV File to write results to, default: " + DEFAULT_OUTPUT_FILENAME);
		options.addOption(COMMANDLINEOPTION_IGNORE_INBOUND, true, "Comma-separated QNames of inbound messages to ignore in form of {namespace}localname");
		options.addOption(COMMANDLINEOPTION_IGNORE_OUTBOUND, true, "Comma-separated QNames of outbound messages to ignore in form of {namespace}localname");
		options.addOption(COMMANDLINEOPTION_IGNORE_MESSAGES, true, "Comma-separated QNames of messages to ignore inbound and outbound in form of {namespace}localname");
		return options;
	}

	private void processMeasurementPoints(Map<String, Context<Element>> contextsByMessage, Element payloadElement) {
		String messageElementQName = QNameUtil.format(payloadElement.getNamespaceURI(), payloadElement.getLocalName());
		Context<Element> ctx = contextsByMessage.get(messageElementQName);
		if(ctx != null) {
			for(MeasurementPoint m : ctx.getMeasurementPoints()) {
				List<String> values = m.getPath().evaluate(payloadElement);
				if(values.size() > 0) {
					for(String value : values) {
						m.addExtractedValue(value);
					}
				}
			}
		}
	}
	
}
