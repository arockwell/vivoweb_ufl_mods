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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Adjust any individual that has a main image but no thumbnail.
 */
public class NoThumbsAdjuster extends FsuScanner {
	private ImageDirectoryWithBackup imageDirectoryWithBackup;

	public NoThumbsAdjuster(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
	}

	/**
	 * For every individual with main images but no thumbnails, create a
	 * thumbnail from the first main image.
	 */
	public void adjust() {
		updateLog.section("Creating thumbnails to match main images.");

		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				imageProperty)) {
			if (resource.getProperty(thumbProperty) == null) {
				createThumbnailFromMainImage(resource);
			}
		}
	}

	/**
	 * This individual has a main image but no thumbnail. Create one.
	 * <ul>
	 * <li>Figure a name for the thumbnail image.</li>
	 * <li>Make a scaled copy of the main image into the thumbnail.</li>
	 * <li>Set that file as a thumbnail (old-style) on the individual.</li>
	 * </ul>
	 */
	private void createThumbnailFromMainImage(Resource resource) {
		String mainFilename = getValues(resource, imageProperty).get(0);
		String thumbFilename = addFilenamePrefix("_thumbnail_", mainFilename);
		updateLog.log(resource, "creating a thumbnail at '" + thumbFilename
				+ "' from the main image at '" + mainFilename + "'");

		File mainFile = imageDirectoryWithBackup.getExistingFile(mainFilename);
		File thumbFile = imageDirectoryWithBackup.getNewfile(thumbFilename);
		thumbFile = checkNameConflicts(thumbFile);
		try {
			generateThumbnailImage(mainFile, thumbFile,
					FileStorageUpdater.THUMBNAIL_WIDTH,
					FileStorageUpdater.THUMBNAIL_HEIGHT);
			ResourceWrapper.addProperty(resource, thumbProperty, thumbFilename);
		} catch (IOException e) {
			updateLog.error(resource, "failed to create thumbnail file '"
					+ thumbFilename + "'", e);
		}
	}

	/**
	 * Read in the main image, and scale it to a thumbnail that maintains the
	 * aspect ratio, but doesn't exceed either of these dimensions.
	 */
	private void generateThumbnailImage(File mainFile, File thumbFile,
			int maxWidth, int maxHeight) throws IOException {
		BufferedImage bsrc = ImageIO.read(mainFile);

		double scale = Math.min(((double) maxWidth) / bsrc.getWidth(),
				((double) maxHeight) / bsrc.getHeight());
		AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
		int newWidth = (int) (scale * bsrc.getWidth());
		int newHeight = (int) (scale * bsrc.getHeight());
		updateLog.log("Scaling '" + mainFile + "' by a factor of " + scale
				+ ", from " + bsrc.getWidth() + "x" + bsrc.getHeight() + " to "
				+ newWidth + "x" + newHeight);

		BufferedImage bdest = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();

		g.drawRenderedImage(bsrc, at);

		ImageIO.write(bdest, "JPG", thumbFile);
	}

}
