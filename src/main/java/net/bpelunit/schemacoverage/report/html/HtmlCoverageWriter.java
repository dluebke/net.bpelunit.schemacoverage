package net.bpelunit.schemacoverage.report.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.report.IReportWriter;
import net.bpelunit.schemacoverage.util.ListMap;

public class HtmlCoverageWriter implements IReportWriter {

	private File outputDir;
	
	public HtmlCoverageWriter() {
		this(new File("."));
	}
	
	public HtmlCoverageWriter(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void writeReport(IProject project, Map<String, Context<Element>> allContexts) throws IOException {
		for(Context<Element> context : allContexts.values()) {
			writeHtmlCoverage(context, project);
		}
	}

	private void writeHtmlCoverage(Context<Element> context, IProject project) throws IOException {
		ListMap<String, MeasurementPoint> results = analyzeContext(context);
		List<String> sortedElements = new ArrayList<>(results.keySet());
		Collections.sort(sortedElements);
		
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		File output = new File(outputDir, project.getName() + "." + context.getType() + "." + getFilenameToken(context.getName()) + ".html");
		try(Writer out = new FileWriter(output)) {
			out.write("<html><body style=\"font-family: Arial,Helvetica,sans-serif;\">");
			writeTag(out, "h1", project.getName());
			
			for(String element : sortedElements) {
				List<MeasurementPoint> measurementPoints = results.get(element);
				out.write("<div>");
				writeTag(out, "h2", colorPath(element, allRulesFufilled(measurementPoints)));
				for(MeasurementPoint mp : measurementPoints) {
					writeTag(out, "p", mp.getMeasurementPointType() + ": " + mp.isFulfilled());
				}
				out.write("</div>");
			}
			
			out.write("</body></html>");
		}
	}

	private String colorPath(String pathAsString, boolean allRulesFufilled) {
		StringBuilder result = new StringBuilder();
		
		int index = Math.max(pathAsString.lastIndexOf("/"), pathAsString.lastIndexOf("}")) + 1;
		
		result.append("<span style=\"color: ");
		if(allRulesFufilled) {
			result.append("#00aa00");
		} else {
			result.append("#aa0000");
		}
		result.append(";\">");
		result.append("<span style=\"font-size: 70%;\">");
		result.append(pathAsString.subSequence(0, index));
		result.append("</span>");
		result.append(pathAsString.substring(index));
		result.append("</span>");
		
		return result.toString();
	}

	private boolean allRulesFufilled(List<MeasurementPoint> measurementPoints) {
		for(MeasurementPoint mp : measurementPoints) {
			if(!mp.isFulfilled()) {
				return false;
			}
		}
		return true;
	}

	private String getFilenameToken(String name) {
		String[] nameComponents = name.split("\\}");
		if(nameComponents.length == 1) {
			return name;
		} else {
			String[] nsComponents = nameComponents[0].split("/");
			for(int i = nsComponents.length-1; i >= 0; i--) {
				String currentComponent = nsComponents[i];
				
				for(int j = 0; j < currentComponent.length(); j++) {
					if(Character.isAlphabetic(currentComponent.charAt(j))) {
						return currentComponent + "_" + nameComponents[1];
					}
				}
			}
		}
		
		return name;
	}

	private ListMap<String, MeasurementPoint> analyzeContext(Context<Element> context) {
		ListMap<String, MeasurementPoint> result = new ListMap<>();
				
		for(MeasurementPoint mp : context.getMeasurementPoints()) {
			String path = mp.getPath().getNodeSelector().toString();
			result.get(path).add(mp);
		}
				
		return result;
	}

	private void writeTag(Writer out, String tag, String value) throws IOException {
		out.write("<");
		out.write(tag);
		out.write(">");
		out.write(value);
		out.write("</");
		out.write(tag);
		out.write(">");
	}

}
