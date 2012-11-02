/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.mapserver.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.RGB;
import org.geotools.referencing.CRS;
import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.mapserver.utils.exceptions.MapServerException;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.tools.GMLConstants;
import org.mapserver.mapserver.BooleanEnum;
import org.mapserver.mapserver.Class;
import org.mapserver.mapserver.ExpressionType;
import org.mapserver.mapserver.ItemType;
import org.mapserver.mapserver.ItemType.Item;
import org.mapserver.mapserver.Label;
import org.mapserver.mapserver.Layer;
import org.mapserver.mapserver.Legend;
import org.mapserver.mapserver.Map;
import org.mapserver.mapserver.ObjectFactory;
import org.mapserver.mapserver.OutputFormat;
import org.mapserver.mapserver.PositionEnum;
import org.mapserver.mapserver.RgbColorType;
import org.mapserver.mapserver.SizeType;
import org.mapserver.mapserver.StateEnum;
import org.mapserver.mapserver.Style;
import org.mapserver.mapserver.Web;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class provides utility functions for dealing with map files.
 *
 * @author Holger Albert
 */
public class MapFileUtilities
{
  private static final URL XSL_URL = MapFileUtilities.class.getResource( "/etc/mapfile/mapfile.xsl" ); //$NON-NLS-1$

  /**
   * The object factory.
   */
  public static final ObjectFactory OF = new ObjectFactory();

  /**
   * The JAXB context.
   */
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  /**
   * The constructor.
   */
  private MapFileUtilities( )
  {
  }

  /**
   * This function loads a map file from XML.
   *
   * @param inputStream
   *          The input stream.
   * @return The contents of the map file.
   */
  public static Map loadFromXML( final InputStream inputStream ) throws JAXBException, SAXException, ParserConfigurationException, IOException
  {
    /* Create the unmarshaller. */
    final Unmarshaller unmarshaller = JC.createUnmarshaller();

    /* Get the sax parser factory. */
    final SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware( true );
    spf.setXIncludeAware( true );

    /* Get the xml reader. */
    final XMLReader xr = spf.newSAXParser().getXMLReader();
    xr.setContentHandler( unmarshaller.getUnmarshallerHandler() );
    xr.parse( new InputSource( inputStream ) );

    return (Map) unmarshaller.getUnmarshallerHandler().getResult();
  }

