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

(function ($) {

$.fn.dataTableExt.oPagination.gmail_style = { 

		"fnInit": function ( oSettings, nPaging, fnCallbackDraw )
		{
			//var nInfo = document.createElement( 'div' );
			var nFirst = document.createElement( 'span' );
			var nPrevious = document.createElement( 'span' );
			var nNext = document.createElement( 'span' );
			var nLast = document.createElement( 'span' );
			
			/*
			nFirst.innerHTML = oSettings.oLanguage.oPaginate.sFirst;
			nPrevious.innerHTML = oSettings.oLanguage.oPaginate.sPrevious;
			nNext.innerHTML = oSettings.oLanguage.oPaginate.sNext;
			nLast.innerHTML = oSettings.oLanguage.oPaginate.sLast;
			*/
			
			nFirst.innerHTML = "<span class='small-arrows'>&laquo;</span> <span class='paginate-nav-text'>First</span>";
			nPrevious.innerHTML = "<span class='small-arrows'>&lsaquo;</span> <span class='paginate-nav-text'>Prev</span>";
			nNext.innerHTML = "<span class='paginate-nav-text'>Next</span><span class='small-arrows'>&rsaquo;</span>";
			nLast.innerHTML = "<span class='paginate-nav-text'>Last</span><span class='small-arrows'>&raquo;</span>";
			
			var oClasses = oSettings.oClasses;
			nFirst.className = oClasses.sPageButton+" "+oClasses.sPageFirst;
			nPrevious.className = oClasses.sPageButton+" "+oClasses.sPagePrevious;
			nNext.className= oClasses.sPageButton+" "+oClasses.sPageNext;
			nLast.className = oClasses.sPageButton+" "+oClasses.sPageLast;
			
			//nPaging.appendChild( nInfo );
			nPaging.appendChild( nFirst );
			nPaging.appendChild( nPrevious );
			nPaging.appendChild( nNext );
			nPaging.appendChild( nLast );
			
			$(nFirst).click( function () {
				if ( oSettings.oApi._fnPageChange( oSettings, "first" ) )
				{
					fnCallbackDraw( oSettings );
				}
			} );
			
			$(nPrevious).click( function() {
				if ( oSettings.oApi._fnPageChange( oSettings, "previous" ) )
				{
					fnCallbackDraw( oSettings );
				}
			} );
			
			$(nNext).click( function() {
				if ( oSettings.oApi._fnPageChange( oSettings, "next" ) )
				{
					fnCallbackDraw( oSettings );
				}
			} );
			
			$(nLast).click( function() {
				if ( oSettings.oApi._fnPageChange( oSettings, "last" ) )
				{
					fnCallbackDraw( oSettings );
				}
			} );
			
			/* Take the brutal approach to cancelling text selection */
			$('span', nPaging)
				.bind( 'mousedown', function () { return false; } )
				.bind( 'selectstart', function () { return false; } );
			
			/* ID the first elements only */
			if ( oSettings.sTableId !== '' && typeof oSettings.aanFeatures.p == "undefined" )
			{
				nPaging.setAttribute( 'id', oSettings.sTableId+'_paginate' );
				nFirst.setAttribute( 'id', oSettings.sTableId+'_first' );
				nPrevious.setAttribute( 'id', oSettings.sTableId+'_previous' );
				//nInfo.setAttribute( 'id', 'infoContainer' );
				nNext.setAttribute( 'id', oSettings.sTableId+'_next' );
				nLast.setAttribute( 'id', oSettings.sTableId+'_last' );
			}
		},
		
		/*
		 * Function: oPagination.full_numbers.fnUpdate
		 * Purpose:  Update the list of page buttons shows
		 * Returns:  -
 		 * Inputs:   object:oSettings - dataTables settings object
		 *           function:fnCallbackDraw - draw function to call on page change
		 */
		"fnUpdate": function ( oSettings, fnCallbackDraw )
		{
			if ( !oSettings.aanFeatures.p )
			{
				return;
			}
			
			var iPageCount = 5;
			var iPageCountHalf = Math.floor(iPageCount / 2);
			var iPages = Math.ceil((oSettings.fnRecordsDisplay()) / oSettings._iDisplayLength);
			var iCurrentPage = Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength) + 1;
			var iStartButton, iEndButton, i, iLen;
			var oClasses = oSettings.oClasses;
			
			/* Pages calculation */
			if (iPages < iPageCount)
			{
				iStartButton = 1;
				iEndButton = iPages;
			}
			else
			{
				if (iCurrentPage <= iPageCountHalf)
				{
					iStartButton = 1;
					iEndButton = iPageCount;
				}
				else
				{
					if (iCurrentPage >= (iPages - iPageCountHalf))
					{
						iStartButton = iPages - iPageCount + 1;
						iEndButton = iPages;
					}
					else
					{
						iStartButton = iCurrentPage - Math.ceil(iPageCount / 2) + 1;
						iEndButton = iStartButton + iPageCount - 1;
					}
				}
			}
			
			/* Loop over each instance of the pager */
			var an = oSettings.aanFeatures.p;
			var anButtons, anStatic, nPaginateList;
			var fnClick = function() {
				/* Use the information in the element to jump to the required page */
				var iTarget = (this.innerHTML * 1) - 1;
				oSettings._iDisplayStart = iTarget * oSettings._iDisplayLength;
				fnCallbackDraw( oSettings );
				return false;
			};
			var fnFalse = function () { return false; };
			
			for ( i=0, iLen=an.length ; i<iLen ; i++ )
			{
				if ( an[i].childNodes.length === 0 )
				{
					continue;
				}
				
				/* Update the 'premanent botton's classes */
				anButtons = an[i].getElementsByTagName('span');
				anStatic = [
					anButtons[0], anButtons[1], 
					anButtons[anButtons.length-2], anButtons[anButtons.length-1]
				];
				$(anStatic).removeClass( oClasses.sPageButton+" "+oClasses.sPageButtonActive+" "+oClasses.sPageButtonStaticDisabled );
				if ( iCurrentPage == 1 )
				{
					anStatic[0].className += " "+oClasses.sPageButtonStaticDisabled;
					anStatic[1].className += " "+oClasses.sPageButtonStaticDisabled;
				}
				else
				{
					anStatic[0].className += " "+oClasses.sPageButton;
					anStatic[1].className += " "+oClasses.sPageButton;
				}
				
				if ( iPages === 0 || iCurrentPage == iPages || oSettings._iDisplayLength == -1 )
				{
					anStatic[2].className += " "+oClasses.sPageButtonStaticDisabled;
					anStatic[3].className += " "+oClasses.sPageButtonStaticDisabled;
				}
				else
				{
					anStatic[2].className += " "+oClasses.sPageButton;
					anStatic[3].className += " "+oClasses.sPageButton;
				}
			}
		}		
};

    $.fn.ellipsis = function () {
        return this.each(function () {
            var el = $(this);

            if (el.css("overflow") == "hidden") {

                var text = el.html();

                var multiline = el.hasClass('multiline');
                var t = $(this.cloneNode(true)).hide().css('position', 'absolute').css('overflow', 'visible').width(multiline ? el.width() : 'auto').height(multiline ? 'auto' : el.height());

                el.after(t);

                function height() {
                    return t.height() > el.height();
                };

                function width() {
                    return t.width() > el.width();
                };

                var func = multiline ? height : width;


                while (text.length > 0 && func()) {
                    text = text.substr(0, text.length - 1);
                    t.html(text + "...");
                }

                el.html(t.html());
                t.remove();
            }
        });
    };
})(jQuery);


