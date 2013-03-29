package edu.mayo.cts2.framework.plugin.service.lexevs.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.commonTypes.EntityDescription;

import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;

import scala.actors.threadpool.Arrays;

public class FakeLexEvsData {	
	final static PropertyReference ABOUT_REF = StandardModelAttributeReference.ABOUT.getPropertyReference();
	final static PropertyReference RESOURCE_SYNOPSIS_REF = StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference();
	final static PropertyReference RESOURCE_NAME_REF = StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference();
	
	final static MatchAlgorithmReference CONTAINS_REF = StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
	final static MatchAlgorithmReference STARTS_WITH_REF = StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference();
	final static MatchAlgorithmReference EXACT_MATCH_REF = StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
	
	public enum CodeSystem{
		AUTOMOBILES ("Automobiles");
		
		String name;
		CodeSystem(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
	}
	
	public enum DataField{
		ABOUT (0, ABOUT_REF),
		RESOURCE_SYNOPSIS (1, RESOURCE_SYNOPSIS_REF),
		RESOURCE_LOCALNAME (2, null),
		RESOURCE_VERSION (3, null),
		RESOURCE_NAME (4, RESOURCE_NAME_REF);
		
		private int index;
		private PropertyReference propertyReference;
		DataField(int index, PropertyReference propertyReference){
			this.index = index;
			this.propertyReference = propertyReference;
		}
		
		public int index(){
			return this.index;
		}
		
		public PropertyReference propertyReference(){
			return this.propertyReference;
		}
	}
	
	private final static String [][] DEFAULT_DATA = {
		{"11.11.0.1", "Auto", "Automobiles", "1.0", ""},
		{"9.0.0.1", "Car", "Vehicles", "1.0", ""},
		{"13.11.0.2", "Auto3", "Automobiles", "1.1", ""},
		{"1.2.3.4", "2Auto", "automobiles", "1.0", ""},
		{"5.6.7.8", "auto", "vehicles", "1.0", ""},
		{"7.6.5.4", "utoA", "hicle", "1.0", ""}
	};
	
	private final static int CODESYSTEM_FIELDCOUNT = DataField.values().length;
	
	private List<String[]> codeSystemList = null;
	
	private int codeSystemCount = 0;
	
	private void initializeDefaultData(){
		for(int i=0; i < DEFAULT_DATA.length; i++){
			DEFAULT_DATA[i][DataField.RESOURCE_NAME.index()] = DEFAULT_DATA[i][DataField.RESOURCE_LOCALNAME.index()];
			DEFAULT_DATA[i][DataField.RESOURCE_NAME.index()] += "-";
			DEFAULT_DATA[i][DataField.RESOURCE_NAME.index()] += DEFAULT_DATA[i][DataField.RESOURCE_VERSION.index()];
		}
	}
	
	@SuppressWarnings("unchecked")
	public FakeLexEvsData() throws IOException{
		initializeDefaultData();
		codeSystemList = Arrays.asList(DEFAULT_DATA);
		
		this.codeSystemCount = codeSystemList.size();
	}
	
	public FakeLexEvsData(int size){
		initializeDefaultData();
		this.codeSystemCount = (size <= DEFAULT_DATA.length) ? size : DEFAULT_DATA.length;
		codeSystemList = new ArrayList<String[]>();
		for(int i=0; i < this.codeSystemCount; i++){
			codeSystemList.add(new String[CODESYSTEM_FIELDCOUNT]);
			this.setFields(i, DEFAULT_DATA[i]);
		}
	}

	public FakeLexEvsData(String [][] data){
		this.codeSystemCount = data.length;
		codeSystemList = new ArrayList<String[]>();
		for(int i=0; i < this.codeSystemCount; i++){
			codeSystemList.add(new String[CODESYSTEM_FIELDCOUNT]);
			this.setFields(i, data[i]);
		}
	}
	
	
	public int size(){
		return this.codeSystemCount;
	}
	
	private void setFields(int index, String [] values){
		DataField[] fields = DataField.values();
		if(index < this.codeSystemList.size()){
			for(int i=0; i < fields.length; i++){
				this.codeSystemList.get(index)[fields[i].index()] = values[i];
			}
		}
	}
	
	public String getScheme_DataField(int schemeIndex, DataField dataField){
		String results = null;
		if(schemeIndex < this.codeSystemCount){
			results = this.codeSystemList.get(schemeIndex)[dataField.index()];
		}
		return results;
	}

	public String getScheme_DataField(int schemeIndex,
			PropertyReference propertyReference) {
		String results = null;
		if(schemeIndex < this.codeSystemCount){
			int fieldIndex = this.getPropertyReferenceIndex(propertyReference);
			
			results = this.codeSystemList.get(schemeIndex)[fieldIndex];
		}
		return results;
	}

	
	private int getPropertyReferenceIndex(PropertyReference propertyReference) {
		int index = 0;
		DataField [] fields = DataField.values();
		for(int i=0; i < fields.length; i++){
			PropertyReference ref = fields[i].propertyReference();
			if(ref != null){
				if(ref.equals(propertyReference)){
					index = i;
				}
			}
		}
		return index;
	}

	public int getCount(Set<ResolvedFilter> filters) {
		int count = 0;
		String exactMatch = EXACT_MATCH_REF.getContent().toLowerCase();
		String contains = CONTAINS_REF.getContent().toLowerCase();
		String startsWith = STARTS_WITH_REF.getContent().toLowerCase();
		
		for(int schemeIndex=0; schemeIndex < this.codeSystemCount; schemeIndex++){
			boolean found = true;
			Iterator<ResolvedFilter> filterIterator = filters.iterator();
			while(found && filterIterator.hasNext()){
				ResolvedFilter filter = filterIterator.next();
				String matchAlgorithmReferenceName = filter.getMatchAlgorithmReference().getContent().toLowerCase();
				PropertyReference propertyReference = filter.getPropertyReference();
				String matchValue = filter.getMatchValue().toLowerCase();
				
				String dataValue = this.getScheme_DataField(schemeIndex, propertyReference).toLowerCase();
				if(matchAlgorithmReferenceName.equals(exactMatch)){
					if(dataValue.equals(matchValue) == false){
						found = false;
					}
				}
				else if(matchAlgorithmReferenceName.equals(contains)){
					if(dataValue.contains(matchValue) == false){
						found = false;
					}
				}
				else if(matchAlgorithmReferenceName.equals(startsWith)){
					if(dataValue.startsWith(matchValue) == false){
						found = false;
					}
				}
				else{
					found = false;
				}
				
			}
			if(found){
				count++;
			}
		}
		return count;
	}

	public void setProperty(CodingSchemeSummary codingSchemeSummary, int schemeIndex, PropertyReference property) {
		if(property.equals(RESOURCE_SYNOPSIS_REF)){
			EntityDescription codingSchemeDescription = new EntityDescription();
			codingSchemeDescription.setContent(this.getScheme_DataField(schemeIndex, DataField.RESOURCE_SYNOPSIS)); 
			codingSchemeSummary.setCodingSchemeDescription(codingSchemeDescription);
		}		
		else if(property.equals(ABOUT_REF)){
			codingSchemeSummary.setCodingSchemeURI(this.getScheme_DataField(schemeIndex, DataField.ABOUT)); 
		}
		else if(property.equals(RESOURCE_NAME_REF)){
			codingSchemeSummary.setLocalName(this.getScheme_DataField(schemeIndex, DataField.RESOURCE_LOCALNAME));
			codingSchemeSummary.setRepresentsVersion(this.getScheme_DataField(schemeIndex, DataField.RESOURCE_VERSION)); 	
		}
	}		
}