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

package edu.cornell.mannlib.vitro.webapp.config;

import static edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.DUMMY_BEAN;
import static edu.cornell.mannlib.vitro.webapp.config.RevisionInfoSetup.DATE_FORMAT;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpSessionStub;

import com.ibm.icu.text.SimpleDateFormat;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;

/**
 * Test for RevisionInfoSetup
 */
public class RevisionInfoSetupTest extends AbstractTestClass {
	private ServletContextStub context;
	private HttpSessionStub session;
	private ServletContextListener listener;
	private ServletContextEvent event;

	@Before
	public void setupContext() {
		context = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(context);

		event = new ServletContextEvent(context);
	}

	@Before
	public void createContextListener() {
		listener = new RevisionInfoSetup();
	}

	@Before
	public void suppressInfoMessages() {
		setLoggerLevel(RevisionInfoBean.class, Level.WARN);
	}

	@Test
	public void noResourceFile() {
		setLoggerLevel(RevisionInfoSetup.class, Level.OFF);
		testThisExpectedFailure("no resource", null);
	}

	@Test
	public void resourceFileIsEmpty() {
		setLoggerLevel(RevisionInfoSetup.class, Level.OFF);
		testThisExpectedFailure("empty resource", "");
	}

	@Test
	public void resourceFileHasNoSignificantLines() {
		setLoggerLevel(RevisionInfoSetup.class, Level.OFF);
		testThisExpectedFailure("no siginificant lines", "    \n    #   \n\n");
	}

	@Test
	public void resourceFileHasInvalidDateLine() {
		setLoggerLevel(RevisionInfoSetup.class, Level.OFF);
		testThisExpectedFailure("invalid date line", "BOGUS DATE LINE\n"
				+ "name ~ release ~ revision");
	}

	@Test
	public void resourceFileHasInvalidLevelLine() {
		setLoggerLevel(RevisionInfoSetup.class, Level.OFF);
		testThisExpectedFailure("invalid level line", "2010-10-13 23:55:00\n"
				+ "name ~ release ~revision");
	}

	@Test
	public void simpleSingleLevel() {
		testThisResourceFile(
				"simple single level",
				"2010-10-13 23:55:00\n" + "name ~ release ~ revision",
				bean(date("2010-10-13 23:55:00"),
						level("name", "release", "revision")));
	}

	@Test
	public void ignoreWhiteSpaceAroundDate() {
		testThisResourceFile(
				"white space around date",
				"   1999-01-01 00:00:00    \n" + "name ~ release ~ revision",
				bean(date("1999-01-01 00:00:00"),
						level("name", "release", "revision")));
	}

	@Test
	public void ignoreWhiteSpaceInLevelInfo() {
		testThisResourceFile(
				"white space in level info",
				"2010-10-13 23:55:00\n"
						+ "   name   ~ release   ~   revision  ",
				bean(date("2010-10-13 23:55:00"),
						level("name", "release", "revision")));
	}

	@Test
	public void ignoreBlankLinesAndComments() {
		testThisResourceFile(
				"ignore empty lines",
				"2010-10-13 23:55:00\n" + "\n" + "     \n" + "   #   \n"
						+ "name ~ release ~ revision",
				bean(date("2010-10-13 23:55:00"),
						level("name", "release", "revision")));
	}

	@Test
	public void parseMultipleLevels() {
		testThisResourceFile(
				"multiple levels",
				"2010-10-13 23:55:00\n" + "name ~ release ~ revision\n"
						+ "name2 ~ release2 ~ revision2\n",
				bean(date("2010-10-13 23:55:00"),
						level("name", "release", "revision"),
						level("name2", "release2", "revision2")));
	}

	@Test
	public void parseNoLevels() {
		testThisResourceFile("no levels", "2010-10-13 23:55:00\n",
				bean(date("2010-10-13 23:55:00")));
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private RevisionInfoBean bean(Date date, LevelRevisionInfo... levels) {
		return new RevisionInfoBean(date, Arrays.asList(levels));
	}

	private LevelRevisionInfo level(String name, String release, String revision) {
		return new LevelRevisionInfo(name, release, revision);
	}

	private Date date(String string) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(string);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Can't parse this date string: '" + string + "'");
		}
	}

	/**
	 * Test these file contents, compare to this expected bean.
	 */
	private void testThisResourceFile(String message, String fileContents,
			RevisionInfoBean expected) {
		context.setMockResource(RevisionInfoSetup.RESOURCE_PATH, fileContents);

		listener.contextInitialized(event);
		assertEquals(message, expected, RevisionInfoBean.getBean(session));
	}

	/**
	 * Test these file contents, expect the dummy bean as a result.
	 */
	private void testThisExpectedFailure(String message, String fileContents) {
		testThisResourceFile(message, fileContents, DUMMY_BEAN);
	}

}
