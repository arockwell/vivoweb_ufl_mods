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

package edu.cornell.mannlib.vitro.webapp.dwr;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
   Represents a Vitro entity to entity property.  It includes values
   from the entities, object property statements, properties, and tables
   bundled up in a usable object.

   This is a class intended to support Direct Web Remoting(DWR).
   It exposes methods that can be called from javascript from browsers.
   We could expose some other objects but this allows us to expose only
   the methods we want for security reasons, though some of these are
   destructive.
*/
public class PropertyDWR {
	
	private static final Log log = LogFactory.getLog(PropertyDWR.class.getName());
	
    public static boolean debug = true;

    public PropertyDWR(){               
    }
    
    private WebappDaoFactory getUnfilteredWebappDaoFactory(VitroRequest vreq) {
    	try {
    		return (WebappDaoFactory) vreq.getSession().getServletContext().getAttribute("webappDaoFactory");
    	} catch (ClassCastException e) {
    		log.warn("Could not find unfiltered WebappDaoFactory in getServletContext().getAttribute(\"webappDaoFactory\"). " +
    				"Using vreq.getWebappDaoFactory() instead.");
    		return vreq.getWebappDaoFactory();
    	}
    }

    public Object test(){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        Collection c = 
            wdf.getPropertyInstanceDao().getExistingProperties("http://example.org/property", null);
        return c.iterator().next();
    }

    /**
     * Gets only unhidden properties.
     * @returns Collection of PropertyInstance objs
     */
    public Collection getAllPropInstByVClass(String classURI){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        Collection c = 
            wdf.getPropertyInstanceDao().getAllPropInstByVClass(classURI);
        return c;
    }
    
    public Collection getAllPossiblePropInstForIndividual(String individualURI) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();

        Collection c = 
            wdf.getPropertyInstanceDao().getAllPossiblePropInstForIndividual(individualURI);
    	return c;
    }

    public PropertyInstance getProperty(String subjectURI, String predicateURI, String objectURI) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
       return wdf.getPropertyInstanceDao().getProperty(subjectURI, predicateURI, objectURI);
    }

    /**
       The following fields are used by this method to write this property
       to the object property statement table:

         domainEntId
         rangeEntId
         PropertyId
         sunrise - optional
         sunset - optional
         qualifier - optional

       Initially this tries an insert, if that fails then an update is attempted.
       @return -1 for error, 0 for no write due to duplicate, > 0 is auto-gen 
       objectPropertyStatement.id
    */
    @Deprecated
    public int writeProp(PropertyInstance prop ){
//        WebContext ctx = WebContextFactory.get();
//        HttpServletRequest req = ctx.getHttpServletRequest();
//        VitroRequest vreq = new VitroRequest(req);
//
//        return getUnfilteredWebappDaoFactory(vreq).getPropertyInstanceDao().writeProp(prop);
        return -1;
    }

    public int insertProp( PropertyInstance prop) {
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);

        return getUnfilteredWebappDaoFactory(vreq).getPropertyInstanceDao().insertProp(prop);                
    }

    public int deleteProp(String subjectUri, String predicateUri, String objectUri){
        WebContext ctx = WebContextFactory.get();
        HttpServletRequest req = ctx.getHttpServletRequest();
        VitroRequest vreq = new VitroRequest(req);

        getUnfilteredWebappDaoFactory(vreq).getPropertyInstanceDao().deleteObjectPropertyStatement(subjectUri, predicateUri, objectUri);
        return 0;
    }

    /**
       Gets all of the existing qualifiers for a given properties.id
    */
    @Deprecated
    public Collection getExistingQualifiers(String propertyURI){
        Collection c = new ArrayList(1);
        c.add("qualifiers are no longer supporeted ");
        return c;
    }

    /**
       Gets all of the properties that exist on the object property statements table for this entity.
       This includes all rows that have the entityId as the domain and as the range.
       Propeties for which the entityId is the domain will be getDomainSide() == true
       and for which the entityId is the range side will be getDomainSide() == false.
    */
   public Collection getExistingProperties(String entityURI){
       WebContext ctx = WebContextFactory.get();
       HttpServletRequest req = ctx.getHttpServletRequest();
       VitroRequest vreq = new VitroRequest(req);
       WebappDaoFactory wdf = vreq.getWebappDaoFactory();
       
       return wdf.getPropertyInstanceDao().getExistingProperties(entityURI, null);       
   }
   
//   private PropertyInstance prepare(PropertyInstance in){
//       return in;
//   }
//   
//   public Collection prepare(Collection properties){
//       if( properties == null || properties.size() == 0 )
//           return Collections.EMPTY_LIST;
//       
//       ArrayList outProps = new ArrayList(properties.size());
//       for( Object prop : properties){
//           if( prop instanceof PropertyIn
//       return outProps;
//   }
}
