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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coauthorship;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VisConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SparklineData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.YearToEntityCountDataElement;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;


public class CoAuthorshipVisCodeGenerator {

	/*
	 * There are 2 modes of sparkline that are available via this visualization.
	 * 		1. Short Sparkline - This sparkline will render all the data points (or sparks),
	 * 			which in this case are the coauthors over the years, from the last 10 years.
	 * 
	 * 		2. Full Sparkline - This sparkline will render all the data points (or sparks) 
	 * 			spanning the career of the person & last 10 years at the minimum, in case if
	 * 			the person started his career in the last 10 yeras.
	 * */
	private static final String DEFAULT_VISCONTAINER_DIV_ID = "unique_coauthors_vis_container";
	
	private Map<String, Set<Node>> yearToUniqueCoauthors;

	private Log log;

	private SparklineData sparklineParameterVO;

	private String individualURI;

	public CoAuthorshipVisCodeGenerator(String individualURI, 
									  String visMode, 
									  String visContainer, 
									  Map<String, Set<Node>> yearToUniqueCoauthors, 
									  Log log) {
		
		this.individualURI = individualURI;
		
		this.yearToUniqueCoauthors = yearToUniqueCoauthors;
		
		this.log = log;
		
		this.sparklineParameterVO = setupSparklineParameters(visMode, visContainer);
	}
	
