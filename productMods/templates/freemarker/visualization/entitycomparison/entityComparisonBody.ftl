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

<div id="body">

        <h2><span id="header-entity-label"><a id="organizationMoniker" href="${organizationVivoProfileURL}">${organizationLabel}</a></span></h2>
        
        <div id="leftblock">
            <div id="leftUpper">
                <h3>How do you want to compare?</h3>
                
                <div style="text-align: left;">
                
                <select class="comparisonValues" style="margin-bottom: 20px;">
                
                <#list parameterOptions as parameter>
                    <#if currentParameter = parameter.name>
                    
                        <#assign selectedText = "selected='selected'">

                    <#else>
                    
                        <#assign selectedText = "">
                        
                    </#if>
                    <option value="${parameter.value}" ${selectedText}>${parameter.dropDownText}</option>
                </#list>

                </select>
                
                </div>
            </div>
                        
            <div id="leftLower">
                <div id="notification-container" style="display:none">
        
                    <div id="error-notification" class="ui-state-error" style="padding:10px; -moz-box-shadow:0 0 6px #980000; -webkit-box-shadow:0 0 6px #980000; box-shadow:0 0 6px #980000;">
                        <a class="ui-notify-close" href="#"><span class="ui-icon ui-icon-close" style="float:right"></span></a>
                        <span style="float:left; margin:0 5px 0 0;" class="ui-icon ui-icon-alert"></span>
                        <h1>&#035;{title}</h1>
                        <p>&#035;{text}</p>
                        <p style="text-align:center"><a class="ui-notify-close" href="#">Close Me</a></p>
                    </div>
                    
                    <div id="warning-notification" class="ui-state-highlight ui-corner-all" >
                    <a class="ui-notify-close ui-notify-cross" href="#">x</a>
                    <span style="float: left; margin-right: 0.3em;" class="ui-icon ui-icon-info"></span>
                        <h1>&#035;{title}</h1>
                        <p>&#035;{text}</p>
                    </div>
                
                </div>
                <h3>Who do you want to compare?</h3>
                <div id="paginatedTable"></div>
                <div id="paginated-table-footer">
                <a id="csv" href="${temporalGraphDownloadFileLink}" class="temporalGraphLinks">Save All as CSV</a>
                <a class="clear-selected-entities temporalGraphLinks" title="Clear all selected entities.">Clear</a>
                </div>
            </div>
<#--        
            <div id = "stopwordsdiv">
                * The entity types core:Person, foaf:Organization have been excluded as they are too general.
            </div>  
-->         
        </div>
        
        <div id="rightblock">
        
            <h3 id="headerText">Comparing <span id="comparisonHeader">${currentParameterObject.value}</span> of <span id="entityHeader">Institutions</span> in ${organizationLabel}</h3>
            
            <div id="temporal-graph">
                <div id="yaxislabel"></div>
                <div id="graphContainer"></div>
                <div id="xaxislabel">Year</div>
            </div>
        
            <div id="bottom">
                <h3><span id="comparisonParameter"></span></h3>
            <p class="displayCounter">You have selected <span id="counter">0</span> of a maximum 
            <span id="total">10</span> <span id="entityleveltext"> schools</span>. 
            <span id="legend-row-header"> 
            <a class="clear-selected-entities temporalGraphLinks" title="Clear all selected entities.">Clear</a>
            </span>
            </p>
        
            </div>
        </div>      
</div>