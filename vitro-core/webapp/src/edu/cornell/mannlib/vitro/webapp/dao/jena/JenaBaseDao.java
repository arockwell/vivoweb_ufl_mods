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

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.jena.vocabulary.SWRL;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class JenaBaseDao extends JenaBaseDaoCon {

	public static final boolean KEEP_ONLY_IF_TRUE = true; //used for updatePropertyBooleanValue()
    public static final boolean KEEP_ONLY_IF_FALSE = false; //used for updatePropertyBooleanValue()
    
    public static final String JENA_ONT_MODEL_ATTRIBUTE_NAME = "jenaOntModel";
    public static final String ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME = "baseOntModel";
    public static final String INFERENCE_ONT_MODEL_ATTRIBUTE_NAME = "inferenceOntModel";
    
    protected static final Log log = LogFactory.getLog(JenaBaseDao.class.getName());
    
    /* ******************* static constants ****************** */
    
    protected String PSEUDO_BNODE_NS = VitroVocabulary.PSEUDO_BNODE_NS;
    
    protected String XSD = "http://www.w3.org/2001/XMLSchema#";
    protected DateFormat xsdDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected DateFormat xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /* ******************* private variables ***************** */

    private OntModelSelector ontModelSelector;
    private List<OntModel> writableOntModelList;
    private WebappDaoFactoryJena webappDaoFactory;

    /* ******************* protected variables *************** */

    protected String DEFAULT_NAMESPACE;
    protected Set<String> NONUSER_NAMESPACES;
    protected String[] PREFERRED_LANGUAGES;

    /* ******************* constructor ************************* */
    
    public JenaBaseDao(WebappDaoFactoryJena wadf) {
    	this.ontModelSelector = wadf.getOntModelSelector();
    	this.DEFAULT_NAMESPACE = wadf.getDefaultNamespace();
    	this.NONUSER_NAMESPACES = wadf.getNonuserNamespaces();
    	this.PREFERRED_LANGUAGES = wadf.getPreferredLanguages();
    	this.webappDaoFactory = wadf;
    	
    }

    /* ******************** accessors ************************** */

    protected OntModel getOntModel() {
        return ontModelSelector.getFullModel();
    }
    
    protected OntModelSelector getOntModelSelector() {
    	return ontModelSelector;
    }

    protected List<OntModel> getWritableOntModelList() {
        return writableOntModelList;
    }

    protected WebappDaoFactoryJena getWebappDaoFactory() {
        return webappDaoFactory;
    }

    /* ********** convenience methods for children ************* */

    /**
     * convenience method
     */
    protected String getPropertyStringValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                ClosableIterator stateIt = res.getModel().listStatements(res,dataprop,(Literal)null);
                try {
                    if (stateIt.hasNext())
                        return ((Literal)((Statement)stateIt.next()).getObject()).getString();
                    else
                        return null;
                } finally {
                    stateIt.close();
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     */
    protected void addPropertyStringValue(Resource res, Property dataprop, String value, Model model) {
        if (res != null && dataprop != null && value != null && value.length()>0) {
            model.add(res, dataprop, value, XSDDatatype.XSDstring);
        }
    }
    
    /**
     * convenience method
     */
    protected Boolean getPropertyBooleanValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                ClosableIterator stateIt = getOntModel().listStatements(res,dataprop,(Literal)null);
                try {
                    if (stateIt.hasNext())
                        return ((Literal)((Statement)stateIt.next()).getObject()).getBoolean();
                    else
                        return null;
                } finally {
                    stateIt.close();
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * convenience method
     */
    protected void addPropertyBooleanValue(Resource res, Property prop, Boolean value, Model model) {
        if (res != null && prop != null && value != null ) {
            model.add(res, prop, model.createTypedLiteral(value));
        }
    }
    
    /**
     * convenience method for use with functional datatype properties
     */
    protected void updatePropertyStringValue(Resource res, Property dataprop, String value, Model model) {
        if (dataprop != null) {
            String existingValue = null;
            Statement stmt = res.getProperty(dataprop);
            if (stmt != null) {
                RDFNode object = stmt.getObject();
                if (object != null && object.isLiteral()){
                    existingValue = ((Literal)object).getString();
                }
            }
            
            if (value == null  || value.length() == 0) {
                 model.removeAll(res, dataprop, null);
            } else if (existingValue == null ) {
                 model.add(res, dataprop, value, XSDDatatype.XSDstring);	
            } else if (!existingValue.equals(value)) {
         		 model.removeAll(res, dataprop, null);
           		 model.add(res, dataprop, value, XSDDatatype.XSDstring);
            }
        }
    }
    
    /**
     * Convenience method for use with functional datatype properties.
     * 
     * Pass keepOnlyIfTrue if a lack of a value in the model indicates false.  
     * See ObjectPropertyDaoJena and PROPERTY_OFFERCREATENEWOPTIONANNOT for an
     * example.
     */
    protected void updatePropertyBooleanValue(Resource res, Property dataprop, Boolean value, Model model, boolean keepOnlyIfTrue) {
        if (dataprop != null) {
            Boolean existingValue = null;
            Statement stmt = res.getProperty(dataprop);
            if (stmt != null) {
                RDFNode object = stmt.getObject();
                if (object != null && object.isLiteral()){
                    existingValue = ((Literal)object).getBoolean();
                }
            }
            if ( (existingValue!=null && value == null) || (existingValue!=null && value != null && !(existingValue.equals(value)))
                    || (existingValue!=null && existingValue == false && keepOnlyIfTrue)) {
                model.removeAll(res, dataprop, null);
            }
            if ( (existingValue==null && value != null) || (existingValue!=null && value != null && !(existingValue.equals(value)) ) ) {
                if (keepOnlyIfTrue) {
                    if (value==true) {
                        model.add(res, dataprop, model.createTypedLiteral(value));
                    }
                } else {
                    model.add(res, dataprop, model.createTypedLiteral(value));
                }
            }
        }
    }


    /**
     * convenience method
     */
    protected int getPropertyNonNegativeIntValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                return ((Literal)res.getPropertyValue(dataprop)).getInt();
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * convenience method
     */
    protected void addPropertyIntValue(Resource res, Property dataprop, int value, Model model) {
        if (dataprop != null) {
            model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
        }
    }

    /**
     * convenience method for use with functional datatype properties
     */
    protected void updatePropertyIntValue(Resource res, Property dataprop, int value, Model model) {
        
    	if (dataprop != null) {	
            Integer existingValue = null;
            Statement stmt = res.getProperty(dataprop);
            if (stmt != null) {
                RDFNode object = stmt.getObject();
                if (object != null && object.isLiteral()){
                    existingValue = ((Literal)object).getInt();
                }
            }
        	
            if (existingValue == null ) {
                  model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);	
            } else if (existingValue.intValue() != value) {
        		  model.removeAll(res, dataprop, null);
        		  model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
            }            
        }
    }

    /**
     * convenience method
     */
    protected int getPropertyIntValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                return ((Literal)res.getPropertyValue(dataprop)).getInt();
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * convenience method
     */
    protected void addPropertyNonNegativeIntValue(Resource res, Property dataprop, int value, Model model) {
        if (dataprop != null && value>-1) {
            model.add(res, dataprop, Integer.toString(value), XSDDatatype.XSDint);
        }
    }

    /**
     * convenience method for use with functional datatype properties
     */
    protected void updatePropertyNonNegativeIntValue(Resource res, Property dataprop, int value, Model model) {

    	if (value < 0)
       	  return;

    	updatePropertyIntValue(res,dataprop,value,model);
        
    }

    /**
     * convenience method
     */
    protected void addPropertyFloatValue(Resource res, Property dataprop, Float value, Model model) {
        if (dataprop != null && value!= null) {
            model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);
        }
    }
    
    /**
     * convenience method for use with functional properties
     */
    protected void updatePropertyFloatValue(Resource res, Property dataprop, Float value, Model model) {

    	if (dataprop != null) {	
            Float existingValue = null;
            Statement stmt = res.getProperty(dataprop);
            if (stmt != null) {
                RDFNode object = stmt.getObject();
                if (object != null && object.isLiteral()){
                    existingValue = ((Literal)object).getFloat();
                }
            }

            if (value == null) {
                 model.removeAll(res, dataprop, null);
            } else if (existingValue == null ) {
                 model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);	
            } else if (existingValue.compareTo(value) != 0) {
         		 model.removeAll(res, dataprop, null);
          		 model.add(res, dataprop, Float.toString(value), XSDDatatype.XSDfloat);
            }
        }
    }
    
    protected Float getPropertyFloatValue(OntResource res, Property prop){
        if( prop != null ){
            try{
                return new Float( ((Literal)res.getPropertyValue(prop)).getFloat() );
            }catch(Exception e){
                return null;
            }
        }else
            return null;
    }
    
    /**
     * convenience method
     */
    protected synchronized Date getPropertyDateValue(OntResource res, DatatypeProperty dataprop) {
        if (dataprop != null) {
            try {
                return xsdDateFormat.parse(((Literal)res.getPropertyValue(dataprop)).getString());
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     * @param ind
     * @param dataprop
     * @param value
     */
    protected synchronized void addPropertyDateValue(Resource res, DatatypeProperty dataprop, Date value, Model model) {
        if (dataprop != null && value != null) {
            model.add(res, dataprop, xsdDateFormat.format(value), XSDDatatype.XSDdate);
        }
    }

    /**
     * convenience method for use with functional datatype properties
     */
    protected synchronized void updatePropertyDateValue(Resource res, DatatypeProperty dataprop, Date value, Model model) {
        try {
            if (dataprop != null) {
                if (value == null) {
                    model.removeAll(res, dataprop, null);
                } else {
	                Date existingValue = null;
	                Statement stmt = res.getProperty(dataprop);
	                if (stmt != null) {
	                    RDFNode object = stmt.getObject();
	                    if (object != null && object.isLiteral()){
	                        existingValue = (Date)((Literal)object).getValue();
	                    }
	                }
		                
	                if (existingValue == null ) {
	                     model.add(res, dataprop, xsdDateFormat.format(value), XSDDatatype.XSDdate);	
	                } else if (existingValue != value) {
	             		 model.removeAll(res, dataprop, null);
	              		 model.add(res, dataprop, xsdDateFormat.format(value), XSDDatatype.XSDdate);
	                }
                }
            }
        } catch (Exception e) {
            log.error("Error in updatePropertyDateValue");
        }
    }
    
    /**
     * convenience method
     */
    protected synchronized Date getPropertyDateTimeValue(OntResource res, Property dataprop) {
        if (dataprop != null) {
            try {
                return xsdDateTimeFormat.parse(((Literal)res.getPropertyValue(dataprop)).getString());
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * convenience method
     * @param ind
     * @param dataprop
     * @param value
     */
    protected synchronized void addPropertyDateTimeValue(Resource res, Property dataprop, Date value, Model model) {
        if (dataprop != null && value != null) {
            model.add(res, dataprop, xsdDateTimeFormat.format(value), XSDDatatype.XSDdateTime);
        }
    }

    /**
     * convenience method for use with functional datatype properties
     */
    protected synchronized void updatePropertyDateTimeValue(Resource res, Property dataprop, Date value, Model model) {
        try {
            if (dataprop != null) {
                String existingValue = null;
                Statement stmt = res.getProperty(dataprop);
                if (stmt != null) {
                    RDFNode object = stmt.getObject();
                    if (object != null && object.isLiteral()){
                        existingValue = ((Literal)object).getString();
                    }
                }
                String formattedDateStr = (value == null) ? null : xsdDateTimeFormat.format(value);
                if ( (existingValue!=null && value == null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
                    model.removeAll(res, dataprop, null);
                }
                if ( (existingValue==null && value != null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
                    model.add(res, dataprop, formattedDateStr, XSDDatatype.XSDdateTime);
                }
            }
        } catch (Exception e) {
            log.error("Error in updatePropertyDateTimeValue");
            log.error(e, e);
        }
    }

    /**
     * convenience method for use with functional object properties
     */
    protected void addPropertyResourceURIValue(Resource res, ObjectProperty prop, String objectURI) {
        Resource objectRes = getOntModel().getResource(objectURI);
        if (prop != null && objectRes != null) {
            res.addProperty(prop, objectRes);
        }
    }

    /**
     * convenience method for use with functional properties
     */
    protected void updatePropertyResourceURIValue(Resource res, Property prop, String objectURI) {

    	Model model = res.getModel();
    	
    	if (model != null) {
    		updatePropertyResourceURIValue(res, prop, objectURI, model);
    	}    	           
    }

    /**
     * convenience method for use with functional properties
     */
    protected void updatePropertyResourceURIValue(Resource res, Property prop, String uri, Model model) {
		log.debug("updatePropertyResourceURIValue(), resource="
				+ (res == null ? "null" : res.getURI()) + ", property="
				+ (prop == null ? "null" : prop.getURI()) + ", uri=" + uri);

        if (prop != null) {
            if (uri == null || uri.length() == 0) { 
            	// the empty string test is due to a deficiency in the
            	// backend editing where empty strings are treated as nulls
                model.removeAll(res, prop, null);
            } else {
                String badURIErrorStr = checkURI(uri);
                if (badURIErrorStr != null) {
                	log.error(badURIErrorStr);
                	return;
                }

                Resource existingValue = null;
                Statement stmt = res.getProperty(prop);
                if (stmt != null) {
                    RDFNode object = stmt.getObject();
                    if (object != null && object.isResource()){
                        existingValue = (Resource)object;
                    }
                }
	                
                if (existingValue == null ) {
                     model.add(res, prop, model.createResource(uri));	
                } else if (!(existingValue.getURI()).equals(uri)) {
             		 model.removeAll(res, prop, null);
              		 model.add(res, prop, model.createResource(uri));
                }
            }
        }
    }

    /**
     * convenience method for use with functional object properties
     */
    protected void addPropertyResourceValue(Resource res, Property prop, Resource objectRes) {
        if (prop != null && objectRes != null) {
            res.addProperty(prop, objectRes);
        }
    }

    /**
     * convenience method for use with functional properties
     */
    protected void updatePropertyResourceValue(Resource res, Property prop, Resource objectRes) {
        
    	Model model = res.getModel();
    	
    	if (model != null) {
    		updatePropertyResourceValue(res, prop, objectRes, model);
        }
    	
    }

    /**
     * convenience method for use with functional properties
     */
    protected void updatePropertyResourceValue(Resource res, Property prop, Resource objectRes, Model model) {
    	
        if (prop != null) {
            if (objectRes == null) {
                model.removeAll(res, prop, null);
            } else {
                Resource existingValue = null;
                Statement stmt = res.getProperty(prop);
                if (stmt != null) {
                    RDFNode object = stmt.getObject();
                    if (object != null && object.isResource()){
                        existingValue = (Resource)object;
                    }
                }
	                
                if (existingValue == null ) {
                     model.add(res, prop, objectRes);	
                } else if (!existingValue.equals(objectRes)) {
             		 model.removeAll(res, prop, null);
              		 model.add(res, prop, objectRes);
                }
            }
        }
    }
    
    /**
     * convenience method for updating the RDFS label
     */
    protected void updateRDFSLabel(OntResource ontRes, String label) {
    	
    	if (label != null && label.length() > 0) {
    		
    		String existingValue = ontRes.getLabel((String) getDefaultLanguage());
    	    
    		if (existingValue == null || !existingValue.equals(label)) {
    			ontRes.setLabel(label, (String) getDefaultLanguage());	
    	    }
    	} else {
    		ontRes.removeAll(RDFS.label);
    	}
    }
    
    private String getLabel(String lang, List<RDFNode>labelList) {
    	Iterator<RDFNode> labelIt = labelList.iterator();
    	while (labelIt.hasNext()) {
    		RDFNode label = labelIt.next();
    		if (label.isLiteral()) {
    			Literal labelLit = ((Literal)label);
    			String labelLanguage = labelLit.getLanguage();
    			if ( (labelLanguage==null) && (lang==null) ) {
    				return labelLit.getLexicalForm();
    			}
    			if ( (lang != null) && (lang.equals(labelLanguage)) ) {
    				return labelLit.getLexicalForm();
    			}
    		}
    	}
    	return null;
    }
    
    private final boolean ALSO_TRY_NO_LANG = true;
    
    /**
     * Get the rdfs:label or vitro:label, working through PERFERED_LANGUAGES,
     * or get local name, bnode Id, or full URI if no labels found.
     */
    protected String getLabelOrId(OntResource r) {
    	String label = null;
    	r.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    		label = getLabel(r);
    		if( label == null || label.length() == 0 )
    		    label = getLocalNameOrId(r);
    	} finally {
    		r.getOntModel().leaveCriticalSection();
    	}
        return label;
    }
    
    /**
     * works through list of PREFERRED_LANGUAGES to find an appropriate 
     * label, or NULL if not found.  
     */
    protected String getLabel(OntResource r){
        String label = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {            
            // try rdfs:label with preferred languages
            label = tryPropertyForPreferredLanguages( r, RDFS.label, ALSO_TRY_NO_LANG );
            
            // try vitro:label with preferred languages
            // Commenting out for NIHVIVO-1962
           /* if ( label == null ) {
                label = tryPropertyForPreferredLanguages( r, r.getModel().getProperty(VitroVocabulary.label), ALSO_TRY_NO_LANG );
            }   */                           
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return label;
    }
    
    /**
     * Get the local name, bnode or URI of the resource. 
     */
    protected String getLocalNameOrId(OntResource r){
        String label = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {                       
            String localName = r.getLocalName();
            if (localName != null) {
                label = localName;
            } else if (r.isAnon()) {
                label = r.getId().toString();
            } else {
                label = r.getURI();                
            }                       
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return label;
    }
    
    private String tryPropertyForPreferredLanguages( OntResource r, Property p, boolean alsoTryNoLang ) {
    	String label = null;
	    List<RDFNode> labels = (List<RDFNode>) r.listPropertyValues(p).toList();
	    for (int i=0; i<PREFERRED_LANGUAGES.length; i++) {
	    	String lang = PREFERRED_LANGUAGES[i];
	    	label = getLabel(lang,labels);
	    	if (label != null) {
	    		break;
	    	}
	    }
        if ( label == null && alsoTryNoLang ) {
        	label = getLabel("", labels);
        }
	    return label;
    }

    protected String getDefaultLanguage() {
        return PREFERRED_LANGUAGES[0];
    }

    protected Map<String,OntClass>getFlag2ValueMap() {
        try {
            return ((WebappDaoFactoryJena) this.webappDaoFactory).getFlag2ValueMap();
        } catch (ClassCastException cce) {
            return null;
        }
    }

    protected Map<Resource,String>getFlag2ClassLabelMap() {
        try {
            return ((WebappDaoFactoryJena) this.webappDaoFactory).getFlag2ClassLabelMap();
        } catch (ClassCastException cce) {
            return null;
        }
    }
    
    /**
     * Checks a URI for validity.  Jena models can store invalid URIs, but this causes RDF/XML output serialization to fail.
     * @param uri
     * @return null if URI is good, otherwise an error message String
     */
    protected String checkURI( String uri ) {
    	IRIFactory factory = IRIFactory.jenaImplementation();
        IRI iri = factory.create( uri );
        if (iri.hasViolation(false) ) {
        	String errorStr = ("Bad URI: "+ uri +
        	"\nOnly well-formed absolute URIrefs can be included in RDF/XML output: "
                 + ((Violation)iri.violations(false).next()).getShortMessage());
        	return errorStr;
        } else {
        	return null;
        }
    }
    
    /* *********************************************************** */
    
    public synchronized boolean isBooleanClassExpression(OntClass cls) {
    	return (cls.isComplementClass() || cls.isIntersectionClass() || cls.isUnionClass());
    }
    
    protected OntClass getOntClass(OntModel ontModel, String vitroURIStr) {
    	ontModel.enterCriticalSection(Lock.READ);
    	try {
    		OntClass cls = null;
    		if (vitroURIStr==null)
    			return null;
    		if (vitroURIStr.indexOf(PSEUDO_BNODE_NS)==0) {
    			String idStr = vitroURIStr.split("#")[1];
    			RDFNode rdfNode = ontModel.getRDFNode(Node.createAnon(AnonId.create(idStr)));
    			if ( (rdfNode != null) && (rdfNode.canAs(OntClass.class)) ) {
    				cls = (OntClass) rdfNode.as(OntClass.class);
    			}
			} else {
				try {
					cls = ontModel.getOntClass(vitroURIStr);
				} catch (Exception e) {
					cls = null;
				}
			}
    		return cls;
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }
    
    protected String getClassURIStr(Resource cls) {
    	if (cls.isAnon()) {
    		return PSEUDO_BNODE_NS+cls.getId().toString();
    	} else {
    		return cls.getURI();
    	}
    }

    protected Node makeNodeForURI(String vitroURIStr) {
    	if (vitroURIStr.indexOf(PSEUDO_BNODE_NS)==0) {
			return Node.createAnon(AnonId.create(vitroURIStr.split("#")[1]));
    	} else {
    		return Node.createURI(vitroURIStr);
    	}
    }
    
    protected List<Resource> listDirectObjectPropertyValues(Resource subj, Property prop) {
    	// This is a quick and dirty algorithm for getting direct property values.
    	// It will only work properly if the full transitive closure is present in the graph;
    	// Otherwise, it will include additional values that are not strictly direct values.
    	Set<Resource> possibleValueSet = new HashSet<Resource>();
    	List<Resource> directValueList = new ArrayList<Resource>();
    	// List all of the property values
    	StmtIterator stmtIt = getOntModel().listStatements(subj, prop, (RDFNode)null);
    	while (stmtIt.hasNext()) {
    		Statement stmt = stmtIt.nextStatement();
    		if (stmt.getObject().isResource()) {
    			possibleValueSet.add((Resource)stmt.getObject());
    		}
    	}
    	// Now for each value, work backwards and see if it has an alternate path to the original resource.
    	// If not, add it to the list of direct values.
    	Iterator<Resource> possibleValueIt = possibleValueSet.iterator();
    	while (possibleValueIt.hasNext()) {
    		Resource possibleRes = possibleValueIt.next();
    		StmtIterator pStmtIt = getOntModel().listStatements((Resource)null, prop, possibleRes);
    		boolean hasAlternatePath = false;
        	while (stmtIt.hasNext()) {
        		Statement stmt = stmtIt.nextStatement();
        		if (possibleValueSet.contains(stmt.getSubject())) {
        			hasAlternatePath = true;
        			break;
        		}
        	}
        	if (!hasAlternatePath) {
        		directValueList.add(possibleRes);
        	}
    	}
    	return directValueList;
    }
    
    // the same thing as the previous method but going the other direction
    protected List<Resource> listDirectObjectPropertySubjects(Resource value, Property prop) {
    	Set<Resource> possibleSubjectSet = new HashSet<Resource>();
    	List<Resource> directSubjectList = new ArrayList<Resource>();
    	StmtIterator stmtIt = getOntModel().listStatements((Resource)null, prop, value);
    	while (stmtIt.hasNext()) {
    		Statement stmt = stmtIt.nextStatement();
    		possibleSubjectSet.add((Resource)stmt.getSubject());
    		
    	}
    	Iterator<Resource> possibleSubjectIt = possibleSubjectSet.iterator();
    	while (possibleSubjectIt.hasNext()) {
    		Resource possibleRes = possibleSubjectIt.next();
    		StmtIterator pStmtIt = getOntModel().listStatements(possibleRes, prop, (RDFNode)null);
    		boolean hasAlternatePath = false;
        	while (stmtIt.hasNext()) {
        		Statement stmt = stmtIt.nextStatement();
        		if (stmt.getObject().isResource() && possibleSubjectSet.contains((Resource)stmt.getObject())) {
        			hasAlternatePath = true;
        			break;
        		}
        	}
        	if (!hasAlternatePath) {
        		directSubjectList.add(possibleRes);
        	}
    	}
    	return directSubjectList;
    }
    
    protected void smartRemove(OntResource ontRes, OntModel ontModel) {
    	removeFromLists(ontRes, ontModel);
    	List<Statement> stmtForDependentRes = DependentResourceDeleteJena.getDependentResourceDeleteList(ontRes,ontModel);
    	removeUsingDescribe(ontRes, ontModel);
    	ontModel.remove(stmtForDependentRes);
    }
 
    /**
     * Removes a resource from any rdf:Lists in which it is a member
     */
    private void removeFromLists(OntResource res, OntModel ontModel) {
    	// Iterate through all of the list nodes this resource is attached to
    	Iterator<Resource> listNodeIt = ontModel.listSubjectsWithProperty(RDF.first, res);
    	while (listNodeIt.hasNext()) {
    		Resource listNode = listNodeIt.next();
    		//get the next node in the list
    		Statement nextNodeStmt = listNode.getProperty(RDF.rest);
    		if (nextNodeStmt != null) {
    			// link the previous node (or resource linking to the head of the list)
    			// to the next node
    			RDFNode nextNode = nextNodeStmt.getObject();
    			StmtIterator prevNodeIt = ontModel.listStatements((Resource) null, null, listNode);
    			while (prevNodeIt.hasNext()) {
    				Statement stmt = prevNodeIt.nextStatement();
    				if (!stmt.getPredicate().equals(RDF.rest)) {
    					// if current node is list head
    					if (!nextNode.equals(RDF.nil)) {
    						// only repair the list if there is more than one node
    						ontModel.add(stmt.getSubject(), RDF.rest, nextNode);
    					}
    				} else {
    					ontModel.add(stmt.getSubject(), RDF.rest, nextNode);
    				}
    			}
    		}
    		//Remove any statements about this node
    		ontModel.remove(listNode, (Property) null, (RDFNode) null);
    	}
    }
    
    public void removeRulesMentioningResource(Resource res, OntModel ontModel) {
    	Iterator<Resource> impIt = ontModel.listSubjectsWithProperty(RDF.type, SWRL.Imp);
    	while (impIt.hasNext()) {
    		Resource imp = impIt.next();
    		boolean removeMe = false;
    		Model description = describeResource(imp, ontModel);
    		NodeIterator objIt = description.listObjects();
    		try {
	    		while(objIt.hasNext()) {
	    			RDFNode obj = objIt.nextNode();
	    			if (obj.equals(res)) {
	    				removeMe = true;
	    			}
	    		}
    		} finally {
    			objIt.close();
    		}
    		if (removeMe) {
    			ontModel.remove(description);
    		}	
    	}
    }
    
    // removes a resource and its bnode closure using ARQ's DESCRIBE semantics 
    // plus any incoming properties
    private void removeUsingDescribe(OntResource ontRes, OntModel ontModel) {
    	Model temp = describeResource(ontRes, ontModel);
		temp.add(ontModel.listStatements((Resource) null, (Property) null, ontRes));
		ontModel.remove(temp);
    }
    
    private Model describeResource(Resource res, OntModel ontModel) {    	
    	Model temp = ModelFactory.createDefaultModel();
    	
    	// For now, not using DESCRIBE on blank nodes unless I can figure out
    	// how to keep it from doing a full kb scan.
    	if (res.isAnon()) {
    		temp.add(ontModel.listStatements(res, (Property) null, (RDFNode) null));
    		return temp;
    	}
    	
    	String describeQueryStr =    "DESCRIBE <" + res.getURI() + ">" ;
    	
//    	?	"PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> \n\n" +
//    		"DESCRIBE ?bnode \n" +
//    	    "WHERE { \n" +
//    		"    FILTER(afn:bnode(?bnode) = \"" + res.getId().toString() + "\")\n" +
//    		"    ?bnode ?p ?o \n" +
//    	    "}"
    	 
	    Query describeQuery = QueryFactory.create(describeQueryStr, Syntax.syntaxARQ);
		QueryExecution qe = QueryExecutionFactory.create(describeQuery, ontModel);
		qe.execDescribe(temp);
		
		return temp;
    }


}
