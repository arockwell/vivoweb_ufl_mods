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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class JenaExportController extends BaseEditController {

	public void doGet( HttpServletRequest request, HttpServletResponse response ) {
		
		VitroRequest vreq = new VitroRequest(request);
		
        if (!checkLoginStatus(vreq,response))
            return;
		
		if ( vreq.getRequestURL().indexOf("/download/") > -1 ) { 
			outputRDF( vreq, response );
			return;
		}
		
		String formatParam = vreq.getParameter("format");
		
		if (formatParam != null) {
			redirectToDownload( vreq, response );
		} else {
			prepareExportSelectionPage( vreq, response );
		}
		
	}
	
	private void redirectToDownload( VitroRequest vreq, HttpServletResponse response ) {
		String formatStr = vreq.getParameter("format");
		String subgraphParam = vreq.getParameter("subgraph");
		String filename = null;
		if ("abox".equals(subgraphParam)) {
			filename = "ABox";
		} else if ("tbox".equals(subgraphParam)) {
			filename = "TBox";
		} else {
			filename = "export";
		}
		String extension =
			( (formatStr != null) && formatStr.startsWith("RDF/XML") && "tbox".equals(subgraphParam) ) 
			? ".owl"
			: formatToExtension.get(formatStr);
		if (extension == null) {
			throw new RuntimeException("Unsupported RDF export format "+formatStr);
		}
		String[] uriParts = vreq.getRequestURI().split("/");
		String base = uriParts[uriParts.length-1];
		try {
			response.sendRedirect("./"+base+"/download/"+filename+extension+"?"+vreq.getQueryString());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	private void outputRDF( VitroRequest vreq, HttpServletResponse response ) {
		Dataset dataset = vreq.getDataset();
		JenaModelUtils xutil = new JenaModelUtils();
		String formatParam = vreq.getParameter("format");
		String subgraphParam = vreq.getParameter("subgraph");
		String assertedOrInferredParam = vreq.getParameter("assertedOrInferred");
		String ontologyURI = vreq.getParameter("ontologyURI");
		
		Model model = null;
		OntModel ontModel = ModelFactory.createOntologyModel();
		
		if(!subgraphParam.equalsIgnoreCase("tbox") && !subgraphParam.equalsIgnoreCase("abox") && !subgraphParam.equalsIgnoreCase("full")){
			ontologyURI = subgraphParam;
			subgraphParam = "tbox";
			char[] uri =  ontologyURI.toCharArray();
			ontologyURI="";
			for(int i =0; i < uri.length-1;i++)
				ontologyURI = ontologyURI + uri[i];
		}
		
		
		String mode = (JenaDataSourceSetupBase.isSDBActive()) ? "SDB" : "RDB"; 
		if( "abox".equals(subgraphParam)){
			model = ModelFactory.createDefaultModel();
			if("inferred".equals(assertedOrInferredParam)){
				if(mode.equals("RDB")){
					Dataset jenaDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("jenaOntModel"));
					Dataset inferenceDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("inferenceOntModel"));
					model = xutil.extractABox(jenaDataset,inferenceDataset,null);
				}
				else{
					model = ModelContext.getInferenceOntModelSelector(getServletContext()).getABoxModel();
				}
			}
			else if("full".equals(assertedOrInferredParam)){
				if(mode.equals("RDB")){
					model = xutil.extractABox((OntModel)getServletContext().getAttribute("jenaOntModel"));
				}
				else{
					model = ModelContext.getUnionOntModelSelector(getServletContext()).getABoxModel();
				}
			}
			else if("asserted".equals(assertedOrInferredParam)){
				if(mode.equals("RDB")){
					Dataset jenaDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("jenaOntModel"));
					Dataset baseDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("baseOntModel"));
					model = xutil.extractABox(jenaDataset,baseDataset,null);
				}
				else{
					model = ModelContext.getBaseOntModelSelector(getServletContext()).getABoxModel();
				}
			}
		}
		else if("tbox".equals(subgraphParam)){
		    if ("inferred".equals(assertedOrInferredParam)) {
		        // the extraction won't work on just the inferred graph,
		        // so we'll extract the whole ontology and then include
		        // only those statements that are in the inferred graph
		        Model tempModel = xutil.extractTBox(
		                ModelContext.getUnionOntModelSelector(
		                        getServletContext()).getTBoxModel(), ontologyURI);
		        Model inferenceModel = ModelContext.getInferenceOntModelSelector(
                        getServletContext()).getTBoxModel();
		        inferenceModel.enterCriticalSection(Lock.READ);
		        try {
    		        model = tempModel.intersection(inferenceModel);
		        } finally {
		            inferenceModel.leaveCriticalSection();
		        }
		    } else if ("full".equals(assertedOrInferredParam)) {
                model = xutil.extractTBox(
                        ModelContext.getUnionOntModelSelector(
                                getServletContext()).getTBoxModel(), ontologyURI);		        
		    } else {
                model = xutil.extractTBox(
                        ModelContext.getBaseOntModelSelector(
                                getServletContext()).getTBoxModel(), ontologyURI);              		        
		    }
//			if("inferred".equals(assertedOrInferredParam)){
//				model = xutil.extractTBox(dataset, ontologyURI,INFERENCE_GRAPH);
//			}
//			else if("full".equals(assertedOrInferredParam)){
//				model = xutil.extractTBox(dataset, ontologyURI, FULL_GRAPH);
//			}
//			else{
//				model = xutil.extractTBox(dataset, ontologyURI, ASSERTIONS_GRAPH);
//			}
			
		}
		else if("full".equals(subgraphParam)){
			if("inferred".equals(assertedOrInferredParam)){
				ontModel = xutil.extractTBox(dataset, ontologyURI,INFERENCE_GRAPH);
				if(mode.equals("RDB")){
					Dataset jenaDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("jenaOntModel"));
					Dataset inferenceDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("inferenceOntModel"));
					ontModel.addSubModel(xutil.extractABox(jenaDataset, inferenceDataset, null));
				}
				else{
					ontModel.addSubModel(ModelContext.getInferenceOntModelSelector(getServletContext()).getABoxModel());
					ontModel.addSubModel(ModelContext.getInferenceOntModelSelector(getServletContext()).getTBoxModel());
				}
			}
			else if("full".equals(assertedOrInferredParam)){
				ontModel = xutil.extractTBox(dataset, ontologyURI, FULL_GRAPH);
				if(mode.equals("RDB")){
					ontModel.addSubModel(xutil.extractABox((OntModel)getServletContext().getAttribute("jenaOntModel")));
				}
				else{
					ontModel.addSubModel(ModelContext.getUnionOntModelSelector(getServletContext()).getABoxModel());
					ontModel.addSubModel(ModelContext.getUnionOntModelSelector(getServletContext()).getTBoxModel());
					ontModel.addSubModel(ModelContext.getUnionOntModelSelector(getServletContext()).getApplicationMetadataModel());
				}
			}
			else{
				ontModel = xutil.extractTBox(dataset, ontologyURI, ASSERTIONS_GRAPH);
				if(mode.equals("RDB")){
					Dataset jenaDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("jenaOntModel"));
					Dataset baseDataset = DatasetFactory.create((OntModel)getServletContext().getAttribute("baseOntModel"));
					ontModel.addSubModel(xutil.extractABox(jenaDataset,baseDataset,null));
				}
				else{
					ontModel.addSubModel(ModelContext.getBaseOntModelSelector(getServletContext()).getABoxModel());
					ontModel.addSubModel(ModelContext.getBaseOntModelSelector(getServletContext()).getTBoxModel());
					ontModel.addSubModel(ModelContext.getBaseOntModelSelector(getServletContext()).getApplicationMetadataModel());
				}
			}
			
		}
		
		if ( formatParam == null ) {
			formatParam = "RDF/XML-ABBREV";  // default
		}
		String mime = formatToMimetype.get( formatParam );
		if ( mime == null ) {
			throw new RuntimeException( "Unsupported RDF format " + formatParam);
		}
		
		response.setContentType( mime );
		if(mime.equals("application/rdf+xml"))
			response.setHeader("content-disposition", "attachment; filename=" + "export.rdf");
		else if(mime.equals("text/n3"))
			response.setHeader("content-disposition", "attachment; filename=" + "export.n3");
		else if(mime.equals("text/plain"))
			response.setHeader("content-disposition", "attachment; filename=" + "export.txt");
		else if(mime.equals("application/x-turtle"))
			response.setHeader("content-disposition", "attachment; filename=" + "export.ttl");
			
		try {
			OutputStream outStream = response.getOutputStream();
			if ( formatParam.startsWith("RDF/XML") ) {
				outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
			}
			// 2010-11-02 workaround for the fact that ARP now always seems to 
			// try to parse N3 using strict Turtle rules.  Avoiding headaches
			// by always serializing out as Turtle instead of using N3 sugar.
			if(!"full".equals(subgraphParam))
				model.write( outStream, "N3".equals(formatParam) ? "TTL" : formatParam );
			else
				ontModel.writeAll(outStream, "N3".equals(formatParam) ? "TTL" : formatParam, null );
			outStream.flush();
			outStream.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
	}
	
	private void prepareExportSelectionPage( VitroRequest vreq, HttpServletResponse response ) {
		vreq.setAttribute( "bodyJsp", Controllers.EXPORT_SELECTION_JSP );
		RequestDispatcher dispatcher = vreq.getRequestDispatcher( Controllers.BASIC_JSP );
		try {
			dispatcher.forward( vreq, response );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private OntModel getOntModelFromAttribute( String attributeName, VitroRequest vreq ) {
		
		Object o = vreq.getAttribute( attributeName );
		if ( (o != null) && (o instanceof OntModel) ) {
			return (OntModel) o;
		} else {
			o = getServletContext().getAttribute( attributeName );
			if ( (o != null) && (o instanceof OntModel) ) {
				return (OntModel) o;
			} else {
				throw new RuntimeException("Unable to find OntModel in request or context attribute "+attributeName);
			}
		}
	}
	
	static final String FULL_ONT_MODEL_ATTR = "jenaOntModel";
	static final String ASSERTIONS_ONT_MODEL_ATTR = "baseOntModel";
	static final String INFERENCES_ONT_MODEL_ATTR = "inferenceOntModel";
	static final String FULL_GRAPH = "?g";
	static final String ASSERTIONS_GRAPH = "<http://vitro.mannlib.cornell.edu/default/vitro-kb-2>";
	static final String INFERENCE_GRAPH = "<http://vitro.mannlib.cornell.edu/default/vitro-kb-inf>";
	
	static Map<String,String> formatToExtension;
	static Map<String,String> formatToMimetype;
	
	static {
		
		formatToExtension = new HashMap<String,String>();
		formatToExtension.put("RDF/XML",".rdf");
		formatToExtension.put("RDF/XML-ABBREV",".rdf");
		formatToExtension.put("N3",".n3");
		formatToExtension.put("N-TRIPLES",".nt");
		formatToExtension.put("TURTLE",".ttl");
		
		formatToMimetype = new HashMap<String,String>();
		formatToMimetype.put("RDF/XML","application/rdf+xml");
		formatToMimetype.put("RDF/XML-ABBREV","application/rdf+xml");
		formatToMimetype.put("N3","text/n3");
		formatToMimetype.put("N-TRIPLES", "text/plain");
		formatToMimetype.put("TURTLE", "application/x-turtle");
		
	}
		
}
