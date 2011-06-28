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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public abstract class ObjectPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyTemplateModel.class);      
    private static final String TYPE = "object";
    private static final String EDIT_PATH = "edit/editRequestDispatch.jsp";
    private static final String IMAGE_UPLOAD_PATH = "/uploadImages";
    
    private static final String END_DATE_TIME_VARIABLE = "dateTimeEnd";
    private static final Pattern ORDER_BY_END_DATE_TIME_PATTERN = 
        /* ORDER BY DESC(?dateTimeEnd)
         * ORDER BY ?subclass ?dateTimeEnd
         * ORDER BY DESC(?subclass) DESC(?dateTimeEnd)
         */
        Pattern.compile("ORDER\\s+BY\\s+((DESC\\()?\\?subclass\\)?\\s+)?DESC\\s*\\(\\s*\\?" + 
                END_DATE_TIME_VARIABLE + "\\)", Pattern.CASE_INSENSITIVE);

    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_PROPERTY = "property";
    private static final String DEFAULT_LIST_VIEW_QUERY_OBJECT_VARIABLE_NAME = "object";
    private static final Pattern SUBJECT_PROPERTY_OBJECT_PATTERN = 
        // ?subject ?property ?\w+
        Pattern.compile("\\?" + KEY_SUBJECT + "\\s+\\?" + KEY_PROPERTY + "\\s+\\?(\\w+)");
    
    protected static enum ConfigError {
        NO_SELECT_QUERY("Missing select query specification"),
        NO_SUBCLASS_SELECT("Query does not select a subclass variable"),
        NO_SUBCLASS_ORDER_BY("Query does not sort first by subclass variable"),
        NO_TEMPLATE("Missing template specification"),
        TEMPLATE_NOT_FOUND("Specified template does not exist");
        
        String message;
        
        ConfigError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String toString() {
            return getMessage();
        }
    }
    
    private PropertyListConfig config;
    private String objectKey;    
    
    // Used for editing
    private boolean addAccess = false;

    ObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq, 
            EditingPolicyHelper policyHelper)
        throws InvalidConfigurationException {
        
        super(op, subject, policyHelper);
        
        log.debug("Creating template model for object property " + op.getURI());
        
        setName(op.getDomainPublic());

        // Get the config for this object property
        try {
            config = new PropertyListConfig(op, vreq);
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
        }
        
        objectKey = getQueryObjectVariableName();
        
        // Determine whether a new statement can be added
        if (policyHelper != null) {
            RequestedAction action = new AddObjectPropStmt(subjectUri, propertyUri, RequestActionConstants.SOME_URI);
            if (policyHelper.isAuthorizedAction(action)) {
                addAccess = true;
            }
        }
    }
    
    protected ConfigError checkQuery(String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return ConfigError.NO_SELECT_QUERY;
        }
        return null;
    }
      
    protected String getSelectQuery() {
        return config.selectQuery;
    }
    
    protected Set<String> getConstructQueries() {
        return config.constructQueries;
    }
    
    protected String getTemplateName() {
        return config.templateName;
    }

    protected boolean hasDefaultListView() {
        return config.isDefaultConfig;
    }
    
    public static String getImageUploadUrl(String subjectUri, String action) {
        ParamMap params = new ParamMap(
                "entityUri", subjectUri,
                "action", action);                              
        return UrlBuilder.getUrl(IMAGE_UPLOAD_PATH, params);        
    }

    /** Return the name of the primary object variable of the query by inspecting the query string.
     * The primary object is the X in the assertion "?subject ?property ?X".
     */
    private String getQueryObjectVariableName() {
        
        String object = null;
        
        if (hasDefaultListView()) {
            object = DEFAULT_LIST_VIEW_QUERY_OBJECT_VARIABLE_NAME;
            log.debug("Using default list view for property " + propertyUri + 
                      ", so query object = '" + object + "'");
        } else {
            String queryString = getSelectQuery();
            Matcher m = SUBJECT_PROPERTY_OBJECT_PATTERN.matcher(queryString);
            if (m.find()) {
                object = m.group(1);
                log.debug("Query object for property " + propertyUri + " = '" + object + "'");
            }
        }
        
        return object;
    }
     
    protected static ObjectPropertyTemplateModel getObjectPropertyTemplateModel(ObjectProperty op, 
            Individual subject, VitroRequest vreq, EditingPolicyHelper policyHelper, 
            List<ObjectProperty> populatedObjectPropertyList) {
        
        if (op.getCollateBySubclass()) {
            try {
                return new CollatedObjectPropertyTemplateModel(op, subject, vreq, policyHelper, populatedObjectPropertyList);
            } catch (InvalidConfigurationException e) {
                log.warn(e.getMessage());     
                // If the collated config is invalid, instantiate an UncollatedObjectPropertyTemplateModel instead.
            }
        } 
        try {
            return new UncollatedObjectPropertyTemplateModel(op, subject, vreq, policyHelper, populatedObjectPropertyList);
        } catch (InvalidConfigurationException e) {
            log.error(e.getMessage());
            return null;
        }
    }
    
    /** Apply post-processing to query results to prepare for template */
    protected void postprocess(List<Map<String, String>> data, WebappDaoFactory wdf) {
        
        if (log.isDebugEnabled()) {
            log.debug("Data for property " + getUri() + " before postprocessing");
            logData(data);
        }
        
        ObjectPropertyDataPostProcessor postprocessor = config.postprocessor;
        if (postprocessor == null) {
            log.debug("No postprocessor for property " + getUri());
            return;
        } else {
            log.debug("Using postprocessor " + postprocessor.getClass().getName() + " for property " + getUri());
        }
        
        postprocessor.process(data);
        
        if (log.isDebugEnabled()) {
            log.debug("Data for property " + getUri() + " after postprocessing");
            logData(data);
        }               
    }
    
    protected void logData(List<Map<String, String>> data) {
        
        if (log.isDebugEnabled()) {
            int count = 1;
            for (Map<String, String> map : data) {
                log.debug("List item " + count);
                count++;
                for (String key : map.keySet()) {
                   log.debug(key + ": " + map.get(key));
               }
            }
        }        
    }
    
    /** The SPARQL query results may contain duplicate rows for a single object, if there are multiple solutions 
     * to the entire query. Remove duplicates here by arbitrarily selecting only the first row returned.
     * @param List<Map<String, String>> data
     */
    protected void removeDuplicates(List<Map<String, String>> data) {
        String objectVariableName = getObjectKey();
        if (objectVariableName == null) {
            log.error("Cannot remove duplicate statements for property " + getUri() + " because no object found to dedupe.");
            return;
        }
        List<String> foundObjects = new ArrayList<String>();
        log.debug("Removing duplicates from property: " + getUri());
        Iterator<Map<String, String>> dataIterator = data.iterator();
        while (dataIterator.hasNext()) {
            Map<String, String> map = dataIterator.next();
            String objectValue = map.get(objectVariableName);
            // We arbitrarily remove all but the first. Not sure what selection criteria could be brought to bear on this.
            if (foundObjects.contains(objectValue)) {
                dataIterator.remove();
            } else {
                foundObjects.add(objectValue);
            }
        }
    }
    
    /* Post-processing that must occur after collation, because it does reordering on collated subclass
     * lists rather than on the entire list. This should ideally be configurable in the config file
     * like the pre-collation post-processing, but for now due to time constraints it applies to all views.
     */
    protected void postprocessStatementList(List<ObjectPropertyStatementTemplateModel> statements) {        
        moveNullEndDateTimesToTop(statements);        
    }
    
    /* SPARQL ORDER BY gives null values the lowest value, so null datetimes occur at the end
     * of a list in descending sort order. Generally we assume that a null end datetime means the
     * activity is  ongoing in the present, so we want to display those at the top of the list.
     * Application of this method should be configurable in the config file, but for now due to
     * time constraints it applies to all views that sort by DESC(?dateTimeEnd), and the variable
     * name is hard-coded here. (Note, therefore, that using a different variable name  
     * effectively turns off this post-processing.)
     */
    protected void moveNullEndDateTimesToTop(List<ObjectPropertyStatementTemplateModel> statements) {
        String queryString = getSelectQuery();
        Matcher m = ORDER_BY_END_DATE_TIME_PATTERN.matcher(queryString);
        if ( ! m.find() ) {
            return;
        }
        
        // Store the statements with null end datetimes in a temporary list, remove them from the original list,
        // and move them back to the top of the original list.
        List<ObjectPropertyStatementTemplateModel> tempList = new ArrayList<ObjectPropertyStatementTemplateModel>();
        Iterator<ObjectPropertyStatementTemplateModel> iterator = statements.iterator();
        while (iterator.hasNext()) {
            ObjectPropertyStatementTemplateModel stmt = (ObjectPropertyStatementTemplateModel)iterator.next();
            String dateTimeEnd = (String) stmt.get(END_DATE_TIME_VARIABLE);
            if (dateTimeEnd == null) {
                // If the first statement has a null end datetime, all subsequent statements in the list also do,
                // so there is nothing to reorder.
                if (statements.indexOf(stmt) == 0) {
                    break;
                }               
                tempList.add(stmt); 
                iterator.remove(); 
            }
        }
        // Put all the statements with null end datetimes at the top of the list, preserving their original order.
        statements.addAll(0, tempList);
    
    }
    
    protected String getObjectKey() {
        return objectKey;
    }
    
    private class PropertyListConfig {  
        
        private static final String CONFIG_FILE_PATH = "/config/";
        private static final String DEFAULT_CONFIG_FILE_NAME = "listViewConfig-default.xml";
        
        private static final String NODE_NAME_QUERY_CONSTRUCT = "query-construct";
        private static final String NODE_NAME_QUERY_BASE = "query-base";
        private static final String NODE_NAME_QUERY_COLLATED = "query-collated";
        private static final String NODE_NAME_TEMPLATE = "template";
        private static final String NODE_NAME_POSTPROCESSOR = "postprocessor";

        /* NB The default post-processor is not the same as the post-processor for the default view. The latter
         * actually defines its own post-processor, whereas the default post-processor is used for custom views
         * that don't define a post-processor, to ensure that the standard post-processing applies.
         */
        private static final String DEFAULT_POSTPROCESSOR = 
            "edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.DefaultObjectPropertyDataPostProcessor";
        
        private boolean isDefaultConfig;
        private Set<String> constructQueries;
        private String selectQuery;
        private String templateName;
        private ObjectPropertyDataPostProcessor postprocessor = null;

        PropertyListConfig(ObjectProperty op, VitroRequest vreq) 
            throws InvalidConfigurationException {

            // Get the custom config filename
            String configFileName = vreq.getWebappDaoFactory().getObjectPropertyDao().getCustomListViewConfigFileName(op);
            if (configFileName == null) { // no custom config; use default config
                configFileName = DEFAULT_CONFIG_FILE_NAME;
            }
            log.debug("Using list view config file " + configFileName + " for object property " + op.getURI());
            
            String configFilePath = getConfigFilePath(configFileName);
            
            try {
                File config = new File(configFilePath);            
                if ( ! isDefaultConfig(configFileName) && ! config.exists() ) {
                    log.warn("Can't find config file " + configFilePath + " for object property " + op.getURI() + "\n" +
                            ". Using default config file instead.");
                    configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE_NAME);
                    // Should we test for the existence of the default, and throw an error if it doesn't exist?
                }                   
                setValuesFromConfigFile(configFilePath, op, vreq.getWebappDaoFactory());           

            } catch (Exception e) {
                log.error("Error processing config file " + configFilePath + " for object property " + op.getURI(), e);
                // What should we do here?
            }
            
            if ( ! isDefaultConfig(configFileName) ) {
                ConfigError configError = checkConfiguration(vreq);
                if ( configError != null ) { // the configuration contains an error
                    // If this is a collated property, throw an error: this results in creating an 
                    // UncollatedPropertyTemplateModel instead.
                    if (ObjectPropertyTemplateModel.this instanceof CollatedObjectPropertyTemplateModel) {
                        throw new InvalidConfigurationException(configError.getMessage());
                    }
                    // Otherwise, switch to the default config
                    log.warn("Invalid list view config for object property " + op.getURI() + 
                            " in " + configFilePath + ":\n" +                            
                            configError + " Using default config instead.");
                    configFilePath = getConfigFilePath(DEFAULT_CONFIG_FILE_NAME);
                    setValuesFromConfigFile(configFilePath, op, vreq.getWebappDaoFactory());                    
                }
            }
            
            isDefaultConfig = isDefaultConfig(configFileName);
        }
        
        private boolean isDefaultConfig(String configFileName) {
            return configFileName.equals(DEFAULT_CONFIG_FILE_NAME);
        }
        
        private ConfigError checkConfiguration(VitroRequest vreq) {

            ConfigError error = ObjectPropertyTemplateModel.this.checkQuery(selectQuery);
            if (error != null) {
                return error;
            }

            if (StringUtils.isBlank(selectQuery)) {
                return ConfigError.NO_SELECT_QUERY;
            }

            if ( StringUtils.isBlank(templateName)) {
               return ConfigError.NO_TEMPLATE;
            }

            Configuration fmConfig = (Configuration) vreq.getAttribute("freemarkerConfig");
            TemplateLoader tl = fmConfig.getTemplateLoader();
            try {
                if ( tl.findTemplateSource(templateName) == null ) {
                    return ConfigError.TEMPLATE_NOT_FOUND;
                }
            } catch (IOException e) {
                log.error("Error finding template " + templateName, e);
            }

            return null;
        }
        
        private void setValuesFromConfigFile(String configFilePath, ObjectProperty op, WebappDaoFactory wdf) {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
 
            try {
                db = dbf.newDocumentBuilder();
                Document doc = db.parse(configFilePath);
                String propertyUri = op.getURI();
                // Required values
                String queryNodeName = 
                    // Don't test op.getCollateBySubclass(), since if creating a CollatedObjectPropertyTemplateModel failed,
                    // we now want to create an UncollatedObjectPropertyTemplateModel
                    (ObjectPropertyTemplateModel.this instanceof CollatedObjectPropertyTemplateModel) ?
                        NODE_NAME_QUERY_COLLATED : NODE_NAME_QUERY_BASE;
                        
                log.debug("Using query element " + queryNodeName + " for object property " + propertyUri);
                selectQuery = getConfigValue(doc, queryNodeName, propertyUri);
                templateName = getConfigValue(doc, NODE_NAME_TEMPLATE, propertyUri); 
                
                constructQueries = getConfigValues(doc, NODE_NAME_QUERY_CONSTRUCT, propertyUri);
                
                String postprocessorName = getConfigValue(doc, NODE_NAME_POSTPROCESSOR, propertyUri);

                if (StringUtils.isBlank(postprocessorName)) {
                    log.debug("No postprocessor specified for property " + propertyUri + ". Using default postprocessor.");
                    postprocessorName = DEFAULT_POSTPROCESSOR;
                } 
                try {
                    getPostProcessor(postprocessorName, wdf);
                } catch (Exception e) {
                    if (! postprocessorName.equals(DEFAULT_POSTPROCESSOR)) {
                        log.debug("Cannot find postprocessor specified for property " + propertyUri + ". Using default postprocessor.");
                        postprocessorName = DEFAULT_POSTPROCESSOR;
                        getPostProcessor(postprocessorName, wdf);
                    }                 
                }
                
            } catch (Exception e) {
                log.error("Error processing config file " + configFilePath, e);
                // What should we do here?
            }            
        }
        
        private void getPostProcessor(String name, WebappDaoFactory wdf) throws Exception {
            Class<?> postprocessorClass = Class.forName(name);
            Constructor<?> constructor = postprocessorClass.getConstructor(ObjectPropertyTemplateModel.class, WebappDaoFactory.class);
            postprocessor = (ObjectPropertyDataPostProcessor) constructor.newInstance(ObjectPropertyTemplateModel.this, wdf);           
        }
 
        private String getConfigValue(Document doc, String nodeName, String propertyUri) {
            NodeList nodes = doc.getElementsByTagName(nodeName);
            Element element = (Element) nodes.item(0); 
            String value = null;
            if (element != null) {
                value = element.getChildNodes().item(0).getNodeValue();   
                log.debug("Found config parameter " + nodeName + " for object property " + propertyUri +  " with value " + value);
            } else {
                log.debug("No value found for config parameter " + nodeName + " for object property " + propertyUri);
            }
            return value;           
        }
        
        private Set<String> getConfigValues(Document doc, String nodeName, String propertyUri) {
            Set<String> values = null;
            NodeList nodes = doc.getElementsByTagName(nodeName);
            int nodeCount = nodes.getLength();
            if (nodeCount > 0) {
                values = new HashSet<String>(nodeCount);
                for (int i = 0; i < nodeCount; i++) {
                    Element element = (Element) nodes.item(i);
                    String value = element.getChildNodes().item(0).getNodeValue();
                    values.add(value);  
                    log.debug("Found config parameter " + nodeName + " for object property " + propertyUri +  " with value " + value);
                }
            } else {
                log.debug("No values found for config parameter " + nodeName + " for object property " + propertyUri);
            }
            return values;
        }
        
        private String getConfigFilePath(String filename) {
            return servletContext.getRealPath(CONFIG_FILE_PATH + filename);
        }
    }
    
    protected class InvalidConfigurationException extends Exception { 

        private static final long serialVersionUID = 1L;

        protected InvalidConfigurationException(String s) {
            super(s);
        }
    }
    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }
    
    public String getTemplate() {
        return config.templateName;
    }
    
    public abstract boolean isCollatedBySubclass();

    @Override
    public String getAddUrl() {
        String addUrl = "";
        if (addAccess) {
            if (propertyUri.equals(VitroVocabulary.IND_MAIN_IMAGE)) {
                return getImageUploadUrl(subjectUri, "add");
            } 
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri);                              
            addUrl = UrlBuilder.getUrl(EDIT_PATH, params);  

        }
        return addUrl;
    }

}
