package net.bpelunit.schemacoverage.report.html;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointType;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.report.IReportWriter;
import net.bpelunit.schemacoverage.util.CounterMap;
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
		CounterMap<MeasurementPointType> fulfilledmeasurementPointTypes = new CounterMap<>();
		CounterMap<MeasurementPointType> allMeasurementPointTypes = new CounterMap<>();
		ListMap<String, MeasurementPoint> results = analyzeContext(context);
		List<String> sortedElements = new ArrayList<>(results.keySet());
		Collections.sort(sortedElements);
		
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		try(
				FileOutputStream out = new FileOutputStream(new File(outputDir, "coverage.css"));
				InputStream in = getClass().getResourceAsStream("coverage.css");
			) {
			while(in.available() > 0) {
				out.write(in.read());
			}
		}
		
		File output = new File(outputDir, project.getName() + "." + context.getType() + "." + getFilenameToken(context.getName()) + ".html");
		try(Writer out = new FileWriter(output)) {
			out.write("<!DOCTYPE html>\n");
			out.write("<html>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"coverage.css\">\n<body>\n");
			writeTag(out, "h1", project.getName());
			writeTag(out, "h2", context.getName());
			
			out.write("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">");
			out.write("<thead>");
			out.write("<tr>");
			writeTag(out, "th", "Element");
			writeTag(out, "th", "Coverage");
			for(MeasurementPointType mpt : MeasurementPointType.values()) {
				out.write("<th class=\"norotate\">" + mpt + "</th>");
			}
			out.write("</tr>");
			out.write("</thead>");
			
			out.write("<tbody style=\"overflow: auto;\">");
			for(String element : sortedElements) {
				out.write("<tr>");
				List<MeasurementPoint> measurementPoints = results.get(element);
				
				int countRulesFulfilled = getCountRulesFulfilled(measurementPoints);
				boolean allRulesFulfilled = countRulesFulfilled == measurementPoints.size();
				out.write("<td>");
				writeTag(out, "span class=\"measuredobject depth" + getPathDepth(element) + "\"", colorPath(element, allRulesFulfilled));
				out.write("</td>");
				String style = allRulesFulfilled ? "covered" : "notcovered";
				writeTag(out, "td class=\"" + style + "\"", countRulesFulfilled + "/" + measurementPoints.size());
				for(MeasurementPointType mpt : MeasurementPointType.values()) {
					out.write("<td>");
					if(mpt != MeasurementPointType.EnumLiteralUsed && mpt != MeasurementPointType.DeclaredTypeInHierarchy && mpt != MeasurementPointType.TypeInHierarchy) {
						MeasurementPoint mp = getFirstMeasurementPointForType(measurementPoints, mpt);
						if(mp != null) {
							allMeasurementPointTypes.inc(mpt);
							if(mp.isFulfilled()) {
								fulfilledmeasurementPointTypes.inc(mpt);
								writeTag(out, "span class=\"covered\"", "ok");
							} else {
								String extractedValues = stringJoin(",", mp.getExtractedValues());
								if(extractedValues.equals("")) {
									extractedValues = "no values found";
								}
								writeTag(out, "span class=\"notcovered\"", "nok (" + extractedValues + ")");
							}
						}
					} else {
						List<MeasurementPoint> mps = getMeasurementPointsForType(measurementPoints, mpt);
						for(MeasurementPoint mp : mps) {
							allMeasurementPointTypes.inc(mpt);
							if(mp.isFulfilled()) {
								style = "covered";
								fulfilledmeasurementPointTypes.inc(mpt);
							} else {
								style = "notcovered";
							}
							writeTag(out, "span class=\"" + style + "\"", mp.getExpectedValue() + " ");
						}
					}
					out.write("</td>");
				}
				out.write("</tr>");
			}
			out.write("</tbody>");
			
			out.write("<tr>");
			writeTag(out, "th", "Summary/Total");
			writeTag(out, "th", fulfilledmeasurementPointTypes.sum() +  "/" + allMeasurementPointTypes.sum());
			for(MeasurementPointType mpt : MeasurementPointType.values()) {
				int all = allMeasurementPointTypes.get(mpt);
				int fulfilled = fulfilledmeasurementPointTypes.get(mpt);
				String style = all == fulfilled ? "covered" : "notcovered";
				writeTag(out, "th class=\"" + style + "\"", fulfilled + "/" + all);
			}
			out.write("</tr>");
			out.write("</table>");
			
			out.write("</body></html>");
		}
	}

	private MeasurementPoint getFirstMeasurementPointForType(
			List<MeasurementPoint> measurementPoints, MeasurementPointType mpt) {
		for(MeasurementPoint mp : measurementPoints) {
			if(mp.getMeasurementPointType() == mpt) {
				return mp;
			}
		}
		return null;
	}
	
	private List<MeasurementPoint> getMeasurementPointsForType(
			List<MeasurementPoint> measurementPoints, MeasurementPointType mpt) {
		List<MeasurementPoint> result = new ArrayList<>();
		for(MeasurementPoint mp : measurementPoints) {
			if(mp.getMeasurementPointType() == mpt) {
				result.add(mp);
			}
		}
		return result;
	}

	private int getPathDepth(String element) {
		return element.replaceAll("\\{[^\\}]*\\}", "").split("/").length - 1;
	}

	private String colorPath(String pathAsString, boolean allRulesFufilled) {
		StringBuilder result = new StringBuilder();

		String lastPathElement = pathAsString.replaceAll("\\{[^\\}]*\\}", "");
		int index = Math.max(lastPathElement.lastIndexOf("/"), lastPathElement.lastIndexOf("}")) + 1;
		lastPathElement = lastPathElement.substring(index);
		
		result.append("<span class=\"tooltip ");
		if(allRulesFufilled) {
			result.append("covered");
		} else {
			result.append("notcovered");
		}
		result.append("\">");
		result.append(lastPathElement);
		result.append("<span class=\"tooltiptext\">").append(pathAsString).append("</span>");
		result.append("</span>");
		
		return result.toString();
	}

	private int getCountRulesFulfilled(List<MeasurementPoint> measurementPoints) {
		int result = 0;
		for(MeasurementPoint mp : measurementPoints) {
			if(mp.isFulfilled()) {
				result++;
			}
		}
		return result;
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
			String path = mp.getMeasuredElement();
//			path = path.replaceAll("\\[[^\\]]*\\]", "");
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
		out.write(tag.split(" ")[0]);
		out.write(">");
	}

	private String stringJoin(String delimiter, Collection<String> strings) {
		StringBuilder result = new StringBuilder();
		
		Iterator<String> iterator = strings.iterator();
		for(int i = 0; i < strings.size(); i++) {
			if(i > 0) {
				result.append(delimiter);
			}
			result.append(iterator.next());
		}
		
		return result.toString();
	}
}