/**

 * init sets some initial options for the default graph. i.e for when the page
 * is initially loaded or when its refreshed or when all the checkboxes on the
 * page are unchecked.
 * 
 * @param graphContainer
 *            is the div that contains the main graph.
 */
function init(graphContainer) {
	
	var optionSelected = $("select.comparisonValues option:selected").val();
	// TODO: make use of the id on the select field instead of a generic one.
	$("#comparisonParameter").text("Total Number of " + $("select.comparisonValues option:selected").val());
	$('#yaxislabel').html("Number of " + optionSelected).mbFlipText(false);
	$('#comparisonHeader').html(optionSelected).css('font-weight', 'bold');
	
	
	var defaultFlotOptions = {
			xaxis : {
				min : globalDateObject.getFullYear() - 9,
				max : globalDateObject.getFullYear(),
				tickDecimals : 0,
				tickSize : 2
			},
			yaxis: {
				tickDecimals : 0,
				min : 0,
				max: 5
			},
			grid: {
				borderColor : "#D9D9D9"
			}
			
	};

	/*
	 * [[]] is an empty 2D array object. $.plot is passed this for the default
	 * behavior. Ex.When the page initially loads, or when no graphs are present
	 * on the webpage.
	 */

	var initialDataObject = [ [] ];
	$.plot(graphContainer, initialDataObject, defaultFlotOptions);
}

