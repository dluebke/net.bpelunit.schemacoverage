package net.bpelunit.schemacoverage.report;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.project.IProject;

public interface IReportWriter {

	void writeReport(IProject project, Map<String, Context<Element>> allContexts) throws IOException;

}
