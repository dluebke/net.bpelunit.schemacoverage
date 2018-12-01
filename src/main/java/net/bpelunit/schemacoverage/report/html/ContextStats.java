package net.bpelunit.schemacoverage.report.html;

import java.io.File;

import org.w3c.dom.Element;

import net.bpelunit.schemacoverage.model.measurement.Context;
import net.bpelunit.schemacoverage.model.measurement.MeasurementPointType;
import net.bpelunit.schemacoverage.util.CounterMap;

public class ContextStats {

	private Context<Element> context;
	private CounterMap<MeasurementPointType> fulfilledmeasurementPointTypes;
	private CounterMap<MeasurementPointType> allMeasurementPointTypes;
	private File outputFile;
	
	public ContextStats(Context<Element> context, CounterMap<MeasurementPointType> fulfilledmeasurementPointTypes,
			CounterMap<MeasurementPointType> allMeasurementPointTypes, File outputFile) {
		this.context = context;
		this.fulfilledmeasurementPointTypes = fulfilledmeasurementPointTypes;
		this.allMeasurementPointTypes = allMeasurementPointTypes;
		this.outputFile = outputFile;
	}
	
	public CounterMap<MeasurementPointType> getAllMeasurementPointTypes() {
		return allMeasurementPointTypes;
	}
	
	public Context<Element> getContext() {
		return context;
	}
	
	public CounterMap<MeasurementPointType> getFulfilledmeasurementPointTypes() {
		return fulfilledmeasurementPointTypes;
	}
	
	public File getOutputFile() {
		return outputFile;
	}

}