/**
 * unStuffZerosFromLineGraphs removes the previously stuffed zero values. r is
 * the current data object. s is the current min and max {year} values. All the
 * datapoints < curr_min{year} && > > curr_max{year} are removed, so that they
 * don't show up on the graph
 * 
 * @param {Object}
 *            jsonObject
 * @param {Object}
 *            arrayOfMinAndMaxYears
 * @returns jsonObject with modified data points.
 */

//TODO: side-effect year.
function unStuffZerosFromLineGraphs(jsonObject, year) {

	calcZeroLessMinAndMax(jsonObject, year);
	var currentMinYear = year.globalMin, currentMaxYear = year.globalMax;
	
	var normalizedYearRange = getNormalizedYearRange();

	$.each(jsonObject,
			function(key, val) {
				var i = 0;
				for (i = 0; i < val.data.length; i++) {
					if (((val.data[i][0] < normalizedYearRange.normalizedMinYear) || (val.data[i][0] > normalizedYearRange.normalizedMaxYear))
							&& val.data[i][1] == 0) {

						val.data.splice(i, 1);
						i--;
					} else {
						continue;
					}
				}
			});
}

/**
 * while unStuffZerosFromLineGraphs is for a group of data objects,
 * unStuffZerosFromLineGraph is for a single data object. It removes zeroes from
 * the single object passed as parameter.
 * 
 * @param {Object}
 *            jsonObject
 */
function unStuffZerosFromLineGraph(jsonObject) {
	var i = 0;
	for (i = 0; i < jsonObject.data.length; i++) {
		if (jsonObject.data[i][1] == 0) {
			jsonObject.data.splice(i, 1);
			i--;
		}
	}
}


/**
 * This is used to normalize the year range for the currently selected entities to always 
 * display the last 10 years worth of data points. 
 * 
 */
function getNormalizedYearRange() {
	
	/*
	 * This is done to make sure that at least last 10 years worth of data points 
	 * can be displayed.
	 * */
	if (globalDateObject.getFullYear() < year.globalMax) {
		
		inferredMaxYear = year.globalMax;
		
	} else {
		
		inferredMaxYear = globalDateObject.getFullYear();
	}
	
	if (globalDateObject.getFullYear() - 9 > year.globalMin) {
		
		inferredMinYear = year.globalMin;
		
	} else {
		
		inferredMinYear = globalDateObject.getFullYear() - 9;
	}
	
	return {
		normalizedMinYear: inferredMinYear,
		normalizedMaxYear: inferredMaxYear,
		normalizedRange: inferredMaxYear - inferredMinYear 
	};
}

/**
 * stuffZerosIntoLineGraphs is used to fill discontinuities in data points. For
 * example, if a linegraph has the following data points [1990,
 * 2],[1992,3],[1994, 5],[1996,5],[2000,4],[2001,1]. stuffZerosIntoLineGraphs
 * inserts [1991,0],[1993,0],1995,0]..and so on. It also inserts zeroes at the
 * beginning and the end if the max and min{year} of the current linegraph fall
 * in between the global max and min{year}
 * 
 * @param {Object}
 *            jsonObject
 * @param {Object}
 *            arrayOfMinAndMaxYears
 * @returns jsonObject with stuffed data points.
 */
function stuffZerosIntoLineGraphs(jsonObject, year) {
	
	calcZeroLessMinAndMax(jsonObject, year);

	var normalizedYearRange = getNormalizedYearRange();
	
	$.each(jsonObject,
			function(key, val) {
				var position = normalizedYearRange.normalizedMinYear, i = 0;
				
				//console.log(key, val, position, (arrayOfMinAndMaxYears[1] - arrayOfMinAndMaxYears[0]) + 1);

				for (i = 0; i < normalizedYearRange.normalizedRange + 1; i++) {

					//console.log("val.data[i]", val.data[i]);
					
					if (val.data[i]) {

						if (val.data[i][0] != position
								&& position <= normalizedYearRange.normalizedMaxYear) {
							val.data.splice(i, 0, [ position, 0 ]);
						}
					}

					else {
						val.data.push( [ position, 0 ]);
					}
					position++;
				}
			});
	
	//console.log("after stuffing", jsonObject);
}
/**
 * During runtime, when the user checks/unchecks a checkbox, the zeroes have to
 * be inserted and removed dynamically. This function calculates the max{year}
 * and min{year} among all the linegraphs present on the graph at a particular
 * instance in time .
 * 
 * @param {Object}
 *            jsonObject
 * @returns an array of current min and max years.
 */
