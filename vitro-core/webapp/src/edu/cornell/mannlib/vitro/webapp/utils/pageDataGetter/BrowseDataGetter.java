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

package edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.JSONServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.utils.JSONtoFmModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividualTemplateModel;

public class BrowseDataGetter implements PageDataGetter {
    final static Log log = LogFactory.getLog(BrowseDataGetter.class);
    
    @Override
    public Map<String, Object> getData(ServletContext context,
            VitroRequest vreq, String pageUri, Map<String, Object> page,
            String type) {
        try{            
            Map params = vreq.getParameterMap();
            Mode mode = getMode( vreq, params );
            switch( mode ){          
                case VCLASS_ALPHA:
                    return doClassAlphaDisplay(params,vreq,context);
                case CLASS_GROUP:
                    return doClassGroupDisplay(params, vreq, context);
                case VCLASS:
                    return doClassDisplay(params, vreq, context);
                case ALL_CLASS_GROUPS:
                    return doAllClassGroupsDisplay( params, page, vreq, context);
                default:
                    return doAllClassGroupsDisplay( params, page, vreq, context);
            }
            }catch(Throwable th){
                log.error(th,th);
                return Collections.emptyMap();
            }
    }

    @Override
    public String getType() { 
        return DisplayVocabulary.HOME_PAGE_TYPE;
    }
    

    private Map<String, Object> doClassAlphaDisplay( Map params, VitroRequest request, ServletContext context) throws Exception {
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context, request));
        body.putAll(getClassAlphaValues(params,request,context));        
        return body;
    }

    private Map<String,Object> getClassAlphaValues( Map params, VitroRequest request, ServletContext context) throws Exception{
        Map<String,Object> map= new HashMap<String,Object>();
        
        String classUri = getParam(Mode.VCLASS, request, params);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        JSONObject vclassRes = JSONServlet.getLuceneIndividualsByVClass(vclass.getURI(), request, context);        
        map.put("totalCount", JSONtoFmModel.convertJSONObjectToMap( (String) vclassRes.get("totalCount") ));
        map.put("alpha", JSONtoFmModel.convertJSONObjectToMap( (String) vclassRes.get("alpha") ));
        map.put("individuals", JSONtoFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("individuals") ));
        map.put("pages", JSONtoFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("pages") ));
        map.put("letters", JSONtoFmModel.convertJSONArrayToList( (JSONArray) vclassRes.get("letters") ));
        
        return map;
    }
    
    private Map<String,Object> getCommonValues( ServletContext context, VitroRequest vreq){
        Map<String,Object> values = new HashMap<String,Object>();              
                
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        List<VClassGroup> cgList = vcgc.getGroups(vreq.getPortalId());        
        LinkedList<VClassGroupTemplateModel> cgtmList = new LinkedList<VClassGroupTemplateModel>();
        for( VClassGroup classGroup : cgList){
            cgtmList.add( new VClassGroupTemplateModel( classGroup ));
        }
        values.put("vClassGroups",cgtmList);
        
        return values;
    }
    
    protected Map<String, Object> doAllClassGroupsDisplay( Map params, Map<String, Object> page, VitroRequest request, ServletContext context) {        
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context,request));        
        body.putAll(getAllClassGroupData(request, params, page, context));
                        
        return body;
    }
   
    /**
     * Gets a list of all VClassGroups with vclasses with individual counts.
     * @param params2 
     */
    protected Map<String,Object> getAllClassGroupData(VitroRequest request, Map params, Map<String, Object> page, ServletContext context){
        Map<String,Object> map = new HashMap<String,Object>();                  
        return map;
    }
    
    protected Map<String, Object> doClassDisplay( Map params,
            VitroRequest request, ServletContext context) {                
        Map<String,Object> body = new HashMap<String,Object>();
        
        body.putAll(getCommonValues(context,request));        
        body.putAll(getClassData(request,params,context));
            
        return body;
    }

    private Map<String, Object> getClassData(VitroRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        map.putAll(getClassGroupData(request, params,context));
        
        String classUri = getParam(Mode.VCLASS, request, params);
        VitroRequest vreq = new VitroRequest(request);
        VClass vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
        map.put("class", new VClassTemplateModel(vclass));
        
        List<Individual> inds = vreq.getWebappDaoFactory().getIndividualDao()
            .getIndividualsByVClass(vclass);
        
        List<ListedIndividualTemplateModel> tInds = new ArrayList<ListedIndividualTemplateModel>(inds.size());
        for( Individual ind : inds){
            tInds.add(new ListedIndividualTemplateModel(ind, vreq));
        }
        map.put("individualsInClass", tInds);

        return map;
    }

    protected Map<String, Object> doClassGroupDisplay(Map params, VitroRequest request, ServletContext context) {
        Map<String,Object> body = new HashMap<String,Object>();
        body.putAll(getCommonValues(context,request));        
        body.putAll( getClassGroupData(request,params, context));
   
        return body;
    } 
        
    protected Map<String, Object> getClassGroupData(VitroRequest request, Map params, ServletContext context) {
        Map<String,Object> map = new HashMap<String,Object>();
        
        String vcgUri = getParam(Mode.CLASS_GROUP, request, params);
        VitroRequest vreq = new VitroRequest(request);        
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        VClassGroup vcg = vcgc.getGroup(vreq.getPortalId(), vcgUri);        
        
        ArrayList<VClassTemplateModel> classes = new ArrayList<VClassTemplateModel>(vcg.size());
        for( VClass vc : vcg){
            classes.add(new VClassTemplateModel(vc));
        }
        map.put("classes", classes);
        
        map.put("classGroup", new VClassGroupTemplateModel(vcg));
        map.put("classGroupUri", vcg.getURI());
        
        return map;
    }
    
        
    enum Mode{
        VCLASS_ALPHA("vclassAlpha"),
        VCLASS("vclassUri"),
        CLASS_GROUP("classgroupUri"),
        ALL_CLASS_GROUPS("all");                                
        String param;
        Mode(String param){        
            this.param = param;
        }
    }
    
    protected final static Mode DEFAULT_MODE = Mode.ALL_CLASS_GROUPS;
    
    protected Mode getMode(VitroRequest request, Map<String, Object> params){
        for( Mode mode : Mode.values()){
            String queryParam = request.getParameter( mode.param );
            if( queryParam != null && !queryParam.isEmpty() ){
                return mode;
            }
            Object obj = params.get( mode.param );
            String param = obj != null ? obj.toString():null;
            if( param != null && !param.isEmpty() ){
                return mode;
            }
        }                
        return DEFAULT_MODE;        
    }
    
    public static String getParam(Mode mode, VitroRequest request, Map params){
        if( request.getParameter(mode.param) != null )
            return request.getParameter(mode.param);
        if( params.get(mode.param) != null )
            return params.get(mode.param).toString();
        else
            return null;
    }

}
