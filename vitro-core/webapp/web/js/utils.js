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

var vitro;
if (!vitro) {
	vitro = {};
}


vitro.utils = {

	/* borrowMethods concept from "JavaScript: The Definitive Guide," 5th edition, by David Flanagan
	 * Copyright 2006 O'Reilly Media, Inc.
	 * ISBN 978-0-596-10199-2
	 */
	// Borrow methods from one class for use by another.
	// The arguments should be the constructor functions for the classes.
	// Methods of built-in types such as Object, Array, Date, and RegExp are
	// not enumerable and cannot be borrowed from with this method.
	borrowPrototypeMethods: function(borrowFrom, addTo) {
		borrowMethods(borrowFrom.prototype, addTo.prototype);
	},
	
	borrowMethods: function(borrowFrom, addTo) {
		for (m in borrowFrom) { // loop through all properties of the prototype
			if (typeof borrowFrom[m] != "function") { continue; } // ignore non-functions
			addTo[m] = borrowFrom[m];
		}
	}

};