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

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSpecialModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetup;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupSDB;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestWorkflowProcessor;
import edu.cornell.mannlib.vitro.webapp.utils.jena.WorkflowOntology;

public class JenaIngestController extends BaseEditController {

	private static final Log log = LogFactory.getLog(JenaIngestController.class);
	
	private OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
	
	private static final String INGEST_MENU_JSP = "/jenaIngest/ingestMenu.jsp";
	private static final String LIST_MODELS_JSP = "/jenaIngest/listModels.jsp";
	private static final String CREATE_MODEL_JSP = "/jenaIngest/createModel.jsp";
	private static final String LOAD_RDF_DATA_JSP = "/jenaIngest/loadRDFData.jsp";
	private static final String EXECUTE_SPARQL_JSP = "/jenaIngest/sparqlConstruct.jsp";
	private static final String RENAME_BNODES_JSP = "/jenaIngest/renameBNodes.jsp";
	private static final String RENAME_BNODES_URI_SELECT_JSP = "/jenaIngest/renameBNodesURISelect.jsp";
	private static final String SMUSH_JSP = "/jenaIngest/smushSingleModel.jsp";
	private static final String CONNECT_DB_JSP = "/jenaIngest/connectDB.jsp";
	private static final String CSV2RDF_JSP = "/jenaIngest/csv2rdf.jsp";
	private static final String PROCESS_STRINGS_JSP = "/jenaIngest/processStrings.jsp";
	private static final String SUBTRACT_MODELS_JSP = "/jenaIngest/subtractModels.jsp";
	private static final String SPLIT_PROPERTY_VALUES_JSP = "/jenaIngest/splitPropertyValues.jsp";
	private static final String EXECUTE_WORKFLOW_JSP = "/jenaIngest/executeWorkflow.jsp";
	private static final String WORKFLOW_STEP_JSP = "/jenaIngest/workflowStep.jsp";
	private static final String GENERATE_TBOX_JSP = "/jenaIngest/generateTBox.jsp";
	private static final String PERMANENT_URI = "/jenaIngest/permanentURI.jsp";
	private static final String MERGE_INDIVIDUALS = "/jenaIngest/mergeIndividuals.jsp";
	private static final String MERGE_RESULT = "/jenaIngest/merge_result.jsp";
	private static final String SPARQL_CONSTRUCT_CLASS = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#SPARQLCONSTRUCTQuery";
	private static final String SPARQL_QUERYSTR_PROP = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#queryStr";
	private static final String RENAME_RESOURCE = "/jenaIngest/renameResource.jsp";
	private static final String RENAME_RESULT = "/jenaIngest/renameResult.jsp";

