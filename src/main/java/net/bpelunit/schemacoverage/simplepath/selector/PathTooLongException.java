package net.bpelunit.schemacoverage.simplepath.selector;

@SuppressWarnings("serial")
public class PathTooLongException extends Exception {

	private INodeSelector parentSelector;
	private int maxLength;
	private INodeSelector newSelector;
	
	public PathTooLongException(INodeSelector parentSelector, int maxLength, INodeSelector newSelector) {
		super();
		this.parentSelector = parentSelector;
		this.maxLength = maxLength;
		this.newSelector = newSelector;
	}

	public INodeSelector getParentSelector() {
		return parentSelector;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public INodeSelector getNewSelector() {
		return newSelector;
	}
	
	@Override
	public String getMessage() {
		return "Max length of " + maxLength + " reached: Cannot add " + newSelector.toString() + " to " + parentSelector;
	}
	
}
