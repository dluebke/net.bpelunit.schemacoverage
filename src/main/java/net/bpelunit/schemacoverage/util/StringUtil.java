package net.bpelunit.schemacoverage.util;

public class StringUtil {

	public static boolean isEmpty(String s) {
		return s == null || "".equals(s);
	}

	public static boolean isEqual(String s1, String s2) {
		if(s1 == null && s2 == null) {
			return true;
		}
		
		if(s1 == null) {
			return false;
		}
		
		return s1.equals(s2);
	}

}
