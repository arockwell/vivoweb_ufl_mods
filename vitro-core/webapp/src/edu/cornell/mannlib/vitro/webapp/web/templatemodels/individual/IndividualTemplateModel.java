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

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class IndividualTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(IndividualTemplateModel.class);
    
    private static final String PATH = Route.INDIVIDUAL.path();
    
    protected Individual individual;
    protected VitroRequest vreq;
    protected UrlBuilder urlBuilder;
    protected GroupedPropertyList propertyList = null;
    protected LoginStatusBean loginStatusBean = null;
    private EditingPolicyHelper policyHelper = null;

    private Map<String, String> qrData = null;

    public IndividualTemplateModel(Individual individual, VitroRequest vreq) {
        this.individual = individual;
        this.vreq = vreq;
        this.loginStatusBean = LoginStatusBean.getBean(vreq);
        // Needed for getting portal-sensitive urls. Remove if multi-portal support is removed.
        this.urlBuilder = new UrlBuilder(vreq.getPortal());
        
        // If editing, create a helper object to check requested actions against policies
        if (isEditable()) {
            policyHelper = new EditingPolicyHelper(vreq, getServletContext());
        } 
    }
    
//    private boolean isVClass(String vClassUri) {
//        boolean isVClass = individual.isVClass(vClassUri);  
//        // If reasoning is asynchronous (under RDB), this inference may not have been made yet. 
//        // Check the superclasses of the individual's vclass.
//        if (!isVClass && SimpleReasoner.isABoxReasoningAsynchronous(getServletContext())) { 
//            log.debug("Checking superclasses to see if individual is a " + vClassUri + " because reasoning is asynchronous");
//            List<VClass> directVClasses = individual.getVClasses(true);
//            for (VClass directVClass : directVClasses) {
//                VClassDao vcDao = vreq.getWebappDaoFactory().getVClassDao();
//                List<String> superClassUris = vcDao.getAllSuperClassURIs(directVClass.getURI());
//                if (superClassUris.contains(vClassUri)) {
//                    isVClass = true;
//                    break;
//                }
//            }
//        }
//        return isVClass;
//    }
    
    
    /* These methods perform some manipulation of the data returned by the Individual methods */
    
    public String getProfileUrl() {
        return UrlBuilder.getIndividualProfileUrl(individual, vreq.getWebappDaoFactory());
    }

    // This remains as a convenience method for getting the image url. We could instead use a custom list 
    // view for mainImage which would provide this data in the query results.
    public String getImageUrl() {
        String imageUrl = individual.getImageUrl();
        return imageUrl == null ? null : getUrl(imageUrl);
    }

    // This remains as a convenience method for getting the thumbnail url. We could instead use a custom list 
    // view for mainImage which would provide this data in the query results.
    public String getThumbUrl() {
        String thumbUrl = individual.getThumbUrl();
        return thumbUrl == null ? null : getUrl(thumbUrl);
    } 
    
    public String getRdfUrl() {
        return getRdfUrl(true);
    }
    
    // Used to create a link to generate the individual's rdf.
    public String getRdfUrl(boolean checkExternalNamespaces) {
        
        String individualUri = getUri();
        String profileUrl = getProfileUrl();
        
        URI uri = new URIImpl(individualUri);
        String namespace = uri.getNamespace();
        
        // Individuals in the default namespace
        // e.g., http://vivo.cornell.edu/individual/n2345/n2345.rdf
        // where default namespace = http://vivo.cornell.edu/individual/ 
        String defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
        if (defaultNamespace.equals(namespace)) {
            return profileUrl + "/" + getLocalName() + ".rdf";
        } 
        
        // An RDF url is not defined for an externally linked namespace. The data does not reside
        // in the current system, and the external system may not accept a request for rdf.
        if (checkExternalNamespaces && vreq.getWebappDaoFactory()
                                           .getApplicationDao()
                                           .isExternallyLinkedNamespace(namespace)) {
            return null;
        }

        // http://some.other.namespace/n2345?format=rdfxml
        return UrlBuilder.addParams(profileUrl, "format", "rdfxml");

    }
    
    public String getEditUrl() {
        return urlBuilder.getPortalUrl(Route.INDIVIDUAL_EDIT, "uri", getUri());
    }

    public GroupedPropertyList getPropertyList() {
        if (propertyList == null) {
            propertyList = new GroupedPropertyList(individual, vreq, policyHelper);
        }
        return propertyList;
    }
    
    public boolean isEditable() {
        // RY This will be improved later. What is important is not whether the user is a self-editor,
        // but whether he has editing privileges on this profile. This is just a crude way of determining
        // whether to even bother looking at the editing policies.
        return VitroRequestPrep.isSelfEditing(vreq) || loginStatusBean.isLoggedIn();            
    }
    
    public boolean getShowAdminPanel() {
        return loginStatusBean.isLoggedInAtLeast(LoginStatusBean.EDITOR);
    }
 
    /* rdfs:label needs special treatment, because it is not possible to construct a 
     * DataProperty from it. It cannot be handled the way the vitro links and vitro public image
     * are handled like ordinary ObjectProperty instances.
     */
    public DataPropertyStatementTemplateModel getNameStatement() {
        String propertyUri = VitroVocabulary.LABEL; // rdfs:label
        DataPropertyStatementTemplateModel dpstm = new DataPropertyStatementTemplateModel(getUri(), propertyUri, vreq, policyHelper);
        
        // If the individual has no rdfs:label, return the local name. It will not be editable (this replicates previous behavior;
        // perhaps we would want to allow a label to be added. But such individuals do not usually have their profiles viewed or
        // edited directly.
        if (dpstm.getValue() == null) {
            dpstm.setValue(getLocalName());
        }
        
        return dpstm;
    }
    
    public String getSelfEditingId() {
        String id = null;
        String idMatchingProperty = ConfigurationProperties.getProperty("selfEditing.idMatchingProperty");
        if (! StringUtils.isBlank(idMatchingProperty)) {
            // Use assertions model to side-step filtering. We need to get the value regardless of whether the property
            // is visible to the current user.
            WebappDaoFactory wdf = vreq.getAssertionsWebappDaoFactory();
            Collection<DataPropertyStatement> ids = 
                wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, idMatchingProperty);
            if (ids.size() > 0) {
                id = ids.iterator().next().getData();
            }
        }
        return id;
    }
    
    /* These methods simply forward to the methods of the wrapped individual. It would be desirable to 
     * implement a scheme for proxying or delegation so that the methods don't need to be simply listed here. 
     * A Ruby-style method missing method would be ideal. 
     * Update: DynamicProxy doesn't work because the proxied object is of type Individual, so we cannot
     * declare new methods here that are not declared in the Individual interface. 
     */
    
    public String getName() {           
        return individual.getName();
    }

    public String getMoniker() {
        return individual.getMoniker();
    }

    public String getUri() {
        return individual.getURI();
    }
    
    public String getLocalName() {
        return individual.getLocalName();
    }   
    public Map<String, String> doQrData() {
        if(qrData == null)
            qrData = generateQrData();
        return qrData;
    }

    private Map<String, String> generateQrData() {
        String core = "http://vivoweb.org/ontology/core#";
        String foaf = "http://xmlns.com/foaf/0.1/";

        Map<String,String> qrData = new HashMap<String,String>();
        WebappDaoFactory wdf = vreq.getAssertionsWebappDaoFactory();
        Collection<DataPropertyStatement> firstNames = wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, foaf + "firstName");
        Collection<DataPropertyStatement> lastNames = wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, foaf + "lastName");
        Collection<DataPropertyStatement> preferredTitles = wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, core + "preferredTitle");
        Collection<DataPropertyStatement> phoneNumbers = wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, core + "phoneNumber");
        Collection<DataPropertyStatement> emails = wdf.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(individual, core + "email");

        if(firstNames != null && ! firstNames.isEmpty())
            qrData.put("firstName", firstNames.toArray(new DataPropertyStatement[firstNames.size()])[0].getData());
        if(lastNames != null && ! lastNames.isEmpty())
            qrData.put("lastName", lastNames.toArray(new DataPropertyStatement[firstNames.size()])[0].getData());
        if(preferredTitles != null && ! preferredTitles.isEmpty())
            qrData.put("preferredTitle", preferredTitles.toArray(new DataPropertyStatement[firstNames.size()])[0].getData());
        if(phoneNumbers != null && ! phoneNumbers.isEmpty())
            qrData.put("phoneNumber", phoneNumbers.toArray(new DataPropertyStatement[firstNames.size()])[0].getData());
        if(emails != null && ! emails.isEmpty())
            qrData.put("email", emails.toArray(new DataPropertyStatement[firstNames.size()])[0].getData());

        String tempUrl = vreq.getRequestURL().toString();
        String prefix = "http://";
        tempUrl = tempUrl.substring(0, tempUrl.replace(prefix, "").indexOf("/") + prefix.length());
        String profileUrl = getProfileUrl();
        String externalUrl = tempUrl + profileUrl;
        qrData.put("externalUrl", externalUrl);

        String individualUri = individual.getURI();
        String contextPath = vreq.getContextPath();
        qrData.put("exportQrCodeUrl", contextPath + "/qrcode?uri=" + UrlBuilder.urlEncode(individualUri));
        
        qrData.put("aboutQrCodesUrl", contextPath + "/qrcode/about");
        
        return qrData;
    }
    
}
