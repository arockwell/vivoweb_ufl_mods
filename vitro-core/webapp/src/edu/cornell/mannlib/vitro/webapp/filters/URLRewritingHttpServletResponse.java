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

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;

public class URLRewritingHttpServletResponse implements HttpServletResponse {

	private final static Log log = LogFactory.getLog(URLRewritingHttpServletResponse.class);
	
	private HttpServletResponse _response;
	private ServletContext _context;
	private WebappDaoFactory wadf;
	private int contextPathDepth;
	private Pattern slashPattern = Pattern.compile("/");
	
	public URLRewritingHttpServletResponse(HttpServletResponse response, HttpServletRequest request, ServletContext context) {
		this._response = response;
		this._context = context;
		this.wadf = (WebappDaoFactory) context.getAttribute("webappDaoFactory");
		this.contextPathDepth = slashPattern.split(request.getContextPath()).length-1;
	}

	/**
	 * For use in testing only.
	 */
	protected URLRewritingHttpServletResponse(){ }
	
	public void addCookie(Cookie arg0) {
		_response.addCookie(arg0);
	}

	public void addDateHeader(String arg0, long arg1) {
		_response.addDateHeader(arg0, arg1);
	}

	public void addHeader(String arg0, String arg1) {
		_response.addHeader(arg0, arg1);
	}

	public void addIntHeader(String arg0, int arg1) {
		_response.addIntHeader(arg0, arg1);
	}

	public boolean containsHeader(String arg0) {
		return _response.containsHeader(arg0);
	}

	/**
	 * @deprecated
	 */
	public String encodeRedirectUrl(String arg0) {
		return _response.encodeRedirectUrl(arg0);
	}

	public String encodeRedirectURL(String arg0) {
		return _response.encodeRedirectURL(arg0);
	}
	
	/**
	 * @deprecated
	 */
	public String encodeUrl(String arg0) {
		return _response.encodeUrl(arg0);
	}

	public String encodeURL(String inUrl) {
	    List<String> externallyLinkedNamespaces = wadf.getApplicationDao().getExternallyLinkedNamespaces();
        NamespaceMapper nsMap = NamespaceMapperFactory.getNamespaceMapper(_context);

        if( log.isDebugEnabled() ){
            log.debug("START");
            log.debug("charEncoding: "  + this.getCharacterEncoding() );
            log.debug("PortalPickerFilter.getPortalPickerFilter(this._context)," + PortalPickerFilter.getPortalPickerFilter(this._context));
            log.debug("wadf.getPortalDao().isSinglePortal(), " + wadf.getPortalDao().isSinglePortal());
            log.debug("contextPathDepth," + contextPathDepth);
            log.debug("nsMap," + nsMap);
            log.debug("wadf.getDefaultNamespace(), " + wadf.getDefaultNamespace());
            log.debug("externallyLinkedNamespaces " + externallyLinkedNamespaces);
            log.debug( inUrl );
        }
        
	    String encodedUrl = encodeForVitro(
	            inUrl,
	            this.getCharacterEncoding(),
	            PortalPickerFilter.getPortalPickerFilter(this._context),
	            wadf.getPortalDao().isSinglePortal(),
	            contextPathDepth,
	            nsMap,
	            wadf.getDefaultNamespace(),
	            externallyLinkedNamespaces
	            );
	    
	    log.debug(encodedUrl);
	    log.debug("END");
	    return encodedUrl;
	}
	