function calcZeroLessMinAndMax(jsonObject, year) {

	var validYearsInData = new Array();

	$.each(jsonObject, function(key, val) {

		for (i = 0; i < val.data.length; i++) {
			
			/*
			 * TO make sure that,
			 * 		1. Not to consider years that dont have any counts attached to it.
			 * 		2. Not to consider unknown years indicated by "-1". 
			 * */
			if (val.data[i][1] != 0 && val.data[i][0] != -1) {
				validYearsInData.push(val.data[i][0]);
			}
		}
		
	});

	year.globalMin = Math.min.apply(Math, validYearsInData);
	year.globalMax = Math.max.apply(Math, validYearsInData);
	
}

/**
 * z is an an object with two properties label and data. data is of the form
 * [year,value] This function returns the min and max values of all such years.
 * 
 * @param {Object}
 *            jsonObject
 * @returns [minYear, maxYear]
 */
function calcMinandMaxYears(jsonObject, year) {
	
	var validYearsInData = new Array();

	$.each(jsonObject, function(key, val) {

		for (i = 0; i < val.data.length; i++) {
			
			/*
			 * TO make sure that,
			 * 		1. Not to consider years that dont have any counts attached to it.
			 * 		2. Not to consider unknown years indicated by "-1". 
			 * */
			if (val.data[i][1] != 0 && val.data[i][0] != -1) {
				validYearsInData.push(val.data[i][0]);
			}
		}
		
	});
	
	
	year.min = Math.min.apply(Math, validYearsInData);
	year.max = Math.max.apply(Math, validYearsInData);
	
}

/**
 * This function returns the max from the counts of all the entities. Mainly used to 
 * normalize the width of bar below the line graph, also known as legend row.

 * @returns maxCount
 */
function calcMaxOfComparisonParameter(allEntities) {
	
	var validCountsInData = new Array();
	
	$.each(allEntities, function(key, currentEntity) {
		validCountsInData.push(calcSumOfComparisonParameter(currentEntity));
	});

	return Math.max.apply(Math, validCountsInData);
}

function calcMaxWithinComparisonParameter(jsonObject){
	
	var validCountsInData = new Array();

	$.each(jsonObject, function(key, val) {

		for (i = 0; i < val.data.length; i++) {
			
			/*
			 * TO make sure that,
			 * 		1. Not to consider years that dont have any counts attached to it.
			 * 		2. Not to consider unknown years indicated by "-1". 
			 * */
			if (val.data[i][1] != 0 && val.data[i][0] != -1) {
				validCountsInData.push(val.data[i][1]);
			}
		}
		
	});
	
	return Math.max.apply(Math, validCountsInData);
}

/**
 * This is used to find out the sum of all the counts of a particular entity. This is
 * especially useful to render the bars below the line graph where it doesnt matter if
 * a count has any associated year to it or not.
 * @returns sum{values}.
 */
function calcSumOfComparisonParameter(entity) {

	var sum = 0;

	$.each(entity.data, function(index, data){
		sum += this[1];
	});

	return sum;
}

/**
 * A simple function to see if the passed
 * 
 * @param {array}
 *            objectArray
 * @param {Object}
 *            object
 * @returns a flag - 0/1 - indicating whether a contains b.
 */
function contains(objectArray, object) {
	var i = 0, flag = 0;
	for (i = 0; i < objectArray.length; i++) {
		if (objectArray[i] == object) {
			flag = i;
		}
			
	}
	return flag;
}

/**
 * Dynamically change the linewidth and ticksize based on input year range.
 * 
 * @param {Object}
 *            yearRange
 */
function setLineWidthAndTickSize(yearRange, flotOptions) {

	if (yearRange > 0 && yearRange < 15) {
		flotOptions.series.lines.lineWidth = 3;
		flotOptions.xaxis.tickSize = 1;
	} else if (yearRange > 15 && yearRange < 70) {
		flotOptions.series.lines.lineWidth = 2;
		flotOptions.xaxis.tickSize = 5;
	} else if (yearRange == 0 ) {
		flotOptions.series.lines.lineWidth = 3;
		flotOptions.xaxis.tickSize = 1;
	} else {
		flotOptions.series.lines.lineWidth = 1;
		flotOptions.xaxis.tickSize = 10;
	}

}

/**
 * Dynamically change the ticksize of y-axis.
 */
function setTickSizeOfYAxis(maxValue, flotOptions){
	
	var tickSize = 0;
	
	if (maxValue > 0 && maxValue <= 5) {
		flotOptions.yaxis.tickSize = 1;
	} else if (maxValue > 5 && maxValue <= 10) {
		flotOptions.yaxis.tickSize = 2;
	} else 	if (maxValue > 10 && maxValue <= 15) {
		flotOptions.yaxis.tickSize = 5;
	} else if (maxValue > 15 && maxValue <= 70) {
		flotOptions.yaxis.tickSize  = 5;
	} else {
		flotOptions.yaxis.tickSize = 10;
	}
}

