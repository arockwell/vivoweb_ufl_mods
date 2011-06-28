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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.personpubcount;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VisConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SparklineData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.YearToEntityCountDataElement;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;


public class PersonPublicationCountVisCodeGenerator {

	/*
	 * There are 2 modes of sparkline that are available via this visualization.
	 * 		1. Short Sparkline - This sparkline will render all the data points (or sparks),
	 * 			which in this case are the publications over the years, from the last 10 years.
	 * 
	 * 		2. Full Sparkline - This sparkline will render all the data points (or sparks) 
	 * 			spanning the career of the person & last 10 years at the minimum, in case if
	 * 			the person started his career in the last 10 yeras.
	 * */

	private static final String DEFAULT_VIS_CONTAINER_DIV_ID = "pub_count_vis_container";
	
	private Map<String, Integer> yearToPublicationCount;

	private Log log;

	private String individualURI;
	
	private SparklineData sparklineParameterVO;

	public PersonPublicationCountVisCodeGenerator(String individualURIParam, 
									  String visMode, 
									  String visContainer, 
									  Set<BiboDocument> authorDocuments, 
									  Map<String, Integer> yearToPublicationCount, 
									  Log log) {
		
		this.individualURI = individualURIParam;
		
		this.yearToPublicationCount = yearToPublicationCount;

		this.log = log;
		
		this.sparklineParameterVO = setupSparklineParameters(visMode, visContainer, authorDocuments);
		
	}
	
	/**
	 * This method is used to setup parameters for the sparkline value object. These parameters
	 * will be used in the template to construct the actual html/javascript code.
	 * @param visMode
	 * @param visContainer
	 * @param authorDocuments
	 * @return 
	 */
	private SparklineData setupSparklineParameters(String visMode,
			  							  String providedVisContainerID,
										  Set<BiboDocument> authorDocuments) {
		
		SparklineData sparklineData = new SparklineData();
		sparklineData.setYearToActivityCount(yearToPublicationCount);
		

		int numOfYearsToBeRendered = 0;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int shortSparkMinYear = currentYear 
									- VisConstants.MINIMUM_YEARS_CONSIDERED_FOR_SPARKLINE
									+ 1;
		
    	/*
    	 * This is required because when deciding the range of years over which the vis
    	 * was rendered we dont want to be influenced by the "DEFAULT_PUBLICATION_YEAR".
    	 * */
		Set<String> publishedYears = new HashSet<String>(yearToPublicationCount.keySet());
    	publishedYears.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * We are setting the default value of minPublishedYear to be 10 years before 
		 * the current year (which is suitably represented by the shortSparkMinYear),
		 * this in case we run into invalid set of published years.
		 * */
		int minPublishedYear = shortSparkMinYear;
		
		String visContainerID = null;
		
		if (yearToPublicationCount.size() > 0) {
			try {
				minPublishedYear = Integer.parseInt(Collections.min(publishedYears));
			} catch (NoSuchElementException e1) {
				log.debug("vis: " + e1.getMessage() + " error occurred for " 
								+ yearToPublicationCount.toString());
			} catch (NumberFormatException e2) {
				log.debug("vis: " + e2.getMessage() + " error occurred for " 
								+ yearToPublicationCount.toString());
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
		
		int publicationCounter = 0;
		
		/*
		 * For the purpose of this visualization I have come up with a term "Sparks" which 
		 * essentially means data points. 
		 * Sparks that will be rendered in full mode will always be the one's which have any year
		 * associated with it. Hence.
		 * */
		int renderedFullSparks = 0;

		List<YearToEntityCountDataElement> yearToPublicationCountDataTable = new ArrayList<YearToEntityCountDataElement>();
		
		for (int publicationYear = minPubYearConsidered; 
					publicationYear <= currentYear; 
					publicationYear++) {

				String stringPublishedYear = String.valueOf(publicationYear);
				Integer currentPublications = yearToPublicationCount.get(stringPublishedYear);

				if (currentPublications == null) {
					currentPublications = 0;
				}

				yearToPublicationCountDataTable.add(new YearToEntityCountDataElement(
															publicationCounter, 
															stringPublishedYear, 
															currentPublications));
				
				/*
				 * Sparks that will be rendered will always be the one's which has 
				 * any year associated with it. Hence.
				 * */
				renderedFullSparks += currentPublications;
				publicationCounter++;
				
		}
		
		sparklineData.setYearToEntityCountDataTable(yearToPublicationCountDataTable);
		sparklineData.setRenderedSparks(renderedFullSparks);
		

		/*
		 * Total publications will also consider publications that have no year associated with
		 * it. Hence.
		 * */
		Integer unknownYearPublications = 0;
		if (yearToPublicationCount.get(VOConstants.DEFAULT_PUBLICATION_YEAR) != null) {
			unknownYearPublications = yearToPublicationCount
											.get(VOConstants.DEFAULT_PUBLICATION_YEAR);
		}
		
		
		sparklineData.setUnknownYearPublications(unknownYearPublications);

		if (providedVisContainerID != null) {
			visContainerID = providedVisContainerID;
		} else {
			visContainerID = DEFAULT_VIS_CONTAINER_DIV_ID;
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
		
		
		if (yearToPublicationCount.size() > 0) {
			
			sparklineData.setFullTimelineNetworkLink(UtilityFunctions.getCollaboratorshipNetworkLink(individualURI,
					VisualizationFrameworkConstants.PERSON_LEVEL_VIS,
					VisualizationFrameworkConstants.COAUTHOR_VIS_MODE));
			
			sparklineData.setDownloadDataLink(UtilityFunctions
													.getCSVDownloadURL(
															individualURI, 
															VisualizationFrameworkConstants.PERSON_PUBLICATION_COUNT_VIS,
															""));
			
		} 
		
		/*
		 * The Full Sparkline will be rendered by default. Only if the url has specific mention of
		 * SHORT_SPARKLINE_MODE_URL_HANDLE then we render the short sparkline and not otherwise.
		 * */
		if (VisualizationFrameworkConstants.SHORT_SPARKLINE_VIS_MODE.equalsIgnoreCase(visMode)) {
			
			sparklineData.setEarliestRenderedPublicationYear(shortSparkMinYear);
			sparklineData.setShortVisMode(true);
			
		} else {
			sparklineData.setShortVisMode(false);
		}
		
		
		return sparklineData; 
	}

	public SparklineData getValueObjectContainer() {
		return this.sparklineParameterVO;
	}
}