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

package edu.cornell.mannlib.vedit.beans;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 *
 * @author jc55
 *
 */
public class LoginFormBean {
    public static final int ANYBODY=0;
    public  int getAnybody(){ return ANYBODY; }
    public static final int NON_EDITOR = 1;
    public int getNonEditor(){ return NON_EDITOR; }
    public static final int EDITOR =4;
    public  int getEditor(){return EDITOR;}
    public static final int CURATOR=5;
    public  int getCurator(){return CURATOR;}
    public static final int DBA    =50;
    public  int getDba(){return DBA;}

    public boolean getBla(){ return true; }

    private String userURI;
    private String sessionId;
    private String loginBrowser;
    private String loginRemoteAddr;
    private String loginName;
    private String loginPassword;
    private String loginStatus;
    private int loginUserId;
    private String loginRole;
    private String duplicatePassword;
    private String emailAddress;
    private Hashtable errors;

    public boolean validateLoginForm() {
        boolean allOk=true;

        if ( loginName.equals("")) {
            errors.put( "loginName","Please enter your Vivo user name" );
            loginName = "";
            allOk = false;
        }

        if ( loginPassword.equals("")) {
            errors.put( "loginPassword","Please enter your Vivo password" );
            loginPassword="";
            allOk=false;
        }

        return allOk;
    }

    public LoginFormBean() {
        sessionId         = "";
        loginBrowser      = "";
        loginRemoteAddr   = "";
        loginName         = "";
        loginPassword     = "";
        loginStatus       = "none";
        loginUserId       = 0;
        loginRole         = "1";
        duplicatePassword = "";
        emailAddress      = "";

        errors = new Hashtable();
    }

    public String toString(){
        String name = "-not-logged-in-";
        if( getLoginName() != null && !"".equals(getLoginName()) )
                name = getLoginName();

        return this.getClass().getName()
        +" loginName: " + name
        +" loginStatus: "+ getLoginStatus()
        +" loginRole: "+ getLoginRole();
    }
    /**
       Tests a HttpSession to see if logged in and authenticated.
       @returns loginRole if seems to be authenticated, -1 otherwise
    */
    public int testSessionLevel( HttpServletRequest request ){
        //TODO: security code added by bdc34, should be checked by jc55
        HttpSession currentSession = request.getSession();
        int returnRole = -1;
        if ( getLoginStatus().equals("authenticated") &&
             currentSession.getId().equals( getSessionId() ) &&
             request.getRemoteAddr().equals( getLoginRemoteAddr() ) ) {
            try{
                returnRole = Integer.parseInt( getLoginRole() );
            }catch(Throwable thr){ }
        }
        return returnRole;
    }

    public static boolean loggedIn(HttpServletRequest request, int minLevel) {
        if( request == null ) return false;
        HttpSession sess = request.getSession(false);
        if( sess == null ) return false;
        Object obj = sess.getAttribute("loginHandler");
        if( obj == null || ! (obj instanceof LoginFormBean))
        return false;

        LoginFormBean lfb = (LoginFormBean)obj;
        return ( "authenticated".equals(lfb.loginStatus ) &&
                Integer.parseInt(lfb.loginRole ) >= minLevel) ;
    }

    /********************** GET METHODS *********************/

    public String getUserURI() {
        return userURI;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getLoginBrowser() {
        return loginBrowser;
    }

    public String getLoginRemoteAddr() {
        return loginRemoteAddr;
    }
    public String getLoginName() {
        return loginName;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public int getLoginUserId() {
        return loginUserId;
    }

    public String getLoginRole() {
        return loginRole;
    }

    public String getDuplicatePassword() {
        return duplicatePassword;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getErrorMsg( String s ) {
        String errorMsg =(String) errors.get( s.trim() );
        return ( errorMsg == null ) ? "" : errorMsg;
    }

    /********************** SET METHODS *********************/

    public void setUserURI( String uri ) {
        this.userURI = uri;
    }

    public void setSessionId( String id ) {
        sessionId = id;
    }

    public void setLoginBrowser( String b ) {
        loginBrowser = b;
    }

    public void setLoginRemoteAddr( String ra ) {
        loginRemoteAddr = ra;
    }

    public void setLoginName( String ln ) {
        loginName = ln;
    }

    public void setLoginPassword( String lp ) {
        loginPassword = lp;
    }

    public void setLoginStatus( String ls ) {
        loginStatus = ls;
    }

    public void setLoginUserId(int int_val) {
        loginUserId=int_val;
    }

    public void setLoginRole( String lr ) {
        loginRole = lr;
    }

    public void setDuplicatePassword( String dp ) {
        duplicatePassword = dp;
    }

    public void setEmailAddress( String ea ) {
        emailAddress = ea;
    }

    public void setErrorMsg( String key, String msg ) {
        errors.put( key,msg );
    }

}
