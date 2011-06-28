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

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Adjust any individual that has a thumbnail with no main image.
 */
public class AllThumbsAdjuster extends FsuScanner {
	private ImageDirectoryWithBackup imageDirectoryWithBackup;

	public AllThumbsAdjuster(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
	}

	/**
	 * For every individual with thumbnails but no main images, create a main
	 * image from the first thumbnail.
	 */
	public void adjust() {
		updateLog.section("Creating main images for thumbnails "
				+ "that have none.");

		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				thumbProperty)) {
			if (ResourceWrapper.getProperty(resource, imageProperty) == null) {
				createMainImageFromThumbnail(resource);
			}
		}
	}

	/**
	 * This individual has a thumbnail but no main image. Create one.
	 * <ul>
	 * <li>Figure a name for the main image.</li>
	 * <li>Copy the thumbnail image file into the main image file.</li>
	 * <li>Set that file as an image (old-style) on the individual.</li>
	 * </ul>
	 */
	private void createMainImageFromThumbnail(Resource resource) {
		String thumbFilename = getValues(resource, thumbProperty).get(0);
		String mainFilename = addFilenamePrefix("_main_image_", thumbFilename);
		updateLog.log(resource, "creating a main file at '" + mainFilename
				+ "' to match the thumbnail at '" + thumbFilename + "'");

		try {
			File thumbFile = imageDirectoryWithBackup
					.getExistingFile(thumbFilename);
			File mainFile = imageDirectoryWithBackup.getNewfile(mainFilename);
			mainFile = checkNameConflicts(mainFile);
			FileUtil.copyFile(thumbFile, mainFile);
			ResourceWrapper.addProperty(resource, imageProperty, mainFilename);
		} catch (IOException e) {
			updateLog.error(resource, "failed to create main file '"
					+ mainFilename + "'", e);
		}
	}
}
