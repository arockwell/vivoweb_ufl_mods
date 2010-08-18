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

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.List;
import java.util.LinkedList;

/**
 * class representing a property that relates an entity (object)
 * to a data literal
 * @author bjl23
 *
 */
public class DataProperty extends Property implements Comparable<DataProperty> {

    private String name = null;
    private String publicName = null;

    private String domainClassURI = null;
    private String rangeDatatypeURI = null;
    
    private boolean functional = false;

    private String example = null;
    private String description = null;
    private String publicDescription = null;

    private int displayTier = -1;
    private int displayLimit = -1;

    private int statusId = 0;

    private boolean externalId = false;

    private List<DataPropertyStatement> dataPropertyStatements = null;
    
    public DataProperty() { //default constructor needed since Property has one
        super();
    }

    public boolean isExternalId() {
        return externalId;
    }

    public void setExternalId(boolean externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public String getDomainClassURI() {
        return domainClassURI;
    }

    public void setDomainClassURI(String domainClassURI) {
        this.domainClassURI = domainClassURI;
    }

    public String getRangeDatatypeURI() {
        return rangeDatatypeURI;
    }

    public void setRangeDatatypeURI(String rangeDatatypeURI) {
        this.rangeDatatypeURI = rangeDatatypeURI;
    }

    public boolean getFunctional() {
    	return this.functional;
    }
    
    public void setFunctional(boolean functional) {
    	this.functional = functional;
    }
    
    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPublicDescription() {
        return publicDescription;
    }
    public void setPublicDescription(String s) {
        this.publicDescription = s;
    }

    public int getDisplayTier() {
        return displayTier;
    }

    public void setDisplayTier(int displayTier) {
        this.displayTier = displayTier;
    }

    public int getDisplayLimit() {
        return displayLimit;
    }

    public void setDisplayLimit(int displayLimit) {
        this.displayLimit = displayLimit;
    }

    public int getStatusId(){
        return statusId;
    }

    public void setStatusId(int statusId){
        this.statusId = statusId;
    }

    public List<DataPropertyStatement> getDataPropertyStatements() {
        return dataPropertyStatements;
    }

    public void setDataPropertyStatements(List<DataPropertyStatement> dataPropertyStatements) {
        this.dataPropertyStatements=dataPropertyStatements;
    }

    /**
     * adds a single DataPropertyStatement object to a DatatypeProperty's DataPropertyStatements list.
     * @param dataPropertyStmt
     */

    public void addDataPropertyStatement(DataPropertyStatement dataPropertyStmt){
        if( dataPropertyStmt == null ) return;
        if( getDataPropertyStatements() == null )
            setDataPropertyStatements(new LinkedList<DataPropertyStatement>() );
        getDataPropertyStatements().add(dataPropertyStmt);
    }

    public int compareTo(DataProperty o) {
        try {
            Collator collator = Collator.getInstance();
            return collator.compare(this.getName(),((DataProperty)o).getName());
        } catch (Exception e) {
            return -1;
        }
    }

    public String toString(){
        if( getURI() != null ) 
            return getURI();
        else 
            return "DataProperty without URI(" + hashCode() + ")"; 
    }
}