/**
 * Create a div that represents the rectangular bar A hidden input class that is
 * used to pass the value and a label beside the checkbox.
 * 
 * @param {Object}
 *            entityLabel
 */

function createLegendRow(entity, bottomDiv) {

    var parentP = $('<p>');
    parentP.attr('id', slugify(entity.label));

    var labelDiv = $('<div>');
    labelDiv.attr('class', 'easy-deselect-label');
    labelDiv.html('<div class="entity-label-url ellipsis"></div>');
    
    /*
     * We should display a further drill-down option only when available. In case of people
     * there is no drill-down possible, so don't diaply the temporal graph icon.
     * */
    if (entity.visMode !== "PERSON") {
    	labelDiv.append('<a class="temporal-vis-url" href="' + getTemporalVisURL(entity) + '"><img src = "' + temporalGraphSmallIcon + '"/></a>');	
    }
    

    var checkbox = $('<input>');
    checkbox.attr('type', 'checkbox');
    checkbox.attr('checked', true);
    checkbox.attr('id', 'checkbox');
    checkbox.attr('class', 'easyDeselectCheckbox');
    checkbox.attr('value', entity.label);

    var hiddenLabel = $('<label>');
    hiddenLabel.attr('type', 'hidden');
    hiddenLabel.attr('value', entity.label);

    var barDiv = $('<div>');
    barDiv.attr('id', 'bar');

    var numAttributeText = $('<span>');
    numAttributeText.attr('id', 'text');

    parentP.append(checkbox);
    parentP.append(labelDiv);
    parentP.append(hiddenLabel);
    parentP.append(barDiv);
    parentP.append(numAttributeText);

    bottomDiv.children('p.displayCounter').after(parentP);

    renderBarAndLabel(entity, barDiv, labelDiv, numAttributeText);
}

/**
 * generate the corresponding bar (representing area under the linegraph)
 * and label of the entity clicked
 */

function renderBarAndLabel(entity, divBar, divLabel, spanElement) {

    var sum = calcSumOfComparisonParameter(entity);
    var normalizedWidth = getNormalizedWidth(entity, sum);

    divBar.css("background-color", colorToAssign);
    divBar.css("width", normalizedWidth);

    var entityLabelForLegend = divLabel.find(".entity-label-url");
    entityLabelForLegend.html(entity.label);
    entityLabelForLegend.ellipsis();
    entityLabelForLegend.wrap("<a class='entity-url' title='" + entity.label + "' href='" + getVIVOURL(entity) + "'></a>");

    spanElement.text(sum).css("font-size", "0.8em").css("color", "#595B5B");

}

function getVIVOURL(entity){
	
	var result  = subOrganizationVivoProfileURL + "uri="+entity.entityURI;
	
	return result;
}

function getTemporalVisURL(entity) {
	
	return subOrganizationTemporalGraphURL + "&uri=" + entity.entityURI ;
}

function getVIVOProfileURL(given_uri) {
	
	finalURL = $.ajax({
		url: contextPath + "/visualization",
		data: ({vis: "utilities", vis_mode: "PROFILE_URL", uri: given_uri}),
		dataType: "text",
		async: false,
		success:function(data){
	}
	}).responseText;

	return finalURL;
	
}

function slugify(textToBeSlugified) {
	return textToBeSlugified.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9\-]/g, '').toLowerCase();
}

/**
 * remove the bar, label and checkbox during runtime.
 * 
 * @param {Object}
 *            label
 * @param {Object}
 *            bar
 * @param {Object}
 *            checkbox
 * @param {Object}
 *            span
 */
function removeLegendRow(checkbox) {
	
	//console.log("removeLegendRow is called for "+$(checkbox).attr("value"));
	var pToBeRemovedIdentifier = $(checkbox).attr("value");
	$('p#' + slugify(pToBeRemovedIdentifier)).remove();
	
    $(checkbox).next('a').css("font-weight", "normal");
}

/**
 * These are the options passed to by $.pagination(). Basically they define the
 * items visible per page, number of navigation tabs, and number of edge
 * navigation tabs.
 * 
 * @param object
 * @param itemsPerPage
 * @param numberOfDisplayEntries
 * @param numOfEdgeEntries
 */
