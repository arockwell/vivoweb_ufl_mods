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

<#-- Contact form -->

<div class="staticPageBackground feedbackForm">

    <h2>${title}</h2>
    
    <p>Thank you for your interest in 
        <#compress>
            <#if portalType == "CALSResearch">
                the Cornell University College of Agriculture and Life Sciences Research Portal
            <#elseif portalType == "VIVO">
                VIVO        
            <#else>
                the ${siteName} portal
            </#if>
        </#compress>. 
        Please submit this form with questions, comments, or feedback about the content of this site.
    </p>
        
    <#if siteName == "CALSResearch" || siteName == "CALSImpact">
        <p>
            ${siteName} is a service that depends on regular updates and feedback.
            Please help us out by providing comments and suggestions for additional content (people, departments, courses, research services, etc.)
            that you would like to see represented. The reference librarians at Albert R. Mann Library will be in touch with you soon.
        </p>
    </#if>

    <form name="contact_form" id="contact_form" action="${formAction}" method="post" onsubmit="return ValidateForm('contact_form');">
        <input type="hidden" name="home" value="${portalId}"/>
        <input type="hidden" name="RequiredFields" value="webusername,webuseremail,s34gfd88p9x1"/>
        <input type="hidden" name="RequiredFieldsNames" value="Name,Email address,Comments"/>
        <input type="hidden" name="EmailFields" value="webuseremail"/>
        <input type="hidden" name="EmailFieldsNames" value="emailaddress"/>
        <input type="hidden" name="DeliveryType" value="contact"/>
    
        <label for="webusername">Full name</label>
        <p><input style="width:33%;" type="text" name="webusername" maxlength="255"/></p>
        <label for="webuseremail">Email address</label>
        <p><input style="width:25%;" type="text" name="webuseremail" maxlength="255"/></p>


        <label>Comments, questions, or suggestions</label>

        <textarea name="s34gfd88p9x1" rows="10" cols="90"></textarea>
        
        <div class="buttons">
            <input id="submit" type="submit" value="Send Mail"/>
        </div

        <p style="font-weight: bold; margin-top: 1em">Thank you!</p>
    </form>    
    
</div>

${scripts.add("/js/commentForm.js")}
