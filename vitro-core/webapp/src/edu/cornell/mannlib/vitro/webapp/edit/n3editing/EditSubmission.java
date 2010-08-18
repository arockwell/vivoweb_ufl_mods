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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;

public class EditSubmission {
    private String editKey;

    private Map<String,Literal> literalsFromForm ;
    private Map<String,String> urisFromForm ;

    private Map<String,String> validationErrors;
    private BasicValidation basicValidation;

	private Map<String, List<FileItem>> filesFromForm;

    private static Model literalCreationModel;
     
    static{
        literalCreationModel = ModelFactory.createDefaultModel();
    }
    
    public EditSubmission(Map<String,String[]> queryParameters,  EditConfiguration editConfig){
        if( editConfig == null )
            throw new Error("EditSubmission needs an EditConfiguration");            
        this.editKey = editConfig.getEditKey();         
        if( this.editKey == null || this.editKey.trim().length() == 0)
            throw new Error("EditSubmission needs an 'editKey' parameter from the EditConfiguration");        

        validationErrors = new HashMap<String,String>();
        
        this.urisFromForm = new HashMap<String,String>();
        for( String var: editConfig.getUrisOnform() ){     
            String[] valuesArray = queryParameters.get( var );
            String uri = null;
            List<String> values = (valuesArray != null) ? Arrays.asList(valuesArray) : null;
            if( values != null && values.size() > 0){
                if(  values.size() == 1 ) {
                	uri = values.get(0);                    	
                } else if( values.size() > 1 ){
                	uri = values.get(0);
                    log.error("Cannot yet handle multiple URIs for a single field, using first URI on list");
                } 
                urisFromForm.put(var,uri);
            } else {
                log.debug("No value found for query parameter " + var);              
            }
            //check to see if a URI field from the form was blank but was intended to create a new URI
            if( uri != null && uri.length() == 0 && editConfig.getNewResources().containsKey(var) ){
            	log.debug("A new resource URI will be made for var " + var + " since it was blank on the form.");
            	urisFromForm.remove(var);
            }
        }
        this.literalsFromForm =new HashMap<String,Literal>();        
        for(String var: editConfig.getLiteralsOnForm() ){            
            Field field = editConfig.getField(var);
            if( field == null ) {
                log.error("could not find field " + var + " in EditConfiguration" );
                continue;
            }
            if( field.getOptionsType() == Field.OptionsType.DATETIME ||
                    XSD.dateTime.getURI().equals(field.getRangeDatatypeUri()) ) {
                Literal literal = getDateTime(queryParameters, var);
                if( literal != null){
                    literalsFromForm.put(var, literal);
                } else {
                    log.debug("datetime fields for parameter " + var  + " were not on form" );
                }
            } else if( field.getOptionsType() == Field.OptionsType.DATE ||
                    XSD.date.getURI().equals(field.getRangeDatatypeUri()) ){
                Literal literal = getDate(queryParameters, var);
                if( literal != null){
                    literalsFromForm.put(var, literal);
                } else {
                    log.debug("date fields for parameter " + var  + " were not on form" );
                }
            } else if( field.getOptionsType() == Field.OptionsType.TIME ||
            	    XSD.time.getURI().equals(field.getRangeDatatypeUri()) ){
            	Literal literal = getTime(queryParameters, var);
            	if( literal != null){
            		literalsFromForm.put(var, literal);
            	} else {
            		log.debug("time fields for parameter " + var + " were not on form" );
            	}
        	} else {
            	String[] valuesArray = queryParameters.get(var); 
                List<String> valueList = (valuesArray != null) ? Arrays.asList(valuesArray) : null;                
                if( valueList != null && valueList.size() > 0 ) {
                    literalsFromForm.put(var, createLiteral(valueList.get(0), field.getRangeDatatypeUri(), field.getRangeLang()));
                    if(valueList != null && valueList.size() > 1 )
                        log.debug("For field " + var +", cannot yet handle multiple " +
                        		"Literals for a single field, using first Literal on list");                            
                }else{
                    log.debug("could not find value for parameter " + var  );
                }
            }
        }

        if( log.isDebugEnabled() ){        	
        	for( String key : literalsFromForm.keySet() ){
        		log.debug( key + " literal " + literalsFromForm.get(key) );
        	}
        	for( String key : urisFromForm.keySet() ){
        		log.debug( key + " uri " + urisFromForm.get(key) );
        	}
        }
        
        this.basicValidation = new BasicValidation(editConfig,this);
        Map<String,String> errors = basicValidation.validateUris( urisFromForm );
        if( errors != null ) {
            validationErrors.putAll( errors );
        }
        
        errors = basicValidation.validateLiterals( literalsFromForm );
        if( errors != null ) {
            validationErrors.putAll( errors);
        }
        
        if(editConfig.getValidators() != null ){
        	for( N3Validator validator : editConfig.getValidators()){
        		if( validator != null ){     
        			errors = validator.validate(editConfig, this);
        			if ( errors != null )
        				validationErrors.putAll(errors);
        		}
        	}
        }
        
    }

