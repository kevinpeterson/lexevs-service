/*
* Copyright: (c) 2004-2013 Mayo Foundation for Medical Education and
* Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
* triple-shield Mayo logo are trademarks and service marks of MFMER.
*
* Except as contained in the copyright notice above, or as used to identify
* MFMER as the author of this software, the trade names, trademarks, service
* marks, or product names of the copyright holder shall not be used in
* advertising, promotion or otherwise in connection with this software without
* prior written authorization of the copyright holder.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package edu.mayo.cts2.framework.plugin.service.lexevs.service.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Extensions.Generic.MappingExtension;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.relations.Relations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.map.MapCatalogEntry;
import edu.mayo.cts2.framework.model.map.MapCatalogEntrySummary;
import edu.mayo.cts2.framework.model.service.core.DocumentedNamespaceReference;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.mapversion.types.MapRole;
import edu.mayo.cts2.framework.plugin.service.lexevs.naming.CodeSystemVersionNameConverter;
import edu.mayo.cts2.framework.plugin.service.lexevs.service.AbstractLexEvsService;
import edu.mayo.cts2.framework.plugin.service.lexevs.utility.CommonMapUtils;
import edu.mayo.cts2.framework.plugin.service.lexevs.utility.CommonSearchFilterUtils;
import edu.mayo.cts2.framework.plugin.service.lexevs.utility.Constants;
import edu.mayo.cts2.framework.service.command.restriction.MapQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.command.restriction.MapQueryServiceRestrictions.CodeSystemRestriction;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.map.MapQuery;
import edu.mayo.cts2.framework.service.profile.map.MapQueryService;

/**
 * @author <a href="mailto:frutiger.kim@mayo.edu">Kim Frutiger</a>
 *
 */
