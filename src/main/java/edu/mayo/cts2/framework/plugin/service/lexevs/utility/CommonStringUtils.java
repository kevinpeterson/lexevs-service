package edu.mayo.cts2.framework.plugin.service.lexevs.utility;

import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;

public class CommonStringUtils {
	
	public static boolean executeMatchAlgorithm(
			String sourceValue, 
			String searchValue, 
			MatchAlgorithmReference matchAlgorithmReference, 
			boolean caseSensitive) {
		
		String searchType = matchAlgorithmReference.getContent();
		
		if (searchType.equals(Constants.SEARCH_TYPE_EXACT_MATCH)) {
			return CommonStringUtils.searchExactMatch(sourceValue, searchValue, caseSensitive);
		} else if (searchType.equals(Constants.SEARCH_TYPE_CONTAINS)) {
			return CommonStringUtils.searchContains(sourceValue, searchValue, caseSensitive);
		} else if (searchType.equals(Constants.SEARCH_TYPE_STARTS_WITH)) {
			return CommonStringUtils.searchStartsWith(sourceValue, searchValue, caseSensitive);
		}  
		
		return false;
	}

	public static boolean searchContains(String sourceValue, String searchValue, boolean caseSensitive) {
		if (caseSensitive) {
			if (sourceValue.indexOf(searchValue) != -1) {
				return true;
			}
		} else {
			if (sourceValue.toLowerCase().indexOf(searchValue.toLowerCase()) != -1) {
				return true;
			}						
		}
		return false;
	}

	public static boolean searchExactMatch(String sourceValue, String searchValue, boolean caseSensitive) {
		if (caseSensitive) {
			if (sourceValue.equals(searchValue)) {
				return true;
			}
		} else {
			if (sourceValue.equalsIgnoreCase(searchValue)) {
				return true;
			}						
		}
		return false;
	}


	public static boolean searchStartsWith(String sourceValue, String searchValue, boolean caseSensitive) {
		if (caseSensitive) {
			if (sourceValue.startsWith(searchValue)) {
				return true;
			}
		} else {
			if (sourceValue.toLowerCase().startsWith(searchValue.toLowerCase())) {
				return true;
			}						
		}
		return false;
	}
}