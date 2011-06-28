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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.auth.AuthorizationHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ForwardResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

/**
 * Handle adding, replacing or deleting the main image on an Individual.
 */
public class ImageUploadController extends FreemarkerHttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(ImageUploadController.class);

	private static final String ATTRIBUTE_REFERRING_PAGE = "ImageUploadController.referringPage";

	private static final String DEFAULT_NAMESPACE = ConfigurationProperties
			.getProperty("Vitro.defaultNamespace");

	public static final String DUMMY_THUMBNAIL_PERSON_URL = "/images/placeholders/person.thumbnail.jpg"; 
    public static final String DUMMY_THUMBNAIL_NON_PERSON_URL = "/images/placeholders/non.person.thumbnail.jpg"; 
    
	/** Limit file size to 6 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 6 * 1024 * 1024;

	/** Generated thumbnails will be this big. */
	public static final int THUMBNAIL_HEIGHT = 200;
	public static final int THUMBNAIL_WIDTH = 200;

	/** The form field that tells what we are doing: uploading? deleting? */
	public static final String PARAMETER_ACTION = "action";

	/** The form field that identifies the Individual. */
	public static final String PARAMETER_ENTITY_URI = "entityUri";

	/** The form field of the uploaded file; use as a key to the FileItem map. */
	public static final String PARAMETER_UPLOADED_FILE = "datafile";

	/** Here is the main image file. Hold on to it. */
	public static final String ACTION_UPLOAD = "upload";

	/** Here is the cropping info; we're ready to save the image. */
	public static final String ACTION_SAVE = "save";

	/** A request to delete the file and return to the referring page. */
	public static final String ACTION_DELETE = "delete";

	/** A request to delete the file and return to the "new image" screen. */
	public static final String ACTION_DELETE_EDIT = "deleteEdit";

	public static final String BODY_TITLE = "title";
	public static final String BODY_ENTITY_NAME = "entityName";
	public static final String BODY_MAIN_IMAGE_URL = "imageUrl";
	public static final String BODY_MAIN_IMAGE_HEIGHT = "imageHeight";
	public static final String BODY_MAIN_IMAGE_WIDTH = "imageWidth";
	public static final String BODY_THUMBNAIL_URL = "thumbnailUrl";
	public static final String BODY_CANCEL_URL = "cancelUrl";
	public static final String BODY_DELETE_URL = "deleteUrl";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";

	public static final String TEMPLATE_NEW = "imageUpload-newImage.ftl";
	public static final String TEMPLATE_REPLACE = "imageUpload-replaceImage.ftl";
	public static final String TEMPLATE_CROP = "imageUpload-cropImage.ftl";
	public static final String TEMPLATE_ERROR = "error-standard.ftl";

	private static final String URL_HERE = UrlBuilder.getUrl("/uploadImages");

	private FileStorage fileStorage;

	/**
	 * When initialized, get a reference to the File Storage system. Without
	 * that, we can do nothing.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		Object o = getServletContext().getAttribute(
				FileStorageSetup.ATTRIBUTE_NAME);
		if (o instanceof FileStorage) {
			fileStorage = (FileStorage) o;
		} else if (o == null) {
			throw new UnavailableException(this.getClass().getSimpleName()
					+ " could not initialize. Attribute '"
					+ FileStorageSetup.ATTRIBUTE_NAME
					+ "' was not set in the servlet context.");
		} else {
			throw new UnavailableException(this.getClass().getSimpleName()
					+ " could not initialize. Attribute '"
					+ FileStorageSetup.ATTRIBUTE_NAME
					+ "' in the servlet context contained an instance of '"
					+ o.getClass().getName() + "' instead of '"
					+ FileStorage.class.getName() + "'");
		}
	}

	/**
	 * <p>
	 * Parse the multi-part request, process the request, and produce the
	 * output.
	 * </p>
	 * <p>
	 * If the request was a multi-part file upload, it will parse to a
	 * normal-looking request with a "file_item_map" attribute.
	 * </p>
	 * <p>
	 * The processing will produce a {@link ResponseValues} object, which
	 * represents either a request for a FreeMarker template or a forwarding
	 * operation.
	 * <ul>
	 * <li>If a FreeMarker template, we emulate the actions that
	 * FreeMarkerHttpServlet would have taken to produce the output.</li>
	 * <li>If a forwarding operation, we create a {@link RequestDispatcher} to
	 * do the forwarding.</li>
	 * </ul>
	 * </p>
	 */

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		try {
			// Parse the multi-part request.
			FileUploadServletRequest request = FileUploadServletRequest
					.parseRequest(vreq, MAXIMUM_FILE_SIZE);
			if (log.isTraceEnabled()) {
				dumpRequestDetails(vreq);
			}

			// If they aren't authorized to do this, send them to login.
			if (!checkAuthorized(vreq)) {
				String loginPage = request.getContextPath() + Controllers.LOGIN;
				return new RedirectResponseValues(loginPage);
			}

			return buildTheResponse(vreq);

		} catch (Exception e) {
			// log.error("Could not produce response page", e);
			return new ExceptionResponseValues(e);
		}
	}

	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the intro screen.
	 */
	private ResponseValues buildTheResponse(VitroRequest vreq) {
		String action = vreq.getParameter(PARAMETER_ACTION);

		try {
			Individual entity = validateEntityUri(vreq);
			if (ACTION_UPLOAD.equals(action)) {
				return doUploadImage(vreq, entity);
			} else if (ACTION_SAVE.equals(action)) {
				return doCreateThumbnail(vreq, entity);
			} else if (ACTION_DELETE.equals(action)) {
				captureReferringUrl(vreq);
				return doDeleteImage(vreq, entity);
			} else if (ACTION_DELETE_EDIT.equals(action)) {
				return doDeleteThenEdit(vreq, entity);
			} else {
				captureReferringUrl(vreq);
				return doIntroScreen(vreq, entity);
			}
		} catch (UserMistakeException e) {
			// Can't find the entity? Complain.
			return showAddImagePageWithError(vreq, null, e.getMessage());
		} catch (Exception e) {
			// We weren't expecting this - log it, and apologize to the user.
			return new ExceptionResponseValues(e);
		}
	}

	/**
	 * We are just starting the upload process. Record where we came from, so if
	 * they hit "cancel" we know where to send them. If we have problems, just
	 * clear it.
	 */
	private void captureReferringUrl(VitroRequest vreq) {
		String referrer = vreq.getHeader("Referer");
		if (referrer == null) {
			vreq.getSession().removeAttribute(ATTRIBUTE_REFERRING_PAGE);
		} else {
			vreq.getSession().setAttribute(ATTRIBUTE_REFERRING_PAGE, referrer);
		}
	}

	/**
	 * Show the first screen in the upload process: Add or Replace.
	 */
	private ResponseValues doIntroScreen(VitroRequest vreq, Individual entity) {
		ImageInfo imageInfo = ImageInfo.instanceFromEntityUri(
				vreq.getFullWebappDaoFactory(), entity);
		if (imageInfo == null) {
			return showAddImagePage(vreq, entity);
		} else {
			return showReplaceImagePage(vreq, entity, imageInfo);
		}
	}

	/**
	 * The user has selected their main image file. Remove any previous main
	 * image (and thumbnail), and attach the new main image.
	 */
	private ResponseValues doUploadImage(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getFullWebappDaoFactory());

		try {
			// Did they provide a file to upload? If not, show an error.
			FileItem fileItem = helper.validateImageFromRequest(vreq);

			// Put it in the file system, and store a reference in the session.
			FileInfo fileInfo = helper.storeNewImage(fileItem, vreq);

			// How big is the new image? If not big enough, show an error.
			Dimensions size = helper.getNewImageSize(fileInfo);

			// Go to the cropping page.
			return showCropImagePage(vreq, entity,
					fileInfo.getBytestreamAliasUrl(), size);
		} catch (UserMistakeException e) {
			return showErrorMessage(vreq, entity, e.getMessage());
		}
	}

	/**
	 * Are we writing the error message to the "Add" page or to the "Replace"
	 * page?
	 */
	private ResponseValues showErrorMessage(VitroRequest vreq,
			Individual entity, String message) {
		ImageInfo imageInfo = ImageInfo.instanceFromEntityUri(
				vreq.getFullWebappDaoFactory(), entity);
		if (imageInfo == null) {
			return showAddImagePageWithError(vreq, entity, message);
		} else {
			return showReplaceImagePageWithError(vreq, entity, imageInfo,
					message);
		}
	}

	/**
	 * The user has specified how to crop the thumbnail. Crop it and attach it
	 * to the main image.
	 */
	private ResponseValues doCreateThumbnail(VitroRequest vreq,
			Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getFullWebappDaoFactory());

		try {
			CropRectangle crop = validateCropCoordinates(vreq);
			FileInfo newImage = helper.getNewImageInfo(vreq);
			FileInfo thumbnail = helper.generateThumbnail(crop, newImage);

			helper.removeExistingImage(entity);
			helper.storeImageFiles(entity, newImage, thumbnail);

			return showExitPage(vreq, entity);
		} catch (UserMistakeException e) {
			return showErrorMessage(vreq, entity, e.getMessage());
		}
	}

	/**
	 * Delete the main image and the thumbnail, and go back to the referring
	 * page.
	 */
	private ResponseValues doDeleteImage(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getFullWebappDaoFactory());

		helper.removeExistingImage(entity);

		return showExitPage(vreq, entity);
	}

	/**
	 * Delete the main image and the thumbnail, and go to the "add image"
	 * screen.
	 */
	private ResponseValues doDeleteThenEdit(VitroRequest vreq, Individual entity) {
		ImageUploadHelper helper = new ImageUploadHelper(fileStorage,
				vreq.getFullWebappDaoFactory());

		helper.removeExistingImage(entity);

		return showAddImagePage(vreq, entity);
	}

	/**
	 * We need to be talking about an actual Individual here.
	 */
	private Individual validateEntityUri(VitroRequest vreq)
			throws UserMistakeException {
		String entityUri = vreq.getParameter(PARAMETER_ENTITY_URI);
		if (entityUri == null) {
			throw new UserMistakeException("No entity URI was provided");
		}

		Individual entity = vreq.getFullWebappDaoFactory().getIndividualDao()
				.getIndividualByURI(entityUri);
		if (entity == null) {
			throw new UserMistakeException(
					"This URI is not recognized as belonging to anyone: '"
							+ entityUri + "'");
		}
		return entity;
	}

	/**
	 * Did we get the cropping coordinates?
	 */
	private CropRectangle validateCropCoordinates(VitroRequest vreq) {
		int x = getIntegerParameter(vreq, "x", 0);
		int y = getIntegerParameter(vreq, "y", 0);
		int h = getIntegerParameter(vreq, "h", THUMBNAIL_HEIGHT);
		int w = getIntegerParameter(vreq, "w", THUMBNAIL_WIDTH);
		return new CropRectangle(x, y, h, w);
	}

	/**
	 * We need this parameter on the request, and it must be a valid integer.
	 */
	private int getIntegerParameter(HttpServletRequest req, String key,
			int defaultValue) {
		String string = req.getParameter(key);
		if ((string == null) || (string.isEmpty())) {
			log.debug("No value for '" + key + "'; using default value = "
					+ defaultValue);
			return defaultValue;
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			log.warn("Value for '" + key + "' was not a valid integer: '"
					+ string + "'; using default value = " + defaultValue);
			return defaultValue;
		}
	}

	/**
	 * The individual has no image - go to the Add Image page.
	 * 
	 * @param entity
	 *            if this is null, then all URLs lead to the welcome page.
	 */
	private TemplateResponseValues showAddImagePage(VitroRequest vreq,
			Individual entity) {
		String formAction = (entity == null) ? "" : formAction(entity.getURI(),
				ACTION_UPLOAD);
		String cancelUrl = (entity == null) ? "" : exitPageUrl(vreq,
				entity.getURI());

		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_NEW);
		
		// rjy7 We should not be referencing particular ontology values here, and ideally
		// the template would add the placeholder url to the edit link, since it already
		// knows which placeholder it's using. However, this requires a significantly more
		// complex implementation, so keeping it simple for now.
		String dummyThumbnailUrl = entity.isVClass("http://xmlns.com/foaf/0.1/Person") ?
		        DUMMY_THUMBNAIL_PERSON_URL : DUMMY_THUMBNAIL_NON_PERSON_URL;
		rv.put(BODY_THUMBNAIL_URL, UrlBuilder.getUrl(dummyThumbnailUrl));
		rv.put(BODY_FORM_ACTION, formAction);
		rv.put(BODY_CANCEL_URL, cancelUrl);
		rv.put(BODY_TITLE, "Upload image" + forName(entity));
		return rv;
	}

	/**
	 * The individual has no image, but the user did something wrong.
	 */
	private TemplateResponseValues showAddImagePageWithError(VitroRequest vreq,
			Individual entity, String message) {
		return showAddImagePage(vreq, entity).put(BODY_ERROR_MESSAGE, message);
	}

	/**
	 * The individual has an image - go to the Replace Image page.
	 */
	private TemplateResponseValues showReplaceImagePage(VitroRequest vreq,
			Individual entity, ImageInfo imageInfo) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_REPLACE);
		rv.put(BODY_THUMBNAIL_URL, UrlBuilder.getUrl(imageInfo.getThumbnail()
				.getBytestreamAliasUrl()));
		rv.put(BODY_DELETE_URL, formAction(entity.getURI(), ACTION_DELETE_EDIT));
		rv.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_UPLOAD));
		rv.put(BODY_CANCEL_URL, exitPageUrl(vreq, entity.getURI()));
		rv.put(BODY_TITLE, "Replace image" + forName(entity));
		return rv;
	}

	/**
	 * The individual has an image, but the user did something wrong.
	 */
	private TemplateResponseValues showReplaceImagePageWithError(
			VitroRequest vreq, Individual entity, ImageInfo imageInfo,
			String message) {
		TemplateResponseValues rv = showReplaceImagePage(vreq, entity,
				imageInfo);
		rv.put(BODY_ERROR_MESSAGE, message);
		return rv;
	}

	/**
	 * We got their main image - go to the Crop Image page.
	 */
	private TemplateResponseValues showCropImagePage(VitroRequest vreq,
			Individual entity, String imageUrl, Dimensions dimensions) {
		TemplateResponseValues rv = new TemplateResponseValues(TEMPLATE_CROP);
		rv.put(BODY_MAIN_IMAGE_URL, UrlBuilder.getUrl(imageUrl));
		rv.put(BODY_MAIN_IMAGE_HEIGHT, dimensions.height);
		rv.put(BODY_MAIN_IMAGE_WIDTH, dimensions.width);
		rv.put(BODY_FORM_ACTION, formAction(entity.getURI(), ACTION_SAVE));
		rv.put(BODY_CANCEL_URL, exitPageUrl(vreq, entity.getURI()));
		rv.put(BODY_TITLE, "Crop Photo" + forName(entity));
		return rv;
	}

	/**
	 * All done - go to the individual display page.
	 */
	private ForwardResponseValues showExitPage(VitroRequest vreq,
			Individual entity) {
		return new ForwardResponseValues(exitPageUrl(vreq, entity.getURI()));
	}

	/**
	 * When we complete the process, by success or by cancellation, go to the
	 * initial referring page. If there wasn't one, go to the individual display
	 * page,
	 */
	private String exitPageUrl(VitroRequest vreq, String entityUri) {
		String referrer = (String) vreq.getSession().getAttribute(
				ATTRIBUTE_REFERRING_PAGE);
		if (referrer != null) {
			return referrer;
		}

		if (DEFAULT_NAMESPACE == null) {
			return "";
		} else if (!entityUri.startsWith(DEFAULT_NAMESPACE)) {
			return "";
		} else {
			String tail = entityUri.substring(DEFAULT_NAMESPACE.length());
			if (!tail.startsWith("/")) {
				tail = "/" + tail;
			}
			return "display" + tail;
		}
	}

	/**
	 * The "action" parameter on the HTML "form" tag should include the path
	 * back to this controller, along with the desired action and the Entity
	 * URI.
	 */
	private String formAction(String entityUri, String action) {
		UrlBuilder.ParamMap params = new UrlBuilder.ParamMap(
				PARAMETER_ENTITY_URI, entityUri, PARAMETER_ACTION, action);
		return UrlBuilder.getPath(URL_HERE, params);
	}

	/**
	 * Format the entity's name for display as part of the page title.
	 */
	private String forName(Individual entity) {
		if (entity != null) {
			String name = entity.getName();
			if (name != null) {
				return " for " + name;
			}
		}
		return "";
	}

	/**
	 * Holds an error message to use as a complaint to the user.
	 */
	static class UserMistakeException extends Exception {
		UserMistakeException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the coordinates that we use to crop the main image.
	 */
	static class CropRectangle {
		final int x;
		final int y;
		final int height;
		final int width;

		CropRectangle(int x, int y, int height, int width) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}

		/** Produce a new crop rectangle that compensates for scaling. */
		public CropRectangle unscale(float scale) {
			int newX = (int) (x / scale);
			int newY = (int) (y / scale);
			int newHeight = (int) (height / scale);
			int newWidth = (int) (width / scale);
			return new CropRectangle(newX, newY, newHeight, newWidth);
		}

		@Override
		public String toString() {
			return "CropRectangle[x=" + x + ", y=" + y + ", w=" + width
					+ ", h=" + height + "]";
		}

	}

	/**
	 * For debugging, dump all sorts of information about the request.
	 * 
	 * WARNING: if "req" represents a Multi-part request which has not yet been
	 * parsed, then reading these parameters will consume them.
	 */
	@SuppressWarnings("unchecked")
	private void dumpRequestDetails(HttpServletRequest req) {
		log.trace("Request is " + req.getClass().getName());

		Map<String, String[]> parms = req.getParameterMap();
		for (Entry<String, String[]> entry : parms.entrySet()) {
			log.trace("Parameter '" + entry.getKey() + "'="
					+ Arrays.deepToString(entry.getValue()));
		}

		Enumeration<String> attrs = req.getAttributeNames();
		while (attrs.hasMoreElements()) {
			String key = attrs.nextElement();
			String valueString = String.valueOf(req.getAttribute(key));
			String valueOneLine = valueString.replace("\n", " | ");
			log.trace("Attribute '" + key + "'=" + valueOneLine);
		}
	}

	static class Dimensions {
		final int width;
		final int height;

		Dimensions(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return "Dimensions[width=" + width + ", height=" + height + "]";
		}
	}

	/**
	 * If they are logged in as an Editor or better, they can do whatever they
	 * want.
	 * 
	 * Otherwise, they will need to be self-editing, and will need to have
	 * authorization for this specific operation they are requesting.
	 */
	private boolean checkAuthorized(VitroRequest vreq)
			throws UserMistakeException {
		if (LoginStatusBean.getBean(vreq).isLoggedInAtLeast(
				LoginStatusBean.EDITOR)) {
			log.debug("Authorized because logged in as Editor");
			return true;
		}

		if (!VitroRequestPrep.isSelfEditing(vreq)) {
			log.debug("Not Authorized because not self-editing");
			return false;
		}

		String action = vreq.getParameter(PARAMETER_ACTION);
		Individual entity = validateEntityUri(vreq);
		String imageUri = entity.getMainImageUri();

		// What are we trying to do? Check if authorized.
		RequestedAction ra;
		if (ACTION_DELETE.equals(action) || ACTION_DELETE_EDIT.equals(action)) {
			ra = new DropObjectPropStmt(entity.getURI(),
					VitroVocabulary.IND_MAIN_IMAGE, imageUri);
		} else if (imageUri != null) {
			ra = new EditObjPropStmt(entity.getURI(),
					VitroVocabulary.IND_MAIN_IMAGE, imageUri);
		} else {
			ra = new AddDataPropStmt(entity.getURI(),
					VitroVocabulary.IND_MAIN_IMAGE,
					RequestActionConstants.SOME_LITERAL, null, null);
		}

		AuthorizationHelper helper = new AuthorizationHelper(vreq);
		boolean authorized = helper.isAuthorizedForRequestedAction(ra);
		log.debug((authorized ? "" : "Not ") + "Authorized for '" + action
				+ "' as self-editor;  requested action = " + ra);
		return authorized;
	}

}
