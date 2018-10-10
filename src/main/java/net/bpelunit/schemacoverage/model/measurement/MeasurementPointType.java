package net.bpelunit.schemacoverage.model.measurement;

import java.util.Set;

public enum MeasurementPointType {
	OptionalNodeSet {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("1");
		}
	},
	OptionalNodeNotSet {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("0");
		}
	},
	EnumLiteralUsed {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains(expectedValue);
		}
	},
	BooleanTrue {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("true");
		}
	},
	BooleanFalse {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("false");
		}
	},
	MultipleValues {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.size() > 1;
		}
	},
	ListLowerBound {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains(expectedValue);
		}
	},
	ListUpperBound {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains(expectedValue);
		}
	},
	DifferentlySizedLists {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.size() > 1;
		}
	},
	MandatoryNodeUsed {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains(expectedValue);
		}
	},
	TypeInHierarchy {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains(expectedValue);
		}
	},
	DeclaredTypeInHierarchy {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("") || extractedValues.contains(expectedValue);
		}
	}, 
	MessageUsed {
		@Override
		public boolean isFulfilled(Set<String> extractedValues, String expectedValue) {
			return extractedValues.contains("1");
		}
	};

	public abstract boolean isFulfilled(Set<String> extractedValues, String expectedValue);
}
