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

package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants.EmployeeType;

/**
 * 
 * This is the Value Object equivalent for vivo's Employee object type.
 * @author cdtank
 *
 */
public class VivoEmployee extends Individual {

	private EmployeeType employeeType; 
	private Set<VivoDepartmentOrDivision> parentDepartments = 
				new HashSet<VivoDepartmentOrDivision>();
	private Set<BiboDocument> authorDocuments = new HashSet<BiboDocument>();

	public VivoEmployee(String employeeURL, 
						EmployeeType employeeType, 
						VivoDepartmentOrDivision parentDepartment) {
		super(employeeURL);
		addParentDepartment(parentDepartment);
	}

	public String getEmployeeURL() {
		return this.getIndividualURL();
	}

	public String getEmployeeName() {
		return this.getIndividualLabel();
	}
	
	public EmployeeType getEmployeeType() {
		return employeeType;
	}

	public void setEmployeeType(EmployeeType employeeType) {
		this.employeeType = employeeType;
	}

	public Set<VivoDepartmentOrDivision> getParentDepartments() {
		return parentDepartments;
	}

	public void addParentDepartment(VivoDepartmentOrDivision parentDepartment) {
		this.parentDepartments.add(parentDepartment);
	}

	public Set<BiboDocument> getAuthorDocuments() {
		return authorDocuments;
	}

	public void addAuthorDocument(BiboDocument authorDocument) {
		this.authorDocuments.add(authorDocument);
	}


}
