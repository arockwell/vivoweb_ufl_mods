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

package edu.cornell.mannlib.vitro.utilities.testrunner;

/**
 * Status for each test, each suite, and the entire run.
 */
public enum Status {
	/** All tests passed, and there were no warnings or errors. */
	OK("good"),

	/**
	 * One or more tests have not been run yet.
	 */
	PENDING("pending"),

	/**
	 * Will not run because it is ignored, or has run and failed but the failure
	 * is ignored.
	 */
	IGNORED("fair"),

	/**
	 * A test failed and could not be ignored, or an error message was
	 * generated.
	 */
	ERROR("bad");

	private final String htmlClass;

	private Status(String htmlClass) {
		this.htmlClass = htmlClass;
	}

	public String getHtmlClass() {
		return this.htmlClass;
	}

	/** When combined, the more severe status (defined later) takes precedence. */
	public static Status combine(Status s1, Status s2) {
		if (s1.compareTo(s2) > 0) {
			return s1;
		} else {
			return s2;
		}
	}

	/** Anything except ERROR is considered to be a success. */
	public static boolean isSuccess(Status status) {
		return status != Status.ERROR;
	}
}
