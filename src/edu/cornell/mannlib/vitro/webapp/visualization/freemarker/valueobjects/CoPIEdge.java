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
package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UniqueIDGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;

/**
 * This stores edge information for Co-PI vis.
 * @author bkoniden
 * Deepak Konidena
 */
public class CoPIEdge {
	
	private int edgeID;
	private Map<String, Integer> yearToGrantCount;
	private Set<Grant> collaboratorGrants = new HashSet<Grant>();
	private CoPINode sourceNode;
	private CoPINode targetNode;
	
	public CoPIEdge(CoPINode sourceNode, CoPINode targetNode, Grant seedCoPIedGrant, UniqueIDGenerator uniqueIDGenerator){
		edgeID = uniqueIDGenerator.getNextNumericID();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.collaboratorGrants.add(seedCoPIedGrant);
	}
	
	public int getEdgeID() {
		return edgeID;
	}
	public Set<Grant> getCollaboratorGrants() {
		return collaboratorGrants;
	}
	public CoPINode getSourceNode() {
		return sourceNode;
	}
	public CoPINode getTargetNode() {
		return targetNode;
	}
	
	public int getNumberOfCoInvestigatedGrants(){
		return collaboratorGrants.size();
	}
	
	public void addCollaboratorGrant(Grant grant){
		this.collaboratorGrants.add(grant);
	}
	
	/*
	 * getEarliest, Latest & Unknown Grant YearCount should only be used after 
	 * the parsing of the entire sparql is done. Else it will give results based on
	 * incomplete dataset.
	 * */
	@SuppressWarnings("serial")
	public Map<String, Integer> getEarliestCollaborationYearCount() {
		if (yearToGrantCount == null) {
			yearToGrantCount = UtilityFunctions.getYearToGrantCount(collaboratorGrants);
		}
		
		/*
		 * We do not want to consider the default grant year when we are checking 
		 * for the min or max grant year. 
		 * */
		Set<String> yearsToBeConsidered = new HashSet<String>(yearToGrantCount.keySet());
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
			final Integer earliestYearGrantCount = yearToGrantCount.get(earliestYear);
			
			return new HashMap<String, Integer>() { {
				put(earliestYear, earliestYearGrantCount);
			} };
		} else {
			return null;
		}
	}
	
	
	@SuppressWarnings("serial")
	public Map<String, Integer> getLatestCollaborationYearCount() {
		if (yearToGrantCount == null) {
			yearToGrantCount = UtilityFunctions.getYearToGrantCount(collaboratorGrants);
		}
		
		/*
		 * We do not want to consider the default grant year when we are checking 
		 * for the min or max grant year. 
		 * */
		Set<String> yearsToBeConsidered = new HashSet<String>(yearToGrantCount.keySet());
		yearsToBeConsidered.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * There can be a case when the only grant the PI has no attached year to it
		 * so essentially an "Unknown". In that case Collections.max or min will throw an 
		 * NoSuchElementException.
		 * 
		 * If there is no maximum year available then we should imply so by returning a "null".
		 * */
		if (yearsToBeConsidered.size() > 0) {
			final String latestYear = Collections.max(yearsToBeConsidered);
			final Integer latestYearGrantCount = yearToGrantCount.get(latestYear);
			
			return new HashMap<String, Integer>() { {
				put(latestYear, latestYearGrantCount);
			} };
		} else {
			return null;
		}
		
	}
	
	public Integer getUnknownCollaborationYearCount() {
		if (yearToGrantCount == null) {
			yearToGrantCount = UtilityFunctions.getYearToGrantCount(collaboratorGrants);
		}
		
		Integer unknownYearGrantCount = yearToGrantCount
											.get(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * If there is no unknown year available then we should imply so by returning a "null".
		 * */
		if (unknownYearGrantCount != null) {
			return unknownYearGrantCount;
		} else {
			return null;
		}
	}
	
}
