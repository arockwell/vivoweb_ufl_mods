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

package edu.cornell.mannlib.vedit.validator;

/**
 * Output from a {@link Validator}. Holds the value that was tested, whether it
 * was valid or not, and an optional message.
 */
public class ValidationObject {
	/**
	 * Create an instance that indicates successful validation.
	 */
	public static ValidationObject success(Object validatedObject) {
		ValidationObject vo = new ValidationObject();
		vo.setValid(true);
		vo.setMessage("");
		vo.setValidatedObject(validatedObject);
		return vo;
	}

	/**
	 * Create an instance that indicates failed validation.
	 */
	public static ValidationObject failure(Object validatedObject,
			String message) {
		ValidationObject vo = new ValidationObject();
		vo.setValid(false);
		vo.setMessage(message);
		vo.setValidatedObject(validatedObject);
		return vo;
	}

    private boolean valid = false;
    private String message;
    private Object validatedObject = null;

    public boolean getValid(){
        return valid;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Object getValidatedObject(){
        return validatedObject;
    }

    public void setValidatedObject(Object validatedObject){
        this.validatedObject = validatedObject;
    }


}
