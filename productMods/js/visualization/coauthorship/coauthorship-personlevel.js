/*
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
*/
var collaboratorTableMetadata = {
	tableID: "coauthorships_table",
	tableContainer: "coauth_table_container",
	tableCaption: "Co-authors ",
	tableColumnTitle1: "Author",
	tableColumnTitle2: "Publications with <br />",
	tableCSVFileLink: egoCoAuthorsListDataFileURL,
	jsonNumberWorksProperty: "number_of_authored_works" 
};

var visType = "coauthorship"; 
var visKeyForFlash = "CoAuthor";

function renderStatsOnNodeClicked(json){
	
	var obj = jQuery.parseJSON(json);
	
	var works = "";
	var persons = "";
	var relation = "";
	var earliest_work = "";
	var latest_work = "";
	var number_of_works = "";
	
	works = "Publication(s)";
	persons = "Co-author(s)";
	relation = "coauthorship"
	earliest_work = obj.earliest_publication;
	latest_work = obj.latest_publication;
	number_of_works = obj.number_of_authored_works;

	$("#dataPanel").attr("style","visibility:visible");
	$("#works").empty().append(number_of_works);

	/*
	 * Here obj.url points to the uri of that individual
	 */
	if(obj.url){
		
		if (obj.url === unEncodedEgoURI) {
			
			$("#authorName").addClass('author_name').removeClass('neutral_author_name');
			$('#num_works > .author_stats_text').text(works);
			$('#num_authors > .author_stats_text').text(persons);
			
		} else {

			$("#authorName").addClass('neutral_author_name').removeClass('author_name');
			$('#num_works > .author_stats_text').text('Joint ' + works);
			$('#num_authors > .author_stats_text').text('Joint ' + persons);
			
		}
		
		$("#profileUrl").attr("href", getWellFormedURLs(obj.url, "profile"));
		$("#coAuthorshipVisUrl").attr("href", getWellFormedURLs(obj.url, relation));
		processProfileInformation("authorName", 
				"profileMoniker",
				"profileImage",
				jQuery.parseJSON(getWellFormedURLs(obj.url, "profile_info")),
				true,
				true);
		
		

	} else{
		$("#profileUrl").attr("href","#");
		$("#coAuthorshipVisUrl").attr("href","#");
	}

	$("#coAuthors").empty().append(obj.noOfCorelations);	
	
	$("#firstPublication").empty().append(earliest_work);
	(earliest_work)?$("#fPub").attr("style","visibility:visible"):$("#fPub").attr("style","visibility:hidden");
	$("#lastPublication").empty().append(latest_work);
	(latest_work)?$("#lPub").attr("style","visibility:visible"):$("#lPub").attr("style","visibility:hidden");

}