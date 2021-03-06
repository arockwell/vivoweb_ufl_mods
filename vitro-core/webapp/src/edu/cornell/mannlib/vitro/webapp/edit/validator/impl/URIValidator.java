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

package edu.cornell.mannlib.vitro.webapp.edit.validator.impl;

import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class URIValidator {

	private WebappDaoFactory wadf = null;
	
	public URIValidator(WebappDaoFactory wadf) {
		this.wadf = wadf;
	}
	
	public ValidationObject validate (Object obj) throws IllegalArgumentException {
		ValidationObject vo = new ValidationObject();
		
		if (obj != null && obj instanceof String) {
			String errMsg = wadf.checkURI((String)obj);
			if (errMsg != null) {
				vo.setValid(false);
				vo.setMessage(errMsg);
			} else {
				vo.setValid(true);
			}
		} else {
			vo.setValid(false);
			vo.setMessage("Please enter a URI");
		}
		
		vo.setValidatedObject(obj);
		
		return vo;
	}
	
}
