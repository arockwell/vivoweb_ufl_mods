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

/**
 * For rendering images dynamically.
 * 
 */
$.fn.image = function(src, successFunc, failureFunc){
	return this.each(function(){ 
		var profileImage = new Image();
		profileImage.onerror = failureFunc;
		profileImage.onload = successFunc;
		profileImage.src = src;

		return profileImage;
	});
};


/**
 * Function by Google Charts API Team to do "extended encode" of data. 
*/
function extendedEncodeDataForChartURL(arrVals, maxVal) {

    var EXTENDED_MAP = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.';
    var EXTENDED_MAP_LENGTH = EXTENDED_MAP.length;
    var chartData = 'e:';

    for (i = 0, len = arrVals.length; i < len; i++) {
        // In case the array vals were translated to strings.
        var numericVal = new Number(arrVals[i]);
        // Scale the value to maxVal.
        var scaledVal = Math.floor(EXTENDED_MAP_LENGTH * EXTENDED_MAP_LENGTH * numericVal / maxVal);

        if (scaledVal > (EXTENDED_MAP_LENGTH * EXTENDED_MAP_LENGTH) - 1) {
            chartData += "..";
        } else if (scaledVal < 0) {
            chartData += '__';
        } else {
            // Calculate first and second digits and add them to the output.
            var quotient = Math.floor(scaledVal / EXTENDED_MAP_LENGTH);
            var remainder = scaledVal - EXTENDED_MAP_LENGTH * quotient;
            chartData += EXTENDED_MAP.charAt(quotient) + EXTENDED_MAP.charAt(remainder);
        }
    }

    return chartData;
}

/**
 * This will be used for getting images directly from the secure https://charts.googleapis.com
 * instead of http://charts.apis.google.com which currently throws security warnings.
 * 
 * see http://code.google.com/apis/chart/docs/chart_params.html FOR chart parameters
 * see http://code.google.com/apis/chart/docs/data_formats.html FOR how to encode data
 * 
 * sample constructed URL - https://chart.googleapis.com/chart?cht=ls&chs=148x58&chdlp=r&chco=3399CC&chd=e%3AW2ttpJbb..ttgAbbNtAA
 */
function constructVisualizationURLForSparkline(dataString, visualizationOptions) {

	/*
	 * Since we are directly going to use this URL in img tag, we are supposed to enocde "&"
	 * update: But since we are directly using it in an Image creating function we dont need to encode it.
	*/
    //var parameterDifferentiator = "&amp;";
    var parameterDifferentiator = "&";

    var rootGoogleChartAPI_URL = "https://chart.googleapis.com/chart?";

    /*
     * cht=ls indicates chart of type "line chart sparklines". 
     * see http://code.google.com/apis/chart/docs/gallery/chart_gall.html 
	*/
    var chartType = "cht=" + visualizationOptions.chartType;

    /*
     * It seems google reduces 2px from width & height before rendering the actual image.
     * We will do the same.
	*/
    var chartSize = "chs=" + (visualizationOptions.width - 2) + "x" + (visualizationOptions.height - 2);

    /*
     * It means that legend, if present, is to be displayed to the right of the chart,
     * legend entries in a vertical column.
	*/
    var chartLabelPosition = "chdlp=" + visualizationOptions.chartLabel;

    /*
     * Color of the sparkline.
	*/
    var chartColor = "chco=" + visualizationOptions.color;

    return rootGoogleChartAPI_URL + chartType + parameterDifferentiator 
    			+ chartSize + parameterDifferentiator 
    			+ chartLabelPosition + parameterDifferentiator 
    			+ chartColor + parameterDifferentiator 
    			+ "chd=" + dataString
}