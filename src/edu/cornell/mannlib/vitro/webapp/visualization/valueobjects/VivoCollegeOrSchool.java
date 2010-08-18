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

/**
 * 
 * This is the Value Object equivalent for vivo:CollegeOrSchoolWithinUniversity object type.
 * @author cdtank
 *
 */
public class VivoCollegeOrSchool extends Individual {

	private Set<VivoDepartmentOrDivision> departments = new HashSet<VivoDepartmentOrDivision>();

	public VivoCollegeOrSchool(String collegeURL) {
		super(collegeURL);
	}
	
	public Set<VivoDepartmentOrDivision> getDepartments() {
		return departments;
	}

	public void addDepartment(VivoDepartmentOrDivision department) {
		this.departments.add(department);
	}

	public String getCollegeURL() {
		return this.getIndividualURL();
	}

	public String getCollegeLabel() {
		return this.getIndividualLabel();
	}

	public void setCollegeLabel(String collegeLabel) {
		this.setIndividualLabel(collegeLabel);
	}

}
