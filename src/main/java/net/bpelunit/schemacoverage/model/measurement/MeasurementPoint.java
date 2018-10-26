package net.bpelunit.schemacoverage.model.measurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.simplepath.function.INodeFunction;
import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;

public class MeasurementPoint {
	
	private MeasurementPointType measurementPointType;
	private INodeFunction path;
	private String expectedValue;
	private Set<String> extractedValues = new HashSet<>();
	private INodeSelector context;
	
	public MeasurementPoint(INodeFunction path) {
		this(MeasurementPointType.MultipleValues, path, null);
	}
	
	public MeasurementPoint(MeasurementPointType type, INodeFunction path, String expectedValue) {
		this.measurementPointType = type;
		this.path = path;
		this.expectedValue = expectedValue;
	}
	
	public MeasurementPoint(MeasurementPointType type, INodeSelector context,
			INodeFunction path, String expectedValue) {
		this(type, path, expectedValue);
		this.context = context;
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

	public INodeFunction getPathInContext() {
		return path;
	}

	public String getMeasuredElement() {
		if(context == null) {
			return path.getNodeSelector().toString();
		} else {
			return context.clone().appendSelector(path.getNodeSelector().clone()).toString();
		}
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

	public void evaluate(Node n) {
		List<Node> contextNodes;
		if(context != null) {
			List<Node> selectedContexts = context.evaluate(n);
			contextNodes = new ArrayList<>();
			for(Node m : selectedContexts) {
				NodeList children = m.getChildNodes();
				for(int i = 0; i < children.getLength(); i++) {
					contextNodes.add(children.item(i));					
				}
			}
		} else {
			contextNodes = Arrays.asList(n);
		}
		
		for(Node currentNode : contextNodes) {
			List<String> results = path.evaluate(currentNode);
			extractedValues.addAll(results);
		}
	}

	public void defineContext(INodeSelector context) {
		this.context = context;
	}
}