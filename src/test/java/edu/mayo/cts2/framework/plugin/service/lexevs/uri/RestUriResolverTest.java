package edu.mayo.cts2.framework.plugin.service.lexevs.uri;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import edu.mayo.cts2.framework.plugin.service.lexevs.uri.UriResolver.IdType;

public class RestUriResolverTest {

	RestUriResolver resolver;

	@Before
	public void SetUp() {
		resolver = new RestUriResolver(
				"https://informatics.mayo.edu/cts2/services/uriresolver");
	}

	@Test
	public void TestIdToName() {
		assertEquals("LNC", resolver.idToName(
				"http://id.nlm.nih.gov/cui/C1136323", IdType.CODE_SYSTEM));
	}

	@Test
	public void TestIdToUri() {
		assertEquals("http://id.nlm.nih.gov/cui/C1136323",
				resolver.idToUri("LNC", IdType.CODE_SYSTEM));
	}

	@Test
	public void TestIdToBaseUri() {
		assertEquals("http://id.nlm.nih.gov/cui/C1136323/",
				resolver.idToBaseUri("LNC"));
	}

	@Test
	public void TestIdAndVersionToUri() {
		assertEquals("http://id.nlm.nih.gov/cui/C3260726",
				resolver.idAndVersionToVersionUri("LNC", "238",
						IdType.CODE_SYSTEM));
	}

	@Test
	public void TestIdAndVersionToName() {
		assertEquals("LNC238", resolver.idAndVersionToVersionName("LNC", "238",
				IdType.CODE_SYSTEM));
	}

}