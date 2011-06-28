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

<#-- Crop the replacement main image for an Individual, to produce a thumbnail. -->

${scripts.add("/js/jquery.js")}
${scripts.add("/js/jquery_plugins/jcrop/jquery.Jcrop.js")}
${scripts.add("/js/imageUpload/cropImage.js")}


${stylesheets.add("/css/uploadImages.css")}
${stylesheets.add("/js/jquery_plugins/jcrop/jquery.Jcrop.css")}


<#--Reduce original image to fit in the page layout  
	If the width of the image is bigger or equal to 500 pixels, 
	the script below will reduce the width to 500 pixels and 
	the height will be in proportion to the new height-->

<#--<#macro newImageSize>
<#if (imageWidth >= 500)>
		width="500" height="${(500*imageHeight)/imageWidth}" 
</#if>	   
</#macro>-->


<section id="photoCroppingContainer" role="region">
    <h2>Photo Upload</h2>
    
    <!-- This is the image we're attaching Jcrop to -->
    <section id="photoCroppingPreview" role="region">
        
        <p class="photoCroppingTitleBody">Your profile photo will look like the image below. </p>
        
        <section class="photoCroppedPreview" role="region">
            <img src="${imageUrl}" id="preview" alt="Image to be cropped"/>
        </section>
        
        <section id="photoCroppingHowTo" role="region">
            <p class="photoCroppingNote">To make adjustments, you can drag around and resize the photo to the right. When you are happy with your photo click the "Save Photo" button. </p>
            
            <form id="cropImage" action="${formAction}"  method="post" role="form">
                <!-- Javascript will populate these values -->
                <input type="hidden" name="x" value="" />
                <input type="hidden" name="y" value="" />
                <input type="hidden" name="w" value="" />
                <input type="hidden" name="h" value="" />
                                      
                <input  class="submit" type="submit" value="Save photo">
                
                <span class="or"> or <a class="cancel"  href="${cancelUrl}">Cancel</a></span>
            </form>
       </section>
    </section>
    
    <section id="photoCropping" role="region">
        <img src="${imageUrl}" id="cropbox" alt="Preview of photo cropped" />
    </section
</section>

<div class="clear"></div>
