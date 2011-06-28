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

package edu.cornell.mannlib.vitro.webapp.filestorage.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * An immutable packet of information about an iamge file and its thumbnail.
 */
public class ImageInfo {
	private static final Log log = LogFactory.getLog(ImageInfo.class);
	
	public static final ImageInfo EMPTY_IMAGE_INFO = emptyInstance();
	
	// ----------------------------------------------------------------------
	// static Factory methods.
	// ----------------------------------------------------------------------

	/**
	 * This is a place holder for an Individual with no image.
	 */
	private static ImageInfo emptyInstance() {
		FileInfo emptyFileInfo = new FileInfo.Builder().build();
		return new ImageInfo(emptyFileInfo, emptyFileInfo);
	}
	
	/**
	 * If this Individual has a main image and a thumbnail, return their info.
	 * Otherwise, return null.
	 */
	public static ImageInfo instanceFromEntityUri(
			WebappDaoFactory webappDaoFactory, Individual entity) {
		if (webappDaoFactory == null) {
			throw new NullPointerException("webappDaoFactory may not be null.");
		}

		if (entity == null) {
			return null;
		}

		IndividualDao individualDao = webappDaoFactory.getIndividualDao();

		String mainImageUri = entity.getMainImageUri();
		if (mainImageUri == null) {
			log.debug("Entity '" + entity.getURI()
					+ "' had no associated main image.");
			return null;
		} else {
			log.debug("Entity '" + entity.getURI()
					+ "' had associated main image: '" + mainImageUri + "'");
		}

		Individual mainFile = individualDao.getIndividualByURI(mainImageUri);
		if (mainFile == null) {
			log.error("Entity '" + entity.getURI()
					+ "' has a main image URI that does not refer to an "
					+ "individual: mainImageURI=" + mainImageUri);
			return null;
		}

		Individual thumbFile = mainFile
				.getRelatedIndividual(VitroVocabulary.FS_THUMBNAIL_IMAGE);
		if (thumbFile == null) {
			log.warn("Main image file '" + mainImageUri
					+ "' had no associated thumbnail.");
			return null;
		} else {
			log.debug("Main image file '" + mainImageUri
					+ "' had associated thumbnail: '" + thumbFile.getURI()
					+ "'");
		}

		FileInfo mainInfo = FileInfo.instanceFromSurrogateUri(webappDaoFactory,
				mainFile.getURI());
		if (mainInfo == null) {
			log.error("Entity '" + entity.getURI()
					+ "' has a mainImage that is not a File "
					+ "surrogate: mainImageURI=" + mainImageUri);
			return null;
		}

		FileInfo thumbInfo = FileInfo.instanceFromSurrogateUri(
				webappDaoFactory, thumbFile.getURI());
		if (thumbInfo == null) {
			log.error("Entity '" + entity.getURI()
					+ "' has a mainImage with a thumbnail that is not a File "
					+ "surrogate: mainImageURI=" + mainImageUri
					+ ", thumbnailURI=" + thumbFile.getURI());
			return null;
		}

		return new ImageInfo(mainInfo, thumbInfo);
	}

	// ----------------------------------------------------------------------
	// instance fields and methods.
	// ----------------------------------------------------------------------

	private final FileInfo mainImage;
	private final FileInfo thumbnail;

	public ImageInfo(FileInfo mainImage, FileInfo thumbnail) {
		if (mainImage == null) {
			throw new NullPointerException("mainImage may not be null.");
		}
		if (thumbnail == null) {
			throw new NullPointerException("thumbnail may not be null.");
		}
		this.mainImage = mainImage;
		this.thumbnail = thumbnail;
	}

	public FileInfo getMainImage() {
		return mainImage;
	}

	public FileInfo getThumbnail() {
		return thumbnail;
	}

}
