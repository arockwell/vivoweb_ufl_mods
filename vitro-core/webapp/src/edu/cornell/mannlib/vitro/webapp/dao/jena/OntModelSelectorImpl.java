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

import com.hp.hpl.jena.ontology.OntModel;

public class OntModelSelectorImpl implements OntModelSelector {

	private OntModel aboxModel;
	private OntModel applicationMetadataModel;
	private OntModel displayModel;
	private OntModel fullModel;
	private OntModel tboxModel;
	private OntModel userAccountsModel;
	
	public OntModel getABoxModel() {
		return this.aboxModel;
	}

	public OntModel getApplicationMetadataModel() {
		return this.applicationMetadataModel;
	}

	public OntModel getDisplayModel() {
		return this.displayModel;
	}

	public OntModel getFullModel() {
		return this.fullModel;
	}

	public OntModel getTBoxModel() {
		return this.tboxModel;
	}

	public OntModel getTBoxModel(String ontologyURI) {
		return this.tboxModel;
	}

	public OntModel getUserAccountsModel() {
		return this.userAccountsModel;
	}
	
	public void setABoxModel(OntModel m) {
		this.aboxModel = m;
	}
	
	public void setApplicationMetadataModel(OntModel m) {
		this.applicationMetadataModel = m;
	}

	public void setDisplayModel(OntModel m) {
		this.displayModel = m;
	}
	
	public void setTBoxModel(OntModel m) {
		this.tboxModel = m;
	}
	
	public void setUserAccountsModel(OntModel m) {
		this.userAccountsModel = m;
	}
	
	public void setFullModel(OntModel m) {
		this.fullModel = m;
	}
	
}
