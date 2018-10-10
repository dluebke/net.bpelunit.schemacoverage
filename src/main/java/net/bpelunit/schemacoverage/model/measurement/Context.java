package net.bpelunit.schemacoverage.model.measurement;

import java.util.ArrayList;
import java.util.List;

public class Context<T> {

	private ContextType type;
	private String name;
	private T schemaReference;
	private List<MeasurementPoint> measurementPoints = new ArrayList<>();
	
	public Context(ContextType type, String name, T schemaReference) {
		super();
		this.type = type;
		this.name = name;
		this.schemaReference = schemaReference;
	}

	public ContextType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public T getSchemaReference() {
		return schemaReference;
	}
	
	@Override
	public String toString() {
		return getType() + ":" + getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		return 
			obj != null
			&&
			obj.getClass() == this.getClass()
			&&
			obj.toString() == this.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public void addMeasurementPoint(MeasurementPoint measurementPoint) {
		this.measurementPoints.add(measurementPoint);
	}

	public List<MeasurementPoint> getMeasurementPoints() {
		return measurementPoints;
	}
}