    public EditSubmission(Map<String, String[]> queryParameters, EditConfiguration editConfig, 
    		Map<String, List<FileItem>> fileItems) {
    	this(queryParameters,editConfig);    	
    	this.filesFromForm = fileItems;      	
    	validationErrors.putAll(this.basicValidation.validateFiles( fileItems ) );
	}

	protected Literal createLiteral(String value, String datatypeUri, String lang){
        if( datatypeUri != null ){            
            if( "http://www.w3.org/2001/XMLSchema:anyURI".equals(datatypeUri) ){
                try {
                    return literalCreationModel.createTypedLiteral( URLEncoder.encode(value, "UTF8"), datatypeUri);
                } catch (UnsupportedEncodingException e) { 
                    log.error(e);
                }                
            }   
            return literalCreationModel.createTypedLiteral(value, datatypeUri);
        }else if( lang != null && lang.length() > 0 )
            return literalCreationModel.createLiteral(value, lang);
        else 
            return ResourceFactory.createPlainLiteral(value);        
    }        
    
    /**
     * need to generate something like
     *  "2008-03-14T00:00:00"^^<http://www.w3.org/2001/XMLSchema#dateTime>
     */
    public Literal  getDateTime(Map<String,String[]> queryParameters,String fieldName){
    	DateTime dt = null;                   
        List<String> year = Arrays.asList(queryParameters.get("year"+ fieldName));
        List<String> month = Arrays.asList(queryParameters.get("month" + fieldName));
        List<String> day = Arrays.asList(queryParameters.get("day" + fieldName));
        List<String> hour = Arrays.asList(queryParameters.get("hour"+ fieldName));
        List<String> minute = Arrays.asList(queryParameters.get("minute" + fieldName));
        
        if( year == null || year.size() == 0 ||
                month == null || month.size() == 0 ||
                day == null || day.size() == 0 ||
                hour == null || hour.size() == 0 ||
                minute == null || minute.size() == 0 ){
           //log.info("Could not find query parameter values for date field " + fieldName );
        } /* else  if(  year.size() > 1 || month.size() > 1 || day.size() > 1 || hour.size() > 1 || minute.size() > 1 ){
           log.info("Cannot yet handle multiple values for the same field ");
        } */

        String yearParamStr = year.get(0);
        String monthParamStr = month.get(0);
        String dayParamStr = day.get(0);
        String hourParamStr = hour.get(0);
        String minuteParamStr = minute.get(0);
        
        // if all fields are blank, just return a null value
        if (yearParamStr.length() == 0 && 
        	monthParamStr.length() == 0 &&
        	dayParamStr.length() == 0 &&
        	hourParamStr.length() == 0 &&
        	minuteParamStr.length() == 0) {
    		return null;
    	}
        
        DateTimeFormatter dateFmt = DateTimeFormat.forPattern("yyyyMMdd:HH:mm");
        try{
            dt = dateFmt.parseDateTime(yearParamStr + monthParamStr + dayParamStr + ':' + hourParamStr + ':' + minuteParamStr);
            String dateStr = dformater.print(dt);
            return new EditLiteral(dateStr,DATE_TIME_URI, null );

        }catch(IllegalFieldValueException ifve){
            validationErrors.put(fieldName, ifve.getLocalizedMessage());
            return null;
        }
    }
    