	/**
	 * This method is used to setup parameters for the sparkline value object. These parameters
	 * will be used in the template to construct the actual html/javascript code.
	 * @param visMode
	 * @param visContainer
	 */
	private SparklineData setupSparklineParameters(String visMode,
										    String providedVisContainerID) {
		
		SparklineData sparklineData = new SparklineData();

		int numOfYearsToBeRendered = 0;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int shortSparkMinYear = currentYear 
									- VisConstants.MINIMUM_YEARS_CONSIDERED_FOR_SPARKLINE 
									+ 1;
		
    	/*
    	 * This is required because when deciding the range of years over which the vis
    	 * was rendered we dont want to be influenced by the "DEFAULT_PUBLICATION_YEAR".
    	 * */
		Set<String> publishedYears = new HashSet<String>(yearToUniqueCoauthors.keySet());
    	publishedYears.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * We are setting the default value of minPublishedYear to be 10 years before 
		 * the current year (which is suitably represented by the shortSparkMinYear),
		 * this in case we run into invalid set of published years.
		 * */
		int minPublishedYear = shortSparkMinYear;
		
		String visContainerID = null;
		
		if (yearToUniqueCoauthors.size() > 0) {
			try {
				minPublishedYear = Integer.parseInt(Collections.min(publishedYears));
			} catch (NoSuchElementException e1) {
				log.debug("vis: " + e1.getMessage() + " error occurred for " 
							+ yearToUniqueCoauthors.toString());
			} catch (NumberFormatException e2) {
				log.debug("vis: " + e2.getMessage() + " error occurred for " 
							+ yearToUniqueCoauthors.toString());
			}
		}
		
		int minPubYearConsidered = 0;
		
		/*
		 * There might be a case that the author has made his first publication within the 
		 * last 10 years but we want to make sure that the sparkline is representative of 
		 * at least the last 10 years, so we will set the minPubYearConsidered to 
		 * "currentYear - 10" which is also given by "shortSparkMinYear".
		 * */
		if (minPublishedYear > shortSparkMinYear) {
			minPubYearConsidered = shortSparkMinYear;
		} else {
			minPubYearConsidered = minPublishedYear;
		}
		
		numOfYearsToBeRendered = currentYear - minPubYearConsidered + 1;
		
		sparklineData.setNumOfYearsToBeRendered(numOfYearsToBeRendered);
		
		int uniqueCoAuthorCounter = 0;
		Set<Node> allCoAuthorsWithKnownAuthorshipYears = new HashSet<Node>();
		List<YearToEntityCountDataElement> yearToUniqueCoauthorsCountDataTable = new ArrayList<YearToEntityCountDataElement>();
		
		for (int publicationYear = minPubYearConsidered; 
					publicationYear <= currentYear; 
					publicationYear++) {

				String publicationYearAsString = String.valueOf(publicationYear);
				Set<Node> currentCoAuthors = yearToUniqueCoauthors.get(publicationYearAsString);
				
				Integer currentUniqueCoAuthors = null;
				
				if (currentCoAuthors != null) {
					currentUniqueCoAuthors = currentCoAuthors.size();
					allCoAuthorsWithKnownAuthorshipYears.addAll(currentCoAuthors); 
				} else {
					currentUniqueCoAuthors = 0;
				}
				
				yearToUniqueCoauthorsCountDataTable.add(new YearToEntityCountDataElement(uniqueCoAuthorCounter, 
																						 publicationYearAsString, 
																						 currentUniqueCoAuthors));
				uniqueCoAuthorCounter++;
		}

		/*
		 * For the purpose of this visualization I have come up with a term "Sparks" which 
		 * essentially means data points. 
		 * Sparks that will be rendered in full mode will always be the one's which have any year
		 * associated with it. Hence.
		 * */
		sparklineData.setRenderedSparks(allCoAuthorsWithKnownAuthorshipYears.size());
		
		sparklineData.setYearToEntityCountDataTable(yearToUniqueCoauthorsCountDataTable);
		
		/*
		 * This is required only for the sparklines which convey collaborationships like coinvestigatorships
		 * and coauthorship. There are edge cases where a collaborator can be present for in a collaboration
		 * with known & unknown year. We do not want to repeat the count for this collaborator when we present 
		 * it in the front-end. 
		 * */
		Set<Node> totalUniqueCoInvestigators = new HashSet<Node>(allCoAuthorsWithKnownAuthorshipYears);

		/*
		 * Total publications will also consider publications that have no year associated with
		 * them. Hence.
		 * */
		Integer unknownYearCoauthors = 0;
		if (yearToUniqueCoauthors.get(VOConstants.DEFAULT_PUBLICATION_YEAR) != null) {
			unknownYearCoauthors = yearToUniqueCoauthors
											.get(VOConstants.DEFAULT_PUBLICATION_YEAR).size();
			
			totalUniqueCoInvestigators.addAll(yearToUniqueCoauthors.get(VOConstants.DEFAULT_GRANT_YEAR));
		}
		
		sparklineData.setUnknownYearPublications(unknownYearCoauthors);
		
		sparklineData.setTotalCollaborationshipCount(totalUniqueCoInvestigators.size());
		
		if (providedVisContainerID != null) {
			visContainerID = providedVisContainerID;
		} else {
			visContainerID = DEFAULT_VISCONTAINER_DIV_ID;
		}

		sparklineData.setVisContainerDivID(visContainerID);
		
		/*
		 * By default these represents the range of the rendered sparks. Only in case of
		 * "short" sparkline mode we will set the Earliest RenderedPublication year to
		 * "currentYear - 10". 
		 * */
		sparklineData.setEarliestYearConsidered(minPubYearConsidered);
		sparklineData.setEarliestRenderedPublicationYear(minPublishedYear);
		sparklineData.setLatestRenderedPublicationYear(currentYear);
		
		/*
		 * The Full Sparkline will be rendered by default. Only if the url has specific mention of
		 * SHORT_SPARKLINE_MODE_KEY then we render the short sparkline and not otherwise.
		 * */
		if (VisualizationFrameworkConstants.SHORT_SPARKLINE_VIS_MODE.equalsIgnoreCase(visMode)) {
			
			sparklineData.setEarliestRenderedPublicationYear(shortSparkMinYear);
			sparklineData.setShortVisMode(true);

		} else {
			sparklineData.setShortVisMode(false);
		}
		
		if (yearToUniqueCoauthors.size() > 0) {
			
			sparklineData.setFullTimelineNetworkLink(UtilityFunctions.getCollaboratorshipNetworkLink(individualURI,
					VisualizationFrameworkConstants.PERSON_LEVEL_VIS,
					VisualizationFrameworkConstants.COAUTHOR_VIS_MODE));
			
			sparklineData.setDownloadDataLink(UtilityFunctions
													.getCSVDownloadURL(
															individualURI, 
															VisualizationFrameworkConstants.COAUTHORSHIP_VIS,
															VisualizationFrameworkConstants.COAUTHORS_COUNT_PER_YEAR_VIS_MODE));
			
			Map<String, Integer> yearToUniqueCoauthorsCount = new HashMap<String, Integer>();
			
			for (Map.Entry<String, Set<Node>> currentYearToCoAuthors : yearToUniqueCoauthors.entrySet()) {
				yearToUniqueCoauthorsCount.put(currentYearToCoAuthors.getKey(), 
											   currentYearToCoAuthors.getValue().size());
			}
			
			sparklineData.setYearToActivityCount(yearToUniqueCoauthorsCount);
		}
		
		return sparklineData;
	}
	
	public SparklineData getValueObjectContainer() {
		return this.sparklineParameterVO;
	}
}