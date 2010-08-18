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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DatatypeDaoJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

/**
 * User: bdc34
 * Date: Jan 24, 2008
 * Time: 1:55:39 PM
 */
public class BasicValidation {

    Map<String, List<String>> varsToValidations;
    EditConfiguration editConfig;
    
    public BasicValidation(EditConfiguration editConfig, EditSubmission editSub){
        this.editConfig = editConfig;
        Map<String,List<String>> validatorsForFields = new HashMap<String,List<String>>();
        for(String fieldName: editConfig.getFields().keySet()){
            Field field = editConfig.getField(fieldName);
            validatorsForFields.put(fieldName,field.getValidators());
        }
        this.varsToValidations = validatorsForFields;
        checkValidations();
    }

    public BasicValidation(Map<String, List<String>> varsToValidations){
        this.varsToValidations = varsToValidations;
        checkValidations();
    }

    public Map<String,String> validateUris(Map<String,String> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();

        for( String name : varNamesToValues.keySet()){

            String value = varNamesToValues.get(name);
            List<String> validations = varsToValidations.get(name);
            if( validations!= null){
                for( String validationType : validations){
                    String validateMsg = validate(validationType,value);
                    if( validateMsg != null) {
                        errors.put(name,validateMsg);
                    }    
                }
            }
        }               
        return errors;
    }


    public Map<String,String> validateLiterals(Map<String, Literal> varNamesToValues){
        HashMap<String,String> errors = new HashMap<String,String>();

        for( String name : editConfig.getLiteralsOnForm() ){
            Literal literal = varNamesToValues.get(name);
            List<String>validations = varsToValidations.get(name);
            if( validations != null ){
                // NB this is case-sensitive
                boolean isRequiredField = validations.contains("nonempty");
                
                for( String validationType : validations){
                    String value = null;
                    try{
                        if( literal != null ){
                            value = literal.getString();
                        }
                    }catch(Throwable th){ 
                        log.debug("could not convert literal to string" , th); 
                    }
                    // Empty field: if required, include only the empty field
                    // error message, not a format validation message. If non-required, 
                    // don't do format validation, since that is both unnecessary and may 
                    // incorrectly generate errors.
                    if (isEmpty(value)) {
                        if (isRequiredField) {
                           errors.put(name, REQUIRED_FIELD_EMPTY_MSG);
                        }
                        break;
                    }
                    
                    String validateMsg = validate(validationType, value);
                    if( validateMsg != null) {
                        errors.put(name,validateMsg);
                    }
                }
            }
        }
        return errors;
    }
    
    public Map<String,String>validateFiles(Map<String, List<FileItem>> fileItemMap) {        
        
        HashMap<String,String> errors = new HashMap<String,String>();
        for(String name: editConfig.getFilesOnForm() ){            
            List<String> validators = varsToValidations.get(name);            
            for( String validationType : validators){
                String validateMsg = validate(validationType, fileItemMap.get(name));
                if( validateMsg != null ) {
                    errors.put(name, validateMsg);
                }    
            }
        }            
        return errors;    
    }
    
    private String validate(String validationType, List<FileItem> fileItems) {
        if( "nonempty".equalsIgnoreCase(validationType)){
            if( fileItems == null || fileItems.size() == 0 ){
                return "a file must be entered for this field.";
            }else{
                FileItem fileItem = fileItems.get(0);
                if( fileItem == null || fileItem.getName() == null || fileItem.getName().length() < 1 || fileItem.getSize() < 0){
                    return "a file must be entered for this field.";
                }
            }
        }
        return null;
    }