@Component
public class LexEvsMapQueryService extends AbstractLexEvsService
		implements MapQueryService, InitializingBean {
	
	@Resource
	CodingSchemeToMapTransform codingSchemeToMapTransform;
	
	@Resource
	private CodeSystemVersionNameConverter nameConverter;
	
	private MappingExtension mappingExtension;
	
	public static final String MAPPING_EXTENSION = "MappingExtension";	

	@Override
	public void afterPropertiesSet() throws Exception {
		this.mappingExtension = (MappingExtension)this.getLexBigService().getGenericExtension(MAPPING_EXTENSION);
	}

	
	public CodingSchemeToMapTransform getCodingSchemeToMapTransform() {
		return codingSchemeToMapTransform;
	}

	public void setCodingSchemeToMapTransform(
			CodingSchemeToMapTransform codingSchemeToMapTransform) {
		this.codingSchemeToMapTransform = codingSchemeToMapTransform;
	}


	// ------ Local methods ----------------------

	protected CodingScheme[] getRenderingPage(CodingScheme[] codingScheme, Page page) {
		int start = page.getStart();
		int end = page.getEnd();
		CodingScheme [] csPage = null;
		
		if(end > codingScheme.length){
			end = codingScheme.length;
		}
		
		if ((start == 0) && (end == codingScheme.length)) {
			csPage = codingScheme.clone();
		} 
		else if(start < end){
			
			int size = end - start;
			csPage = new CodingScheme [size];
			
			for (int i = 0; i < csPage.length; i++) {
				csPage[i] = codingScheme[start + i];
			}
		}
	
		return csPage;
	}

	protected List<CodingScheme> doGetResourceSummaries(
			MapQuery query, SortCriteria sortCriteria) {

		List<CodingScheme> codingSchemeList = new ArrayList<CodingScheme>();
		boolean resolvedToCodingSchemeFlag = false;
		
		Set<ResolvedFilter> filters = null; 
		MapQueryServiceRestrictions mapQueryServiceRestrictions = null;
		
		if (query != null) {
			mapQueryServiceRestrictions = query.getRestrictions();
			filters = query.getFilterComponent();
		}		
		
		CodeSystemRestriction codeSystemRestriction = null;
		// ValueSetRestriction valueSetRestriction = null;  // Not in current plan
		if (mapQueryServiceRestrictions != null) {
			codeSystemRestriction = query.getRestrictions().getCodeSystemRestriction();  
			// valueSetRestriction = query.getRestrictions().getValueSetRestriction();
		}
		
		
		LexBIGService lexBigService = getLexBigService();
		try {
			CodingSchemeRenderingList csrFilteredList = lexBigService.getSupportedCodingSchemes();
			
			// Remove any items in above returned list that are not LexEVS Mapping CodingScheme type CodingSchemes 
			csrFilteredList = CommonMapUtils.filterByMappingCodingSchemes(csrFilteredList, mappingExtension);
			
			if ((filters != null) && (csrFilteredList != null) && (csrFilteredList.getCodingSchemeRenderingCount() > 0)) {
				Iterator<ResolvedFilter> filtersItr = filters.iterator();
				while (filtersItr.hasNext() && (csrFilteredList.getCodingSchemeRenderingCount() > 0)) {
						ResolvedFilter resolvedFilter = filtersItr.next();
						csrFilteredList = CommonSearchFilterUtils.filterResourceSummariesByResolvedFilter(resolvedFilter, 
								csrFilteredList, nameConverter);
				}
			}
			
			// NOTE:  Logic requires the processing of CodeSystemRestrictions to be last in order to save on 
			//   the resolving to a list of CodingScheme objects.  Filter items based on the CodingScheme Relations 
			//   sourceCodingScheme and/or targetCodingScheme string values.
			if (codeSystemRestriction != null) {
				codingSchemeList = filterByCodeSystemRestriction(csrFilteredList, codeSystemRestriction);
				resolvedToCodingSchemeFlag = true;
			}
			
			if (!resolvedToCodingSchemeFlag) {
				codingSchemeList = resolveToCodingSchemeList(csrFilteredList.getCodingSchemeRendering());
			}
						
			return codingSchemeList;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	protected List<CodingScheme> filterByCodeSystemRestriction(CodingSchemeRenderingList csrFilteredList, 
			CodeSystemRestriction codeSystemRestriction) {

		List<CodingScheme> codingSchemeList = new ArrayList<CodingScheme>();

		MapRole codeSystemRestrictionMapRole = null;
		Set<NameOrURI> codeSystemSet = null;
		if (codeSystemRestriction != null) {
			codeSystemRestrictionMapRole = codeSystemRestriction.getMapRole();
			codeSystemSet = codeSystemRestriction.getCodeSystems();
		}
		
		String csrMapRoleValue = null;
		if (codeSystemRestrictionMapRole != null) {
			csrMapRoleValue = codeSystemRestrictionMapRole.value();
		}
		
		if (csrMapRoleValue != null && codeSystemSet != null && codeSystemSet.size() > 0) {
			// Get array of CodingSchemeRendering object and loop checking each item in array
			CodingSchemeRendering[] csRendering = csrFilteredList.getCodingSchemeRendering();
			for (CodingSchemeRendering render : csRendering) {
				CodingScheme codingScheme = getCodingSchemeForCodeSystemRestriction(render, codeSystemSet, csrMapRoleValue); 
				if (codingScheme != null) {
					codingSchemeList.add(codingScheme);
				}
			}			
		} 
		
		return codingSchemeList;		
	}
	
	private CodingScheme getCodingSchemeForCodeSystemRestriction(CodingSchemeRendering render, 
			Set<NameOrURI> codeSystemSet, 
			String csrMapRoleValue) {

		CodingScheme notFoundCodingScheme = null;
		CodingScheme codingScheme = getCodingScheme(render);
		
		// Assuming format of Map has only has 1 relations section/1 relations element in xml file
		if (codingScheme.getRelationsCount() != 1) {
			throw new UnsupportedOperationException("Invalid format for Map. Expecting only one metadata section for Relations.");
		}
		Relations relations = codingScheme.getRelations(0);
		String sourceCodingScheme = relations.getSourceCodingScheme();
		String targetCodingScheme = relations.getTargetCodingScheme();
		
		if (csrMapRoleValue.equals(Constants.MAP_TO_ROLE) && isCodingSchemeFound(targetCodingScheme, codeSystemSet)) {
			return codingScheme;
		}
		
		if (csrMapRoleValue.equals(Constants.MAP_FROM_ROLE) && isCodingSchemeFound(sourceCodingScheme, codeSystemSet)) { 
			return codingScheme;
		}
		
		if (csrMapRoleValue.equals(Constants.BOTH_MAP_ROLES) && 
				isCodingSchemeFound(targetCodingScheme, sourceCodingScheme, codeSystemSet)) {
			return codingScheme;
		}
		
		return notFoundCodingScheme;
	}

	
	protected boolean isCodingSchemeFound(String relationCodingScheme, Set<NameOrURI> codeSystemSet) {

		boolean returnFlag = false;
		Iterator<NameOrURI> iterator = codeSystemSet.iterator();
		while (iterator.hasNext() && returnFlag == false) {
			NameOrURI nameOrURI = iterator.next();
			if (nameOrURI.getName() != null && nameOrURI.getName().equals(relationCodingScheme)) {
				returnFlag = true;
			}
			if (nameOrURI.getUri() != null && nameOrURI.getUri().equals(relationCodingScheme)) {
				returnFlag = true;
			}
		}
		return returnFlag;
	}
	
	
	protected boolean isCodingSchemeFound(String targetCodingScheme, String srcCodingScheme, Set<NameOrURI> codeSystemSet) {

		boolean returnFlag = false;
		Iterator<NameOrURI> iterator = codeSystemSet.iterator();
		while (iterator.hasNext() && returnFlag == false) {
			NameOrURI nameOrURI = iterator.next();
			if (nameOrURI.getName() != null && (nameOrURI.getName().equals(srcCodingScheme) || 
					nameOrURI.getName().equals(targetCodingScheme))) {
				returnFlag = true;
			}
			if (nameOrURI.getUri() != null && (nameOrURI.getUri().equals(srcCodingScheme) || 
					nameOrURI.getUri().equals(targetCodingScheme))) {
				returnFlag = true;
			}
		}
		return returnFlag;
	}
	
	protected CodingScheme getCodingScheme(CodingSchemeRendering render) {
		String codingSchemeName = render.getCodingSchemeSummary().getCodingSchemeURI();			
		String version = render.getCodingSchemeSummary().getRepresentsVersion();
		CodingSchemeVersionOrTag tagOrVersion = Constructors.createCodingSchemeVersionOrTagFromVersion(version);
		CodingScheme codingScheme;
		try {
			codingScheme = this.getLexBigService().resolveCodingScheme(codingSchemeName, tagOrVersion);
		} catch (LBException e) {
			throw new RuntimeException(e);
		}
		return codingScheme;
	}
	
	protected List<CodingScheme> resolveToCodingSchemeList(CodingSchemeRendering[] codingSchemeRenderingArray) {
		List<CodingScheme> codingSchemeList = new ArrayList<CodingScheme>();
		
		if (codingSchemeRenderingArray != null && codingSchemeRenderingArray.length > 0) {
			for (int i=0; i<codingSchemeRenderingArray.length; i++) {
				CodingScheme codingScheme = getCodingScheme(codingSchemeRenderingArray[i]);
				codingSchemeList.add(codingScheme);
			}
		}
		return codingSchemeList;
	}
	
	
	// -------- Implemented methods ----------------
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.BaseService#getKnownNamespaceList()
	 */
	@Override
	public List<DocumentedNamespaceReference> getKnownNamespaceList() {
		return new ArrayList<DocumentedNamespaceReference>();
	}

	@Override
	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {

		MatchAlgorithmReference exactMatch = StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		MatchAlgorithmReference contains = StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		MatchAlgorithmReference startsWith = StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference();

		return new HashSet<MatchAlgorithmReference>(Arrays.asList(exactMatch,contains,startsWith));
	}

	@Override
	public Set<? extends PropertyReference> getSupportedSearchReferences() {
		
		PropertyReference name = StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference();		
		PropertyReference about = StandardModelAttributeReference.ABOUT.getPropertyReference();	
		PropertyReference description = StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference();
		
		return new HashSet<PropertyReference>(Arrays.asList(name,about,description));
	}

	@Override
	public Set<? extends PropertyReference> getSupportedSortReferences() {
		return new HashSet<PropertyReference>();
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return new HashSet<PredicateReference>();
	}


	@Override
	public DirectoryResult<MapCatalogEntrySummary> getResourceSummaries(
			MapQuery query, SortCriteria sortCriteria, Page page) {
		
		List<CodingScheme> codingSchemeList = this.doGetResourceSummaries(query, sortCriteria);
		CodingScheme[] codingSchemeArray = codingSchemeList.toArray(new CodingScheme[0]);
		CodingScheme[] codingSchemePage = getRenderingPage(codingSchemeArray, page);
		
		List<MapCatalogEntrySummary> list = new ArrayList<MapCatalogEntrySummary>();

		for (CodingScheme codingScheme : codingSchemePage) {
			list.add(codingSchemeToMapTransform.transformToMapCatalogEntrySummary(codingScheme));
		}

		boolean atEnd = (page.getEnd() >= codingSchemeArray.length) ? true : false;
		
		return new DirectoryResult<MapCatalogEntrySummary>(list, atEnd);
	}


	@Override
	public DirectoryResult<MapCatalogEntry> getResourceList(MapQuery query,
			SortCriteria sortCriteria, Page page) {

		List<CodingScheme> codingSchemeList = this.doGetResourceSummaries(query, sortCriteria);
		CodingScheme[] codingSchemeArray = codingSchemeList.toArray(new CodingScheme[0]);
		CodingScheme[] codingSchemePage = getRenderingPage(codingSchemeArray, page);
		
		List<MapCatalogEntry> list = new ArrayList<MapCatalogEntry>();

		for (CodingScheme codingScheme : codingSchemePage) {
			list.add(codingSchemeToMapTransform.transformToMapCatalogEntry(codingScheme));
		}

		boolean atEnd = (page.getEnd() >= codingSchemeArray.length) ? true : false;
		
		return new DirectoryResult<MapCatalogEntry>(list, atEnd);
	}


	@Override
	public int count(MapQuery query) {
		return doGetResourceSummaries(query, null).size();
	}

}