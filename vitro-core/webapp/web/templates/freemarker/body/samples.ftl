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

<#-- FreeMarker samples -->

<#import "lib-datetime.ftl" as dt>

<h2>${title}</h2>

<@widget name="test" />

<h3>Dates</h3>
<ul>
    <li>Current date & time: ${now?datetime}</li>
    <li>Current date: ${now?date}</li>
    <li>Current time: ${now?time}</li>
</ul>

<h3>Formatted datetime</h3>
<p><p>${dt.xsdDateTimeToYear(xsddatetime)}</p>

<h3>Apples</h3>
<ul>
<#list apples as apple>
    <li>${apple}</li>
</#list>
</ul>

<h3>Fruit</h3>
<ul>
<#list fruit as f>
    <li>${f}</li>
</#list>
</ul>

<p><strong>Animal:</strong> ${animal}</p>

<p><strong>Book Title:</strong> ${bookTitle}</p>


<h3>Zoo 1</h3>
<ul>
<#list zoo1.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<h3>Zoo 2</h3>
<ul>
<#list zoo2.animals as animal>
    <li>${animal}</li>
</#list>
</ul>

<p><strong>Berries: </strong>${berries}</p>

<p>${year?number?c}</p>

<h3>Raw String Literals</h3>
<p>${r"#{title}"}</p>
<p>${r"${title}"}</p>

<h2>Containers do not pick up changes to the value of their elements</h2>
<#assign
    fruit = ["apples", "oranges", "bananas"]
    veg = ["beans", "peas", "carrots"]
    food = [fruit, veg]
    fruit = fruit + ["pears"]
>

<#noparse>
    <#assign<br />
        fruit = ["apples", "oranges", "bananas"]<br />
        veg = ["beans", "peas", "carrots"]<br />
        food = [fruit, veg]<br />
        fruit = fruit + ["pears"]<br />
    ><br />
</#noparse>

<h3>List elements of ${r"${fruit}"}</h3>
<#list fruit as f>
    ${f}<br />
</#list>

<h3>List elements of ${r"${food}"}: contains no pears</h3>
<#list food as item>
    <#list item as i>
        ${i}<br />
    </#list>
</#list>


<h3>Numbers</h3>

<#assign
    one = 1
    two = 2
    numbers = [one, two]
    two = 20
    numbers2 = [one, two]
>

<#noparse>
    <#assign<br />
        one = 1<br />
        two = 2<br />
        numbers = [one, two]<br />
        two = 20<br />
        numbers2 = [one, two]<br />
    ><br />
</#noparse>

${r"${two}"}: ${two}<br />
${r"${numbers[1]}"}: ${numbers[1]}<br />
${r"${numbers2[1]}"}: ${numbers2[1]}<br />


<@dump var="now" />
<@dump var="urls" />
<@dump var="fruit" />
<@dump var="trueStatement" />
<@dump var="zoo1" />
<@dump var="pojo" />

${stylesheets.addFromTheme("/css/sstest.css", "/css/sstest2.css")}
${scripts.addFromTheme("/js/jstest.js")}
${scripts.add("/js/script1.js", "/js/script2.js", "/js/script3.js")}


<@dumpAll />

<@help directive="dump" />

<@describe var="stylesheets" />

<@describe var="scripts" />

<@describe var="headScripts" />

