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

//Hard coded color constants
var	TURQUOISE = "#8DD3C7";
var	DARK_TURQUOISE = "#009999";
var	LIGHT_YELLOW = "#FFFFB3";
var	LIGHT_VIOLET = "#BEBADA";
var	RED = "#CC0000";
var	LIGHT_RED = "#FB8072";
var	DARK_RED = "#520000";
var	SKY_BLUE = "#80B1D3";
var	DARK_BLUE = "#80B1D3";
var	NAVY_BLUE = "#003366";
var	LIGHT_BLUE = "#3399FF";
var	ORANGE = "#FDB462";
var DARK_ORANGE = "#FF9900";
var	LIGHT_GREEN = "#B3DE69";
var	DARK_GREEN = "#006600";
var	VIBRANT_GREEN = "#99CC00";
var	LIGHT_PINK = "#FCCDE5";
var	LIGHT_GREY = "#D9D9D9";
var	PURPLE = "#BC80BD";
var	DARK_PURPLE = "#6600CC";
var	PINK_PURPLE = "#CC00CC";
var	HOT_PINK = "#FF00B4";
var MEHENDI_GREEN = "#7A7900";

var colorConstantQueue = [ LIGHT_BLUE, DARK_ORANGE, VIBRANT_GREEN, 
                           NAVY_BLUE, RED, PINK_PURPLE, 
                           DARK_TURQUOISE, MEHENDI_GREEN, HOT_PINK, 
                           DARK_RED ];

var freeColors;

var globalDateObject;

var year;

var colors, prevColor, colorToAssign, 
	colorToRemove, renderedObjects, labelToEntityRecord,
	setOfLabels, labelToCheckedEntities, stopWordsToCount;

var graphContainer;
var tableDiv;
var entityLevel;

//options for Flot
var FlotOptions;

function initConstants() {
	
	freeColors = colorConstantQueue.slice();
	
	globalDateObject = new Date();
	
	year = {
			min: globalDateObject.getFullYear() - 9,
			max: globalDateObject.getFullYear(),
			globalMin: globalDateObject.getFullYear() - 9,
			globalMax: globalDateObject.getFullYear()
	};
	
	colors = {};
	prevColor = {};
	colorToAssign, colorToRemove;
	renderedObjects = [];
	labelToEntityRecord = {};
	setOfLabels = [];
	labelToCheckedEntities = {};
	stopWordsToCount = {};
	
	//options for Flot
	FlotOptions = {
			legend : {
				show : false
			},
			lines : {
				show : true
			},
			points : {
				show : false
			},
			xaxis : {
				tickDecimals : 0,
				tickSize : 10
			},
			series : {
				lines : {
					lineWidth : 7
				}
			},
			yaxis : {
				tickSize : 1,
				tickDecimals : 0,
				min : 0
			},
			grid : {
				borderColor : "#D9D9D9"
			}
	};

	FlotOptions.colors = colorConstantQueue;
	
}