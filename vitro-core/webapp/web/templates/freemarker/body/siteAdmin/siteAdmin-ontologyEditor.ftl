<#--
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
-->

<#-- Template for Site Administration Ontology Editor -->

<#if ontologyEditor??>
    <div class="pageBodyGroup">

        <h3>Ontology Editor</h3>
        
        <#if ontologyEditor.pellet??>
            <div class="notice">
                <p>${ontologyEditor.pellet.error}</p>
                <#if ontologyEditor.pellet.explanation??>
                    <p>Cause: ${ontologyEditor.pellet.explanation}</p>
                </#if>
            </div>
        </#if>
        
        <ul>
            <li><a href="${ontologyEditor.urls.ontologies}">Ontology list</a></li>
        </ul>
    
        <h4>Class Management</h4>
        <ul>
            <li><a href="${ontologyEditor.urls.classHierarchy}">Class hierarchy</a></li> 
            <li><a href="${ontologyEditor.urls.classGroups}">Class groups</a></li>
        </ul>
    
        <h4>Property Management</h4>
        <ul>
            <li><a href="${ontologyEditor.urls.objectPropertyHierarchy}">Object property hierarchy</a></li>
            <li><a href="${ontologyEditor.urls.dataPropertyHierarchy}">Data property hierarchy</a></li>      
            <li><a href="${ontologyEditor.urls.propertyGroups}">Property groups</a></li>
        </ul>
        
        <#-- NIHVIVO-1590 This feature temporarily disabled in v1.2 due to time constraints.
        <#assign formId = "verbosePropertyForm">
        <form id="${formId}" action="${ontologyEditor.verbosePropertyForm.action}#${formId}" method="get">
            <input type="hidden" name="verbose" value="${ontologyEditor.verbosePropertyForm.verboseFieldValue}" />
            <span>Verbose property display for this session is <b>${ontologyEditor.verbosePropertyForm.currentValue}</b>.</span>
            <input type="submit" id="submit" value="Turn ${ontologyEditor.verbosePropertyForm.newValue}" />
        </form>  
        -->
        
    </div>                       
</#if>