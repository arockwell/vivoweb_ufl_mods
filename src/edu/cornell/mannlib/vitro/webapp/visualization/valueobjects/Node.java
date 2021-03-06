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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.UniqueIDGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.UtilityFunctions;

/**
 * 
 * This is the Value Object for storing node information mainly for co-author vis.
 * @author cdtank
 *
 */
public class Node extends Individual {

	private int nodeID;
	private Map<String, Integer> yearToPublicationCount;

	private Set<BiboDocument> authorDocuments = new HashSet<BiboDocument>();

	public Node(String nodeURL,
				UniqueIDGenerator uniqueIDGenerator) {
		super(nodeURL);
		nodeID = uniqueIDGenerator.getNextNumericID();
	}

	public int getNodeID() {
		return nodeID;
	}
	
	public String getNodeURL() {
		return this.getIndividualURL();
	}

	public String getNodeName() {
		return this.getIndividualLabel();
	}
	
	public void setNodeName(String nodeName) {
		this.setIndividualLabel(nodeName);
	}
	
	public Set<BiboDocument> getAuthorDocuments() {
		return authorDocuments;
	}
	
	public int getNumOfAuthoredWorks() {
		return authorDocuments.size();
	}

	public void addAuthorDocument(BiboDocument authorDocument) {
		this.authorDocuments.add(authorDocument);
	}
	
	
	public Map<String, Integer> getYearToPublicationCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = UtilityFunctions.getYearToPublicationCount(authorDocuments);
		}
		return yearToPublicationCount;
	}
	/*
	 * getEarliest, Latest & Unknown Publication YearCount should only be used after 
	 * the parsing of the entire sparql is done. Else it will give results based on
	 * incomplete dataset.
	 * */
	@SuppressWarnings("serial")
	public Map<String, Integer> getEarliestPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = UtilityFunctions.getYearToPublicationCount(authorDocuments);
		}
		
		/*
		 * We do not want to consider the default publication year when we are checking 
		 * for the min or max publication year. 
		 * */
		Set<String> yearsToBeConsidered = new HashSet<String>(yearToPublicationCount.keySet());
		yearsToBeConsidered.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * There can be a case when the only publication the author has no attached year to it
		 * so essentially an "Unknown". In that case Collections.max or min will throw an 
		 * NoSuchElementException.
		 * 
		 * If there is no maximum year available then we should imply so by returning a "null".
		 * */
		if (yearsToBeConsidered.size() > 0) {
			final String earliestYear = Collections.min(yearsToBeConsidered);
			final Integer earliestYearPubCount = yearToPublicationCount.get(earliestYear);
			
			return new HashMap<String, Integer>() { {
				put(earliestYear, earliestYearPubCount);
			} };
		} else {
			return null;
		}
	}

	@SuppressWarnings("serial")
	public Map<String, Integer> getLatestPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = UtilityFunctions.getYearToPublicationCount(authorDocuments);
		}
		
		/*
		 * We do not want to consider the default publication year when we are checking 
		 * for the min or max publication year. 
		 * */
		Set<String> yearsToBeConsidered = new HashSet<String>(yearToPublicationCount.keySet());
		yearsToBeConsidered.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * There can be a case when the only publication the author has no attached year to it
		 * so essentially an "Unknown". In that case Collections.max or min will throw an 
		 * NoSuchElementException.
		 * 
		 * If there is no maximum year available then we should imply so by returning a "null".
		 * */
		if (yearsToBeConsidered.size() > 0) {
			final String latestYear = Collections.max(yearsToBeConsidered);
			final Integer latestYearPubCount = yearToPublicationCount.get(latestYear);
			
			return new HashMap<String, Integer>() { {
				put(latestYear, latestYearPubCount);
			} };
		} else {
			return null;
		}
		
	}
	
	public Integer getUnknownPublicationYearCount() {
		if (yearToPublicationCount == null) {
			yearToPublicationCount = UtilityFunctions.getYearToPublicationCount(authorDocuments);
		}
		
		Integer unknownYearPubCount = yearToPublicationCount
											.get(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * If there is no unknown year available then we should imply so by returning a "null".
		 * */
		if (unknownYearPubCount != null) {
			return unknownYearPubCount;
		} else {
			return null;
		}
	}
	

}
