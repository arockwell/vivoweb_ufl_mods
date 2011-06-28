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

package edu.cornell.mannlib.vitro.utilities.loadtesting.reportmerger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Holds the data that was parsed from a single results file.
 */
public class TestResultsFileData {
	private final String vivoVersion;
	private final String resultsFilename;
	private final long created;
	private final LinkedHashMap<String, TestResultInfo> testMap;

	public TestResultsFileData(String vivoVersion, String resultsFilename,
			long created, Map<String, TestResultInfo> testMap) {
		this.vivoVersion = vivoVersion;
		this.resultsFilename = resultsFilename;
		this.created = created;
		this.testMap = new LinkedHashMap<String, TestResultInfo>(testMap);
	}

	public String getTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(
				created));
	}

	public String getVivoVersion() {
		return vivoVersion;
	}

	public String getResultsFilename() {
		return resultsFilename;
	}

	public long getCreated() {
		return created;
	}

	public LinkedHashMap<String, TestResultInfo> getTestMap() {
		return testMap;
	}

	@Override
	public String toString() {
		return "TestResultsFileData[vivoVersion=" + vivoVersion
				+ ", resultsFilename=" + resultsFilename + ", created="
				+ getTimeStamp() + ", testMap=" + testMap + "]";
	}

}
