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
${headScripts.add("http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js")}
${headScripts.add("https://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22areachart%22%2C%22imagesparkline%22%5D%7D%5D%7D")}

This is Dummy Vis Client. For Real!
Really Re!

${urls.base}

<c:url var="loadingImageLink" value="/${themeDir}site_icons/visualization/ajax-loader.gif"></c:url>

<#assign loadingImageLink = '/${themeDir}site_icons/visualization/ajax-loader.gif'>

<#assign uri="http://vivo-trunk.indiana.edu/individual/n6079">
<#assign testURL = '${urls.base}/visualization?vis=person_pub_count&container=ajax_recipient&render_mode=dynamic&vis_mode=wth&uri=${uri?url}'>

<style type="text/css">
	.get_vis {
		background-color:Yellow;
		color:blue;
		cursor:pointer;
		height:36px;
		width:225px;
	}
</style>





<script type="text/javascript">
<!--

$(document).ready(function() {

	function renderVisualization(visualizationURL) {

		$("#ajax_recipient").empty().html('<img src="${loadingImageLink?url}" />');

		   $.ajax({
			   url: visualizationURL,
			   dataType: "html",
			   success:function(data){
			     $("#ajax_recipient").html(data);

			   }
			 });

	}

	   $("#ajax_activator").click(function() {
		   $.ajax({
			   url: '${testURL}',
			   dataType: "html",
			   success:function(data){


			     $("#ajax_recipient").html(data);

			   }
			 });
	   });


	 });



//-->
</script>

<div class="staticPageBackground">

<style type="text/css">

#test-bed {
	background-color:red;
	color:white;
	text-align:center;
}

</style>

<h1 id="test-bed">Visualization Testbed (Not to be seen by eventual end users)</h1>




<h2 id="ajax_activator">Hello World!</h2>

<a href="${testURL}">vis query for person -> "Crane, Brian"</a>


<div id="ajax_recipient">iioio</div>