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

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_HEIGHT;
import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadController.THUMBNAIL_WIDTH;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.StreamDescriptor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.ImageUploadHelper.NonNoisyImagingListener;

/**
 * This is not a unit test, so it is not named BlahBlahTest.
 * 
 * Instead, it's a test harness that creates thumbnails and writes them to
 * files, while also displaying them in a window on the screen. It takes human
 * intervention to evaluate.
 * 
 * This is especially true because the images on the screen look color-correct,
 * but when viewed in the browser, they might not be.
 */
public class ImageUploaderThumbnailerTester extends Frame {
	static {
		JAI.getDefaultInstance().setImagingListener(
				new NonNoisyImagingListener());
	}

	/** Big enough to hold the JPEG file, certainly. */
	private final static int BUFFER_SIZE = 200 * 200 * 4;

	private final static ImageCropData[] THUMBNAIL_DATA = new ImageCropData[] {
			new ImageCropData("/Users/jeb228/Pictures/JimBlake_20010915.jpg",
					50, 50, 115),
			new ImageCropData("/Users/jeb228/Pictures/brazil_collab.png", 600,
					250, 400),
			new ImageCropData("/Users/jeb228/Pictures/wheel.png", 0, 0, 195),
			new ImageCropData("/Users/jeb228/Pictures/DSC04203w-trans.gif",
					400, 1200, 800) };

	private final ImageUploadThumbnailer thumbnailer = new ImageUploadThumbnailer(
			THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH);

	@SuppressWarnings("deprecation")
	private ImageUploaderThumbnailerTester() {
		setTitle("Alpha Killer Test");
		addWindowListener(new CloseWindowListener());
		setLayout(createLayout());
		for (ImageCropData icd : THUMBNAIL_DATA) {
			try {
				InputStream mainStream = new FileInputStream(icd.filename);
				File thumbFile = writeToTempFile(thumbnailer.cropAndScale(
						mainStream, icd.crop));
				System.out.println(thumbFile.getAbsolutePath());

				MemoryCacheSeekableStream thumbFileStream = new MemoryCacheSeekableStream(
						new FileInputStream(thumbFile));
				RenderedOp thumbImage = StreamDescriptor.create(
						thumbFileStream, null, null);
				add(new javax.media.jai.widget.ImageCanvas(thumbImage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		pack();
		setVisible(true);
	}

	/**
	 * @param thumbStream
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File writeToTempFile(InputStream thumbStream) throws IOException,
			FileNotFoundException {
		File thumbFile = File.createTempFile("ImageUploaderThumbnailerTester",
				"");
		OutputStream imageOutputStream = new FileOutputStream(thumbFile);
		byte[] buffer = new byte[BUFFER_SIZE];
		int howMany = thumbStream.read(buffer);
		imageOutputStream.write(buffer, 0, howMany);
		imageOutputStream.close();
		return thumbFile;
	}

	private GridLayout createLayout() {
		GridLayout layout = new GridLayout(1, THUMBNAIL_DATA.length);
		layout.setHgap(10);
		return layout;
	}

	public static void main(String[] args) {
		Logger.getLogger(ImageUploadThumbnailer.class).setLevel(Level.DEBUG);
		new ImageUploaderThumbnailerTester();
	}

	private static class ImageCropData {
		final String filename;
		final ImageUploadController.CropRectangle crop;

		ImageCropData(String filename, int x, int y, int size) {
			this.filename = filename;
			this.crop = new ImageUploadController.CropRectangle(x, y, size,
					size);
		}
	}

	private class CloseWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			setVisible(false);
			dispose();
			System.exit(0);
		}
	}
}
