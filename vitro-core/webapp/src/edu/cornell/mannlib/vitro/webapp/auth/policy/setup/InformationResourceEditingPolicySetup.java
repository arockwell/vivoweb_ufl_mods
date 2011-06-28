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

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.AdministrativeUriRestrictor;
import edu.cornell.mannlib.vitro.webapp.auth.policy.InformationResourceEditingPolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Set up the InformationResourceEditingPolicy. This is tied to the SelfEditor
 * identifier, but has enough of its own logic to merit its own policy class.
 */
public class InformationResourceEditingPolicySetup implements
		ServletContextListener {
	private static final Log log = LogFactory
			.getLog(InformationResourceEditingPolicySetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			log.debug("Setting up InformationResourceEditingPolicy");

			OntModel model = (OntModel) sce.getServletContext().getAttribute(
					"jenaOntModel");
			replacePolicy(sce.getServletContext(), model);

			log.debug("InformationResourceEditingPolicy has been setup. ");
		} catch (Exception e) {
			log.error("could not run SelfEditingPolicySetup: " + e);
			e.printStackTrace();
		}
	}

	public static InformationResourceEditingPolicy makePolicyFromModel(
			OntModel model) {
		InformationResourceEditingPolicy policy = null;
		if (model == null)
			policy = new InformationResourceEditingPolicy(null,
					new AdministrativeUriRestrictor(null, null, null, null));
		else {
			Set<String> prohibitedProps = new HashSet<String>();

			// need to iterate through one level higher than SELF (the lowest
			// level where restrictions make sense) plus all higher levels
			for (BaseResourceBean.RoleLevel e : EnumSet.range(
					BaseResourceBean.RoleLevel.EDITOR,
					BaseResourceBean.RoleLevel.NOBODY)) {
				ResIterator it = model
						.listSubjectsWithProperty(
								model.createProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT),
								ResourceFactory.createResource(e.getURI()));
				while (it.hasNext()) {
					Resource resource = it.nextResource();
					if (resource != null && resource.getURI() != null) {
						log.debug("adding '"
								+ resource.getURI()
								+ "' to properties prohibited from information resource editing ("
								+ e.getLabel() + ")");
						prohibitedProps.add(resource.getURI());
					}
				}
			}
			policy = new InformationResourceEditingPolicy(model,
					new AdministrativeUriRestrictor(prohibitedProps, null, null, null));
		}
		return policy;
	}

	public static void replacePolicy(ServletContext sc, OntModel model) {
		ServletPolicyList.replacePolicy(sc, makePolicyFromModel(model));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}

}
