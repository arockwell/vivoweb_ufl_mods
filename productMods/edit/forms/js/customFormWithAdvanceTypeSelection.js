/*
Copyright (c) 2010, Cornell University
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

var customForm = {
    
    /* *** Initial page setup *** */
   
    onLoad: function() {
        
        if (this.disableFormInUnsupportedBrowsers()) {
            return;
        }
        this.mixIn();
        this.initObjects();                 
        this.initPage();       
    },
    
    disableFormInUnsupportedBrowsers: function() {       
        this.disableWrapper = $('#ie67DisableWrapper');
        
        // Check for unsupported browsers only if the element exists on the page
        if (this.disableWrapper.length) {
            if (vitro.browserUtils.isIELessThan8()) {
                this.disableWrapper.show();
                $('.noIE67').hide();
                return true;
            }
        }            
        return false;      
    },

    mixIn: function() {
        // Mix in the custom form utility methods
        $.extend(this, vitro.customFormUtils);

        // Get the custom form data from the page
        $.extend(this, customFormData);
    },
    
    // On page load, create references for easy access to form elements.
    // NB These must be assigned after the elements have been loaded onto the page.
    initObjects: function(){

        this.form = $('#content form');
        this.fullViewOnly = $('.fullViewOnly');
        this.button = $('#submit');
        this.baseButtonText = this.button.val();
        this.requiredLegend = $('#requiredLegend');
        this.typeSelector = this.form.find('#typeSelector');

        // These are classed rather than id'd in case we want more than one autocomplete on a form.
        // At that point we'll use ids to match them up with one another.
        this.acSelector = this.form.find('.acSelector');
        this.acSelection = this.form.find('.acSelection');
        this.acSelectionInfo = this.form.find('.acSelectionInfo');
        this.acUriReceiver = this.form.find('.acUriReceiver');
        //this.acLabelReceiver = this.form.find('.acLabelReceiver');
        this.verifyMatch = this.form.find('.verifyMatch');    
        this.verifyMatchBaseHref = this.verifyMatch.attr('href');    
        this.acSelectorWrapper = this.acSelector.parent();
        
        this.relatedIndLabel = $('#relatedIndLabel');
        this.labelFieldLabel = $('label[for=' + this.relatedIndLabel.attr('id') + ']');       
        // Get this on page load, so we can prepend to it. We can't just prepend to the current label text,
        // because it may have already been modified for a previous selection.
        this.baseLabelText = this.labelFieldLabel.html();

        // Label field for new individual being created
        this.newIndLabel = $('#newIndLabel');
        this.newIndLabelFieldLabel = $('label[for=' + this.newIndLabel.attr('id') + ']');
        this.newIndBaseLabelText = this.newIndLabelFieldLabel.html();
        
        this.dateHeader = $('#dateHeader');
        this.baseDateHeaderText = this.dateHeader.html();
        
        this.or = $('span.or');       
        this.cancel = this.form.find('.cancel');
        
        this.placeHolderText = '###';

    },

    // Set up the form on page load
    initPage: function() {

        if (!this.editMode) {
            this.editMode = 'add'; // edit vs add: default to add
        }
        
        if (!this.typeSelector.length || this.editMode == 'edit' || this.editMode == 'repair') {
            this.formSteps = 1;
        // there may also be a 3-step form - look for this.subTypeSelector
        } else {
            this.formSteps = 2;
        }
                
        this.bindEventListeners();
        
        this.initAutocomplete();
        
        this.initFormView();

    },

    initFormView: function() {
      
        var typeVal = this.typeSelector.val();  
        
        // Put this case first, because in edit mode with
        // validation errors we just want initFormFullView.
        if (this.editMode == 'edit' || this.editMode == 'repair') {
            this.initFormFullView();
        }
        else if (this.findValidationErrors()) {
            this.initFormWithValidationErrors();
        }
        // If type is already selected when the page loads (Firefox retains value
        // on a refresh), go directly to full view. Otherwise user has to reselect
        // twice to get to full view.        
        else if ( this.formSteps == 1 || typeVal.length ) {
            this.initFormFullView();
        }
        else {
            this.initFormTypeView();
        }     
    },
    
    initFormTypeView: function() {

        this.setType(); // empty any previous values (perhaps not needed)
        this.hideFields(this.fullViewOnly);
        this.button.hide();
        this.requiredLegend.hide();
        this.or.hide();

        this.cancel.unbind('click');
    },
    
    initFormFullView: function() {

        this.setType();        
        this.fullViewOnly.show();
        this.or.show();
        this.requiredLegend.show();
        this.button.show();
        this.setButtonText('new');
        this.setLabels(); 
           
        if( this.formSteps > 1 ){  // NB includes this.editMode == 1
            this.cancel.unbind('click');   
            this.cancel.click(function() {
                customForm.clearFormData(); // clear any input and validation errors
                customForm.initFormTypeView();
                return false;            
            });
        }
    },
    
    initFormWithValidationErrors: function() {
        var uri = this.acUriReceiver.val(), 
            label = this.acSelector.val(); 
        
        // Call initFormFullView first, because showAutocompleteSelection needs
        // acType, which is set in initFormFullView. 
        this.initFormFullView();
        
        if (uri) {            
            this.showAutocompleteSelection(label, uri);
        }
        
        this.cancel.unbind('click');
        this.cancel.click(function() {
           // Cancel back to full view with only type selection showing
           customForm.undoAutocompleteSelection();
           customForm.clearFields(customForm.fullViewOnly);
           customForm.initFormFullView(); 
           return false;
        });
       
    },
    
    // Bind event listeners that persist over the life of the page. Event listeners
    // that depend on the view should be initialized in the view setup method.
    bindEventListeners: function() {

        this.typeSelector.change(function() {
            var typeVal = $(this).val();
            
            // If an autocomplete selection has been made, undo it
            customForm.undoAutocompleteSelection();

            // If no selection, go back to type view. This prevents problems like trying to run autocomplete
            // or submitting form without a type selection. Exception: in repair editMode, stay in full view,
            // else we lose the role information.          
            (typeVal.length || customForm.editMode == 'repair') ? customForm.initFormFullView() : customForm.initFormTypeView();
    
        }); 
        
        this.verifyMatch.click(function() {
            window.open($(this).attr('href'), 'verifyMatchWindow', 'width=640,height=640,scrollbars=yes,resizable=yes,status=yes,toolbar=no,menubar=no,location=no');
            return false;
        });   
        
    },
    
    initAutocomplete: function() {

        if (this.editMode === 'edit') {
            return;
        }
        
        this.getAcFilter();
        this.acCache = {};
        
        this.acSelector.autocomplete({
            minLength: 3,
            source: function(request, response) {
                if (request.term in customForm.acCache) {
                    // console.log('found term in cache');
                    response(customForm.acCache[request.term]);
                    return;
                }
                // console.log('not getting term from cache');

                $.ajax({
                    url: customForm.acUrl,
                    dataType: 'json',
                    data: {
                        term: request.term,
                        type: customForm.acType
                    },
                    complete: function(xhr, status) {
                        // Not sure why, but we need an explicit json parse here. jQuery
                        var results = $.parseJSON(xhr.responseText), 
                            filteredResults = customForm.filterAcResults(results);
                        customForm.acCache[request.term] = filteredResults;
                        response(filteredResults);
                    }
                });
            },
            select: function(event, ui) {
                customForm.showAutocompleteSelection(ui.item.label, ui.item.uri);                     
            }
        });
    },
    
    getAcFilter: function() {

        if (!this.sparqlForAcFilter) {
            //console.log('autocomplete filtering turned off');
            this.acFilter = null;
            return;
        }
        
        //console.log("sparql for autocomplete filter: " + this.sparqlForAcFilter);

        // Define this.acFilter here, so in case the sparql query fails
        // we don't get an error when referencing it later.
        this.acFilter = [];
        $.ajax({
            url: customForm.sparqlQueryUrl,
            data: {
                resultFormat: 'RS_JSON',
                query: customForm.sparqlForAcFilter
            },
            success: function(data, status, xhr) {
                // Not sure why, but we need an explicit json parse here. jQuery
                // should parse the response text and return a json object.
                customForm.setAcFilter($.parseJSON(data));
            }
        });
    },
    
    setAcFilter: function(data) {

        var key = data.head.vars[0];
        
        $.each(data.results.bindings, function() {
            customForm.acFilter.push(this[key].value);
        });         
    },
    
    filterAcResults: function(results) {
        var filteredResults;
        
        if (!this.acFilter || !this.acFilter.length) {
            //console.log('no autocomplete filtering applied');
            return results;
        }
        
        filteredResults = [];
        $.each(results, function() {
            if ($.inArray(this.uri, customForm.acFilter) == -1) {
                //console.log('adding ' + this.label + ' to filtered results');
                filteredResults.push(this);
            }
            else {
                //console.log('filtering out ' + this.label);
            }
        });
        return filteredResults;
    },

    // Reset some autocomplete values after type is changed
    resetAutocomplete: function(typeVal) {
        // Append the type parameter to the base autocomplete url
        var glue = this.baseAcUrl.indexOf('?') > -1 ? '&' : '?';
        this.acUrl = this.baseAcUrl + glue + 'type=' + typeVal;
        
        // Flush autocomplete cache when type is reset, since the cached values 
        // pertain only to the previous type.
        this.acCache = {};
    },       
        
    showAutocompleteSelection: function(label, uri) {

        this.acSelectorWrapper.hide();
        //this.acSelector.attr('disabled', 'disabled');
        
        // If only one form step, type is pre-selected, and the label is coded in the html.
        if (this.formSteps > 1) {
            this.acSelection.find('label').html('Selected ' + this.typeName + ':');
        }
              
        this.acSelection.show();

        this.acUriReceiver.val(uri);
        this.acSelector.val(label);
        this.acSelectionInfo.html(label);
        this.verifyMatch.attr('href', this.verifyMatchBaseHref + uri);
        
        this.setButtonText('existing');            

        this.cancel.unbind('click');
        this.cancel.click(function() {
            customForm.undoAutocompleteSelection();
            customForm.initFormFullView();
            return false;
        });
    },
    
    // Cancel action after making an autocomplete selection: undo autocomplete 
    // selection (from showAutocomplete) before returning to full view.
    undoAutocompleteSelection: function() {
        
        this.acSelectorWrapper.show();
        this.hideFields(this.acSelection);
        this.acSelector.val('');
        this.acUriReceiver.val('');
        this.acSelectionInfo.html('');
        this.verifyMatch.attr('href', this.verifyMatchBaseHref);
        
        if (this.formSteps > 1) {
            this.acSelection.find('label').html('Selected ');
        }
                
    },
    
    // Set type uri for autocomplete, and type name for labels and button text.
    // Note: we still need this in edit mode, to set the text values.
    setType: function() {
        
        var selectedType;
        
        // If there's no type selector, these values have been specified in customFormData,
        // and will not change over the life of the form.
        if (!this.typeSelector.length) {
            return;
        }

        selectedType = this.typeSelector.find(':selected'); 
        if (selectedType.length) {
            this.acType = selectedType.val();
            this.typeName = selectedType.html();
        } 
        // reset to empty values; may not need
        else {
            this.acType = '';
            this.typeName = '';
        }
    },

    // Set field labels based on type selection. Although these won't change in edit
    // mode, it's easier to specify the text here than in the jsp.
    setLabels: function() {
        var newLabelTextForNewInd, 
            // if this.acType is empty, we are in repair mode with no activity type selected.
            // Prevent the labels from showing 'Select one' by using the generic term 'Activity' 
            typeText = this.acType ? this.typeName : 'Activity';
            
        
        this.labelFieldLabel.html(typeText + ' ' + this.baseLabelText);
        
        if (this.dateHeader.length) {
            this.dateHeader.html(this.baseDateHeaderText + typeText);
        } 
                   
        if (this.newIndLabel.length) {
            newLabelTextForNewInd = this.newIndBaseLabelText.replace(this.placeHolderText, typeText);
            this.newIndLabelFieldLabel.html(newLabelTextForNewInd);
        }  

    },
    
    // Set button text based on both type selection and whether it's an autocomplete selection
    // or a new related individual. Called when setting up full view of form, and after
    // an autocomplete selection.
    setButtonText: function(newOrExisting) {
        var typeText, buttonText;
        
        // Edit mode button doesn't change, so it's specified in the jsp
        if (this.editMode === 'edit') {
            return;
        }  

        // if this.acType is empty, we are in repair mode with no activity type selected.
        // Prevent the labels from showing 'Select one' by using the generic term 'Activity' 
        typeText = this.acType ? this.typeName : 'Activity';
                
        // Creating new related individual      
        if (newOrExisting === 'new') {
            if (this.submitButtonTextType == 'compound') { // use == to tolerate nulls
                // e.g., 'Create Grant & Principal Investigator'
                buttonText = 'Create ' + typeText + ' & ' + this.baseButtonText;                
            } else {
                // e.g., 'Create Publication'
                buttonText = 'Create ' + this.baseButtonText;
            }            
        }
        // Using existing related individual
        else {  
            // In repair mode, baseButtonText is "Edit X". Keep that for this case.
            buttonText = this.editMode == 'repair' ? this.baseButtonText : 'Add ' + this.baseButtonText;
        } 
        
        this.button.val(buttonText);
    }
    
};

$(document).ready(function() {   
    customForm.onLoad();
});
