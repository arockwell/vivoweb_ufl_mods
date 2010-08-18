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

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

public class OntologyUpdateSettings {

	private String dataDir;
	private String sparqlConstructAdditionsDir;
	private String sparqlConstructDeletionsDir;
	private String askQueryFile;
	private String successAssertionsFile;
	private String successRDFFormat = "N3";
	private String diffFile;
	private String logFile;
	private String errorLogFile;
	private String addedDataFile;
	private String removedDataFile;
	private String defaultNamespace;
	private OntModelSelector ontModelSelector;
	private OntModel oldTBoxModel;
	private OntModel newTBoxModel;
	private OntModel oldTBoxAnnotationsModel;
	private OntModel newTBoxAnnotationsModel;
	
	public String getDataDir() {
		return dataDir;
	}
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
	public String getSparqlConstructAdditionsDir() {
		return sparqlConstructAdditionsDir;
	}
	public void setSparqlConstructAdditionsDir(String sparqlConstructAdditionsDir) {
		this.sparqlConstructAdditionsDir = sparqlConstructAdditionsDir;
	}
	public String getSparqlConstructDeletionsDir() {
		return sparqlConstructDeletionsDir;
	}
	public void setSparqlConstructDeletionsDir(String sparqlConstructDeletionsDir) {
		this.sparqlConstructDeletionsDir = sparqlConstructDeletionsDir;
	}
	public String getAskQueryFile() {
		return askQueryFile;
	}
	public void setAskQueryFile(String askQueryFile) {
		this.askQueryFile = askQueryFile;
	}
	public String getSuccessAssertionsFile() {
		return successAssertionsFile;
	}
	public void setSuccessAssertionsFile(String successAssertionsFile) {
		this.successAssertionsFile = successAssertionsFile;
	}
	public String getSuccessRDFFormat() {
		return successRDFFormat;
	}
	public void setSuccessRDFFormat(String successRDFFormat) {
		this.successRDFFormat = successRDFFormat;
	}
	public String getDiffFile() {
		return diffFile;
	}
	public void setDiffFile(String diffFile) {
		this.diffFile = diffFile;
	}
	public OntModelSelector getOntModelSelector() {
		return ontModelSelector;
	}
	public String getLogFile() {
		return logFile;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	public String getErrorLogFile() {
		return errorLogFile;
	}
	public void setErrorLogFile(String errorLogFile) {
		this.errorLogFile = errorLogFile;
	}
	public String getAddedDataFile() {
		return addedDataFile;
	}
	public void setAddedDataFile(String addedDataFile) {
		this.addedDataFile = addedDataFile;
	}
	public String getRemovedDataFile() {
		return removedDataFile;
	}
	public void setRemovedDataFile(String removedDataFile) {
		this.removedDataFile = removedDataFile;
	}
	public String getDefaultNamespace() {
		return defaultNamespace;
	}
	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}
	public void setOntModelSelector(OntModelSelector ontModelSelector) {
		this.ontModelSelector = ontModelSelector;
	}
	public OntModel getOldTBoxModel() {
		return oldTBoxModel;
	}
	public void setOldTBoxModel(OntModel oldTBoxModel) {
		this.oldTBoxModel = oldTBoxModel;
	}
	public OntModel getNewTBoxModel() {
		return newTBoxModel;
	}
	public void setNewTBoxModel(OntModel newTBoxModel) {
		this.newTBoxModel = newTBoxModel;
	}
	public OntModel getOldTBoxAnnotationsModel() {
		return oldTBoxAnnotationsModel;
	}
	public void setOldTBoxAnnotationsModel(OntModel oldTBoxAnnotationsModel) {
		this.oldTBoxAnnotationsModel = oldTBoxAnnotationsModel;
	}
	public OntModel getNewTBoxAnnotationsModel() {
		return newTBoxAnnotationsModel;
	}
	public void setNewTBoxAnnotationsModel(OntModel newTBoxAnnotationsModel) {
		this.newTBoxAnnotationsModel = newTBoxAnnotationsModel;
	}
	
}
