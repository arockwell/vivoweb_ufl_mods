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

<#-- Macros for generating html lists -->

<#-- 
    Macro: firstLastList

    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    Especially useful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements. It is currently not supported for these <li> elements to contain nested
    <li> elements. An <li> element may span multiple lines.
    
    Usage:
        <@firstLastList>
            <li>apples</li>
            <li>bananas</li>
            <li>oranges</li>
        </@firstLastList>
        
        <@firstLastList>
            <#list animals as animal>
                <#if animal != "cow"> 
                    <li>${animal}</li>
                </#if>
            </#list>
        </@firstLastList>

    RY Consider rewriting in Java. Probably designers won't want to modify this. That would allow us to support
    nested <li> elements.
           
-->
<#macro firstLastList>
    <#local text>
        <#nested>
    </#local>

    <@processListItems text?matches("<li.*?</li>", "s") />
</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: firstLastListNested
    
    Output a sequence of <li> elements, adding classes "first" and "last" to first and last list elements, respectively. 
    Especially useful when the list elements are generated conditionally, to avoid complex tests for the presence/absence
    of other list elements in order to assign these classes.
    
    Input should be a series of <li> elements separated by some delimiter. Default delimiter value is ",".
    
    Tolerates a delimiter following the last <li> element.
    
    Unlike firstLastList, this macro can process <li> elements that contain other <li> elements, because the delimiters
    indicate how to split the text.
    
    Usage:
        <@firstLastListNested>
            <li>apples</li>,
            <li>bananas</li>,
            <li>oranges</li>
        </@firstLastListNested>
        
        <@firstLastListNested>
            <#list animals as animal>
                <#if animal != "cow"> 
                    <li>${animal}</li>,
                </#if>
            </#list>
        </@firstLastListNested>
        
        <@firstLastListNested delim="??">
            <li>apples, oranges</li>??
            <li>bananas, lemons</li>??
            <li>grapefruit, limes</li>
        </@firstLastListNested>
        
        <@firstLastListNested delim="??">
            <li>Books
                <ul>
                    <li>Persuasion</li>
                    <li>Sense and Sensibility</li>
                </ul>
            </li>,
            <li>Magazines
                <ul>
                    <li>The New Yorker</li>
                    <li>Time</li>
                </ul>
            </li>
        </@firstLastListNested>

    RY Consider rewriting in Java. Probably designers won't want to modify this.
-->
<#macro firstLastListNested delim=",">
    <#local text>
        <#nested>
    </#local>

    <#-- Strip out a list-final delimiter, else (unlike most languages) it 
    results in an empty final array item. -->
    <#local text = text?replace("${delim}\\s*$", "", "r")>

    <@processListItems text?split(delim) />

</#macro>

<#---------------------------------------------------------------------------->

<#-- 
    Macro: processListItems 

     Given a list of <li> elements, adds class "first" to the first item and
     "last" to the last item. The <li> elements must have already been put into
     a list; this macro does not handle the raw text. Can be called from 
     firstLastList

-->
<#macro processListItems items>
    <#list items as item>

        <#-- A FreeMarker loop variable cannot have its value modified, so we use a new variable. -->
        <#local newItem = item?trim>

        <#local classVal = "">
        
        <#-- Keep any class value already assigned -->
        <#local classMatch = newItem?matches("^<li [^>]*(class=([\'\"])(.*?)\\2)")>
        <#list classMatch as m>
            <#local classVal = m?groups[3]> <#-- get the assigned class value -->
            <#local newItem = newItem?replace(m?groups[1], "")> <#-- remove 'class="xyz"' -->
        </#list>

        <#if item_index == 0> 
            <#local classVal = "${classVal} first">
        </#if>
        <#if ! item_has_next>       
            <#local classVal = "${classVal} last">
        </#if>
        
        <#local classVal = classVal?trim>
        
        <#if classVal?has_content>    
            <#-- Replace first instance only, in case the item contains nested li tags. -->     
            <#local newItem = newItem?replace("<li", "<li class=\"${classVal}\"", "f")>
        </#if>
        ${newItem}
    </#list>
</#macro>