    /**
     * need to generate something like
     *  "09:10:11"^^<http://www.w3.org/2001/XMLSchema#time>
     */ 
    public Literal getTime(Map<String,String[]> queryParameters,String fieldName) {
    	List<String> hour = Arrays.asList(queryParameters.get("hour" + fieldName));
    	List<String> minute = Arrays.asList(queryParameters.get("minute" + fieldName));
    	
    	if ( hour == null || hour.size() == 0 ||
    		 minute == null || minute.size() == 0 ) {
    		log.info("Could not find query parameter values for time field " + fieldName);
    		validationErrors.put(fieldName, "time must be supplied");
    		return null;
    	}
    	
    	int hourInt = -1;
    	int minuteInt = -1;
    	
    	String hourParamStr = hour.get(0);
    	String minuteParamStr = minute.get(0);
    	
    	// if all fields are blank, just return a null value
    	if (hourParamStr.length() == 0 && minuteParamStr.length() == 0) {
    		return null;
    	}
    	
    	 String errors = "";
         try{ 
        	 hourInt = Integer.parseInt(hour.get(0));
        	 if (hourInt < 0 || hourInt > 23) {
        		 throw new NumberFormatException();
             }
         } catch( NumberFormatException nfe ) { 
        	 errors += "Please enter a valid hour.  "; 
         }        
         try{
        	 minuteInt = Integer.parseInt(minute.get(0));
        	 if (minuteInt < 0 || minuteInt > 59) {
        		 throw new NumberFormatException();
        	 }
         } catch( NumberFormatException nfe ) { 
        	 errors += "Please enter a valid minute.  "; 
         }        
         if( errors.length() > 0 ){
             validationErrors.put( fieldName, errors);
             return null;
         }
    	
         
         String hourStr = (hourInt < 10) ? "0" + Integer.toString(hourInt) :  Integer.toString(hourInt);
         String minuteStr = (minuteInt < 10) ? "0" + Integer.toString(minuteInt) :  Integer.toString(minuteInt);
         String secondStr = "00";
         
         return new EditLiteral(hourStr + ":" + minuteStr + ":" + secondStr, TIME_URI, null);
         
    }
    
    /**
     * need to generate something like
     *  "2008-03-14"^^<http://www.w3.org/2001/XMLSchema#date>
     */
    public Literal  getDate(Map<String,String[]> queryParameters,String fieldName){
        DateTime dt = null;                   
        List<String> year = Arrays.asList(queryParameters.get("year"+ fieldName));
        List<String> month = Arrays.asList(queryParameters.get("month" + fieldName));
        List<String> day = Arrays.asList(queryParameters.get("day" + fieldName));
        
        if( year == null || year.size() == 0 || 
            month == null || month.size() == 0 ||
            day == null || day.size() == 0  ){
           log.info("Could not find query parameter values for date field " + fieldName );
           validationErrors.put( fieldName, "date must be supplied");
           return null;
        } 
         
        String yearParamStr = year.get(0);
        String monthParamStr = month.get(0);
        String dayParamStr = day.get(0);
        
        // if all fields are blank, just return a null value
        if (yearParamStr.length() == 0 && 
        	monthParamStr.length() == 0 &&
        	dayParamStr.length() == 0) {
    		return null;
    	}
        
        String errors = "";
        try{ Integer.parseInt(yearParamStr); }
        catch( NumberFormatException nfe )
            { errors += "Please enter a valid year.  "; }        
        try{ Integer.parseInt(monthParamStr); }
        catch( NumberFormatException nfe )
            { errors += "Please enter a valid month.  "; }
        try{ Integer.parseInt(dayParamStr); }
        catch( NumberFormatException nfe )
            { errors += "Please enter a valid day.  "; }        
        if( errors.length() > 0 ){
            validationErrors.put( fieldName, errors);
            return null;
        }
        
        //Removing this 
        /*
        boolean compareCurrentDate = false;
        String[] dateNotPastArgs = queryParameters.get("validDateParam");
        if(dateNotPastArgs != null && dateNotPastArgs.length > 0) {
        	
        	compareCurrentDate = (dateNotPastArgs[0].equals("dateNotPast"));
        }*/ 
        
        try{
            dt = dateFormater.parseDateTime(year.get(0) +'-'+ month.get(0) +'-'+ day.get(0));
            String dateStr = dateFormater.print(dt);
            return new EditLiteral(dateStr,DATE_URI, null );
        }catch(IllegalFieldValueException ifve){
            validationErrors.put( fieldName, ifve.getLocalizedMessage() );
            return null;
        }
    }

