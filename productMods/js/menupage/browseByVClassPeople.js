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

// This file extends and proxies the default behavior defined in vitro/webapp/web/js/menupage/browseByVClass.js

// Saving the original getIndividuals function from browseByVClass
var getPersonIndividuals = browseByVClass.getIndividuals;

// Assigning the proxy function
browseByVClass.getIndividuals = function(vclassUri, alpha, page, scroll) {
    url = this.dataServiceUrl + encodeURIComponent(vclassUri);
    if ( alpha && alpha != "all") {
        url = url + '&alpha=' + alpha;
    }
    if ( page ) {
        url += '&page=' + page;
    } else {
        page = 1;
    }
    if ( typeof scroll === "undefined" ) {
        scroll = true;
    }
    
    // Scroll to #menupage-intro page unless told otherwise
    if ( scroll != false ) {
        // only scroll back up if we're past the top of the #browse-by section
        scrollPosition = browseByVClass.getPageScroll();
        browseByOffset = $('#browse-by').offset();
        if ( scrollPosition[1] > browseByOffset.top) {
            $.scrollTo('#menupage-intro', 500);
        }
    }
    
    $.getJSON(url, function(results) {
        individualList = "";
        
        // Catch exceptions when empty individuals result set is returned
        // This is very likely to happen now since we don't have individual counts for each letter and always allow the result set to be filtered by any letter
        if ( results.individuals.length == 0 ) {
            browseByVClass.emptyResultSet(results.vclass, alpha)
        } else {
            $.each(results.individuals, function(i, item) {
                label = results.individuals[i].label;
                firstName = results.individuals[i].firstName;
                lastName = results.individuals[i].lastName;
                if ( firstName && lastName ) {
                    fullName = firstName + ' ' + lastName;
                } else {
                    fullName = label;
                }
                moniker = results.individuals[i].moniker;
                vclassName = results.individuals[i].vclassName;
                if ( results.individuals[i].preferredTitle == "") {
                   // Use the moniker only if it's not empty and not equal to the VClass name
                   if ( moniker != vclassName && moniker != "" ) {
                       preferredTitle = moniker;
                   } else {
                       preferredTitle = "";
                   }
                } else {
                   preferredTitle = results.individuals[i].preferredTitle;
                }
                uri = results.individuals[i].URI;
                profileUrl = results.individuals[i].profileUrl;
                if ( !results.individuals[i].thumbUrl ) {
                    image = browseByVClass.baseUrl + '/images/placeholders/person.thumbnail.jpg';
                } else {
                    image = browseByVClass.baseUrl + results.individuals[i].thumbUrl;
                }
                // Build the content of each list item, piecing together each component
                listItem = '<li class="vcard individual foaf-person" role="listitem" role="navigation">';
                listItem += '<img src="'+ image +'" width="90" alt="'+ fullName +'" />';
                listItem += '<h1 class="fn thumb"><a href="'+ profileUrl +'" title="View the profile page for '+ fullName +'">'+ fullName +'</a></h1>';
                // Include the calculated preferred title (see above) only if it's not empty
                if ( preferredTitle != "" ) {
                    listItem += '<span class="title">'+ preferredTitle +'</span>';
                }
                listItem += '</li>';
                // browseByVClass.individualsInVClass.append(listItem);
                individualList += listItem;
            })
            
            // Remove existing content
            browseByVClass.wipeSlate();
            
            // And then add the new content
            browseByVClass.individualsInVClass.append(individualList);
            
            // Check to see if we're dealing with pagination
            if ( results.pages.length ) {
                pages = results.pages;
                browseByVClass.pagination(pages, page);
            }
            
            selectedClassHeading = '<h3 class="selected-class">'+ results.vclass.name +'</h3>';
            browseByVClass.individualsContainer.prepend(selectedClassHeading);
            
            // set selected class, alpha and page
            browseByVClass.selectedVClass(results.vclass.URI);
            browseByVClass.selectedAlpha(alpha);
        }
    });
};