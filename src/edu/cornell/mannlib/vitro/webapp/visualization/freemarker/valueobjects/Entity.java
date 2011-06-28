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
package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * @author bkoniden
 * Deepak Konidena
 *
 */
public class Entity extends Individual{
	
	Set<BiboDocument> publications = new HashSet<BiboDocument>();
	Set<SubEntity> children = new LinkedHashSet<SubEntity>();
	Set<Grant> grants = new HashSet<Grant>();
	
	public Entity(String departmentURI, String departmentLabel){
		super(departmentURI, departmentLabel);
	}
	
	public void setDepartmentLabel(String departmentURI){
		this.setIndividualLabel(departmentURI);
	}
	
	public String getEntityURI(){
		return this.getIndividualURI();
	}
	
	public Set<BiboDocument> getPublications() {
		return publications;
	}

	public String getEntityLabel(){
		return this.getIndividualLabel();
	}

	public Set<SubEntity> getSubEntities() {
		return children;
	}

	public void addPublication(BiboDocument biboDocument) {
		this.publications.add(biboDocument);
	}

	public void addSubEntity(SubEntity subEntity) {
		this.children.add(subEntity);
		
	}
	
	public void addSubEntitities(Collection<SubEntity> subEntities) {
		this.children.addAll(subEntities);
		
	}

	public void addGrant(Grant grant) {
		this.grants.add(grant);
	}

}