    private static final String DATE_TIME_URI = XSD.dateTime.getURI();
    private static final String DATE_URI = XSD.date.getURI();
    private static final String TIME_URI = XSD.time.getURI();

    private static DateTimeFormatter dformater = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:00");
    private static DateTimeFormatter dateFormater = DateTimeFormat.forPattern("yyyy-MM-dd");

    public Map<String,String> getValidationErrors(){
        return validationErrors;
    }

    public Map<String, Literal> getLiteralsFromForm() {
        return literalsFromForm;
    }

    public Map<String, String> getUrisFromForm() {
        return urisFromForm;
    }

    public void setLiteralsFromForm(Map<String, Literal> literalsFromForm) {
        this.literalsFromForm = literalsFromForm;
    }

    public void setUrisFromForm(Map<String, String> urisFromForm) {
        this.urisFromForm = urisFromForm;
    }

    public String toString(){
        String[] names ={
                 "literalsFromForm",
                 "urisFromForm","validationErrors","basicValidation"
        };
        JSONObject obj = new JSONObject(this,names);
        return obj.toString();
    }

    /* *************** Static utility methods to get EditSub from Session *********** */

    public static EditSubmission getEditSubmissionFromSession(HttpSession sess, EditConfiguration editConfig){
        Map<String,EditSubmission> submissions = (Map<String,EditSubmission>)sess.getAttribute("EditSubmissions");
        if( submissions == null )
          return null;
        if( editConfig != null )
            return submissions.get(  editConfig.getEditKey() ); //this might be null
        else
            return null;
    }

    public static void putEditSubmissionInSession(HttpSession sess, EditSubmission editSub){
        Map<String,EditSubmission> submissions = (Map<String,EditSubmission>)sess.getAttribute("EditSubmissions");
        if( submissions == null ){
            submissions = new HashMap<String,EditSubmission>();
            sess.setAttribute("EditSubmissions",submissions);
        }
        submissions.put(editSub.editKey, editSub);
    }


    public static void clearEditSubmissionInSession(HttpSession sess, EditSubmission editSub){
        if( sess == null) return;
        if( editSub == null ) return;
        Map<String,EditSubmission> submissions = (Map<String,EditSubmission>)sess.getAttribute("EditSubmissions");
        if( submissions == null ){
            throw new Error("EditSubmission: could not get a Map of EditSubmissions from the session.");
        }

        submissions.remove( editSub.editKey );
    }

    public static void clearAllEditSubmissionsInSession(HttpSession sess ){
        if( sess == null) return;
        sess.removeAttribute("editSubmission");
    }

    public static Map<String, String[]> convertParams(
            Map<String, List<String>> queryParameters) {
        HashMap<String,String[]> out = new HashMap<String,String[]>();
        for( String key : queryParameters.keySet()){
            List item = queryParameters.get(key);            
            out.put(key, (String[])item.toArray(new String[item.size()]));
        }
        return out;
    }     

    private Log log = LogFactory.getLog(EditSubmission.class);
}
