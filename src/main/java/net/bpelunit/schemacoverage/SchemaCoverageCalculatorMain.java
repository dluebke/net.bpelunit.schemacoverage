package net.bpelunit.schemacoverage;

import java.io.File;
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
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointBuilder;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointType;
import net.bpelunit.schemacoverage.model.project.BpelProject;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.model.project.XsdProject;
import net.bpelunit.schemacoverage.report.CSVWriter;
import net.bpelunit.schemacoverage.report.IReportWriter;
import net.bpelunit.schemacoverage.report.html.HtmlCoverageWriter;
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
	private static final String COMMANDLINEOPTION_OUTPUT_HTML = "html";
	private static final String COMMANDLINEOPTION_IGNORE_INBOUND = "ignoreinbound";
	private static final String COMMANDLINEOPTION_IGNORE_OUTBOUND = "ignoreoutbound";
	private static final String COMMANDLINEOPTION_IGNORE_ALLINBOUND = "ignoreallinbound";
	private static final String COMMANDLINEOPTION_IGNORE_ALLOUTBOUND = "ignorealloutbound";
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
			messageSource = BptsLogFileMessageSource.readBptsLogFiles(cmd.getOptionValue(COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG).split(","));
		} else if(cmd.hasOption(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES)) {
			messageSource = XmlFileMessageSource.readXmlFiles(cmd.getOptionValue(COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES).split(","));
		} else {
			System.err.println(String.format("You must specify a message source with -%s or -%s!", COMMANDLINEOPTION_MESSAGESOURCE_BPELUNITXMLLOG, COMMANDLINEOPTION_MESSAGESOURCE_XMLFILES));
			printHelpAndExit(options);
		}
		
		String csvFileName = DEFAULT_OUTPUT_FILENAME;
		if(cmd.hasOption(COMMANDLINEOPTION_OUTPUT_CSV)) {
			csvFileName = cmd.getOptionValue(COMMANDLINEOPTION_OUTPUT_CSV);
		}
		List<IReportWriter> reportWriters = new ArrayList<>();
		reportWriters.add(new CSVWriter(csvFileName));
		
		if(cmd.hasOption(COMMANDLINEOPTION_OUTPUT_HTML)) {
			reportWriters.add(new HtmlCoverageWriter(new File(cmd.getOptionValue(COMMANDLINEOPTION_OUTPUT_HTML))));
		}
		
		// resolve used messages
		Set<String> providerRequestMessages = project.getProviderRequestMessages();
		Set<String> providerResponseMessages = project.getProviderResponseMessages();
		Set<String> consumerRequestMessages = project.getConsumerRequestMessages();
		Set<String> consumerResponseMessages = project.getConsumerResponseMessages();
		System.out.println(
				"Found " + 
						providerRequestMessages.size() + 
						" provider request message type(s), " +
						providerResponseMessages.size() + 
						" provider response message type(s), " +
						consumerRequestMessages.size() + 
						" consumer request message type(s), and " +
						consumerResponseMessages.size() + 
						" consumer response message type(s)"
				);

		// Ignore messages specified by user
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_INBOUND)) {
			String[] ignore = cmd.getOptionValue(COMMANDLINEOPTION_IGNORE_INBOUND).split(",");
			providerRequestMessages.removeAll(Arrays.asList(ignore));
			consumerResponseMessages.removeAll(Arrays.asList(ignore));
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_OUTBOUND)) {
			String[] ignore = cmd.getOptionValue(COMMANDLINEOPTION_IGNORE_OUTBOUND).split(",");
			providerResponseMessages.removeAll(Arrays.asList(ignore));
			consumerRequestMessages.removeAll(Arrays.asList(ignore));
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_ALLINBOUND)) {
			providerRequestMessages.clear();
			consumerResponseMessages.clear();
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_ALLOUTBOUND)) {
			providerResponseMessages.clear();
			consumerRequestMessages.clear();
		}
		if(cmd.hasOption(COMMANDLINEOPTION_IGNORE_MESSAGES)) {
			String[] ignore = cmd.getOptionValues(COMMANDLINEOPTION_IGNORE_MESSAGES);
			providerRequestMessages.removeAll(Arrays.asList(ignore));
			providerResponseMessages.removeAll(Arrays.asList(ignore));
			consumerRequestMessages.removeAll(Arrays.asList(ignore));
			consumerResponseMessages.removeAll(Arrays.asList(ignore));
		}
		System.out.println(
				"Using " + 
						providerRequestMessages.size() + 
						" provder request message type(s), " +
						providerResponseMessages.size() + 
						" provider response message type(s), " + 
						consumerRequestMessages.size() + 
						" consumer request request message type(s), and " +
						consumerResponseMessages.size() + 
						" consumer response message type(s)" 
				);

		
		
		Map<String, Context<Element>> inboundContexts = new HashMap<>();
		Map<String, Context<Element>> outboundContexts = new HashMap<>();
		List<MeasurementPoint> measurementPoints = new ArrayList<>();
		MeasurementPointBuilder mb = new MeasurementPointBuilder();
		
		buildMeasurementPointsForContext(project, measurementPoints, ContextType.PROVIDER_REQUEST_MESSAGE,
				providerRequestMessages, inboundContexts, mb);
		buildMeasurementPointsForContext(project, measurementPoints, ContextType.CONSUMER_RESPONSE_MESSAGE, consumerResponseMessages, inboundContexts, mb);
		
		buildMeasurementPointsForContext(project, measurementPoints, ContextType.PROVIDER_RESPONSE_MESSAGE, providerResponseMessages, outboundContexts, mb);
		buildMeasurementPointsForContext(project, measurementPoints, ContextType.CONSUMER_REQUEST_MESSAGE, consumerRequestMessages, outboundContexts, mb);
		
		// TODO read customizing for coverage criteria
		
		
		System.out.println("Found " + measurementPoints.size() + " measurement points");
		System.out.println("Skipped at " + mb.getRulesTruncatedAt().size() + " schema locations");
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
		
		for(IReportWriter reportWriter : reportWriters) {
			reportWriter.writeReport(project, allContexts);
		}
		
		int fulfilled = 0;
		for(Context<Element> ctx : allContexts.values()) {
			for(MeasurementPoint m : ctx.getMeasurementPoints()) {
				if(m.isFulfilled()) {
					fulfilled++;
				}
			}
		}
		System.out.println("Coverage: " + fulfilled + "/" + measurementPoints.size() + " = " + (fulfilled * 100 / measurementPoints.size()) + "%");
	}

	private void buildMeasurementPointsForContext(IProject project,
			List<MeasurementPoint> measurementPoints, ContextType messageContextType, Set<String> messages,
			Map<String, Context<Element>> contexts, MeasurementPointBuilder mb) {
		for(String m : messages) {
			Context<Element> ctx = new Context<Element>(messageContextType, m, project.getSchemaElementByQName(m));
			contexts.put(m, ctx);
			mb.buildMeasurements(ctx, project);
			measurementPoints.addAll(ctx.getMeasurementPoints());
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
		options.addOption(COMMANDLINEOPTION_OUTPUT_HTML, true, "Directory to write HTMLK reports to");
		options.addOption(COMMANDLINEOPTION_IGNORE_INBOUND, true, "Comma-separated QNames of inbound messages to ignore in form of {namespace}localname");
		options.addOption(COMMANDLINEOPTION_IGNORE_OUTBOUND, true, "Comma-separated QNames of outbound messages to ignore in form of {namespace}localname");
		options.addOption(COMMANDLINEOPTION_IGNORE_ALLINBOUND, false, "Ignore all inbound messages");
		options.addOption(COMMANDLINEOPTION_IGNORE_ALLOUTBOUND, false, "Ignore all outbound messages");
		options.addOption(COMMANDLINEOPTION_IGNORE_MESSAGES, true, "Comma-separated QNames of messages to ignore inbound and outbound in form of {namespace}localname");
		return options;
	}

	private void processMeasurementPoints(Map<String, Context<Element>> contextsByMessage, Element payloadElement) {
		String messageElementQName = QNameUtil.format(payloadElement.getNamespaceURI(), payloadElement.getLocalName());
		Context<Element> ctx = contextsByMessage.get(messageElementQName);
		if(ctx != null) {
			for(MeasurementPoint m : ctx.getMeasurementPoints()) {
				m.evaluate(payloadElement);
			}
		}
	}
	
}
