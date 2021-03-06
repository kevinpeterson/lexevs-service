/*
* Copyright: (c) Mayo Foundation for Medical Education and
* Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
* triple-shield Mayo logo are trademarks and service marks of MFMER.
*
* Distributed under the OSI-approved BSD 3-Clause License.
* See http://ncip.github.com/lexevs-service/LICENSE.txt for details.
*/
package edu.mayo.cts2.framework.plugin.service.lexevs.service.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import javax.annotation.Resource;

import org.LexGrid.LexBIG.test.LexEvsTestRunner.LoadContent;
import org.LexGrid.LexBIG.test.LexEvsTestRunner.LoadContents;
import org.junit.Ignore;
import org.junit.Test;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.plugin.service.lexevs.test.AbstractQueryServiceTest;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.QueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;

/**
 *  @author <a href="mailto:frutiger.kim@mayo.edu">Kim Frutiger</a>
 *  @author <a href="mailto:hardie.linda@mayo.edu">Linda Hardie</a>
 *
 */
@Ignore
@LoadContents({
@LoadContent(contentPath="lexevs/test-content/German_Made_Parts.xml"),
@LoadContent(contentPath="lexevs/test-content/Automobiles.xml")})
public class SearchExtensionEntityQueryServiceTestIT 
	extends AbstractQueryServiceTest<EntityListEntry, EntityDirectoryEntry, EntityDescriptionQuery> {
	
	@Resource
	private SearchExtensionEntityQueryService service;

	@Test
	public void testSetUp() {
		assertNotNull(this.service);
	}
	
	@Test
	public void testGetEntitiesFromUriList() throws Exception {
		// Create restriction for query
		// ----------------------------
		EntityDescriptionQueryServiceRestrictions restrictions = new EntityDescriptionQueryServiceRestrictions();
		
		for(String uri : Arrays.asList(
				"urn:oid:11.11.0.1:C0001",
				"urn:oid:11.11.0.1:A0001",
				"urn:oid:11.11.0.2:H0001"
				)){
			EntityNameOrURI nameOrUri = new EntityNameOrURI();
			nameOrUri.setUri(uri);
			
			restrictions.getEntities().add(nameOrUri);
		}

		// Create query, no filters
		// -------------------------
		EntityDescriptionQuery query = new EntityDescriptionQueryImpl(null, null, restrictions);	
		
		// Call getResourceSummaries from service
		// --------------------------------------
		Page page = new Page();		
		DirectoryResult<EntityDirectoryEntry> directoryResult = this.service.getResourceSummaries(query, null, page);
		
		// Test results
		// ------------
		assertEquals(3, directoryResult.getEntries().size());		
	}

	@Ignore
	@Override
	public void testQueryLists(){
		//no-op -- this service can't resolve lists -- will delegate to the other query service.
	}
	
	@Override
	protected QueryService<EntityListEntry, EntityDirectoryEntry, EntityDescriptionQuery> getService() {
		return this.service;
	}

	@Override
	protected EntityDescriptionQuery getQuery() {
		return new EntityDescriptionQueryImpl(null,null,new EntityDescriptionQueryServiceRestrictions());
	}

}

