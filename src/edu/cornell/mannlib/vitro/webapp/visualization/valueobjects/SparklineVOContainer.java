/*
Copyright (c) 2010, Cornell University
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

package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

public class SparklineVOContainer {
	
	/*
	 * For now sparklineNumPublicationsText & sparklinePublicationRangeText is left 
	 * as empty but later on we would want to leverage the granularity that this 
	 * provides.
	 * */
	private String sparklineNumPublicationsText = "";
	private String sparklinePublicationRangeText = "";
	
	private Integer earliestRenderedPublicationYear;
	private Integer latestRenderedPublicationYear;
	
	private String table = "";
	
	private String downloadDataLink = "";
	private String fullTimelineNetworkLink = "";
	
	private String sparklineContent;
	private String sparklineContext;
	
	public String getSparklineNumPublicationsText() {
		return sparklineNumPublicationsText;
	}
	public void setSparklineNumPublicationsText(String sparklineNumPublicationsText) {
		this.sparklineNumPublicationsText = sparklineNumPublicationsText;
	}
	public String getSparklinePublicationRangeText() {
		return sparklinePublicationRangeText;
	}
	public void setSparklinePublicationRangeText(
			String sparklinePublicationRangeText) {
		this.sparklinePublicationRangeText = sparklinePublicationRangeText;
	}
	public Integer getEarliestRenderedPublicationYear() {
		return earliestRenderedPublicationYear;
	}
	public void setEarliestRenderedPublicationYear(
			Integer earliestRenderedPublicationYear) {
		this.earliestRenderedPublicationYear = earliestRenderedPublicationYear;
	}
	public Integer getLatestRenderedPublicationYear() {
		return latestRenderedPublicationYear;
	}
	public void setLatestRenderedPublicationYear(
			Integer latestRenderedPublicationYear) {
		this.latestRenderedPublicationYear = latestRenderedPublicationYear;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getDownloadDataLink() {
		return downloadDataLink;
	}
	public void setDownloadDataLink(String downloadDataLink) {
		this.downloadDataLink = downloadDataLink;
	}
	public String getFullTimelineNetworkLink() {
		return fullTimelineNetworkLink;
	}
	public void setFullTimelineNetworkLink(String fullTimelineNetworkLink) {
		this.fullTimelineNetworkLink = fullTimelineNetworkLink;
	}
	
	public String getSparklineContent() {
		return sparklineContent;
	}
	public void setSparklineContent(String shortSparklineContent) {
		this.sparklineContent = shortSparklineContent;
	}

	public String getSparklineContext() {
		return sparklineContext;
	}
	public void setSparklineContext(String shortSparklineContext) {
		this.sparklineContext = shortSparklineContext;
	}

}
