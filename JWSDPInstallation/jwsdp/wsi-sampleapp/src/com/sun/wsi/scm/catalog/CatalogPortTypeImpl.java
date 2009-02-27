/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;

import com.sun.wsi.scm.util.AttachmentConverter;
import com.sun.wsi.scm.util.Localizer;
import com.sun.wsi.scm.util.WSIConstants;
import com.sun.xml.rpc.server.ServerPropertyConstants;
import com.sun.wsi.scm.catalog.holders.ImageHolder;
import com.sun.wsi.scm.catalog.holders.SourceHolder;
import com.sun.wsi.scm.catalog.holders.ProductDetailsTypeHolder;

public class CatalogPortTypeImpl
	implements WSIConstants, CatalogPortType, ServiceLifecycle {
	String _className = getClass().getName();

	Logger _logger = null;
	PropertyResourceBundle _rrb = null;
	PropertyResourceBundle _crb = null;

	ServletEndpointContext _servletEndpointContext = null;
	ServletContext _servletContext = null;

	AttachmentConverter _attachmentConverter = null;

	public void init(Object context) {
		_servletEndpointContext = (ServletEndpointContext) context;
		_servletContext = _servletEndpointContext.getServletContext();

		_logger = Logger.getLogger(LOGGER);
		_logger.entering(_className, INIT);

		try {
			// Prepare the stream for retailer resource bundle
			InputStream is =
				_servletContext.getResourceAsStream(RETAILER_RESOURCES);
			_rrb = new PropertyResourceBundle(is);

			// Prepare the stream for retailer resource bundle
			is =
				_servletContext.getResourceAsStream(
					Localizer.localizedBundleName(
						CONF + "/com/sun/wsi/scm/resources/catalog.server"));
			_crb = new PropertyResourceBundle(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		_attachmentConverter = new AttachmentConverter(_servletContext);

		_logger.exiting(_className, INIT);
	}

	public void destroy() {
	}

	public ProductCatalogType getCatalogWithImages(GetCatalogWithImagesType request) {

		_logger.entering(_className, GET_CATALOG);

		SOAPMessageContext soapMessageContext =
			(SOAPMessageContext) _servletEndpointContext.getMessageContext();
		SOAPMessage soapMessage = soapMessageContext.getMessage();

		ProductCatalogType catalog = new ProductCatalogType();

		ProductType[] products = new ProductType[10];
		Vector attachments = new Vector();

		try {
			for (int i = 0; i < 10; i++) {
				products[i] = new ProductType();
				products[i].setProductNumber(
					Integer.parseInt(
						_rrb.getString(
							"retailer.catalogItem." + i + ".productNumber")));
				products[i].setName(
					_rrb.getString("retailer.catalogItem." + i + ".name"));
				products[i].setDescription(
					_rrb.getString(
						"retailer.catalogItem." + i + ".description"));
				products[i].setBrand(
					_rrb.getString("retailer.catalogItem." + i + ".brand"));
				products[i].setCategory(
					_rrb.getString("retailer.catalogItem." + i + ".category"));
				products[i].setPrice(
					(new BigDecimal(_rrb
						.getString("retailer.catalogItem." + i + ".price"))
						.setScale(2, BigDecimal.ROUND_HALF_UP)));

				// No image for prouct 605010
				String streamName = "";
				if (i != 9) {
					// set the URI of the swaRef content
					products[i].setThumbnail(
						new URI("cid:catalog" + i + "@jaxrpc.sun.com"));

					streamName =
						"/images/"
							+ _rrb.getString(
								"retailer.catalogItem." + i + ".productNumber")
							+ "_small.jpg";

					// create a new attachment part and add to SOAP message 
					AttachmentPart attachmentPart =
						soapMessage.createAttachmentPart();
					String cid = products[i].getThumbnail().toString();
					attachmentPart.setContentId(
						"<catalog" + i + "@jaxrpc.sun.com>");
					attachmentPart.setContent(
						_attachmentConverter.streamToImage(streamName),
						IMAGE_MIME_TYPE);
					attachments.add(attachmentPart);
				}

				_logger.log(
					Level.INFO,
					_rrb.getString("retailer.catalogItem.thProduct"),
					String.valueOf(i + 1));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.productNumber"),
					String.valueOf(products[i].getProductNumber()));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.name"),
					String.valueOf(products[i].getName()));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.description"),
					String.valueOf(products[i].getDescription()));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.brand"),
					String.valueOf(products[i].getBrand()));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.category"),
					String.valueOf(products[i].getCategory()));
				_logger.log(
					Level.CONFIG,
					_rrb.getString("retailer.catalogItem.price"),
					products[i].getPrice());
				_logger.log(
					Level.INFO,
					_crb.getString("catalog.item.image"),
					products[i].getThumbnail());
				_logger.log(Level.CONFIG, "");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			_logger.log(Level.SEVERE, t.getMessage(), t);
		}
		soapMessageContext.setProperty(
			ServerPropertyConstants.SET_ATTACHMENT_PROPERTY,
			attachments);
		catalog.setProduct(products);

		_logger.exiting(_className, GET_CATALOG);

		return catalog;
	}

	public void getProductDetails(
		GetProductDetailsType request,
		ProductDetailsTypeHolder productDetailsHolder,
		ImageHolder imageHolder,
		SourceHolder sourceHolder) {
		_logger.log(
			Level.INFO,
			_crb.getString("catalog.product.details"),
			String.valueOf(request.getProductNumber()));

		ProductDetailsType productDetails = new ProductDetailsType();
		productDetails.setWeight(
			Integer.parseInt(
				_crb.getString(
					"catalog.product."
						+ request.getProductNumber()
						+ ".weight")));
		productDetails.setWeightUnit(
			_crb.getString(
				"catalog.product."
					+ request.getProductNumber()
					+ ".weight.unit"));
		DimensionsType dimensions = new DimensionsType();
		dimensions.setHeight(
			Integer.parseInt(
				_crb.getString(
					"catalog.product."
						+ String.valueOf(request.getProductNumber())
						+ ".dimensions.height")));
		dimensions.setWidth(
			Integer.parseInt(
				_crb.getString(
					"catalog.product."
						+ String.valueOf(request.getProductNumber())
						+ ".dimensions.width")));
		dimensions.setDepth(
			Integer.parseInt(
				_crb.getString(
					"catalog.product."
						+ String.valueOf(request.getProductNumber())
						+ ".dimensions.depth")));
		productDetails.setDimensions(dimensions);
		productDetails.setDimensionsUnit(
			_crb.getString(
				"catalog.product."
					+ request.getProductNumber()
					+ ".dimensions.unit"));

		String imageStream = "";
		String sourceStream = "";
		try {
			// read the image stream
			imageStream =
				"/images/"
					+ _crb.getString(
						"catalog.product."
							+ request.getProductNumber()
							+ ".productNumber")
					+ ".jpg";
			imageHolder.value = _attachmentConverter.streamToImage(imageStream);

			// read the source stream
			sourceStream =
				"/specsheet/"
					+ _crb.getString(
						"catalog.product."
							+ request.getProductNumber()
							+ ".productNumber")
					+ ".xml";
			sourceHolder.value = _attachmentConverter.streamToXML(sourceStream);
		} catch (IOException ex) {
			ex.printStackTrace();
			_logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		
		if (productDetailsHolder == null) {
			_logger.log(Level.WARNING, _crb.getString("catalog.product.details.holder"));
			productDetailsHolder = new ProductDetailsTypeHolder();
		}

		productDetailsHolder.value = productDetails;

		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.weight"),
			String.valueOf(productDetails.getWeight()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.weight.unit"),
			String.valueOf(productDetails.getWeightUnit()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.dimensions.height"),
			String.valueOf(productDetails.getDimensions().getHeight()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.dimensions.width"),
			String.valueOf(productDetails.getDimensions().getWidth()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.dimensions.depth"),
			String.valueOf(productDetails.getDimensions().getDepth()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.dimensions.unit"),
			String.valueOf(productDetails.getDimensionsUnit()));
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.picture.stream"),
			imageStream);
		_logger.log(
			Level.CONFIG,
			_crb.getString("catalog.product.details.specsheet.stream"),
			sourceStream);

		return;
	}
}
