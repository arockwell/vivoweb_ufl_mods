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

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Test the methods of {@link FlatteningTemplateLoader}.
 */
public class FlatteningTemplateLoaderTest extends AbstractTestClass {
	/**
	 * TODO test plan
	 * 
	 * <pre>
	 * findTemplateSource
	 *   null arg
	 *   not found
	 *   found in top level
	 *   found in lower level
	 *   with path
	 *   
	 * getReader
	 *   get it, read it, check it, close it.
	 *   
	 * getLastModified
	 * 	 check the create date within a range
	 *   modify it and check again.
	 * 
	 * </pre>
	 */
	// ----------------------------------------------------------------------
	// setup and teardown
	// ----------------------------------------------------------------------

	private static final String SUBDIRECTORY_NAME = "sub";

	private static final String TEMPLATE_NAME_UPPER = "template.ftl";
	private static final String TEMPLATE_NAME_UPPER_WITH_PATH = "path/template.ftl";
	private static final String TEMPLATE_UPPER_CONTENTS = "The contents of the file.";

	private static final String TEMPLATE_NAME_LOWER = "another.ftl";
	private static final String TEMPLATE_LOWER_CONTENTS = "Another template file.";

	private static File tempDir;
	private static File notADirectory;
	private static File upperTemplate;
	private static File lowerTemplate;

	private FlatteningTemplateLoader loader;

	@BeforeClass
	public static void setUpFiles() throws IOException {
		notADirectory = File.createTempFile(
				FlatteningTemplateLoader.class.getSimpleName(), "");

		tempDir = createTempDirectory(FlatteningTemplateLoader.class
				.getSimpleName());
		upperTemplate = createFile(tempDir, TEMPLATE_NAME_UPPER,
				TEMPLATE_UPPER_CONTENTS);

		File subdirectory = new File(tempDir, SUBDIRECTORY_NAME);
		subdirectory.mkdir();
		lowerTemplate = createFile(subdirectory, TEMPLATE_NAME_LOWER,
				TEMPLATE_LOWER_CONTENTS);
	}

	@Before
	public void initializeLoader() {
		loader = new FlatteningTemplateLoader(tempDir);
	}

	@AfterClass
	public static void cleanUpFiles() throws IOException {
		purgeDirectoryRecursively(tempDir);
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void constructorNull() {
		new FlatteningTemplateLoader(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNonExistent() {
		new FlatteningTemplateLoader(new File("bogusDirName"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorNotADirectory() {
		new FlatteningTemplateLoader(notADirectory);
	}

	@Test
	public void findNull() throws IOException {
		Object source = loader.findTemplateSource(null);
		assertNull("find null", source);
	}

	@Test
	public void findNotFound() throws IOException {
		Object source = loader.findTemplateSource("bogus");
		assertNull("not found", source);
	}

	@Test
	public void findInTopLevel() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		assertEquals("top level", upperTemplate, source);
	}

	@Test
	public void findInLowerLevel() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_LOWER);
		assertEquals("lower level", lowerTemplate, source);
	}

	@Test
	public void findIgnoringPath() throws IOException {
		Object source = loader
				.findTemplateSource(TEMPLATE_NAME_UPPER_WITH_PATH);
		assertEquals("top level", upperTemplate, source);
	}

	@Test
	public void checkTheReader() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		Reader reader = loader.getReader(source, "UTF-8");
		String contents = readAll(reader);
		assertEquals("read the contents", contents, TEMPLATE_UPPER_CONTENTS);
	}

	/**
	 * Some systems only record last-modified times to the nearest second, so we
	 * can't rely on them changing during the course of the test. Force the
	 * change, and test for it.
	 */
	@Test
	public void teplateLastModified() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		long modified = loader.getLastModified(source);
		long now = System.currentTimeMillis();
		assertTrue("near to now: modified=" + formatTimeStamp(modified)
				+ ", now=" + formatTimeStamp(now),
				2000 > Math.abs(modified - now));

		upperTemplate.setLastModified(5000);
		assertEquals("modified modified", 5000, loader.getLastModified(source));
	}

	@Test
	public void closeDoesntCrash() throws IOException {
		Object source = loader.findTemplateSource(TEMPLATE_NAME_UPPER);
		loader.closeTemplateSource(source);
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private String formatTimeStamp(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		return formatter.format(time);
	}

}
