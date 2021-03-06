package net.bpelunit.schemacoverage.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPoint;
import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.xml.QNameUtil;

public class CSVWriter implements IReportWriter {

	private String csvFileName;

	public CSVWriter(String csvFileName) {
		this.csvFileName = csvFileName;
	}
	
	@Override
	public String getName() {
		return "CSV";
	}

	@Override
	public void writeReport(IProject project, Map<String, Context<Element>> allContexts) throws IOException {
		try (FileWriter out = new FileWriter(csvFileName)) {

			String headerLine = String.join("\t", "Project", "Context Type", "Context Namespace", "Context Local Name", "Context QName", "Measurement Point",
					"Path Expression", "Expected Value", "Encountered Values", "Satisfied");
			out.write(headerLine);
			out.write("\n");
			for (Context<Element> ctx : allContexts.values()) {
				for (MeasurementPoint m : ctx.getMeasurementPoints()) {
					boolean measurementFulfilled = m.isFulfilled();
					Set<String> extractedValues = m.getExtractedValues();
					List<String> sanitizedExtractedValues = new ArrayList<>(extractedValues.size());
					for(String s : extractedValues) {
						sanitizedExtractedValues.add(s.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " "));
					}
					String resultLine = String.join("\t",
							project.getName(),
							ctx.getType().toString(), 
							QNameUtil.resolveQName(ctx.getName()).getNamespaceURI(),
							QNameUtil.resolveQName(ctx.getName()).getLocalPart(),
							ctx.getName(),
							m.getMeasurementPointType().toString(), 
							m.getMeasuredElement(),
							m.getExpectedValue() != null ? m.getExpectedValue() : "",
							String.join(",", extractedValues),
							measurementFulfilled + ""
							);
					out.write(resultLine);
					out.write("\n");
				}
			}
		}
	}

}
