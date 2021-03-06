<%--
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
--%>

<jsp:include page="addRoleToPersonTwoStage.jsp">    
	<jsp:param name="roleActivityTypeLabel" value="service to the profession" />
	<jsp:param name="roleType" value="http://vivoweb.org/ontology/core#ServiceProviderRole" />
	
	<jsp:param name="roleActivityType_optionsType" value="HARDCODED_LITERALS" />
	<jsp:param name="roleActivityType_objectClassUri" value="" /> 	
	<jsp:param name="roleActivityType_literalOptions" 
    value='["", "Select one"],
           [ "http://vivoweb.org/ontology/core#Association", "Association" ],
           [ "http://vivoweb.org/ontology/core#Center", "Center" ],
           [ "http://vivoweb.org/ontology/core#Clinical Organization", "Clinical Organization" ],
           [ "http://vivoweb.org/ontology/core#College", "College" ],
           [ "http://vivoweb.org/ontology/core#Committee", "Committee" ],                     
           [ "http://vivoweb.org/ontology/core#Consortium", "Consortium" ],
           [ "http://vivoweb.org/ontology/core#Department", "Department" ],
           [ "http://vivoweb.org/ontology/core#Division", "Division" ], 
           [ "http://purl.org/NET/c4dm/event.owl#Event", "Event" ], 
           [ "http://vivoweb.org/ontology/core#Extension Unit", "Extension Unit" ],
           [ "http://vivoweb.org/ontology/core#Foundation", "Foundation" ],
           [ "http://vivoweb.org/ontology/core#FundingOrganization", "Funding Organization" ],
           [ "http://vivoweb.org/ontology/core#GovernmentAgency", "Government Agency" ],
           [ "http://vivoweb.org/ontology/core#Hospital", "Hospital" ],
           [ "http://vivoweb.org/ontology/core#Institute", "Institute" ],
           [ "http://vivoweb.org/ontology/core#Laboratory", "Laboratory" ],
           [ "http://vivoweb.org/ontology/core#Library", "Library" ],
           [ "http://vivoweb.org/ontology/core#Museum", "Museum" ],        
           [ "http://xmlns.com/foaf/0.1/Organization", "Organization" ],
           [ "http://vivoweb.org/ontology/core#PrivateCompany", "Private Company" ],
           [ "http://vivoweb.org/ontology/core#Program", "Program" ],
           [ "http://vivoweb.org/ontology/core#Project", "Project" ],
           [ "http://vivoweb.org/ontology/core#Publisher", "Publisher" ],
           [ "http://vivoweb.org/ontology/core#ResearchOrganization", "Research Organization" ],
           [ "http://vivoweb.org/ontology/core#Team", "Team" ],
           [ "http://vivoweb.org/ontology/core#School", "School" ],
           [ "http://vivoweb.org/ontology/core#Service","Service"],
           [ "http://vivoweb.org/ontology/core#Student Organization", "Student Organization" ],
           [ "http://vivoweb.org/ontology/core#University", "University" ]' />  
</jsp:include>