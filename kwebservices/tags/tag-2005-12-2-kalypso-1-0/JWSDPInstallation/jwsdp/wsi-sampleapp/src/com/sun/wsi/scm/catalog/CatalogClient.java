/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.xml.rpc.Stub;

import com.sun.wsi.scm.retailer.vendor.Configurations;
import com.sun.wsi.scm.retailer.vendor.Configuration;
import com.sun.wsi.scm.retailer.vendor.ServiceURL;

import com.sun.wsi.scm.catalog.holders.ProductDetailsTypeHolder;
import com.sun.wsi.scm.catalog.holders.ImageHolder;
import com.sun.wsi.scm.catalog.holders.SourceHolder;
import com.sun.wsi.scm.util.AttachmentHelper;
import com.sun.wsi.scm.util.HTMLReporter;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.wsi.scm.util.XMLWriter;
import com.sun.xml.rpc.client.StubBase;
import com.sun.xml.rpc.client.http.HttpClientTransportFactory;
import com.sun.xml.rpc.client.StubPropertyConstants;

public class CatalogClient implements WSIConstants {

	static Logger _logger = null;
	static String _className = null;
	static PropertyResourceBundle _rrb = null;
	static PropertyResourceBundle _crb = null;
	CatalogPortType _catalogStub = null;
	static String _vendor = null;

	final String imageDirName =
		System.getProperty("log.home")
			+ System.getProperty("file.separator")
			+ "images";

	final String specsheetDirName =
		System.getProperty("log.home")
			+ System.getProperty("file.separator")
			+ "specsheet";

	/**
	* Prepares the log files and initializes the XMLWriter
	*/
	public CatalogClient() {
		_logger = Logger.getLogger(LOGGER);

		_className = getClass().getName();

		try {
			// Prepare the stream for retailer client resource bundle
			FileInputStream fis =
				new FileInputStream(System.getProperty("retailer.props"));
			_rrb = new PropertyResourceBundle(fis);

			// Prepare the stream for catalog client resource bundle
			fis = new FileInputStream(System.getProperty("catalog.props"));
			_crb = new PropertyResourceBundle(fis);

			// Prepare the stream for output result
			PrintStream xmlLog =
				new PrintStream(
					new FileOutputStream(
						System.getProperty("log.home")
							+ System.getProperty("file.separator")
							+ System.getProperty("log.file")));
			XMLWriter.setOutputStream(xmlLog);

			// Read the endpoint properties file
			Properties props = new Properties();
			FileInputStream is =
				new FileInputStream(System.getProperty("endpoints.props"));
			props.load(is);

			_vendor = System.getProperty("endpoint");
			_logger.log(
				Level.CONFIG,
				_crb.getString("catalog.endpoint"),
				_vendor);

			String uri = props.getProperty(_vendor + ".catalog");
			_logger.log(
				Level.CONFIG,
				_crb.getString("catalog.endpoint.uri"),
				uri);

			CatalogService_Impl serviceImpl = new CatalogService_Impl();
			_catalogStub = serviceImpl.getCatalogPort();
			((Stub) _catalogStub)._setProperty(
				Stub.ENDPOINT_ADDRESS_PROPERTY,
				uri);
			FileOutputStream fos =
				new FileOutputStream(
					System.getProperty("log.home")
						+ System.getProperty("file.separator")
						+ System.getProperty("soap.msgs.file"));
			((StubBase) _catalogStub)._setTransportFactory(
				new HttpClientTransportFactory(fos));
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
			System.exit(1);
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		}
	}

	public static void main(String[] args) {
		CatalogClient client = new CatalogClient();

		XMLWriter.writeStartTag("catalog");
		HTMLReporter htmlReporter = HTMLReporter.getInstance();
		htmlReporter.logEnvironment();

		String methods = System.getProperty("catalog");

		if ((methods == null) || (methods.indexOf("thumbnail") != -1)) {
			try {
				client.getCatalogWithImages();
			} catch (Throwable t) {
				t.printStackTrace();
				_logger.log(Level.SEVERE, t.getMessage(), t);
			}
		}
		if ((methods == null) || (methods.indexOf("details") != -1)) {
			try {
				client.getProductDetails();
			} catch (Throwable t) {
				t.printStackTrace();
				_logger.log(Level.SEVERE, t.getMessage(), t);
			}
		}

		XMLWriter.writeEndTag("catalog");
		htmlReporter.prepareHTMLReport();
	}

