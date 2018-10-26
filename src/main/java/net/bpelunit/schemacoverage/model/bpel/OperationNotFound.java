package net.bpelunit.schemacoverage.model.bpel;

@SuppressWarnings("serial")
public class OperationNotFound extends RuntimeException {

	private String portTypeName;
	private String operationName;

	public OperationNotFound(String operationName, String portTypeName) {
		super("Operation " + operationName + " could not be resolved in portType " + portTypeName);
		this.operationName = operationName;
		this.portTypeName = portTypeName;
	}
	
	public String getOperationName() {
		return operationName;
	}
	
	public String getPortTypeName() {
		return portTypeName;
	}

}
