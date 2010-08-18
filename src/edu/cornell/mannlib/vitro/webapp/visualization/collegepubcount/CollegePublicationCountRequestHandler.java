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

package edu.cornell.mannlib.vitro.webapp.visualization.collegepubcount;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.skife.csv.CSVWriter;
import org.skife.csv.SimpleWriter;

import com.hp.hpl.jena.query.DataSource;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Individual;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.VivoCollegeOrSchool;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.VivoDepartmentOrDivision;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.VivoEmployee;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.PDFDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.VisualizationRequestHandler;

public class CollegePublicationCountRequestHandler extends VisualizationRequestHandler {

	public CollegePublicationCountRequestHandler(VitroRequest vitroRequest,
			HttpServletRequest request, HttpServletResponse response, Log log) {
		
		super(vitroRequest, request, response, log);

	}

	public void generateVisualization(DataSource dataSource) {

        ServletRequest vitroRequest = super.getVitroRequest();
        
		String collegeURIParam = vitroRequest.getParameter(
										VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE);

        String renderMode = vitroRequest.getParameter(
        								VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE);
        
        String visMode = vitroRequest.getParameter(
        								VisualizationFrameworkConstants.VIS_MODE_URL_HANDLE);

        String visContainer = vitroRequest.getParameter(
        								VisualizationFrameworkConstants.VIS_CONTAINER_URL_HANDLE);

        Log log = super.getLog();
		QueryHandler<Set<VivoEmployee>> queryManager =
        	new CollegePublicationCountQueryHandler(collegeURIParam,
						     dataSource,
						     log);

		try {
			
			Set<VivoEmployee> employees = queryManager.getVisualizationJavaValueObjects();
			
			Map<VivoDepartmentOrDivision, Map<String, Integer>> departmentToPublicationsOverTime = 
				new HashMap<VivoDepartmentOrDivision, Map<String, Integer>>();
			
			Set<String> publishedYearsForCollege = new HashSet<String>();
			
			for (VivoEmployee currentEmployee : employees) {
				
				Map<String, Integer> currentEmployeeYearToPublicationCount = 
					UtilityFunctions.getYearToPublicationCount(
							currentEmployee.getAuthorDocuments());
				
				if (currentEmployeeYearToPublicationCount.size() > 0) {
					
					
					publishedYearsForCollege.addAll(currentEmployeeYearToPublicationCount.keySet());
				
					for (VivoDepartmentOrDivision currentDepartment 
								: currentEmployee.getParentDepartments()) {
						
						departmentToPublicationsOverTime
								.put(currentDepartment, 
										 getUpdatedDepartmentPublicationsOverTime(
												 currentEmployeeYearToPublicationCount,
												 departmentToPublicationsOverTime
												 		.get(currentDepartment)));
						
					}
				}
			}

	    	/*
	    	 * In order to avoid unneeded computations we have pushed this "if" condition up.
	    	 * This case arises when the render mode is data. In that case we dont want to generate 
	    	 * HTML code to render sparkline, tables etc. Ideally I would want to avoid this flow.
	    	 * It is ugly! 
	    	 * */
	    	if (VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE.equalsIgnoreCase(renderMode)) { 
				prepareVisualizationQueryDataResponse(
													  departmentToPublicationsOverTime,
													  ((CollegePublicationCountQueryHandler) queryManager).getCollegeURLToVO());
				
				log.debug(publishedYearsForCollege);
				return;
			}
	    	
	    	
	    	
	    	/*
	    	if (PDF_RENDER_MODE_URL_VALUE.equalsIgnoreCase(renderMode)) { 
				prepareVisualizationQueryPDFResponse(authorDocuments,
													  yearToPublicationCount);
				return;
			}
	    	*/
	    	
	    	/*
	    	 * Computations required to generate HTML for the sparklines & related context.
	    	 * */
	    	
	    	/*
	    	 * This is required because when deciding the range of years over which the vis
	    	 * was rendered we dont want to be influenced by the "DEFAULT_PUBLICATION_YEAR".
	    	 * */
	    	publishedYearsForCollege.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);

		} catch (MalformedQueryParametersException e) {
			try {
				handleMalformedParameters(e.getMessage());
			} catch (ServletException e1) {
				log.error(e1.getStackTrace());
			} catch (IOException e1) {
				log.error(e1.getStackTrace());
			}
			return;
		}

	}

	private Map<String, Integer> getUpdatedDepartmentPublicationsOverTime(
					Map<String, Integer> currentEmployeeYearToPublicationCount,
					Map<String, Integer> currentDepartmentYearToPublicationCount) {
		
		Map<String, Integer> departmentYearToPublicationCount;
		
		/*
		 * In case this is the first time we are consolidating publication counts 
		 * over time for a department.
		 * */
		if (currentDepartmentYearToPublicationCount == null) {

			departmentYearToPublicationCount = new TreeMap<String, Integer>();
			
		} else {
			departmentYearToPublicationCount = currentDepartmentYearToPublicationCount;
		}
		
		
		Iterator employeePubCountIterator = currentEmployeeYearToPublicationCount
													.entrySet().iterator();
		
		while (employeePubCountIterator.hasNext()) {
			Map.Entry<String, Integer> employeePubCountEntry = 
				(Map.Entry) employeePubCountIterator.next();
			
			String employeePublicationYear = employeePubCountEntry.getKey();
			Integer employeePublicationCount = employeePubCountEntry.getValue();
			
			try {
			if (departmentYearToPublicationCount.containsKey(employeePublicationYear)) {
				departmentYearToPublicationCount.put(employeePublicationYear,
															departmentYearToPublicationCount
																.get(employeePublicationYear) 
															+ employeePublicationCount);

    		} else {
    			
    			departmentYearToPublicationCount.put(employeePublicationYear, 
    												 employeePublicationCount);
    			
    		}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return departmentYearToPublicationCount;
	}

	private void prepareVisualizationQueryPDFResponse(Individual college, 
													  List<BiboDocument> authorDocuments,
													  Map<String, Integer> yearToPublicationCount) {
		
		String authorName = null; 
		
		/*
		 * To protect against cases where there are no author documents associated with the
		 * individual. 
		 * */
		if (authorDocuments.size() > 0) {
			authorName = college.getIndividualLabel();
		}
		
		/*
		 * To make sure that null/empty records for author names do not cause any mischief.
		 * */
		if (authorName == null) {
			authorName = "";
		}
		
		String outputFileName = UtilityFunctions.slugify(authorName + "-report") 
								+ ".pdf";
		
		HttpServletResponse response = super.getResponse();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment;filename=" + outputFileName);
 
			ServletOutputStream responseOutputStream;
			try {
				responseOutputStream = response.getOutputStream();
				
				
				Document document = new Document();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
				document.open();
				
				PDFDocument pdfDocument = new PDFDocument(authorName, 
														  yearToPublicationCount, 
														  document, 
														  pdfWriter);
				document.close();

				response.setHeader("Expires", "0");
				response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
				response.setHeader("Pragma", "public");
				response.setContentLength(baos.size());
				
				baos.writeTo(responseOutputStream);
				responseOutputStream.flush();
				responseOutputStream.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
	}

	private void prepareVisualizationQueryDataResponse(
			Map<VivoDepartmentOrDivision, Map<String, Integer>> departmentToPublicationsOverTime,
			Map<String, VivoCollegeOrSchool> collegeURLToVO) {

		String collegeName = null; 
		
		/*
		* To protect against cases where there are no author documents associated with the
		* individual. 
		* */

		if (collegeURLToVO.size() > 0) {
			
			collegeName = ((VivoCollegeOrSchool) collegeURLToVO.values()
									.iterator().next()).getCollegeLabel();
			
		}
		
		/*
		* To make sure that null/empty records for author names do not cause any mischief.
		* */
		if (collegeName == null) {
		collegeName = "";
		}
		
		String outputFileName = UtilityFunctions.slugify(collegeName) + "depts-pub-count" + ".csv";
		
		HttpServletResponse response = super.getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=" + outputFileName);
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		generateCsvFileBuffer(departmentToPublicationsOverTime, 
							  collegeURLToVO, 
							  responseWriter);

		responseWriter.close();
		
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	private void generateCsvFileBuffer(
			Map<VivoDepartmentOrDivision, Map<String, Integer>> departmentToPublicationsOverTime,
			Map<String, VivoCollegeOrSchool> collegeURLToVO, PrintWriter printWriter) {
		
        CSVWriter csvWriter = new SimpleWriter(printWriter);
        
        try {
			csvWriter.append(new String[]{"School", "Department", "Year", "Publications"});
			
			Iterator<VivoCollegeOrSchool> collegeIterator = collegeURLToVO.values().iterator();
			
			while (collegeIterator.hasNext()) {
				VivoCollegeOrSchool college = collegeIterator.next();
				String collegeLabel = college.getCollegeLabel();
				for (VivoDepartmentOrDivision currentDepartment : college.getDepartments()) {
					
					Map<String, Integer> currentDepartmentPublicationsOverTime = 
							departmentToPublicationsOverTime.get(currentDepartment);
					
					/*
					 * This because many departments might not have any publication.
					 * */
					if (currentDepartmentPublicationsOverTime != null) {
						
					for (Entry<String, Integer> currentEntry 
								: currentDepartmentPublicationsOverTime.entrySet()) {
						csvWriter.append(new Object[]{collegeLabel,
													  currentDepartment.getDepartmentLabel(),
													  currentEntry.getKey(), 
													  currentEntry.getValue()});
					}
					
					}
					
				}
			}
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		printWriter.flush();
		
	}

	private void prepareVisualizationQueryStandaloneResponse(HttpServletRequest request,
			HttpServletResponse response, VitroRequest vreq,
			String visContentCode, String visContextCode) {

        Portal portal = vreq.getPortal();

        request.setAttribute("visContentCode", visContentCode);
        request.setAttribute("visContextCode", visContextCode);

        request.setAttribute("bodyJsp", "/templates/visualization/publication_count.jsp");
        request.setAttribute("portalBean", portal);
        request.setAttribute("title", "Individual Publication Count Visualization");
        request.setAttribute("scripts", "/templates/visualization/visualization_scripts.jsp");

	}

	private void prepareVisualizationQueryDynamicResponse(HttpServletRequest request,
			HttpServletResponse response, VitroRequest vreq,
			String visContentCode, String visContextCode) {

        Portal portal = vreq.getPortal();

        request.setAttribute("visContentCode", visContentCode);
        request.setAttribute("visContextCode", visContextCode);

        request.setAttribute("portalBean", portal);
        request.setAttribute("bodyJsp", "/templates/visualization/ajax_vis_content.jsp");

	}

	private void handleMalformedParameters(String errorMessage)
			throws ServletException, IOException {

		Portal portal = super.getVitroRequest().getPortal();

		HttpServletRequest request = super.getRequest();
		request.setAttribute("error", errorMessage);

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp", "/templates/visualization/visualization_error.jsp");
		request.setAttribute("portalBean", portal);
		request.setAttribute("title", "Visualization Query Error - Individual Publication Count");

		try {
			requestDispatcher.forward(request, super.getResponse());
		} catch (Exception e) {
			Log log = super.getLog();
			log.error("EntityEditController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}
	}

}
