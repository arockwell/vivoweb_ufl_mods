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

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.impl.Util;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Used by several policies to disallow the modification of Vitro-reserved
 * resources and/or properties.
 */
public class AdministrativeUriRestrictor {
	private static final Log log = LogFactory
			.getLog(AdministrativeUriRestrictor.class);

	private static final String[] DEFAULT_PROHIBITED_PROPERTIES = {};

	private static final String[] DEFAULT_PROHIBITED_RESOURCES = {};

	private static final String[] DEFAULT_PROHIBITED_NAMESPACES = {
			VitroVocabulary.vitroURI, 
			VitroVocabulary.OWL, 
			"" };

	private static final String[] DEFAULT_EDITABLE_VITRO_URIS = {
			VitroVocabulary.MONIKER, 
			VitroVocabulary.BLURB,
			VitroVocabulary.DESCRIPTION, 
			VitroVocabulary.MODTIME,
			VitroVocabulary.TIMEKEY,

			VitroVocabulary.CITATION, 
			VitroVocabulary.IND_MAIN_IMAGE,

			VitroVocabulary.LINK, 
			VitroVocabulary.PRIMARY_LINK,
			VitroVocabulary.ADDITIONAL_LINK, 
			VitroVocabulary.LINK_ANCHOR,
			VitroVocabulary.LINK_URL,

			VitroVocabulary.KEYWORD_INDIVIDUALRELATION,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL,
			VitroVocabulary.KEYWORD_INDIVIDUALRELATION_MODE };

	/**
	 * Namespaces from which Self Editors should not be able to use resources.
	 */
	private final Set<String> prohibitedNamespaces;

	/**
	 * URIs of properties that SelfEditors should not be able to use in
	 * statements
	 */
	protected final Set<String> prohibitedProperties;

	/**
	 * URIs of resources that SelfEditors should not be able to use in
	 * statements
	 */
	protected final Set<String> prohibitedResources;

	/**
	 * URIs of properties from prohibited namespaces that Self Editors need to
	 * be able to edit
	 */
	protected final Set<String> editableVitroUris;

	public AdministrativeUriRestrictor(Set<String> prohibitedProperties,
			Set<String> prohibitedResources, Set<String> prohibitedNamespaces,
			Set<String> editableVitroUris) {
		this.prohibitedProperties = useDefaultIfNull(prohibitedProperties,
				DEFAULT_PROHIBITED_PROPERTIES);
		this.prohibitedResources = useDefaultIfNull(prohibitedResources,
				DEFAULT_PROHIBITED_RESOURCES);
		this.prohibitedNamespaces = useDefaultIfNull(prohibitedNamespaces,
				DEFAULT_PROHIBITED_NAMESPACES);
		this.editableVitroUris = useDefaultIfNull(editableVitroUris,
				DEFAULT_EDITABLE_VITRO_URIS);
	}

	private Set<String> useDefaultIfNull(Set<String> valueSet,
			String[] defaultArray) {
		Collection<String> strings = (valueSet == null) ? Arrays
				.asList(defaultArray) : valueSet;
		return Collections.unmodifiableSet(new HashSet<String>(strings));
	}

	public boolean canModifyResource(String uri) {
		if (uri == null || uri.length() == 0) {
			log.debug("Resource URI is empty: " + uri);
			return false;
		}

		if (editableVitroUris.contains(uri)) {
			log.debug("Resource matches an editable URI: " + uri);
			return true;
		}

		String namespace = uri.substring(0, Util.splitNamespace(uri));
		if (prohibitedNamespaces.contains(namespace)) {
			log.debug("Resource matches a prohibited namespace: " + uri);
			return false;
		}

		log.debug("Resource is not prohibited: " + uri);
		return true;
	}

	public boolean canModifyPredicate(String uri) {
		if (uri == null || uri.length() == 0) {
			log.debug("Predicate URI is empty: " + uri);
			return false;
		}

		if (prohibitedProperties.contains(uri)) {
			log.debug("Predicate matches a prohibited predicate: " + uri);
			return false;
		}

		if (editableVitroUris.contains(uri)) {
			return true;
		}

		String namespace = uri.substring(0, Util.splitNamespace(uri));
		if (prohibitedNamespaces.contains(namespace)) {
			log.debug("Predicate matches a prohibited namespace: " + uri);
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AdministrativeUriRestrictor[prohibitedNamespaces="
				+ prohibitedNamespaces + ", prohibitedProperties="
				+ prohibitedProperties + ", prohibitedResources="
				+ prohibitedResources + ", editableVitroUris="
				+ editableVitroUris + "]";
	}

}