function setOptionsForPagination(object, itemsPerPage, numberOfDisplayEntries,
		numOfEdgeEntries) {

	object = {
			items_per_page : itemsPerPage,
			num_display_entries : numberOfDisplayEntries,
			num_edge_entries : numOfEdgeEntries,
			prev_text : "Prev",
			next_text : "Next"
	};
}

/**
 * function for removing "unknown" values (-1) just before data plotting.
 * 
 * @jsonRecords the set of entities from which the unknowns have to be removed.
 */

function removeUnknowns(jsonRecords) {
	
	var i = 0, j = 0;

	while (j < jsonRecords.length) {

		jsonRecords[j].unknownsValue = -1;

		for (i = 0; i < jsonRecords[j].data.length; i++) {
			if (jsonRecords[j].data[i][0] == -1) {
				jsonRecords[j].unknownsValue = jsonRecords[j].data[i][1];
				jsonRecords[j].data.splice(i, 1);
				i--;
			}
		}
		j++;
	}
	
}

function insertBackUnknowns(jsonRecords) {
	
	var i = 0, j = 0;

	while (j < jsonRecords.length) {
		if (jsonRecords[j].unknownsValue != -1) {
			jsonRecords[j].data.push( [ -1, jsonRecords[j].unknownsValue ]);
		}
		j++;
	}
}

/**
 * function to get the next free color in the queue
 * side-effecting entity here.
 */
function getNextFreeColor(entity){
   
	/* check freeColors is not empty and
     * Remove the first element out of freeColors
     */

    if (contains(freeColors, prevColor[entity.label])) {
        var index = contains(freeColors, prevColor[entity.label]);
        colorToAssign = freeColors[index];
        freeColors.splice(index, 1);
    } else {
        colorToAssign = freeColors.shift();
    }    
    /*
     * use colorToAssign to plot the current linegraph
     * also store it in colors
     */
    entity.color = colorToAssign;
    colors[entity.label] = colorToAssign;
}

function getNormalizedWidth(entity, sum){
	
	 var maxValueOfComparisonParameter = calcMaxOfComparisonParameter(labelToEntityRecord);
	 var normalizedWidth = 0;
	 
	 normalizedWidth = Math.floor(225 * (sum / maxValueOfComparisonParameter));

	 /*
	  * This will make sure that the entites that have very low <parameter> count have at least
	  * 1 pixel width bar. This happens when the highest count organization has a very high count
	  * compared to the lowest count organization.
	  * */
	 return normalizedWidth === 0 ? 1 : normalizedWidth;
}

function renderLineGraph(renderedObjects, entity){
	
    renderedObjects.push(entity);
    stuffZerosIntoLineGraphs(renderedObjects, year);
    
}


function removeUsedColor(entity){
	
    if (colors[entity.label]) {
        colorToRemove = colors[entity.label];
        prevColor[entity.label] = colorToRemove;
        entity.color = "";
    }
    
	//Insert it at the end of freeColors
    freeColors.push(colorToRemove);
}

function removeEntityUnChecked(renderedObjects, entity){
	
	//remove the entity that is unchecked
    var ii = 0;
    while (ii < renderedObjects.length) {
    	
        if (renderedObjects[ii].label == entity.label) {
            unStuffZerosFromLineGraph(renderedObjects[ii]);
            renderedObjects.splice(ii, 1);
        } else {
        	ii++;
        }             
    }
    unStuffZerosFromLineGraphs(renderedObjects, year);
    
}


function generateCheckBoxes(label, checkedFlag, fontFlag){
	
	var parentP = $('<p>');
	
	var li = $('<li>');
	
	var checkbox = $('<input>');
	checkbox.attr('type','checkbox');
	checkbox.attr('class', entityCheckboxSelectorDOMClass);
	checkbox.attr('value', label);
	if(checkedFlag == 1){
		checkbox.attr('checked');
	}
	
	var a = $('<a/>');
	if(fontFlag == 1){
		a.css("font-weight", "bold");
	}
	a.html(label);
	
	parentP.append(li);
	parentP.append(checkbox);
	parentP.append(a);
	
    return parentP;
}

function clearRenderedObjects(){
	
	$.each(labelToCheckedEntities, function(index, val){
		if($(val).is(':checked')){
			$(val).attr("checked", false);
			updateRowHighlighter(val);
			removeUsedColor(labelToEntityRecord[$(val).attr("value")]);
			removeEntityUnChecked(renderedObjects, labelToEntityRecord[$(val).attr("value")]);
			removeLegendRow(val);
			displayLineGraphs();
		}
	});
	
	labelToCheckedEntities = {};
	checkIfColorLimitIsReached();
	updateCounter();

}

