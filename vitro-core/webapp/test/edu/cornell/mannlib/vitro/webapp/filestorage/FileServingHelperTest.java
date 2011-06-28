/*
Copyright (c) 2011, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.filestorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import stubs.javax.naming.InitialContextStub;
import stubs.javax.naming.spi.InitialContextFactoryStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;

/**
 */
public class FileServingHelperTest extends AbstractTestClass {
	private static final String DEFAULT_NAMESPACE = "http://some.crazy.domain/individual/";
	private static final String CONFIG_PROPERTIES = "#mock config properties file\n";
	private static File tempDir;

	// ----------------------------------------------------------------------
	// framework
	// ----------------------------------------------------------------------

	/**
	 * Use a mock {@link InitialContext} to create an empty
	 * {@link ConfigurationProperties} object. Each test can use
	 * {@link #setConfigurationProperties(String, String)} to populate it as
	 * they choose.
	 */
	@BeforeClass
	public static void createConfigurationProperties() throws Exception {
		tempDir = createTempDirectory("FileServingHelperTest");

		File propsFile = createFile(tempDir, "config.properties",
				CONFIG_PROPERTIES);

		System.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				InitialContextFactoryStub.class.getName());
		InitialContextStub.reset();
		new InitialContext().bind("java:comp/env/path.configuration", propsFile
				.getPath());

		setConfigurationProperties(DEFAULT_NAMESPACE);
	}

	@AfterClass
	public static void cleanup() {
		purgeDirectoryRecursively(tempDir);
	}

	// ----------------------------------------------------------------------
	// tests
	// ----------------------------------------------------------------------

	@Test
	public void nullUri() {
		assertCorrectUrl(null, "somefilename.ext", null);
	}

	@Test
	public void nullFilename() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324", null,
				null);
	}

	@Test
	public void notInDefaultNamespace() {
		setLoggerLevel(FileServingHelper.class, Level.ERROR);
		assertCorrectUrl("notInTheNamespace",
				"somefilename.ext", "notInTheNamespace");
	}

	@Test
	public void inDefaultNamespaceNoTrailingSlash() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324",
				"somefilename.ext", "/file/n4324/somefilename.ext");
	}

	@Test
	public void inDefaultNamespaceTrailingSlash() {
		assertCorrectUrl("http://some.crazy.domain/individual/n4324/",
				"somefilename.ext", "/file/n4324/somefilename.ext");
	}
	
	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private static void setConfigurationProperties(String defaultNamespace) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE, defaultNamespace);

		try {
			Field f = ConfigurationProperties.class.getDeclaredField("theMap");
			f.setAccessible(true);
			f.set(null, map);
		} catch (Exception e) {
			fail("Exception while setting config properties: " + e);
		}
	}

	private void assertCorrectUrl(String uri, String filename, String expected) {
		String actual = FileServingHelper.getBytestreamAliasUrl(uri, filename);
		assertEquals("url", expected, actual);
	}

}