	/**
	 * bdc34: Isolating this method for unit 
	 * testing purposes.  This method should not use 
	 * any object properties, only objects passed into method. 
	 */
	protected String encodeForVitro(
	        String inUrl, 
	        String characterEncoding , 
	        PortalPickerFilter portalPickerFilter,
	        Boolean isSInglePortal,
	        int contextPathDepth,
	        NamespaceMapper nsMap,
	        String defaultNamespace,
	        List<String> externalNamespaces) {
		try {
			if( log.isDebugEnabled() ){
			    log.debug("Incomming URL '" + inUrl + "'");
			}
			VitroURL url = new VitroURL(inUrl,characterEncoding);
			if (url.host != null) {
				// if it's not an in-context URL, we don't want to mess with it
				// It looks like encodeURL isn't even called for external URLs
			    //String rv = _response.encodeURL(inUrl); 
	            String rv = inUrl;
			    if( log.isDebugEnabled()){
			        log.debug("Encoded as  '"+rv+"'");
			    }
				return rv;
			}
			
			// rewrite home parameters as portal prefixes or remove
			// if there is only one portal
			if ( url.pathBeginsWithSlash && 
					(PortalPickerFilter.isPortalPickingActive || isSInglePortal) ) {
				
				if ( (portalPickerFilter != null) && (url.queryParams != null) ) {
					Iterator<String[]> qpIt = url.queryParams.iterator();
					int qpIndex = -1;
					int indexToRemove = -1;
					while (qpIt.hasNext()) {
						String[] keyAndValue = qpIt.next();
						qpIndex++;			
						if ( ("home".equals(keyAndValue[0])) && (keyAndValue.length>1) && (keyAndValue[1] != null) ) {
							try {
								int portalId = Integer.decode(keyAndValue[1].trim());
								if ((Portal.DEFAULT_PORTAL_ID == portalId)) {
									indexToRemove = qpIndex;
								} else {
									String prefix = portalPickerFilter.getPortalId2PrefixMap().get(portalId);
									if ( (prefix != null) && (!prefix.equals(url.pathParts.get(contextPathDepth))) ) {		
										url.pathParts.add(contextPathDepth,prefix);									
										url.pathBeginsWithSlash = true;
										indexToRemove = qpIndex;
									}
								}
							} catch (NumberFormatException nfe) {
								log.info("Invalid portal id string: "+keyAndValue[1], nfe);
							}
						}
					}
					if (indexToRemove > -1) {
						url.queryParams.remove(indexToRemove);
					}
		
				}
			}
			
			// rewrite "entity" as "individual"
			if ("entity".equals(url.pathParts.get(url.pathParts.size()-1))) {
				url.pathParts.set(url.pathParts.size()-1, "individual");
			}
			
			// rewrite individual URI parameters as pretty URLs if possible
			if ("individual".equals(url.pathParts.get(url.pathParts.size()-1))) {
				Iterator<String[]> qpIt = url.queryParams.iterator();
				int qpIndex = -1;
				int indexToRemove = -1;
				while (qpIt.hasNext()) {
					String[] keyAndValue = qpIt.next();
					qpIndex++;
					if ( ("uri".equals(keyAndValue[0])) && (keyAndValue.length>1) && (keyAndValue[1] != null) ) {
						try {
							URI uri = new URIImpl(keyAndValue[1]);
							String namespace = uri.getNamespace();
							String localName = uri.getLocalName();
							if ( (namespace != null) && (localName != null) ) { 
								String prefix = nsMap.getPrefixForNamespace(namespace);
								if (defaultNamespace.equals(namespace) && prefix == null) {
									// make a URI that matches the URI
									// of the resource to support
									// linked data request
									url.pathParts.add(localName);
									// remove the ugly uri parameter
									indexToRemove = qpIndex;
							    // namespace returned from URIImpl.getNamespace() ends in a slash, so will 
							    // match externally linked namespaces, which also end in a slash
								} else if (isExternallyLinkedNamespace(namespace,externalNamespaces)) {
								    log.debug("Found externally linked namespace " + namespace);
								    // Use the externally linked namespace in the url
								    url.pathParts = new ArrayList<String>();
								    // toString() will join pathParts with a slash, so remove this one.
								    url.pathParts.add(namespace.replaceAll("/$", ""));
								    url.pathParts.add(localName);
								    // remove the ugly uri parameter
								    indexToRemove = qpIndex;
								    // remove protocol, host, and port, since the external namespace
								    // includes these elements
								    url.protocol = null;
								    url.host = null;
								    url.port = null;
								    url.pathBeginsWithSlash = false;
								} else if (prefix != null) {
									// add the pretty path parts
									url.pathParts.add(prefix);
									url.pathParts.add(localName);
									// remove the ugly uri parameter
									indexToRemove = qpIndex;
								}
							}
						} catch (Exception e) {
						    if( keyAndValue.length > 0 )
						        log.debug("Invalid URI: '"+keyAndValue[1] + "'");
						    else
						        log.debug("empty URI");
						}
					}
				}
				if (indexToRemove > -1) {
					url.queryParams.remove(indexToRemove);
				}
	
			}
			//String rv = _response.encodeURL(_response.encodeURL(url.toString()));
	         String rv = url.toString();
			if( log.isDebugEnabled()){
			    log.debug("Encoded as  '" + rv + "'");
			}
			return rv;
		} catch (Exception e) {			
			log.error(e,e);			
            //String rv =  _response.encodeURL(inUrl);
			String rv =  inUrl;
            log.error("Encoded as  '"+rv+"'");
			return rv;
		}
	}

	public void flushBuffer() throws IOException {
		_response.flushBuffer();
	}

	public int getBufferSize() {
		return _response.getBufferSize();
	}

	public String getCharacterEncoding() {
		return _response.getCharacterEncoding();
	}

	public String getContentType() {
		return _response.getContentType();
	}

	public Locale getLocale() {
		return _response.getLocale();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return _response.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		return _response.getWriter();
	}

	public boolean isCommitted() {
		return _response.isCommitted();
	}

	public void reset() {
		_response.reset();
	}

	public void resetBuffer() {
		_response.resetBuffer();
	}

	public void sendError(int arg0, String arg1) throws IOException {
		_response.sendError(arg0, arg1);
	}

	public void sendError(int arg0) throws IOException {
		_response.sendError(arg0);
	}

	public void sendRedirect(String arg0) throws IOException {
		_response.sendRedirect(arg0);
	}

	public void setBufferSize(int arg0) {
		_response.setBufferSize(arg0);
	}

	public void setCharacterEncoding(String arg0) {
		_response.setCharacterEncoding(arg0);
	}

	public void setContentLength(int arg0) {
		_response.setContentLength(arg0);
	}

	public void setContentType(String arg0) {
		_response.setContentType(arg0);
	}

	public void setDateHeader(String arg0, long arg1) {
		_response.setDateHeader(arg0, arg1);
	}

	public void setHeader(String arg0, String arg1) {
		_response.setHeader(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		_response.setIntHeader(arg0, arg1);
	}

	public void setLocale(Locale arg0) {
		_response.setLocale(arg0);
	}

	/**
	 * @deprecated
	 */
	public void setStatus(int arg0, String arg1) {
		_response.setStatus(arg0, arg1);
	}

	public void setStatus(int arg0) {
		_response.setStatus(arg0);
	}		
	
	private boolean isExternallyLinkedNamespace(String namespace,List<String> externallyLinkedNamespaces) {	    
	    return externallyLinkedNamespaces.contains(namespace);
	}
	
}