function createNotification( template, vars, opts ){
	return $notificationContainer.notify("create", template, vars, opts);
}

function updateCounter(){
	//notification about the max items that can be clicked
	$("#counter").text(renderedObjects.length);
}

function displayLineGraphs(){
	
	//plot all we got
    if (renderedObjects.length == 0) {
    	
    	init(graphContainer);
    	
    } else {
    	
    	removeUnknowns(renderedObjects);
        $.plot(graphContainer, renderedObjects, FlotOptions);
        insertBackUnknowns(renderedObjects);

    }
}



function removeCheckBoxFromGlobalSet(checkbox){
    //remove checkbox object from the globals
	var value = $(checkbox).attr("value");
	if(labelToCheckedEntities[value]){
		delete labelToCheckedEntities[value];
	}
}


/*
 * function to create a table to be 
 * used by jquery.dataTables. The JsonObject 
 * returned is used to populate the pagination table.
 */	
function prepareTableForDataTablePagination(jsonData){
	
	resetStopWordCount();
	var checkboxCount = 0;
	var table = $('<table>');
	table.attr('cellpadding', '0');
	table.attr('cellspacing', '0');
	table.attr('border', '0');
	table.attr('id', 'datatable');
	table.css('font-size', '0.9em');
	table.css('width', '100%');
	
	var thead = $('<thead>');
	var tr = $('<tr>');
	
	var checkboxTH = $('<th>');
	checkboxTH.html(' ');
	
	var entityLabelTH = $('<th>');
	entityLabelTH.html('Entity Name');
	
	var publicationCountTH = $('<th>');
	if($("select.comparisonValues option:selected").text() === "by Publications"){
		publicationCountTH.html('Publication Count');
	}else{
		publicationCountTH.html('Grant Count');		
	}

	var entityTypeTH = $('<th>');
	entityTypeTH.html('Entity Type');

	tr.append(checkboxTH);
	tr.append(entityLabelTH);
	tr.append(publicationCountTH);
	tr.append(entityTypeTH);
	
	thead.append(tr);
	
	table.append(thead);
	
	var tbody = $('<tbody>');
	
	$.each(labelToEntityRecord, function(index, val){
		var entityTypesWithoutStopWords = removeStopWords(val);
		var row = $('<tr>'); 
		
		var checkboxTD = $('<td>');
		checkboxTD.html('<div class="disabled-checkbox-event-receiver">&nbsp;</div><input type="checkbox" class="' + entityCheckboxSelectorDOMClass + '" value="' + index + '"'+'/>');
		
		var labelTD =  $('<td>');
		labelTD.css("width", "100px");
		labelTD.html(index);
		
		var publicationCountTD =  $('<td>');
		publicationCountTD.html(calcSumOfComparisonParameter(val));
		
		var entityTypeTD =  $('<td>');
		entityTypeTD.html(entityTypesWithoutStopWords);
		
		row.append(checkboxTD);
		row.append(labelTD);
		row.append(publicationCountTD);
		row.append(entityTypeTD);
		
		tbody.append(row);
		checkboxCount++;
	});
	
	table.append(tbody);
	tableDiv.append(table);
	
	var searchBarParentContainerDIVClass = "searchbar";
	
	var entityListTable = $('#datatable').dataTable({
	    "sDom": '<"' + searchBarParentContainerDIVClass + '"f><"filterInfo"i><"paginatedtabs"p><"table-separator"><"datatablewrapper"t>',
	    "aaSorting": [
	        [2, "desc"], [1,'asc']
	    ],
	    "asStripClasses": [],
	    "iDisplayLength": 10,
	    "bInfo": true,
	    "oLanguage": {
			"sInfo": "Records _START_ - _END_ of _TOTAL_",
			"sInfoEmpty": "No matching entities found",
			"sInfoFiltered": ""
		},
	    "sPaginationType": "gmail_style",
	    "fnDrawCallback": function () {
	    	
	        /* We check whether max number of allowed comparisions (currently 10) is reached
	         * here as well becasue the only function that is guaranteed to be called during 
	         * page navigation is this. No need to bind it to the nav-buttons becuase 1. It is over-ridden
	         * by built-in navigation events & this is much cleaner.
	         * */
	        checkIfColorLimitIsReached();
	    }
	});
	
	
	var searchInputBox = $("." + searchBarParentContainerDIVClass).find("input[type=text]");
	
	searchInputBox.after("<span id='reset-search' title='Clear Search query'>X</span>");
	
	$("#reset-search").live('click', function() {
		entityListTable.fnFilter("");
	});
	
	/*
	var filterInfo = $(".filterInfo").detach();
	$("#infoContainer").append(filterInfo);
	*/
	
}