	public void doGet (HttpServletRequest request, HttpServletResponse response) {

		if (!checkLoginStatus(request,response)) {
			return;
		}
		
		VitroRequest vreq = new VitroRequest(request);
		
		ModelMaker maker = getVitroJenaModelMaker(vreq);
		
		String actionStr = vreq.getParameter("action");
		actionStr = (actionStr != null) ? actionStr : "";
		String modelType = vreq.getParameter("modelType");
		if ("listModels".equals(actionStr)) {
			String modelT = (String)getServletContext().getAttribute("modelT");
			String info = (String)getServletContext().getAttribute("info");
			if(modelT == null){
				boolean initialSwitch = new JenaDataSourceSetupBase().isSDBActive();
				if(initialSwitch){
					VitroJenaSDBModelMaker vsmm = (VitroJenaSDBModelMaker) getServletContext().getAttribute("vitroJenaSDBModelMaker");
					vreq.getSession().setAttribute("vitroJenaModelMaker", vsmm);
					modelT = "sdb";
				}
				else{
					modelT = "rdb";
				}
			}
			if(modelT.equals("rdb")){
				request.setAttribute("modelType", "rdb");
				request.setAttribute("infoLine", "RDB models");
			}
			else{
				request.setAttribute("modelType", "sdb");
				request.setAttribute("infoLine", "SDB models");
			}
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		}else if("rdbModels".equals(actionStr)){
			VitroJenaModelMaker vjmm = (VitroJenaModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
			vreq.getSession().setAttribute("vitroJenaModelMaker", vjmm);
		    getServletContext().setAttribute("modelT", "rdb");
		    getServletContext().setAttribute("info", "RDB models");
        	request.setAttribute("modelType", "rdb");
        	request.setAttribute("infoLine", "RDB models");
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		}else if("sdbModels".equals(actionStr)){
			VitroJenaSDBModelMaker vsmm = (VitroJenaSDBModelMaker) getServletContext().getAttribute("vitroJenaSDBModelMaker");
			vreq.getSession().setAttribute("vitroJenaModelMaker", vsmm);
        	getServletContext().setAttribute("modelT", "sdb");
 		    getServletContext().setAttribute("info", "SDB models");
        	request.setAttribute("modelType", "sdb");
        	request.setAttribute("infoLine", "SDB models");
        	request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("createModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				if(modelType.equals("sdb")){
		        	maker = (VitroJenaSDBModelMaker) getServletContext().getAttribute("vitroJenaSDBModelMaker");
		        	request.setAttribute("modelType", "sdb");
		        	request.setAttribute("infoLine", "SDB models");
				}
				else{
					request.setAttribute("modelType", "rdb");
					request.setAttribute("infoLine", "RDB models");
				}
				doCreateModel(modelName, maker);
				request.setAttribute("title","Available Models");
				request.setAttribute("bodyJsp",LIST_MODELS_JSP);
			} else {
				request.setAttribute("modelType", modelType);
				request.setAttribute("title","Create New Model");
				request.setAttribute("bodyJsp",CREATE_MODEL_JSP);
			}
		} else if ("removeModel".equals(actionStr)) {
			if(modelType.equals("sdb")){
				request.setAttribute("modelType", "sdb");
				request.setAttribute("infoLine", "SDB models");
			}
			else{
				request.setAttribute("modelType", "rdb");
				request.setAttribute("infoLine", "RDB models");
			}
			String modelName = vreq.getParameter("modelName");
			if (modelName!=null) {
				doRemoveModel(modelName, maker);
			}
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("loadRDFData".equals(actionStr)) {
			String docLoc = vreq.getParameter("docLoc");
			String filePath = vreq.getParameter("filePath");
			String modelName = vreq.getParameter("modelName");
			String languageParam = null;
			String language = ( (languageParam = vreq.getParameter("language")) != null) ? languageParam : "RDF/XML";
			if (docLoc!=null && modelName != null) {
				doLoadRDFData(modelName,docLoc,filePath,language,maker);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Load RDF Data");
				request.setAttribute("bodyJsp",LOAD_RDF_DATA_JSP);
			}
		} else if ("cleanLiterals".equals(actionStr)) {
			String modelNameStr = vreq.getParameter("modelName");
			Model model = getModel(modelNameStr,vreq);
			doCleanLiterals(model);
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else if ("outputModel".equals(actionStr)) {
			String modelNameStr = vreq.getParameter("modelName");
			Model model = getModel(modelNameStr,vreq);
			model.enterCriticalSection(Lock.READ);
			try {
				OutputStream out = response.getOutputStream();
				response.setContentType("application/x-turtle");
				//out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
				model.write(out, "TTL");
				out.flush();
				out.close();
			} catch (com.hp.hpl.jena.shared.CannotEncodeCharacterException cece) {
				// there's got to be a better way to do this
				byte[] badCharBytes = String.valueOf(cece.getBadChar()).getBytes();
				System.out.println("Cannot encode character with byte values: (decimal) ");
				for (int i=0; i<badCharBytes.length; i++) {
					System.out.println(badCharBytes[i]);
				}
			} catch (Exception e) {
				// Well if we can't write out to the response I guess there ain't much we can do.
				e.printStackTrace();
			} finally {
				model.leaveCriticalSection();
			}
			return;
		} else if ("clearModel".equals(actionStr)) {
			if(modelType.equals("sdb")){
				request.setAttribute("infoLine", "SDB models");
			}
			else{
				request.setAttribute("infoLine", "RDB models");
			}
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doClearModel(modelName,maker);
			}
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} 
		/*else if ("setWriteLayer".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				OntModel mainModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
				WebappDaoFactoryJena existingDaoFactory = null;
				try {
					existingDaoFactory = (WebappDaoFactoryJena) getServletContext().getAttribute("webappDaoFactory");
				} catch (Exception e) {}
				Model writeModel = maker.getModel(modelName);
				Model dynamicUnion = ModelFactory.createUnion(writeModel,mainModel);
				OntModel ontModelForDaos = ModelFactory.createOntologyModel(ONT_MODEL_SPEC, dynamicUnion);
				WebappDaoFactory wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector(ontModelForDaos), (existingDaoFactory != null) ? existingDaoFactory.getDefaultNamespace() : null, null, null);
				request.getSession().setAttribute("webappDaoFactory", wadf);
				request.getSession().setAttribute("jenaOntModel",ontModelForDaos);
				System.out.println("Setting jenaOntModel session attribute");
				Model baseModel = (OntModel) getServletContext().getAttribute("baseOntModel");
				OntModel ontModelForAssertions = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,ModelFactory.createUnion(writeModel,baseModel));
				request.getSession().setAttribute("assertionsWebappDaoFactory", new WebappDaoFactoryJena(new SimpleOntModelSelector(ontModelForAssertions)));
				request.getSession().setAttribute("baseOntModel", ontModelForAssertions);
			}
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} */
		else if ("attachModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doAttachModel(modelName,maker);
			}
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("detachModel".equals(actionStr)) {
			String modelName = vreq.getParameter("modelName");
			if (modelName != null) {
				doDetachModel(modelName,maker);
			}
			//request.setAttribute("title","Ingest Menu");
			//request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			request.setAttribute("title","Available Models");
			request.setAttribute("bodyJsp",LIST_MODELS_JSP);
		} else if ("renameBNodes".equals(actionStr)) {
			String[] sourceModel = vreq.getParameterValues("sourceModelName");
			Model model = ModelFactory.createDefaultModel();
			JenaIngestUtils utils = new JenaIngestUtils();
			if(sourceModel!=null && sourceModel.length!=0){
				Map<String,LinkedList<String>> propertyMap = utils.generatePropertyMap(sourceModel, model, maker);
				getServletContext().setAttribute("sourceModel",sourceModel);
				request.setAttribute("propertyMap", propertyMap);
				request.setAttribute("title","URI Select");
				request.setAttribute("bodyJsp",RENAME_BNODES_URI_SELECT_JSP);
			} else {
				request.setAttribute("title","Rename Blank Nodes");
				request.setAttribute("bodyJsp",RENAME_BNODES_JSP);
			}
		}else if("renameBNodesURISelect".equals(actionStr)){
			String namespaceEtcStr = vreq.getParameter("namespaceEtcStr");
			String pattern = vreq.getParameter("pattern");
			String concatenate = vreq.getParameter("concatenate");
			String[] sourceModel = (String[])getServletContext().getAttribute("sourceModel");
			if(namespaceEtcStr!=null && !namespaceEtcStr.isEmpty()){
				if(concatenate.equals("integer")){
					doRenameBNodes(vreq,namespaceEtcStr, false, null, sourceModel);
				}
				else{
					pattern = pattern.trim();
					doRenameBNodes(vreq,namespaceEtcStr, true, pattern, sourceModel);
				}
				request.setAttribute("title", "Ingest Menu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			}
			else{
				request.setAttribute("title", "URI Select");
				request.setAttribute("bodyJsp", RENAME_BNODES_URI_SELECT_JSP);
			}
		
		}else if ("smushSingleModel".equals(actionStr)) {
			String propertyURIStr = vreq.getParameter("propertyURI");
			if (propertyURIStr != null) {
				doSmushSingleModel(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Smush Resources");
				request.setAttribute("bodyJsp",SMUSH_JSP);
			}
		} else if ("connectDB".equals(actionStr)) {
			String jdbcUrl = vreq.getParameter("jdbcUrl");
			String tripleStore = vreq.getParameter("tripleStore");
			if (jdbcUrl != null) {
			    try {
			        doConnectDB(vreq);
			    } catch (SQLException sqle) {
			        throw new RuntimeException("Unable to connect to DB", sqle);
			    }
				if ("SDB".equals(tripleStore)) {
		        	getServletContext().setAttribute("modelT", "sdb");
		 		    getServletContext().setAttribute("info", "SDB models");
		        	request.setAttribute("modelType", "sdb");
		        	request.setAttribute("infoLine", "SDB models");
				} else {
			        getServletContext().setAttribute("modelT", "rdb");
				    getServletContext().setAttribute("info", "RDB models");
		        	request.setAttribute("modelType", "rdb");
		        	request.setAttribute("infoLine", "RDB models");
				}
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
			    List<String> dbTypes = DatabaseType.allNames();
			    Collections.sort(dbTypes, new CollationSort());
			    request.setAttribute("dbTypes", dbTypes);
				request.setAttribute("title", "Connect Jena Database");
				request.setAttribute("bodyJsp",CONNECT_DB_JSP);
			}
		} else if ("csv2rdf".equals(actionStr)) {
			String csvUrl = vreq.getParameter("csvUrl");
			if (csvUrl != null) {
				/*doExecuteCsv2Rdf(vreq);*/
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Convert CSV to RDF");
				request.setAttribute("bodyJsp",CSV2RDF_JSP);
			}
		}else if ("processStrings".equals(actionStr)) {
			String className = vreq.getParameter("className");
			if (className != null) {
				doProcessStrings(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Process Strings");
				request.setAttribute("bodyJsp",PROCESS_STRINGS_JSP);
			}
		} else if ("splitPropertyValues".equals(actionStr)) {
			String splitRegex = vreq.getParameter("splitRegex");
			if (splitRegex != null) {
				doSplitPropertyValues(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Split PropertyValues");
				request.setAttribute("bodyJsp",SPLIT_PROPERTY_VALUES_JSP);
			}
		} else if ("subtractModels".equals(actionStr)) {
			String modela = vreq.getParameter("modela");
			if (modela != null) {
				doSubtractModels(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else {
				request.setAttribute("title", "Subtract Models");
				request.setAttribute("bodyJsp",SUBTRACT_MODELS_JSP);
			}
		} else if ("executeWorkflow".equals(actionStr)) {
			String workflowURIStr = vreq.getParameter("workflowURI");
			String workflowStepURIStr = vreq.getParameter("workflowStepURI");
			if (workflowURIStr != null && workflowStepURIStr != null) {
				doExecuteWorkflow(vreq);
				request.setAttribute("title","IngestMenu");
				request.setAttribute("bodyJsp", INGEST_MENU_JSP);
			} else if (workflowURIStr != null) {
				// Select the workflow step at which to start
				OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
				request.setAttribute("workflowSteps", new JenaIngestWorkflowProcessor(jenaOntModel.getIndividual(workflowURIStr),getVitroJenaModelMaker(vreq)).getWorkflowSteps(null));
				request.setAttribute("title", "Choose Workflow Step");
				request.setAttribute("bodyJsp", WORKFLOW_STEP_JSP);
			} else {
				OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
				jenaOntModel.enterCriticalSection(Lock.READ);
				List<Individual> savedQueryList = new LinkedList<Individual>();
				try {
					Resource workflowClassRes = WorkflowOntology.Workflow;
					savedQueryList.addAll(jenaOntModel.listIndividuals(workflowClassRes).toList());
				} finally {
					jenaOntModel.leaveCriticalSection();
				}
				request.setAttribute("workflows",savedQueryList);
				request.setAttribute("title", "Execute Workflow");
				request.setAttribute("bodyJsp", EXECUTE_WORKFLOW_JSP);
			}
		} else if ("executeSparql".equals(actionStr)) {
			String sparqlQueryStr = vreq.getParameter("sparqlQueryStr");
			OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
			jenaOntModel.enterCriticalSection(Lock.READ);
			List<Individual> savedQueryList = new LinkedList<Individual>();
			try {
				Resource sparqlConstructClassRes = ResourceFactory.createResource(SPARQL_CONSTRUCT_CLASS);
				savedQueryList.addAll(jenaOntModel.listIndividuals(sparqlConstructClassRes).toList());
			} finally {
				jenaOntModel.leaveCriticalSection();
			}
			/*ass92*/
			 OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
	            List ontologiesObj = daoObj.getAllOntologies();
	            ArrayList prefixList = new ArrayList();
	            
	            if(ontologiesObj !=null && ontologiesObj.size()>0){
	            	
	            	Iterator ontItr = ontologiesObj.iterator();
	            	while(ontItr.hasNext()){
	            		Ontology ont = (Ontology) ontItr.next();
	            		prefixList.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
	            		prefixList.add(ont.getURI() == null ? "" : ont.getURI());
	            	}
	            	
	            }
	            else{
	            	prefixList.add("<strong>" + "No Ontologies added" + "</strong>");
	            	prefixList.add("<strong>" + "Load Ontologies" + "</strong>");
	            }
	            
	            request.setAttribute("prefixList", prefixList);
	            /*complete*/
			if (sparqlQueryStr != null) {
				String validationMessage = "";
				if (request.getParameterValues("sourceModelName") == null) {
					validationMessage += "<p>Please select one or more source models.</p>";
				}
				if (request.getParameter("destinationModelName") == null) {
					validationMessage += "<p>Please select a destination model</p>";
				}
				if (validationMessage.length() > 0) {
					request.setAttribute("validationMessage", validationMessage);
				} else {
					long constructedStmtCount = 0;
					try {
						constructedStmtCount = doExecuteSparql(vreq);
					} catch (QueryParseException qpe) {
						String errorMsg = "<p>Unable to parse query:</p>";
						if (qpe.getMessage() != null) {
							errorMsg += "<p>" + qpe.getMessage() + "</p>"; 
						}
						request.setAttribute("errorMsg", errorMsg);
					} catch (InconsistentOntologyException ioe) {
						String errorMsg = "<p>Inconsistent source ontology:</p>";
						if (ioe.getMessage() != null) {
							errorMsg += "<p>" + ioe.getMessage() + "</p>";
						}
						request.setAttribute("errorMsg", errorMsg);
					}
					request.setAttribute("constructedStmtCount", constructedStmtCount);
				}
				request.setAttribute("savedQueries",savedQueryList);
				request.setAttribute("title","SPARQL CONSTRUCT result");
				request.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
			} else {	
				request.setAttribute("savedQueries",savedQueryList);
				request.setAttribute("title","Execute SPARQL Construct");
				request.setAttribute("bodyJsp",EXECUTE_SPARQL_JSP);
			}
		} else if ("generateTBox".equals(actionStr)) {
			String testParam = vreq.getParameter("sourceModelName");
			if (testParam != null) {
				doGenerateTBox(vreq);
				request.setAttribute("title","Ingest Menu");
				request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			} else {
				request.setAttribute("title","Generate TBox from Assertions Data");
				request.setAttribute("bodyJsp",GENERATE_TBOX_JSP);
			}			
		}else if("permanentURI".equals(actionStr)){
			  String modelName = vreq.getParameter("modelName");
			  String oldModel = vreq.getParameter("oldModel");
			  String newModel = vreq.getParameter("newModel");
			  String oldNamespace = vreq.getParameter("oldNamespace");
			  String newNamespace = vreq.getParameter("newNamespace");
			  String dNamespace = vreq.getParameter("defaultNamespace");
			  
			  if(modelName!=null){
			  Model m = maker.getModel(modelName);
			  ArrayList namespaceList = new ArrayList();
              ResIterator resItr = m.listResourcesWithProperty((Property)null);
			  
			  if(resItr!=null){
          	    while(resItr.hasNext()){
          		String namespace = resItr.nextResource().getNameSpace();
                if(!namespaceList.contains(namespace)){
                	namespaceList.add(namespace);
                }		
          	}
          	}
			  else {
				  namespaceList.add("no resources present");
			  }
			  String defaultNamespace = vreq.getFullWebappDaoFactory().getDefaultNamespace();
			  request.setAttribute("modelName", modelName);
			  request.setAttribute("defaultNamespace", defaultNamespace);
          	  request.setAttribute("namespaceList", namespaceList);
			  request.setAttribute("title","Permanent URI");
			  request.setAttribute("bodyJsp",PERMANENT_URI);
			  }
			  else if(oldModel!=null){
				  JenaIngestUtils utils = new JenaIngestUtils();
				  utils.doPermanentURI(oldModel,newModel,oldNamespace,newNamespace,dNamespace,maker,vreq);
				  request.setAttribute("title","Ingest Menu");
				  request.setAttribute("bodyJsp",INGEST_MENU_JSP);
			  }
			  
			  
		  }
		else if("mergeIndividuals".equals(actionStr)){
			  String uri1 = vreq.getParameter("uri1");
			  String uri2 = vreq.getParameter("uri2");
			  if(uri1!=null){
				  JenaIngestUtils utils = new JenaIngestUtils();
				  OntModel baseOntModel = (OntModel) getServletContext().getAttribute("baseOntModel");
					OntModel ontModel = (OntModel)
					getServletContext().getAttribute("jenaOntModel");
					OntModel infOntModel = (OntModel)
					getServletContext().getAttribute(JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME);
				  String result = utils.doMerge(uri1,uri2,baseOntModel,ontModel,infOntModel);
				 // request.getSession().setAttribute("leftoverModel", utils.getLeftOverModel());
				  getServletContext().setAttribute("leftoverModel", utils.getLeftOverModel());
				  request.setAttribute("result",result);
				  request.setAttribute("title","Merge Individuals");
				  request.setAttribute("bodyJsp",MERGE_RESULT);
				   
			  }
			  else{
				  request.setAttribute("title","Merge Individuals");
				  request.setAttribute("bodyJsp",MERGE_INDIVIDUALS);  
			  }
			   
		  }
		else if("renameResource".equals(actionStr)){
			  String uri1 = vreq.getParameter("uri1");
			  String uri2 = vreq.getParameter("uri2");
			  if(uri1!=null){
				  String result = doRename(uri1,uri2,response);
				  request.setAttribute("result",result);
				  request.setAttribute("title","Rename Resources");
				  request.setAttribute("bodyJsp",RENAME_RESULT);
				   
			  }
			  else{
				  request.setAttribute("title","Rename Resource");
				  request.setAttribute("bodyJsp",RENAME_RESOURCE);  
			  }
			  
		  }
		else if("mergeResult".equals(actionStr)){
			//Model lmodel = (Model)request.getSession().getAttribute("leftoverModel");
			Model lmodel = (Model)getServletContext().getAttribute("leftoverModel");
			response.setContentType("RDF/XML-ABBREV");
			try{
			OutputStream outStream = response.getOutputStream();
			outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
			lmodel.write( outStream,"RDF/XML-ABBREV");
			outStream.flush();
			outStream.close();
			}
			catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
		}
		
		else {
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		}
		
        Portal portal = vreq.getPortal();
		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);      
        request.setAttribute("portalBean",portal);
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            System.out.println(this.getClass().getName()+" could not forward to view.");
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
		
	}
	
	private ModelMaker getVitroJenaModelMaker(HttpServletRequest request) {
		ModelMaker myVjmm = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
		myVjmm = (myVjmm == null) ? (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker") : myVjmm;
		return new VitroJenaSpecialModelMaker(myVjmm, request);
	}
	
	private Model getModel(String name, HttpServletRequest request) {
		if ("vitro:jenaOntModel".equals(name)) {
			Object sessionOntModel = request.getSession().getAttribute("jenaOntModel");
			if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
				return (OntModel) sessionOntModel;
			} else {
				return (OntModel) getServletContext().getAttribute("jenaOntModel");
			}
		} else if ("vitro:baseOntModel".equals(name)) {
			Object sessionOntModel = request.getSession().getAttribute("baseOntModel");
			if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
				return (OntModel) sessionOntModel;
			} else {
				return (OntModel) getServletContext().getAttribute("baseOntModel");
			}
		} else if ("vitro:inferenceOntModel".equals(name)) {
			Object sessionOntModel = request.getSession().getAttribute("inferenceOntModel");
			if (sessionOntModel != null && sessionOntModel instanceof OntModel) {
				return (OntModel) sessionOntModel;
			} else {
				return (OntModel) getServletContext().getAttribute("inferenceOntModel");
			}
		} else {
			return getVitroJenaModelMaker(request).getModel(name);
		}
	}
	
	private void doCreateModel(String modelName, ModelMaker modelMaker) {
		modelMaker.createModel(modelName);
	}
	
	private void doRemoveModel(String modelName, ModelMaker modelMaker) {
	    //Try to detach first since it cause problems to remove an attached model.	    
	    doDetachModel(modelName, modelMaker);
	    System.out.println("Removing "+modelName+" from webapp");
		modelMaker.removeModel(modelName);		
	}
	
	private void doClearModel(String modelName, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		OntModel o = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,m);
		o.enterCriticalSection(Lock.WRITE);
		try {
			o.removeAll(null,null,null);
		} finally {
			o.leaveCriticalSection();
		}
		// removeAll() doesn't work with the listeners!
	}
	
	private void doLoadRDFData(String modelName, String docLoc, String filePath, String language, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		m.enterCriticalSection(Lock.WRITE);
		try {
			if ( (docLoc != null) && (docLoc.length()>0) ) {
				m.read(docLoc, language);
			} else if ( (filePath != null) && (filePath.length()>0) ) {
				File file = new File(filePath);
				File[] files;
				if (file.isDirectory()) {
					files = file.listFiles();
				} else {
					files = new File[1];
					files[0] = file;
				}
				for (int i=0; i<files.length; i++) {
					File currentFile = files[i];
					log.info("Reading file "+currentFile.getName());
					FileInputStream fis;
					try {
						fis = new FileInputStream(currentFile);
						m.read(fis, null, language);
						fis.close();
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			}
		} finally { 
			m.leaveCriticalSection();
		}
	}
	
	private void doAttachModel(String modelName, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		System.out.println("Attaching "+modelName+" ("+m.hashCode()+") to webapp");
		vitroJenaModel.addSubModel(m);
	}
	
	private void doDetachModel(String modelName, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		OntModel vitroJenaModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		System.out.println("Detaching "+modelName+" ("+m.hashCode()+") from webapp");
		vitroJenaModel.removeSubModel(m);
	}
	
	private void doRenameBNodes(VitroRequest vreq, String namespaceEtc, boolean patternBoolean, String pattern, String[] sourceModel) {
		OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		String property = vreq.getParameter("property");
		Boolean csv2rdf = (Boolean)getServletContext().getAttribute("csv2rdf");
		for (int i=0; i<sourceModel.length; i++) {
			Model m = getModel(sourceModel[i],vreq);
			source.addSubModel(m);
		}
		System.out.println(vreq.getParameter("destinationModelName"));
		Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
		JenaIngestUtils utils = new JenaIngestUtils();
		destination.enterCriticalSection(Lock.WRITE);
		try {
			if(!patternBoolean){
				destination.add(utils.renameBNodes(source, namespaceEtc, vreq.getJenaOntModel()));
			}
			else{
				destination.add(utils.renameBNodesByPattern(source, namespaceEtc, vreq.getJenaOntModel(), pattern, property));
			}
			if(csv2rdf!=null){
				if(csv2rdf && property!=null){
					ClosableIterator closeIt = destination.listSubjects();
					Property prop = ResourceFactory.createProperty(property);
					try {
						for (Iterator it = closeIt; it.hasNext();) {
							Resource res = (Resource) it.next();
							if (res.isAnon()) {
								ClosableIterator closfIt = destination.listStatements(res,prop,(RDFNode)null);
								Statement stmt = null;
								try {
									if (closfIt.hasNext()) {
										stmt = (Statement) closfIt.next();
									}
								} finally {
									closfIt.close();
								}
								if (stmt != null) {
									Resource outRes = stmt.getSubject();
									destination.removeAll(outRes,(Property)null,(RDFNode)null);
								}
							}
						}
					} finally {
						closeIt.close();
					}
					csv2rdf = false;
					getServletContext().setAttribute("csv2rdf", csv2rdf);
				}
				else if(csv2rdf && property == null){
					ClosableIterator closeIt = destination.listSubjects();
					try {
						for (Iterator it = closeIt; it.hasNext();) {
							Resource res = (Resource) it.next();
							if (res.isAnon()) {
								destination.removeAll(res,(Property)null,(RDFNode)null);
							}
						}
					} finally {
						closeIt.close();
					}
					csv2rdf = false;
					getServletContext().setAttribute("csv2rdf", csv2rdf);
				}
			}
			
		} finally {
			destination.leaveCriticalSection();
		}
	}
	
	private void doSmushSingleModel(VitroRequest vreq) {
		OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		String[] sourceModel = vreq.getParameterValues("sourceModelName");
		for (int i=0; i<sourceModel.length; i++) {
			Model m = getModel(sourceModel[i],vreq);
			source.addSubModel(m);
		}
		Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
		String propertyURIStr = vreq.getParameter("propertyURI");
		Property prop = ResourceFactory.createProperty(propertyURIStr);
		JenaIngestUtils utils = new JenaIngestUtils();
		destination.enterCriticalSection(Lock.WRITE);
		try {
			destination.add(utils.smushResources(source, prop));
		} finally {
			destination.leaveCriticalSection();
		}
	}
	
	private long doExecuteSparql(VitroRequest vreq) {
		OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		OntModel source = null;
		if ("pellet".equals(vreq.getParameter("reasoning"))) {
			source = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		} else {
 		    source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		}
		String[] sourceModel = vreq.getParameterValues("sourceModelName");
		for (int i=0; i<sourceModel.length; i++) {
			Model m = getModel(sourceModel[i],vreq);
			source.addSubModel(m);
		}
		Model destination = getModel(vreq.getParameter("destinationModelName"),vreq); 
		String sparqlQueryStr = vreq.getParameter("sparqlQueryStr");
		String savedQueryURIStr = vreq.getParameter("savedQuery");
		String queryStr;
		if (savedQueryURIStr.length()==0) {
			System.out.println("Using entered query");
			queryStr = sparqlQueryStr;
		} else {
			Property queryStrProp = ResourceFactory.createProperty(SPARQL_QUERYSTR_PROP);
			jenaOntModel.enterCriticalSection(Lock.READ);
			try {
				Individual ind = jenaOntModel.getIndividual(savedQueryURIStr);
				System.out.println("Using query "+savedQueryURIStr);
				queryStr = ( (Literal) ind.getPropertyValue(queryStrProp)).getLexicalForm();
				queryStr = StringEscapeUtils.unescapeHtml(queryStr); // !!! We need to turn off automatic HTML-escaping for data property editing.
			} finally {
				jenaOntModel.leaveCriticalSection();
			}
		}
		//System.out.println(queryStr);
		Model tempModel = ModelFactory.createDefaultModel();
		Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
        QueryExecution qexec = QueryExecutionFactory.create(query,source);
        try {
        	qexec.execConstruct(tempModel);
        } catch (QueryExecException qee) {
        	qexec.execDescribe(tempModel);
        }
        destination.enterCriticalSection(Lock.WRITE);
        try {
        	if (destination instanceof OntModel) {
        		((OntModel) destination).getBaseModel().notifyEvent(new EditEvent(null, true));
        	} else {
        		destination.notifyEvent(new EditEvent(null, true));
        	}
        	destination.add(tempModel);      
        } finally {
        	if (destination instanceof OntModel) {
        		((OntModel) destination).getBaseModel().notifyEvent(new EditEvent(null, false));
        	} else {
        		destination.notifyEvent(new EditEvent(null, false));
        	}
        	destination.leaveCriticalSection();
        }
        return tempModel.size();     
	}
	
	public void doConnectDB(VitroRequest vreq) throws SQLException {
		String jdbcUrl = vreq.getParameter("jdbcUrl");
		String username = vreq.getParameter("username");
		String password = vreq.getParameter("password");
		String dbType = vreq.getParameter("dbType");
		String tripleStore = vreq.getParameter("tripleStore");
		DatabaseType dbTypeObj = null;
		if ("MySQL".equals(dbType)) {
			jdbcUrl += (jdbcUrl.contains("?")) ? "&" : "?";
			jdbcUrl += "useUnicode=yes&characterEncoding=utf8";
		}
		dbTypeObj = DatabaseType.fetch(dbType);
        String driver = loadDriver(dbTypeObj);
		System.out.println("Connecting to DB at "+jdbcUrl);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash,dbTypeObj) ; 
    	BasicDataSource bds = JenaDataSourceSetup.makeBasicDataSource(
    	        driver, jdbcUrl, username, password);
    	try {
    	    VitroJenaSDBModelMaker vsmm = new VitroJenaSDBModelMaker(storeDesc, bds);
    	  	VitroJenaModelMaker vjmm = new VitroJenaModelMaker(jdbcUrl, username, password, dbType);
        	getServletContext().setAttribute("vitroJenaSDBModelMaker", vsmm);
        	getServletContext().setAttribute("vitroJenaModelMaker", vjmm);
        	if("SDB".equals(tripleStore))
        		vreq.getSession().setAttribute("vitroJenaModelMaker",vsmm);
        	else
        		vreq.getSession().setAttribute("vitroJenaModelMaker",vjmm);
    	} catch (SQLException sqle) {
            throw new RuntimeException("Unable to create SDB ModelMaker", sqle);
        }
	}
	
	
	private String loadDriver(DatabaseType dbType) {
	    String driverName = JDBC.getDriver(dbType);
	    JDBC.loadDriver(driverName);
	    return driverName;
	    
//        if (DatabaseType.MySQL.equals(dbType)) {
//            JDBC.loadDriverMySQL();
//        } else if (DatabaseType.DB2.equals(dbType)) {
//            JDBC.loadDriverDB2();
//        } else if (DatabaseType.Derby.equals(dbType)) {
//            JDBC.loadDriverDerby();
//        } else if (DatabaseType.H2.equals(dbType)) {
//            JDBC.loadDriverH2();
//        } else if (DatabaseType.HSQLDB.equals(dbType)) {
//            JDBC.loadDriverHSQL();
//        } else if (DatabaseType.Oracle.equals(dbType)) {
//            JDBC.loadDriverOracle();
//        } else if (DatabaseType.PostgreSQL.equals(dbType)) {
//            JDBC.loadDriverPGSQL();
//        } else if (DatabaseType.SQLServer.equals(dbType)) {
//            JDBC.loadDriverSQLServer();
//        }
	}
	
	/*public void doExecuteCsv2Rdf(VitroRequest vreq) {
		char[] quoteChars = {'"'};
		String namespace = vreq.getParameter("namespace");
		String tboxNamespace = vreq.getParameter("tboxNamespace");
		String typeName = vreq.getParameter("typeName");
		String csvUrl = vreq.getParameter("csvUrl");
		Model destination = null;
		String destinationModelNameStr = vreq.getParameter("destinationModelName");
		if (destinationModelNameStr != null && destinationModelNameStr.length()>0) {
			destination = getModel(destinationModelNameStr, vreq);
		}
		Model tboxDestination = null;
		String tboxDestinationModelNameStr = vreq.getParameter("tboxDestinationModelName");
		if (tboxDestinationModelNameStr != null && tboxDestinationModelNameStr.length()>0) {
			tboxDestination = getModel(tboxDestinationModelNameStr, vreq);
		}
		
		char separatorChar = ',';
		if ("tab".equalsIgnoreCase(vreq.getParameter("separatorChar"))) {
			separatorChar = '\t';
		}
		
		Csv2Rdf c2r = new Csv2Rdf(separatorChar, quoteChars,namespace,tboxNamespace,typeName);
		
		InputStream is = null;
		
		try {
			is = new URL(csvUrl).openStream();
		} catch (IOException e) {
			System.out.println("IOException opening URL "+csvUrl);
			return;
		}
		
		Model[] models = null;
		
		try {
			 models = c2r.convertToRdf(is,vreq,destination);
		} catch (IOException e) {
			System.out.println("IOException converting "+csvUrl+" to RDF");
		}
		
		if (destination != null) {
			destination.add(models[0]);
		}
		if (tboxDestination != null) {
			tboxDestination.add(models[1]);
		}
				
	}*/

	public void doSubtractModels(VitroRequest vreq) {
			String modela = vreq.getParameter("modela");
			String modelb = vreq.getParameter("modelb");
			String destination = vreq.getParameter("destinationModelName");
			Model ma = getModel(modela,vreq);
			Model mb = getModel(modelb,vreq);
			Model destinationModel = getModel(destination,vreq);
			if(!destination.equals(modela))
			    destinationModel.add(ma.difference(mb));
			else
				ma.remove(mb);
	}
	
	public void doSplitPropertyValues(VitroRequest vreq) {
		String sourceModelStr = vreq.getParameter("sourceModelName");
		String destinationModelStr = vreq.getParameter("destinationModelName");
		Model sourceModel = getModel(sourceModelStr,vreq);
		Model destinationModel = getModel(destinationModelStr,vreq);
		String propertyURI = vreq.getParameter("propertyURI");
		String splitRegex = vreq.getParameter("splitRegex");
		String newPropertyURI = vreq.getParameter("newPropertyURI");
		String trimStr = vreq.getParameter("trim");
		boolean trim = ( "TRUE".equalsIgnoreCase(trimStr) );
		destinationModel.add( (new JenaIngestUtils()).splitPropertyValues(sourceModel, propertyURI, splitRegex, newPropertyURI, trim));
	}
	
	public void doGenerateTBox(VitroRequest vreq) {
		OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		String[] sourceModel = vreq.getParameterValues("sourceModelName");
		for (int i=0; i<sourceModel.length; i++) {
			Model m = getModel(sourceModel[i],vreq);
			source.addSubModel(m);
		}
		String destinationModelStr = vreq.getParameter("destinationModelName");
		Model destination = getModel(destinationModelStr,vreq);
		destination.add( (new JenaIngestUtils()).generateTBox(source));
	}
		
	public void doProcessStrings(VitroRequest vreq) {
		try {
			String className = vreq.getParameter("className");
			String methodName = vreq.getParameter("methodName");
			String propertyName = vreq.getParameter("propertyName");
			String newPropertyName = vreq.getParameter("newPropertyName");
			// for now, we'll make the destination and source models the same
			Model destination = getModel(vreq.getParameter("destinationModelName"),vreq);
			String processModel = vreq.getParameter("processModel");
			Model savedAdditionsModel = null;
			Model savedRetractionsModel = null;
			String additionsModelStr = vreq.getParameter("additionsModel");
			if ( (additionsModelStr != null) && ( additionsModelStr.length() > 0 ) ) {
				savedAdditionsModel = getModel(additionsModelStr, vreq);
			}
			String retractionsModelStr = vreq.getParameter("retractionsModel");
			if ( (retractionsModelStr != null) && ( retractionsModelStr.length() > 0 ) ) {
				savedRetractionsModel = getModel(retractionsModelStr, vreq);
			} 
			Model additionsModel = ModelFactory.createDefaultModel();
			Model retractionsModel = ModelFactory.createDefaultModel();
			Class stringProcessorClass = Class.forName(className);
			Object processor = stringProcessorClass.newInstance();
			Class[] methArgs = {String.class};
			Method meth = stringProcessorClass.getMethod(methodName,methArgs);
			Property prop = ResourceFactory.createProperty(propertyName);
			Property newProp = ResourceFactory.createProperty(newPropertyName);
			destination.enterCriticalSection(Lock.READ);
			try {
				ClosableIterator closeIt = destination.listStatements((Resource)null,prop,(RDFNode)null);
				for (Iterator stmtIt = closeIt; stmtIt.hasNext(); ) {
					Statement stmt = (Statement) stmtIt.next();
					if (stmt.getObject().isLiteral()) {
						Literal lit = (Literal) stmt.getObject();
						String lex = lit.getLexicalForm();
						Object[] args = {lex};
						String newLex = "";
						try {
							newLex = (String) meth.invoke(processor,args);
						} catch (InvocationTargetException e) {
							e.getTargetException().printStackTrace();
						}
						if (!newLex.equals(lex)) {
							retractionsModel.add(stmt);
							if (newLex.length()>0) {	
									Literal newLit = null;
									if (lit.getLanguage()!=null && lit.getLanguage().length()>0) {
										newLit = additionsModel.createLiteral(newLex,lit.getLanguage());
									} else if (lit.getDatatype() != null) {
										newLit = additionsModel.createTypedLiteral(newLex,lit.getDatatype());
									} else {
										newLit = additionsModel.createLiteral(newLex);
									}
									additionsModel.add(stmt.getSubject(),newProp,newLit);
							}
						}
					}
				}
				if (processModel != null) {
					destination.add(additionsModel);
					destination.remove(retractionsModel);
				}
				if (savedAdditionsModel != null)  {
					savedAdditionsModel.add(additionsModel);
				}
				if (savedRetractionsModel != null) {
					savedRetractionsModel.add(retractionsModel);
				}
			} finally {
				destination.leaveCriticalSection();
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doCleanLiterals(Model model) {
		Model retractionsModel = ModelFactory.createDefaultModel();
		Model additionsModel = ModelFactory.createDefaultModel();
		model.enterCriticalSection(Lock.WRITE);
		try {
			ClosableIterator closeIt = model.listStatements();
			try {
				for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
					Statement stmt = (Statement) stmtIt.next();
					if (stmt.getObject().isLiteral()) {
						Literal lit = (Literal) stmt.getObject();
						String lex = lit.getLexicalForm();
						char[] chars = lex.toCharArray();
						char[] cleanChars = new char[chars.length];
						int cleanPos = 0;
						boolean badChar = false;
						for (int i=0; i<chars.length; i++) {
							if (java.lang.Character.getNumericValue(chars[i])>31 && java.lang.Character.isDefined(chars[i])) {
								cleanChars[cleanPos] = chars[i];
								cleanPos++;
							} else {
								System.out.println("Bad char in "+lex);
								System.out.println("Numeric value "+java.lang.Character.getNumericValue(chars[i])); 
								badChar = true;
							}
						}
						String cleanLex = new String(cleanChars);
						if (badChar) {
							retractionsModel.add(stmt);
							Literal newLit = null;
							if (lit.getLanguage()!=null && lit.getLanguage().length()>0) {
								newLit = additionsModel.createLiteral(cleanLex,lit.getLanguage());
							} else if (lit.getDatatype() != null) {
								newLit = additionsModel.createTypedLiteral(cleanLex,lit.getDatatype());
							} else {
								newLit = additionsModel.createLiteral(cleanLex);
							}
							additionsModel.add(stmt.getSubject(),stmt.getPredicate(),newLit);
						}
					}
				}	
			} finally {
				closeIt.close();
			}
			model.remove(retractionsModel);
			model.add(additionsModel);
			System.out.println("Cleaned "+additionsModel.size()+" literals");
		} finally {
			model.leaveCriticalSection();
		}
	}
	
	private void doExecuteWorkflow(VitroRequest vreq) {
		String workflowURI = vreq.getParameter("workflowURI");
		String workflowStepURI = vreq.getParameter("workflowStepURI");
		OntModel jenaOntModel = (OntModel) getModel("vitro:jenaOntModel",vreq);
		new JenaIngestWorkflowProcessor(jenaOntModel.getIndividual(workflowURI),getVitroJenaModelMaker(vreq)).run(jenaOntModel.getIndividual(workflowStepURI));
	}

	
	
private String doRename(String oldNamespace,String newNamespace,HttpServletResponse response){
		
		String userURI = null;
		String uri = null;
		String result = null;
		Integer counter = 0;
		Boolean namespacePresent = false;
		OntModel baseOntModel = (OntModel)
		getServletContext().getAttribute("baseOntModel");
		OntModel ontModel = (OntModel)
		getServletContext().getAttribute("jenaOntModel");
		OntModel infOntModel = (OntModel)
		getServletContext().getAttribute(JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME);
		WebappDaoFactory wdf =
		(WebappDaoFactory)getServletContext().getAttribute("webappDaoFactory");
		List<String> urisToChange = new LinkedList<String>();
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
		Iterator<Individual> indIter = ontModel.listIndividuals();
		while( indIter.hasNext()){
		Individual ind = indIter.next();
		String namespace = ind.getNameSpace();
		if( namespace != null ){
		if( oldNamespace.equals(namespace) ){
		uri = ind.getURI();	
		urisToChange.add(uri);
		namespacePresent = true;
		}
		}
		}
		}finally {
		ontModel.leaveCriticalSection();
		}
		if(!namespacePresent){
			result = "0 resource renamed";
			return result;
		}
		
		
		for( String oldURIStr : urisToChange){
		baseOntModel.enterCriticalSection(Lock.WRITE);
		ontModel.enterCriticalSection(Lock.WRITE);
		infOntModel.enterCriticalSection(Lock.WRITE);
		try{
		long time1 = System.currentTimeMillis();
		Resource res = baseOntModel.getResource(oldURIStr);
		Resource infRes = infOntModel.getResource(oldURIStr);
		long time2 = System.currentTimeMillis();
		String newURIStr=null;
		Pattern p = Pattern.compile(oldNamespace);
		String candidateString = res.getURI();
		Matcher matcher = p.matcher(candidateString);
		newURIStr = matcher.replaceFirst(newNamespace);
		long time3 = System.currentTimeMillis();
		System.out.println("time to get new uri: " + Long.toString(time3 - time2)
		);
		System.out.println("Renaming "+ oldURIStr + " to " + newURIStr);
		ResourceUtils.renameResource(res,newURIStr);
		ResourceUtils.renameResource(infRes,newURIStr);
		long time4 = System.currentTimeMillis();
		System.out.println(" time to rename : " + Long.toString( time4 - time3));
		System.out.println(" time for one resource: " + Long.toString( time4 -
		time1));
		} finally {
		infOntModel.leaveCriticalSection();
		ontModel.leaveCriticalSection();
		baseOntModel.leaveCriticalSection();
		}
		try {
			Thread.currentThread().sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		counter++;
		}
		
		/*baseOntModel.enterCriticalSection(Lock.WRITE);
		ontModel.enterCriticalSection(Lock.WRITE);
		try{
		baseOntModel.getBaseModel().notifyEvent(new EditEvent(null,true));
		baseOntModel.getBaseModel().notifyEvent(new EditEvent(null,false));
		} finally {
		ontModel.leaveCriticalSection();
		baseOntModel.leaveCriticalSection();
		}*/
		result = counter.toString() + " resources renamed";
		return result;
		
    }

    private class CollationSort implements Comparator<String> {
        
        Collator collator = Collator.getInstance();
        
        public int compare(String s1, String s2) {
            return collator.compare(s1, s2);
        }
        
    }
	
}