	/**
	 * Invoking getCatalogWithImages
	 */
	private void getCatalogWithImages() {
		_logger.log(
			Level.INFO,
			_crb.getString("catalog.client.getCatalogWithImages"),
			_vendor);
		XMLWriter.writeStartTag("catalog-with-images");
		try {
			ProductCatalogType catalog =
				_catalogStub.getCatalogWithImages(
					new GetCatalogWithImagesType());

			// invoke the endpoints
			ProductType[] products = catalog.getProduct();

			// get the attachments from stub 
			Collection attachments =
				(Collection) ((Stub) _catalogStub)._getProperty(
					StubPropertyConstants.GET_ATTACHMENT_PROPERTY);
			if (attachments == null) {
				_logger.log(
					Level.SEVERE,
					_crb.getString(
						"catalog.client.getCatalogWithImages.notFound"));
				return;
			}

			_logger.log(
				Level.INFO,
				_crb.getString("catalog.client.getCatalogWithImages.found"),
				String.valueOf(attachments.size()));

			// make the directory for storing images
			makeDirectory(imageDirName);

			// keep all the attachments in a helper class
			// this enables easy retrieval of images by their content-id 
			AttachmentHelper attachmentHelper =
				new AttachmentHelper(attachments, _logger, _crb);

			for (int i = 0; i < products.length; i++) {
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.thProduct"),
					String.valueOf(i + 1));

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.productNumber"),
					String.valueOf(products[i].getProductNumber()));

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.name"),
					products[i].getName());

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.description"),
					products[i].getDescription());

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.category"),
					products[i].getCategory());

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.brand"),
					products[i].getBrand());

				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.client.catalog.price"),
					products[i].getPrice().setScale(
						2,
						BigDecimal.ROUND_HALF_UP));

				URI uri = products[i].getThumbnail();

				DataHandler dataHandler = null;
				if (uri != null) {
					int index = uri.toString().indexOf(":");
					String contentId =
						"<"
							+ uri.toString().substring(
								index + 1,
								uri.toString().length())
							+ ">";
					_logger.log(
						Level.FINE,
						_crb.getString("catalog.client.registry.search"),
						contentId);
					dataHandler = attachmentHelper.search(contentId);
				}

				// No image returned for 605010
				String imageFileName = null;
				if (dataHandler != null) {
					imageFileName =
						imageDirName
							+ System.getProperty("file.separator")
							+ String.valueOf(products[i].getProductNumber())
							+ "_small.jpg";
					FileOutputStream imageFile =
						new FileOutputStream(imageFileName);
					dataHandler.writeTo(imageFile);
					_logger.log(
						Level.CONFIG,
						_crb.getString(
							"catalog.client.getCatalogWithImages.thumbnail"),
						imageFileName);
				}

				// prepare the XML log 
				XMLWriter.writeStartTag("catalog-item");
				XMLWriter.writeContent(
					"number",
					String.valueOf(products[i].getProductNumber()));
				XMLWriter.writeContent("name", products[i].getName());
				XMLWriter.writeContent(
					"description",
					products[i].getDescription());
				XMLWriter.writeContent("category", products[i].getCategory());
				XMLWriter.writeContent("brand", products[i].getBrand());
				XMLWriter.writeContent(
					"price",
					products[i].getPrice().toString());
				if (imageFileName != null)
					XMLWriter.writeContent("thumbnail", imageFileName);
				XMLWriter.writeEndTag("catalog-item");
			}
			_logger.log(
				Level.INFO,
				_crb.getString("catalog.client.getCatalogWithImages.copied"),
				imageDirName);

		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		} finally {
			XMLWriter.writeEndTag("catalog-with-images");
		}
	}

	/**
	* Invoking getProductDetails for each product
	*/
	private void getProductDetails() {
		_logger.log(
			Level.INFO,
			_crb.getString("catalog.client.getProductDetails"),
			_vendor);

		XMLWriter.writeStartTag("catalog-with-details");
		try {
			// make the directory for storing images
			makeDirectory(imageDirName);

			// make the directory for storing specsheet
			makeDirectory(specsheetDirName);

			// get the product detais for each of the
			// product in the catalog
			for (int i = 605001; i <= 605009; i++) {
				GetProductDetailsType request = new GetProductDetailsType();
				ProductDetailsTypeHolder productDetailsHolder =
					new ProductDetailsTypeHolder();
				ImageHolder imageHolder = new ImageHolder();
				SourceHolder specsheetHolder = new SourceHolder();
				request.setProductNumber(i);

				// invoke the endpoint
				_catalogStub.getProductDetails(
						request,
						productDetailsHolder,
						imageHolder,
						specsheetHolder);

				// check for the response
				if ((productDetailsHolder == null)
					|| (productDetailsHolder.value == null)) {
					_logger.log(
						Level.SEVERE,
						_crb.getString(
							"catalog.client.getProductDetails.holder"));
					return;
				}
				ProductDetailsType productDetails = productDetailsHolder.value;

				// check for product detail image
				if (imageHolder.value == null) {
					_logger.log(
						Level.SEVERE,
						_crb.getString(
							"catalog.client.getProductDetails.noImage"),
						String.valueOf(i));
					return;
				}

				// write the image to a file stream
				String imageFileName =
					imageDirName
						+ System.getProperty("file.separator")
						+ String.valueOf(i)
						+ ".jpg";
				FileOutputStream imageFile =
					new FileOutputStream(imageFileName);
				DataHandler dh =
					new DataHandler(imageHolder.value, IMAGE_MIME_TYPE);
				dh.writeTo(imageFile);

				// check for product specsheet
				if (specsheetHolder.value == null) {
					_logger.log(
						Level.SEVERE,
						_crb.getString(
							"catalog.client.getProductDetails.noSpecsheet"),
						String.valueOf(i));
					return;
				}

				// write the spechsheet to a file stream
				String specsheetFileName =
					specsheetDirName
						+ System.getProperty("file.separator")
						+ String.valueOf(i)
						+ ".xml";
				FileOutputStream specsheetFile =
					new FileOutputStream(specsheetFileName);
				dh = new DataHandler(specsheetHolder.value, XML_MIME_TYPE);
				dh.writeTo(specsheetFile);
				specsheetFile.close();

				// display the product details
				_logger.log(
					Level.INFO,
					_rrb.getString("retailer.client.catalog.thProduct"),
					String.valueOf(i - 605000));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.number"),
					String.valueOf(i));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.weight"),
					String.valueOf(productDetails.getWeight()));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.weight.unit"),
					productDetails.getWeightUnit());

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.dimensions.width"),
					String.valueOf(productDetails.getDimensions().getWidth()));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.dimensions.height"),
					String.valueOf(productDetails.getDimensions().getHeight()));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.dimensions.depth"),
					String.valueOf(productDetails.getDimensions().getDepth()));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.dimensions.unit"),
					String.valueOf(productDetails.getDimensionsUnit()));

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.picture"),
					imageFileName);

				_logger.log(
					Level.CONFIG,
					_crb.getString("catalog.product.specsheet"),
					specsheetFileName);

				_logger.log(Level.CONFIG, "");

				// prepare the XML log
				XMLWriter.writeStartTag("catalog-item-detail");
				XMLWriter.writeContent("number", String.valueOf(i));
				XMLWriter.writeContent(
					"weight",
					String.valueOf(productDetails.getWeight()));
				XMLWriter.writeContent(
					"weight-unit",
					productDetails.getWeightUnit());
				XMLWriter.writeContent(
					"width",
					String.valueOf(productDetails.getDimensions().getWidth()));
				XMLWriter.writeContent(
					"height",
					String.valueOf(productDetails.getDimensions().getHeight()));
				XMLWriter.writeContent(
					"depth",
					String.valueOf(productDetails.getDimensions().getDepth()));
				XMLWriter.writeContent(
					"dimensions-unit",
					productDetails.getDimensionsUnit());
				XMLWriter.writeContent("picture", imageFileName);
				XMLWriter.writeContent("specsheet", specsheetFileName);
				XMLWriter.writeEndTag("catalog-item-detail");
			}
			_logger.log(
				Level.INFO,
				_crb.getString("catalog.client.getProductDetails.copied"),
				new String[] { imageDirName, specsheetDirName });

		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		} finally {
			XMLWriter.writeEndTag("catalog-with-details");
		}
	}

	private void makeDirectory(String dirName) throws IOException {
		File dir = new File(dirName);

		// check if the directory exists
		if (dir.exists() && !dir.isDirectory())
			throw new IOException(
				MessageFormat.format(
					_crb.getString("catalog.client.dir.notADir"),
					new String[] { dirName }));

		// check if the parent has write permission
		if (!dir.getParentFile().canWrite())
			throw new IOException(
				MessageFormat.format(
					_crb.getString("catalog.client.dir.cannotWrite"),
					new String[] { dir.getParentFile().toString()}));

		// make the directory
		dir.mkdir();
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.client.dir.created"),
			dirName);
	}

}
