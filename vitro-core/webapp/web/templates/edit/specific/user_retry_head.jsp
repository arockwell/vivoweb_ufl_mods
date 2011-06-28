<%--
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
--%>

<script language="JavaScript" type="text/javascript">
<!--
     var bypassValidateUF = false;
     
     function forceCancel(theForm) {           // we don't want validation to stop us if we're canceling
         theForm.Md5password.value = "CANCEL"; // a dummy string to force validation to succeed
         theForm.passwordConfirmation.value = theForm.Md5password.value;
         bypassValidateUF = true;
         return true;
     }

     function forceCancelTwo(theForm) {           // called when there are no password fields displayed
         bypassValidateUF = true;
         return true;
     }

     function validateUserFields(theForm) {
         if ( bypassValidateUF ) {
             return true;
         }
         if (theForm.Username.value.length == 0 ) {
             alert("Please enter a valid Email address.");
             theForm.Username.focus();
             return false;
         }
        if (theForm.FirstName.value.length == 0 ) {
             alert("Please enter a First Name.");
             theForm.FirstName.focus();
             return false;
         }
         if (theForm.LastName.value.length == 0 ) {
             alert("Please enter a Last Name.");
             theForm.LastName.focus();
             return false;
         }
		 else {    
             return true;
         }
     }

     function validatePw(theForm) {

         if ( !validateUserFields(theForm) ) {
                 return false;
         }
         if (theForm.Md5password.value.length == 0 ) {
             alert("Please enter a password.");
             theForm.Md5password.focus();
             return false;
         }
         if (theForm.Md5password.value != theForm.passwordConfirmation.value) {
             alert("The passwords do not match.");
             theForm.Md5password.focus();
             return false;
         }
         if (theForm.Md5password.value.length < 6 || theForm.Md5password.value.length > 12) {
             alert("Please enter a password between 6 and 12 characters long."); 
             theForm.Md5password.focus();
             return false;
         } 
 		 else {    
             return true;
         }
     }

    function confirmDelete() {
        bypassValidateUF = true;
        var msg="Are you SURE you want to delete this user? If in doubt, CANCEL."
        var answer = confirm(msg)
        if ( answer ) 
                return true;
        else
                bypassValidateUF = false;
                return false;
    }

-->
</script>
