package net.bpelunit.schemacoverage.model.measurement;

import java.util.HashSet;
import java.util.Set;

import net.bpelunit.schemacoverage.simplepath.function.INodeFunction;

public class MeasurementPoint {
	
	private MeasurementPointType measurementPointType;
	private INodeFunction path;
	private String expectedValue;
	private Set<String> extractedValues = new HashSet<>();
	
	public MeasurementPoint(INodeFunction path) {
		this(MeasurementPointType.MultipleValues, path, null);
	}
	
	public MeasurementPoint(MeasurementPointType type, INodeFunction path, String expectedValue) {
		this.measurementPointType = type;
		this.path = path;
		this.expectedValue = expectedValue;
	}
	
	public String getMessage() {
		return path.toString();
	}
	
	public boolean isFulfilled() {
		return measurementPointType.isFulfilled(extractedValues, expectedValue);
	}
	
	public MeasurementPointType getMeasurementPointType() {
		return measurementPointType;
	}

	public INodeFunction getPath() {
		return path;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public Set<String> getExtractedValues() {
		return extractedValues;
	}

	public void addExtractedValue(String value) {
		extractedValues.add(value.replaceAll("[\r\n\t]", " ").replaceAll("  ", " ").trim());
	}
	
	@Override
	public String toString() {
		return measurementPointType + ":" + path + (expectedValue != null ? "[=" + expectedValue + "]" : "");
	}
}