  /**
   * This function saves a map file as XML.
   *
   * @param map
   *          The contents of the map file.
   * @param outputStream
   *          The output stream.
   * @param encoding
   *          The encoding.
   */
  public static void saveAsXML( final Map map, final OutputStream outputStream, final String encoding ) throws JAXBException
  {
    /* Create the marshaller. */
    final Marshaller marshaller = JC.createMarshaller();
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.setProperty( Marshaller.JAXB_ENCODING, encoding );
    marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new MapFileNamespacePrefixMapper() ); //$NON-NLS-1$
    marshaller.marshal( map, outputStream );
  }

  /**
   * This function saves a map file in ASCII.
   *
   * @param map
   *          The contents of the map file.
   * @param outputStream
   *          The output stream.
   * @param encoding
   *          The encoding.
   */
  public static void saveInASCII( final Map map, final OutputStream outputStream, final String encoding ) throws ParserConfigurationException, JAXBException, SAXException, IOException, TransformerException
  {

    /* the input stream. */
    InputStream inputStream = null;

    try
    {
      /* Create the document builder factory. */
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware( true );

      /* Create the document builder. */
      final DocumentBuilder builder = factory.newDocumentBuilder();

      /* Create a XML document. */
      final Document xmlDOM = builder.newDocument();

      /* Create the marshaller. */
      final Marshaller marshaller = JC.createMarshaller();
      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      marshaller.setProperty( Marshaller.JAXB_ENCODING, encoding );
      marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new MapFileNamespacePrefixMapper() ); //$NON-NLS-1$

      /* Marshal the contents of the map file into the XML document. */
      marshaller.marshal( map, xmlDOM );

      /* Load the XLS document. */
      inputStream = XSL_URL.openStream();
      final Document xslDOM = builder.parse( inputStream );

      /* Create the transformer factory. */
      final TransformerFactory transformerFactory = KalypsoCommonsPlugin.getDefault().getTransformerFactory();

      /* Create the transformer using the XSL document. */
      final Transformer transformer = transformerFactory.newTransformer( new DOMSource( xslDOM ) );

      /* Transform the XML document document and write it to the output stream. */
      transformer.transform( new DOMSource( xmlDOM ), new StreamResult( outputStream ) );
    }
    finally
    {
      /* Close the input stream. */
      IOUtils.closeQuietly( inputStream );
    }
  }

  /**
   * This function creates the contents of the map file without any layers.
   *
   * @param wmsURL
   * @param mapFile
   * @param width
   * @param height
   * @param mapName
   * @param envelope
   * @param sourceCRS
   * @param otherCRSs
   * @return The contents of the map file.
   */
  public static Map createMap( final String wmsURL, final File mapFile, final int width, final int height, final String mapName, final GM_Envelope envelope, final String sourceCRS, final String[] otherCRSs ) throws NoSuchAuthorityCodeException, FactoryException
  {
    /* Create the root element of the XML map file. */
    final Map map = OF.createMap();
    map.setName( mapName );
    map.setStatus( StateEnum.ON );

    /* Create the size element. */
    if( width > 0 && height > 0 )
    {
      final SizeType size = OF.createSizeType();
      size.setX( BigInteger.valueOf( width ) );
      size.setY( BigInteger.valueOf( height ) );
      map.setSize( size );
    }

    /* Fill the extent element. */
    map.getExtent().add( envelope.getMinX() );
    map.getExtent().add( envelope.getMinY() );
    map.getExtent().add( envelope.getMaxX() );
    map.getExtent().add( envelope.getMaxY() );

    /* Detrermine the units. */
    final CoordinateReferenceSystem referenceSystem = CRS.decode( sourceCRS );
    final CoordinateSystem coordinateSystem = referenceSystem.getCoordinateSystem();
    final CoordinateSystemAxis axis = coordinateSystem.getAxis( 0 );
    final Unit< ? > unit = axis.getUnit();
    String units = null;
    if( SI.KILOMETER.equals( unit ) )
      units = "kilometers"; //$NON-NLS-1$
    else if( SI.METER.equals( unit ) )
      units = "meters"; //$NON-NLS-1$
    else if( NonSI.DEGREE_ANGLE.equals( unit ) )
      units = "dd"; //$NON-NLS-1$

    if( units != null && units.length() > 0 )
      map.setUnits( units );

    /* Create the rgb color element. */
    // RgbColorType rgbColor = OF.createRgbColorType();
    // rgbColor.setRed( 255 );
    // rgbColor.setGreen( 255 );
    // rgbColor.setBlue( 255 );
    // map.setImageColor( rgbColor );

    /* Create the output format element. */
    final OutputFormat outputFormat = OF.createOutputFormat();
    outputFormat.setName( "PNG" ); //$NON-NLS-1$
    outputFormat.setDriver( "AGG/PNG" ); //$NON-NLS-1$
    outputFormat.setMimeType( "image/png" ); //$NON-NLS-1$
    outputFormat.setImageMode( "RGBA" ); //$NON-NLS-1$
    outputFormat.setExtension( "png" ); //$NON-NLS-1$
    map.getOutputFormat().add( outputFormat );

    /* Fill the projection element. */
    map.getProjection().add( "init=" + sourceCRS ); //$NON-NLS-1$

    /* Create the item element. */
    final String resource = String.format( "%s?map=%s&", wmsURL, mapFile.getAbsolutePath().replace( "\\", "/" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final ItemType item = OF.createItemType();
    item.getItem().add( createItem( "wms_title", mapName ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_onlineresource", resource ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_srs", String.format( "%s %s", sourceCRS, StringUtils.join( otherCRSs, " " ) ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    item.getItem().add( createItem( "wms_feature_info_mime_type", "text/html" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    /* Create the map element. */
    final Web web = OF.createWeb();
    web.setMetadata( item );

    /* Fill the web element. */
    map.getWeb().add( web );

    /* Create a label element. */
    final Label label = OF.createLabel();
    label.setAntialias( BooleanEnum.TRUE );
    label.setType( "bitmap" ); //$NON-NLS-1$
    label.setSize( "medium" ); //$NON-NLS-1$
    label.setPosition( "AUTO" ); //$NON-NLS-1$
    label.setPartials( BooleanEnum.FALSE );

    /* Create the outline color element. */
    final RgbColorType outlineColor = OF.createRgbColorType();
    outlineColor.setRed( 0 );
    outlineColor.setGreen( 0 );
    outlineColor.setBlue( 0 );

    /* Create the legend element. */
    final Legend legend = OF.createLegend();
    legend.setStatus( "on" ); //$NON-NLS-1$
    legend.setPosition( PositionEnum.LR );
    legend.setLabel( label );
    legend.setOutlineColor( outlineColor );
    map.setLegend( legend );

    return map;
  }

  /**
   * This function creates the contents of the item.
   *
   * @param name
   * @param value
   * @return The contents of the item.
   */
  public static Item createItem( final String name, final String value )
  {
    final Item item = OF.createItemTypeItem();
    item.setName( name );
    item.setValue( value );

    return item;
  }

  /**
   * This function creates the contents of the layer.
   *
   * @param wmsURL
   * @param mapFile
   * @param layerName
   *          Used for naming the layer and the wms layer.
   * @paramd data
   * @param geoemtryType
   * @param envelope
   * @param sourceCRS
   * @param otherCRSs
   * @return The contents of the layer.
   */
  public static Layer createLayerForShape( final String wmsURL, final File mapFile, final String layerName, final String data, final QName geoemtryType, final GM_Envelope envelope, final String sourceCRS, final String[] otherCRSs ) throws MapServerException
  {
    String shapeType = "POLYGON"; //$NON-NLS-1$
    if( GM_Point.POINT_ELEMENT.equals( geoemtryType ) || GM_MultiPoint.MULTI_POINT_ELEMENT.equals( geoemtryType ) )
      shapeType = "POINT"; //$NON-NLS-1$
    else if( GM_Curve.CURVE_ELEMENT.equals( geoemtryType ) || GM_MultiCurve.MULTI_CURVE_ELEMENT.equals( geoemtryType ) )
      shapeType = "LINE"; //$NON-NLS-1$
    else if( GM_Polygon.POLYGON_ELEMENT.equals( geoemtryType ) || GMLConstants.QN_MULTI_POLYGON.equals( geoemtryType ) )
      shapeType = "POLYGON"; //$NON-NLS-1$

    /* Create the layer element. */
    final Layer layer = OF.createLayer();
    layer.setName( layerName );
    layer.setType( shapeType );
    layer.setStatus( "ON" ); //$NON-NLS-1$
    layer.setData( data );
    layer.setTemplate( "getfeatureinfo.html" ); //$NON-NLS-1$

    /* Fill the projection element. */
    layer.getProjection().add( "init=" + sourceCRS ); //$NON-NLS-1$

    /* Create the item element. */
    final String resource = String.format( "%s?map=%s&", wmsURL, mapFile.getAbsolutePath().replace( "\\", "/" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final ItemType item = OF.createItemType();
    item.getItem().add( createItem( "wms_title", layerName ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_onlineresource", resource ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_srs", String.format( "%s %s", sourceCRS, StringUtils.join( otherCRSs, " " ) ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    item.getItem().add( createItem( "wms_extent", String.format( Locale.PRC, "%f %f %f %f", envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    layer.setMetadata( item );

    /* Create the class element. */
    final Class clazz = createClass( "Standard", null, null, new RGB( 0, 0, 0 ) ); //$NON-NLS-1$

    /* Fill the class element. */
    layer.getClazz().add( clazz );

    return layer;
  }

  /**
   * This function creates the contents of the layer without any classes.
   *
   * @param wmsURL
   * @param mapFile
   * @param layerName
   *          Used for naming the layer and the wms layer.
   * @param opacity
   * @param data
   * @param bigMin
   * @param bigMax
   * @param envelope
   * @param sourceCRS
   * @param otherCRSs
   * @return The contents of the layer.
   */
  public static Layer createLayerForRaster( final String wmsURL, final File mapFile, final String layerName, final String opacity, final String data, final BigDecimal bigMin, final BigDecimal bigMax, final GM_Envelope envelope, final String sourceCRS, final String[] otherCRSs )
  {
    /* Create the layer element. */
    final Layer layer = OF.createLayer();
    layer.setName( layerName );
    layer.setType( "RASTER" ); //$NON-NLS-1$
    layer.setStatus( "ON" ); //$NON-NLS-1$
    layer.setOpacity( opacity );
    layer.setData( data );
    layer.setTemplate( "getfeatureinfo.html" ); //$NON-NLS-1$

    /* Fill the processing element. */
    double min = -Double.MAX_VALUE;
    if( bigMin != null )
      min = bigMin.doubleValue();

    double max = Double.MAX_VALUE;
    if( bigMax != null )
      max = bigMax.doubleValue();

    double buckets = Math.floor( (max - min) / 0.05 );
    if( buckets < 2.0 )
      buckets = 2.0;

    final List<String> processing = layer.getProcessing();
    processing.add( String.format( Locale.PRC, "SCALE=%.2f %.2f", min, max ) ); //$NON-NLS-1$
    processing.add( String.format( Locale.PRC, "SCALE_BUCKETS=%d", (int) buckets ) ); //$NON-NLS-1$
    processing.add( "RESAMPLE=BILINEAR" ); //$NON-NLS-1$

    /* Fill the projection element. */
    layer.getProjection().add( "init=" + sourceCRS ); //$NON-NLS-1$

    /* Create the item element. */
    final String resource = String.format( "%s?map=%s&", wmsURL, mapFile.getAbsolutePath().replace( "\\", "/" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final ItemType item = OF.createItemType();
    item.getItem().add( createItem( "wms_title", layerName ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_onlineresource", resource ) ); //$NON-NLS-1$
    item.getItem().add( createItem( "wms_srs", String.format( "%s %s", sourceCRS, StringUtils.join( otherCRSs, " " ) ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    item.getItem().add( createItem( "wms_extent", String.format( Locale.PRC, "%f %f %f %f", envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    layer.setMetadata( item );

    return layer;
  }

  /**
   * This function creates the content of the class.
   *
   * @param label
   * @param expressionValue
   *          Used to evaluate specific criteria. May be null.
   * @param expressionType
   *          Must be set, if a expressionValue is set. Otherwise it may be null.
   * @param rgb
   *          The rgb colors.
   * @return The contents of the class.
   */
  public static Class createClass( final String label, final String expressionValue, final String expressionType, final RGB rgb ) throws MapServerException
  {
    /* Create the class element. */
    final Class clazz = OF.createClass();
    clazz.setName( label );

    /* Create the expression element. */
    if( expressionValue != null && expressionValue.length() > 0 )
    {
      if( expressionType == null || expressionType.length() == 0 )
        throw new MapServerException( "Expression value without expression type set..." ); //$NON-NLS-1$

      final ExpressionType expression = OF.createExpressionType();
      expression.setValue( expressionValue );
      expression.setType( expressionType );
      clazz.setExpression( expression );
    }

    /* Create the rgb color element. */
    final RgbColorType rgbColor = OF.createRgbColorType();
    rgbColor.setRed( rgb.red );
    rgbColor.setGreen( rgb.green );
    rgbColor.setBlue( rgb.blue );

    /* Create the style element. */
    final Style style = OF.createStyle();
    style.setColor( rgbColor );
    clazz.getStyle().add( style );

    return clazz;
  }
}