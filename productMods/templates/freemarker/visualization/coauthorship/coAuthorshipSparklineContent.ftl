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
<#assign visContainerID = '${sparklineVO.visContainerDivID}'>

<#if sparklineVO.shortVisMode>
    <#assign sparklineContainerID = 'unique_coauthors_short_sparkline_vis'>
<#else>
    <#assign sparklineContainerID = 'unique_coauthors_full_sparkline_vis'>
</#if>

<#-- This is used to prevent collision between sparkline & visualization conatiner div ids. -->
<#if visContainerID?upper_case == sparklineContainerID?upper_case>
    <#assign sparklineContainerID = visContainerID + "_spark"> 
</#if>

<div class="staticPageBackground">
    <div id="${visContainerID}">
        <script type="text/javascript">
        
            function drawCoauthorsSparklineVisualization(providedSparklineImgTD) {
            
                var unknownYearPublicationCounts = ${sparklineVO.unknownYearPublications};
                var onlyUnknownYearPublications = false;
    
                var data = new google.visualization.DataTable();
                data.addColumn('string', 'Year');
                data.addColumn('number', 'Unique co-authors');
                data.addRows(${sparklineVO.yearToEntityCountDataTable?size});
                
                var knownYearPublicationCounts = 0;
                
                <#list sparklineVO.yearToEntityCountDataTable as yearToUniqueCoauthorsDataElement>                        
                    data.setValue(${yearToUniqueCoauthorsDataElement.yearToEntityCounter}, 0, '${yearToUniqueCoauthorsDataElement.year}');
                    data.setValue(${yearToUniqueCoauthorsDataElement.yearToEntityCounter}, 1, ${yearToUniqueCoauthorsDataElement.currentEntitiesCount});
                    knownYearPublicationCounts += ${yearToUniqueCoauthorsDataElement.currentEntitiesCount};
                </#list>
                
                <#-- Create a view of the data containing only the column pertaining to coauthors count. -->
                var sparklineDataView = new google.visualization.DataView(data);
                sparklineDataView.setColumns([1]);
        
                <#if sparklineVO.shortVisMode>
         
                <#-- For the short view we only want the last 10 year's view of coauthors count, hence we filter 
                    the data we actually want to use for render. -->
         
                sparklineDataView.setRows(data.getFilteredRows([{
                        column: 0,
                        minValue: '${sparklineVO.earliestRenderedPublicationYear?c}',
                        maxValue: '${sparklineVO.latestRenderedPublicationYear?c}'
                }]));
         
                <#else>

                </#if>
         
                var visualizationOptions = {
                    width: 150,
                    height: 60,
                    color: '3399CC',
                    chartType: 'ls',
                    chartLabel: 'r'
                }
                
                /*
                This means that all the publications have unknown years & we do not need to display
                the sparkline.
                */            
                if (unknownYearPublicationCounts > 0 && knownYearPublicationCounts < 1) {
                    
                    onlyUnknownYearPublications = true;
                    
                } else {

                /* 
                Test if we want to go for the approach when serving visualizations from a secure site..
                If "https:" is not found in location.protocol then we do everything normally.
                */
                if (location.protocol.indexOf("https") == -1) {
                    /*
                    This condition will make sure that the location protocol (http, https, etc) does not have 
                    for word https in it.
                    */
                             
                    <#-- Create the vis object and draw it in the div pertaining to sparkline. -->
                    var sparkline = new google.visualization.ImageSparkLine(providedSparklineImgTD[0]);
                    sparkline.draw(sparklineDataView, {
                            width: visualizationOptions.width,
                            height: visualizationOptions.height,
                            showAxisLines: false,
                            showValueLabels: false,
                            labelPosition: 'none'
                    });

                } else {
                
                    <#-- Prepare data for generating google chart URL. -->
                    
                    <#-- If we need to serve data for https:, we have to create an array of values to be plotted. -->
                    var chartValuesForEncoding = new Array();
                    
                    $.each(sparklineDataView.getViewRows(), function(index, value) {
                        chartValuesForEncoding.push(data.getValue(value, 1));
                    });
                    
                    var chartImageURL = constructVisualizationURLForSparkline(
                                extendedEncodeDataForChartURL(chartValuesForEncoding, 
                                                              sparklineDataView.getColumnRange(0).max), 
                                visualizationOptions);

                    var imageContainer = $(providedSparklineImgTD[0]);
                    
                    imageContainer.image(chartImageURL, 
                            function(){
                                imageContainer.empty().append(this); 
                                $(this).addClass("google-visualization-sparkline-image");
                            },
                            function(){
                                // For performing any action on failure to
                                // find the image.
                                imageContainer.empty();
                            }
                    );
                
                }
                
                }
                
                if (${sparklineVO.totalCollaborationshipCount?c}) {
                    var totalPublicationCount = ${sparklineVO.totalCollaborationshipCount?c};
                } else {
                    var totalPublicationCount = knownYearPublicationCounts + unknownYearPublicationCounts; 
                }
         
                <#if sparklineVO.shortVisMode>
         
                    <#-- We want to display how many coauthors were considered, so this is used to calculate this. -->
         
                    var shortSparkRows = sparklineDataView.getViewRows();
                    var renderedShortSparks = 0;
                    $.each(shortSparkRows, function(index, value) {
                        renderedShortSparks += data.getValue(value, 1);
                    });
                    
                     /*
                    In case that there are only unknown publications we want the text to mention these counts,
                    which would not be mentioned in the other case because the renderedShortSparks only hold counts
                    of publications which have any date associated with it.
                    */
                    var totalPubs = onlyUnknownYearPublications ? unknownYearPublicationCounts : renderedShortSparks;
                    
                    if (totalPubs === 1) {
                        var pubDisplay = "co-author";
                    } else {
                        var pubDisplay = "co-authors";
                    }
         
                    $('#${sparklineContainerID} td.sparkline_number').text(totalPubs).css("font-weight", "bold").attr("class", "grey").append("<span style='color: #2485AE;'> " + pubDisplay + " <br/></span>");
            
                    var sparksText = '  within the last 10 years';
                                        
                    if (totalPubs !== totalPublicationCount) {
                        sparksText += ' (' + totalPublicationCount + ' total)';
                    }
            
                 <#else>
            
                    /*
                     * Sparks that will be rendered will always be the one's which has 
                     * any year associated with it. Hence.
                     * */
                    var renderedSparks = ${sparklineVO.renderedSparks};
                    
                    /*
                    In case that there are only unknown publications we want the text to mention these counts,
                    which would not be mentioned in the other case because the renderedSparks only hold counts
                    of publications which have any date associated with it.
                    */
                    var totalPubs = onlyUnknownYearPublications ? unknownYearPublicationCounts : renderedSparks;
                          
                    if ( totalPubs == 1 ) {
                        var pubDisplay = "co-author";
                    } else {
                        var pubDisplay = "co-authors";
                    }
                          
                    $('#${sparklineContainerID} td.sparkline_number').text(totalPubs).css("font-weight", "bold").attr("class", "grey").append("<span style='color: #2485AE;'> " + pubDisplay + " <br/></span>");
            
                    var sparksText = '  from <span class="sparkline_range">${sparklineVO.earliestYearConsidered?c}' 
                                        + ' to ${sparklineVO.latestRenderedPublicationYear?c}</span>';
                                        
                    if (totalPubs !== totalPublicationCount) {
                        sparksText += ' (' + totalPublicationCount + ' total)';
                    }
                    
                    if (totalPublicationCount) {
                        sparksText += ' <br /><a href="${sparklineVO.downloadDataLink}">(.CSV File)</a> ';
                    }
                                         
                 </#if>
         
                 if (!onlyUnknownYearPublications) {
                    $('#${sparklineContainerID} td.sparkline_text').html(sparksText);
                 }
         
            }
    
            /*
             * This will activate the visualization. It takes care of creating 
             * div elements to hold the actual sparkline image and then calling the 
             * drawCoauthorsSparklineVisualization function. 
             * */

            $(document).ready(function() {
                var sparklineImgTD; 
        
            /*
             * This is a nuclear option (creating the container in which everything goes)
             * the only reason this will be ever used is the API user never submitted a 
             * container ID in which everything goes. The alternative was to let the 
             * vis not appear in the calling page at all. So now atleast vis appears but 
             * appended at the bottom of the body.
             * */
     
                if ($('#${visContainerID}').length === 0) {
                    $('<div/>', {
                        'id': '${visContainerID}'
                    }).appendTo('body');
                }       
        
                if ($('#${sparklineContainerID}').length === 0) {
        
                    $('<div/>', {
                        'id': '${sparklineContainerID}',
                        'class': 'sparkline_style'
                    }).prependTo('#${visContainerID}');
            
                    var table = $('<table>');
                    table.attr('class', 'sparkline_wrapper_table');
                    var row = $('<tr>');
                    sparklineImgTD = $('<td>');
                    sparklineImgTD.attr('id', '${sparklineContainerID}_img');
                    sparklineImgTD.attr('width', '150');
                    sparklineImgTD.attr('class', 'sparkline_style');
            
                    row.append(sparklineImgTD);
                    var row2 = $('<tr>');
                    var sparklineNumberTD = $('<td>');
                    sparklineNumberTD.attr('class', 'sparkline_number');
                    sparklineNumberTD.css('text-align', 'left');
                    row2.append(sparklineNumberTD);
                    var row3 = $('<tr>');
                    
                    var sparklineTextTD = $('<td>');
                    sparklineTextTD.attr('class', 'sparkline_text');
                    sparklineTextTD.css('text-align', 'left');
                    row3.append(sparklineTextTD);
                    table.append(row);
                    table.append(row2);
                    table.append(row3);
                    table.prependTo('#${sparklineContainerID}');
         
                }
            
                drawCoauthorsSparklineVisualization(sparklineImgTD);
            });
        </script>
         
    </div>
    
    <!-- Sparkline Viz -->

    <#if sparklineVO.shortVisMode>
        <#--<span class="vis_link">-->
            <p><a class="all-vivo-publications" href="${sparklineVO.fullTimelineNetworkLink}">View full timeline and co-author network.</a></p>
        <#--</span>-->
    <#else>
        <!-- For Full Sparkline - Print the Table of Couauthor Counts per Year -->

            <#if displayTable?? && displayTable>
        
                <p> 
                    <#assign tableID = "coauthors_sparkline_data_table" />
                    <#assign tableCaption = "Unique Co-Authors per year " />
                    <#assign tableActivityColumnName = "Count" />
                    <#assign tableContent = sparklineVO.yearToActivityCount />
                    <#assign fileDownloadLink = sparklineVO.downloadDataLink />
                    
                    <#include "yearToActivityCountTable.ftl">
        
                    Download data as <a href="${sparklineVO.downloadDataLink}">.csv</a> file.
                    <br />
                </p>
        
            </#if>

    </#if>
</div>