function updateRowHighlighter(linkedCheckBox){
	linkedCheckBox.closest("tr").removeClass('datatablerowhighlight');
}


function resetStopWordCount(){
	stopWordsToCount["Person"] = 0;
	stopWordsToCount["Organization"] = 0;
}

function removeStopWords(val){
	var typeStringWithoutStopWords = "";
	$.each(val.organizationType, function(index, value){
		if(value == "Person"){
			stopWordsToCount["Person"]++;
		}else if(value == "Organization"){
			stopWordsToCount["Organization"]++;
		}else{
			typeStringWithoutStopWords += ', '+ value; 
		}
	});
	//console.log(stopWordsToCount["Person"],stopWordsToCount["Organization"]);
	return typeStringWithoutStopWords.substring(1, typeStringWithoutStopWords.length);
}

function setEntityLevel(entityLevel){
	//$('#entitylevelheading').text(' - ' + toCamelCase(entityLevel) + ' Level').css('font-style', 'italic');
	$('#entityleveltext').text('  ' + entityLevel.toLowerCase()).css('font-style', 'italic');
	$('#entityHeader').text(entityLevel).css('font-weight', 'bold');
	$('#headerText').css("color", "#2485ae");
}

function getEntityVisMode(jsonData){
	
	var entityLevels = new Array();
	
	$.each(jsonData, function(index, val) {
		if (val.visMode ==  "PERSON"){
			entityLevels.push("People");
		} else {
			entityLevels.push("Organizations");
		}
	});
	
	var uniqueEntityLevels = $.unique(entityLevels);

	/*
	 * This case is when organizations & people are mixed because both are directly attached
	 * to the parent organization. 
	 * */
	if (uniqueEntityLevels.length > 1) {
		entityLevel = "Organizations & People";
	} else if (uniqueEntityLevels.length === 1) {
		entityLevel = uniqueEntityLevels[0]; 
	} else {
		/* To provide graceful degradation set entity level to a default error message.*/
		entitylevel = "ENTITY LEVEL UNDEFINED ERROR";
	}
	
	return entityLevel;
}

function toCamelCase(string){
	return string ? (string.substr(0,1).toUpperCase() + string.substr(1, string.length-1).toLowerCase()) : "";
}

function getSize(map){
	var size = 0;
	
	$.each(map, function(){
		size++;
	});
	
	return size;
}

function disableUncheckedEntities(){

	$.each($("input[type=checkbox]." + entityCheckboxSelectorDOMClass + ":not(:checked)"), function(index, val){
		$(val).attr('disabled', true);
		$(val).prev().show();
	});
	
	/*
	 * This indicates the first time this function is called presumably after the 10th checkbox is selected.
	 * We want to display a warning message only in Internet Explorer because in IE the div that handles
	 * disabled-checkbox-clicks is colored white & we cant see the actual checkbox.
	 * 
	 * Note that the usual Error message will still display if the user tries to click on the white box 
	 * (or a disabled checkbox in case of non-IE browsers).   
	 * */
	if ($("#datatable").data("isEntitySelectionAllowed")) {
		if ($.browser.msie) {
			createNotification("warning-notification", { title:'Information', 
				text:'A Maximum of 10 entities can be compared.' },{
				custom: false,
				expires: false
				});	
		}
	} 
		
	
	$("#datatable").data("isEntitySelectionAllowed", false);
}

function enableUncheckedEntities(){
	
	$.each($("input[type=checkbox]." + entityCheckboxSelectorDOMClass + ":not(:checked)"), function(index, val){
		$(val).attr('disabled', false);
		$(val).prev().hide();
	});
	
	
	
	$("#datatable").data("isEntitySelectionAllowed", true);
}

function checkIfColorLimitIsReached(){
	
	if (getSize(labelToCheckedEntities) >= 10) {
		disableUncheckedEntities();
	} else {
		enableUncheckedEntities();
	}
}

function setTickSizeOfAxes(){
	
	var checkedLabelToEntityRecord = {};
	var yearRange;
	
	$.each(labelToCheckedEntities, function(index, val){
		checkedLabelToEntityRecord[index] = labelToEntityRecord[index];
	});
	
	var normalizedYearRange = getNormalizedYearRange();
	
    setLineWidthAndTickSize(normalizedYearRange.normalizedRange, FlotOptions);     
	setTickSizeOfYAxis(calcMaxWithinComparisonParameter(checkedLabelToEntityRecord), FlotOptions);
}