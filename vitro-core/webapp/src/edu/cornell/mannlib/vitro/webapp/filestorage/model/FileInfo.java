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

package edu.cornell.mannlib.vitro.webapp.filestorage.model;

/**
 * An immutable packet of information about an uploaded file, with a builder
 * class to permit incremental construction.
 */
public class FileInfo {
	private final String uri;
	private final String filename;
	private final String mimeType;
	private final String bytestreamUri;
	private final String bytestreamAliasUrl;

	private FileInfo(Builder builder) {
		this.uri = builder.uri;
		this.filename = builder.filename;
		this.mimeType = builder.mimeType;
		this.bytestreamUri = builder.bytestreamUri;
		this.bytestreamAliasUrl = builder.bytestreamAliasUrl;
	}

	public String getUri() {
		return uri;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getBytestreamUri() {
		return bytestreamUri;
	}

	public String getBytestreamAliasUrl() {
		return bytestreamAliasUrl;
	}

	@Override
	public String toString() {
		return "FileInfo[uri=" + uri + ", filename=" + filename + ", mimeType="
				+ mimeType + ", bytestreamUri=" + bytestreamUri + ", aliasUrl="
				+ bytestreamAliasUrl + "]";
	}

	/**
	 * A builder class allows us to supply the values one at a time, and then
	 * freeze them into an immutable object.
	 */
	public static class Builder {
		private String uri;
		private String filename;
		private String mimeType;
		private String bytestreamUri;
		private String bytestreamAliasUrl;

		public Builder setUri(String uri) {
			this.uri = uri;
			return this;
		}

		public Builder setFilename(String filename) {
			this.filename = filename;
			return this;
		}

		public Builder setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		public Builder setBytestreamUri(String bytestreamUri) {
			this.bytestreamUri = bytestreamUri;
			return this;
		}

		public Builder setBytestreamAliasUrl(String bytestreamAliasUrl) {
			this.bytestreamAliasUrl = bytestreamAliasUrl;
			return this;
		}

		public FileInfo build() {
			return new FileInfo(this);
		}
	}
}