    /* null indicates success. A returned string is the validation
    error message.
     */
    public String validate(String validationType, String value){
        // Required field validation.
        // For literals, testing empty required values in validateLiterals.
        // This case may be needed for validation of other field types.
        if( "nonempty".equalsIgnoreCase(validationType)){
            if( isEmpty(value) )
                return REQUIRED_FIELD_EMPTY_MSG;
        }
        // Format validation
        else if("isDate".equalsIgnoreCase(validationType)){
            if( isDate( value))
                return SUCCESS;
            else
                return "must be in valid date format mm/dd/yyyy.";
        }
        else if( validationType.indexOf("datatype:") == 0 ) {
            String datatypeURI = validationType.substring(9);
            String errorMsg = validateAgainstDatatype( value, datatypeURI ); 
            if ( errorMsg == null ) { 
                return SUCCESS;
            } else {
                return errorMsg;
            }
        } else if ("httpUrl".equalsIgnoreCase(validationType)){ 
        	//check if it has http or https, we could do more but for now this is all.
        	if(! value.startsWith("http://") && ! value.startsWith("https://") ){
        		return "This URL must start with http:// or https://"; 
        	}else{
        		return SUCCESS;        		
        	}        	 
        }
        //Date not past validation
        else if( "dateNotPast".equalsIgnoreCase(validationType)){
        	//if( ! past (value) )
        	// return "date must not be in the past";
        	//Current date
        	Calendar c = Calendar.getInstance();
        	//Input
        	Calendar inputC = Calendar.getInstance();
        	String yearParamStr, monthParamStr, dayParamStr;
        	int yearDash = value.indexOf("-");
    		int monthDash = value.lastIndexOf("-");
        	if(yearDash != -1 && yearDash != monthDash) {
        		yearParamStr = value.substring(0, yearDash);
        		monthParamStr = value.substring(yearDash + 1, monthDash);
        		dayParamStr = value.substring(monthDash + 1, value.length());
        		inputC.set(Integer.parseInt(yearParamStr), Integer.parseInt(monthParamStr) - 1, Integer.parseInt(dayParamStr));
        		if(inputC.before(c)) {
            		return this.DATE_NOT_PAST_MSG;
            		//Returning null makes the error message "field is empty" display instead
            		//return null;
            	} else {
            		return SUCCESS;
            	}
        	}	
        }
        return null; //
    }

    private boolean isDate(String in){
         return datePattern.matcher(in).matches();
    }
    
    private static DatatypeDaoJena ddao = null;
    
    public static synchronized String validateAgainstDatatype( String value, String datatypeURI ) {
        if ( ( datatypeURI != null ) && ( datatypeURI.length()>0 ) ) {
            RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
            if ( datatype == null ) {
                throw new RuntimeException( datatypeURI + " is not a recognized datatype");
            }
            if ( datatype.isValid(value) ) {
                return null;
            } else {
                // TODO: better way of getting more friendly names for common datatypes
                if (ddao == null) {
                    ddao = new DatatypeDaoJena(new WebappDaoFactoryJena(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)));
                }
                Datatype dtype = ddao.getDatatypeByURI(datatypeURI);
                String dtypeMsg = (dtype != null) ? dtype.getName() : datatypeURI;
                return " Please correct this value: must be a valid " + dtypeMsg + ".";
            }
        }
        return null;
    }

    private void checkValidations(){
        List<String> unknown = new ArrayList<String>();
        for( String key : varsToValidations.keySet()){
            for( String validator : varsToValidations.get(key)){
                if( ! basicValidations.contains( validator)) {
                    if ( ! ( ( validator != null) &&  
                         ( validator.indexOf( "datatype:" ) == 0 ) ) ) {
                        unknown.add(validator);
                    }
                }
            }
        }
        if( unknown.isEmpty() )
            return ;

        throw new Error( "Unknown basic validators: " + unknown.toArray());
    }
    
    private static boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0); 
    }

    
    
    private static Pattern urlRX = Pattern.compile("(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?");

    /** we use null to indicate success */
    public final static String SUCCESS = null;
    public final static String REQUIRED_FIELD_EMPTY_MSG = "This field must not be empty.";
    public final static String DATE_NOT_PAST_MSG = "Please enter a future target date for publication (past dates are invalid).";
    //public final static String MIN_FIELDS_NOT_POPULATED = "Please enter values for at least ";
    //public final static String FORM_ERROR_FIELD_ID = "formannotationerrors";
    /** regex for strings like "12/31/2004" */
    private final String dateRegex = "((1[012])|([1-9]))/((3[10])|([12][0-9])|([1-9]))/[\\d]{4}";
    private final Pattern datePattern = Pattern.compile(dateRegex);

    static final List<String> basicValidations;
    static{
        basicValidations = Arrays.asList(
        "nonempty","isDate","dateNotPast","httpUrl" );
    }

    private Log log = LogFactory.getLog(BasicValidation.class);
}
