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
import java.util.Collection;

/**
 * Clean out the old image directory. Copy the files into the upgrade directory,
 * separating into the ones that we translated, and the ones that weren't
 * referenced.
 */
public class ImageDirectoryCleaner extends FsuScanner {
	private final ImageDirectoryWithBackup imageDirectoryWithBackup;
	protected final File translatedDirectory;
	protected final File unreferencedDirectory;

	public ImageDirectoryCleaner(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();

		this.translatedDirectory = controller.getTranslatedDirectory();
		this.unreferencedDirectory = controller.getUnreferencedDirectory();
	}

	/**
	 * Remove all of the files from the old image directory.
	 */
	public void clean(Collection<String> translatedFiles) {
		updateLog.section("Cleaning the old image directory of "
				+ "files that were translated.");
		removeTranslatedFiles(translatedFiles);

		updateLog.section("Cleaning the old image directory of "
				+ "files that were not referenced.");
		removeRemainingFiles(imageDirectoryWithBackup
				.getPrimaryImageDirectory());
	}

	/**
	 * Move all of the files that we translated into the new system.
	 */
	private void removeTranslatedFiles(Collection<String> translatedFiles) {
		for (String path : translatedFiles) {
			File oldFile = new File(
					imageDirectoryWithBackup.getPrimaryImageDirectory(), path);
			if (oldFile.exists()) {
				updateLog.log("moving image file '" + path
						+ "' to the 'translated' directory.");
				File deletedFile = new File(translatedDirectory, path);
				try {
					FileUtil.moveFile(oldFile, deletedFile);
				} catch (IOException e) {
					updateLog.error("Failed to move translated file '"
							+ oldFile.getAbsolutePath() + "'");
				}
			} else {
				updateLog.log("Not moving image file '" + path
						+ "' to the 'translated' directory -- "
						+ "found it in the backup directory.");
			}
		}
	}

	/**
	 * Go through the images directory, and discard any that remain. They must
	 * not have been referenced by any existing individuals.
	 */
	private void removeRemainingFiles(File directory) {
		updateLog.log("Cleaning image directory '" + directory + "'");
		try {
			File targetDirectory = makeCorrespondingDirectory(directory);
			File[] children = directory.listFiles();
			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						removeRemainingFiles(child);
					} else {
						moveUnreferencedFile(targetDirectory, child);
					}
				}
			}
		} catch (IOException e) {
			updateLog.error(
					"Failed to clean images directory '"
							+ directory.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * Move this file from its current location to its new home in the
	 * "unreferenced" directory. Log it.
	 */
	private void moveUnreferencedFile(File targetDirectory, File file) {
		updateLog.log("Moving image file '" + file.getPath()
				+ "' to the 'unreferenced' directory");
		try {
			File newFile = new File(targetDirectory, file.getName());
			FileUtil.moveFile(file, newFile);
		} catch (IOException e) {
			updateLog.error(
					"Can't move unreferenced file '" + file.getAbsolutePath()
							+ "'", e);
		}
	}

	/**
	 * Figure out the path from the "images" directory to this one, and create a
	 * corresponding directory in the "unreferenced" area.
	 */
	private File makeCorrespondingDirectory(File directory) throws IOException {
		String imagesPath = imageDirectoryWithBackup.getPrimaryImageDirectory()
				.getAbsolutePath();
		String thisPath = directory.getAbsolutePath();

		if (!thisPath.startsWith(imagesPath)) {
			throw new IOException("Can't make a corresponding directory for '"
					+ thisPath + "'");
		}

		String suffix = thisPath.substring(imagesPath.length());

		File corresponding = new File(unreferencedDirectory, suffix);
		corresponding.mkdirs();
		if (!corresponding.exists()) {
			throw new IOException("Failed to create corresponding directory '"
					+ corresponding.getAbsolutePath() + "'");
		}

		return corresponding;
	}
}
