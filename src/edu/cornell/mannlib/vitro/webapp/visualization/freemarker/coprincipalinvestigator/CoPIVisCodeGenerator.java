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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coprincipalinvestigator;

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
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoPINode;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SparklineData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.YearToEntityCountDataElement;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;

/**
 * This class contains code for rendering sparklines and displaying tables for 
 * Co-PI visualization.
 * @author bkoniden
 * Deepak Konidena
 */
public class CoPIVisCodeGenerator {

	/*
	 * There are 2 modes of sparkline that are available via this visualization.
	 * 		1. Short Sparkline - This sparkline will render all the data points (or sparks),
	 * 			which in this case are the copi(s) over the years, from the last 10 years.
	 * 
	 * 		2. Full Sparkline - This sparkline will render all the data points (or sparks) 
	 * 			spanning the career of the person & last 10 years at the minimum, in case if
	 * 			the person started his career in the last 10 years.
	 * */
	private static final String DEFAULT_VISCONTAINER_DIV_ID = "unique_coinvestigators_vis_container";
	
	private Map<String, Set<CoPINode>> yearToUniqueCoPIs;

	private Log log;

	private SparklineData sparklineParameterVO;

	private String individualURI;

	public CoPIVisCodeGenerator(String individualURI, 
			  String visMode, 
			  String visContainer, 
			  Map<String, Set<CoPINode>> yearToUniqueCoPIs, 
			  Log log){
		
		this.individualURI = individualURI;
		
		this.yearToUniqueCoPIs = yearToUniqueCoPIs;

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
				- VisConstants.MINIMUM_YEARS_CONSIDERED_FOR_SPARKLINE + 1;

		/*
		 * This is required because when deciding the range of years over which
		 * the vis was rendered we dont want to be influenced by the
		 * "DEFAULT_GRANT_YEAR".
		 */
		Set<String> investigatedYears = new HashSet<String>(yearToUniqueCoPIs
				.keySet());
		investigatedYears.remove(VOConstants.DEFAULT_GRANT_YEAR);

		/*
		 * We are setting the default value of minGrantYear to be 10 years
		 * before the current year (which is suitably represented by the
		 * shortSparkMinYear), this in case we run into invalid set of investigated
		 * years.
		 */
		int minGrantYear = shortSparkMinYear;

		String visContainerID = null;

		if (yearToUniqueCoPIs.size() > 0) {
			try {
				minGrantYear = Integer.parseInt(Collections
						.min(investigatedYears));
			} catch (NoSuchElementException e1) {
				log.debug("vis: " + e1.getMessage() + " error occurred for "
						+ yearToUniqueCoPIs.toString());
			} catch (NumberFormatException e2) {
				log.debug("vis: " + e2.getMessage() + " error occurred for "
						+ yearToUniqueCoPIs.toString());
			}
		}

		int minGrantYearConsidered = 0;

		/*
		 * There might be a case that the person investigated his first grant
		 * within the last 10 years but we want to make sure that the sparkline
		 * is representative of at least the last 10 years, so we will set the
		 * minGrantYearConsidered to "currentYear - 10" which is also given by
		 * "shortSparkMinYear".
		 */
		if (minGrantYear > shortSparkMinYear) {
			minGrantYearConsidered = shortSparkMinYear;
		} else {
			minGrantYearConsidered = minGrantYear;
		}

		numOfYearsToBeRendered = currentYear - minGrantYearConsidered + 1;
		
		sparklineData.setNumOfYearsToBeRendered(numOfYearsToBeRendered);
		
		int uniqueCoPICounter = 0;
		Set<CoPINode> allCoPIsWithKnownGrantShipYears = new HashSet<CoPINode>();
		List<YearToEntityCountDataElement> yearToUniqueInvestigatorsCountDataTable = new ArrayList<YearToEntityCountDataElement>();

		for (int grantYear = minGrantYearConsidered; grantYear <= currentYear; grantYear++) {

			String grantYearAsString = String.valueOf(grantYear);
			Set<CoPINode> currentCoPIs = yearToUniqueCoPIs
					.get(grantYearAsString);

			Integer currentUniqueCoPIs = null;

			if (currentCoPIs != null) {
				currentUniqueCoPIs = currentCoPIs.size();
				allCoPIsWithKnownGrantShipYears.addAll(currentCoPIs);
			} else {
				currentUniqueCoPIs = 0;
			}

			yearToUniqueInvestigatorsCountDataTable.add(new YearToEntityCountDataElement(uniqueCoPICounter, 
					grantYearAsString, 
					currentUniqueCoPIs));
			
			uniqueCoPICounter++;
		}
		
		/*
		 * For the purpose of this visualization I have come up with a term
		 * "Sparks" which essentially means data points. Sparks that will be
		 * rendered in full mode will always be the one's which have any year
		 * associated with it. Hence.
		 */
		sparklineData.setRenderedSparks(allCoPIsWithKnownGrantShipYears.size());
		
		sparklineData.setYearToEntityCountDataTable(yearToUniqueInvestigatorsCountDataTable);

		/*
		 * This is required only for the sparklines which convey collaborationships like coinvestigatorships
		 * and coauthorship. There are edge cases where a collaborator can be present for in a collaboration
		 * with known & unknown year. We do not want to repeat the count for this collaborator when we present 
		 * it in the front-end. 
		 * */
		Set<CoPINode> totalUniqueCoInvestigators = new HashSet<CoPINode>(allCoPIsWithKnownGrantShipYears);
		
		/*
		 * Total grants will also consider grants that have no year
		 * associated with them. Hence.
		 */
		Integer unknownYearGrants = 0;
		if (yearToUniqueCoPIs.get(VOConstants.DEFAULT_GRANT_YEAR) != null) {
			unknownYearGrants = yearToUniqueCoPIs.get(
					VOConstants.DEFAULT_GRANT_YEAR).size();
			
			totalUniqueCoInvestigators.addAll(yearToUniqueCoPIs.get(VOConstants.DEFAULT_GRANT_YEAR));
			
		}
		
		sparklineData.setTotalCollaborationshipCount(totalUniqueCoInvestigators.size());
		
		sparklineData.setUnknownYearGrants(unknownYearGrants);

		if (providedVisContainerID != null) {
			visContainerID = providedVisContainerID;
		} else {
			visContainerID = DEFAULT_VISCONTAINER_DIV_ID;
		}

		sparklineData.setVisContainerDivID(visContainerID);
		
		/*
		 * By default these represents the range of the rendered sparks. Only in
		 * case of "short" sparkline mode we will set the Earliest
		 * RenderedGrant year to "currentYear - 10".
		 */
		sparklineData.setEarliestYearConsidered(minGrantYearConsidered);
		sparklineData.setEarliestRenderedGrantYear(minGrantYear);
		sparklineData.setLatestRenderedGrantYear(currentYear);

		/*
		 * The Full Sparkline will be rendered by default. Only if the url has
		 * specific mention of SHORT_SPARKLINE_MODE_KEY then we render the short
		 * sparkline and not otherwise.
		 */
		if (VisualizationFrameworkConstants.SHORT_SPARKLINE_VIS_MODE
				.equalsIgnoreCase(visMode)) {

			sparklineData.setEarliestRenderedGrantYear(shortSparkMinYear);
			sparklineData.setShortVisMode(true);
			
		} else {
			sparklineData.setShortVisMode(false);
		}
		
		if (yearToUniqueCoPIs.size() > 0) {
			
			sparklineData.setFullTimelineNetworkLink(UtilityFunctions.getCollaboratorshipNetworkLink(individualURI,
					VisualizationFrameworkConstants.PERSON_LEVEL_VIS,
					VisualizationFrameworkConstants.COPI_VIS_MODE));
			
			sparklineData.setDownloadDataLink(UtilityFunctions
													.getCSVDownloadURL(
															individualURI, 
															VisualizationFrameworkConstants.CO_PI_VIS,
															VisualizationFrameworkConstants.COPIS_COUNT_PER_YEAR_VIS_MODE));
			
			Map<String, Integer> yearToUniqueCoPIsCount = new HashMap<String, Integer>();
			for (Map.Entry<String, Set<CoPINode>> currentYearToUniqueCoPIsCount : yearToUniqueCoPIs.entrySet()) {
				yearToUniqueCoPIsCount.put(currentYearToUniqueCoPIsCount.getKey(), 
											   currentYearToUniqueCoPIsCount.getValue().size());
			}

			sparklineData.setYearToActivityCount(yearToUniqueCoPIsCount);
			
		}

		return sparklineData;
	}

	public SparklineData getValueObjectContainer() {
		return this.sparklineParameterVO;
	}
}
