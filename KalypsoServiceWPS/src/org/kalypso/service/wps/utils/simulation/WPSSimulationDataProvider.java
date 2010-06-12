/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.service.wps.utils.simulation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.LiteralValueType;

import org.apache.commons.io.FileUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * A DataProvider for simulations running in the WPS service.
 * 
 * @author Holger Albert
 */
public class WPSSimulationDataProvider implements ISimulationDataProvider
{
  public static final String TYPE_GML = "text/gml"; //$NON-NLS-1$

  /**
   * Contains the id of the inputs as key and the input itself as value.
   */
  private final Map<String, Object> m_inputList;

  /**
   * The constructor.
   * 
   * @param execute
   *          The execute request.
   */
  public WPSSimulationDataProvider( final Map<String, Object> inputList )
  {
    m_inputList = inputList;
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    /*
     * The input data will not be deleted, this is a concern of the client, because only he could have write access to
     * this place.
     */
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#getInputForID(java.lang.String)
   */
  @Override
  public Object getInputForID( final String id ) throws SimulationException
  {
    final Object input = m_inputList.get( id );
    if( input == null )
      throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.0", id ), null ); //$NON-NLS-1$

    if( input instanceof ComplexValueType )
      return parseComplexValue( (ComplexValueType) input );
    else if( input instanceof LiteralValueType )
    {
      final LiteralValueType literalValue = (LiteralValueType) input;
      final String dataType = literalValue.getDataType();
      final String value = literalValue.getValue();

      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final QName type = new QName( NS.XSD_SCHEMA, dataType );
      final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );
      try
      {
        return handler.parseType( value );
      }
      catch( final ParseException e )
      {
        throw new SimulationException( "Could not parse " + value + " as an object of type " + type, e ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    else if( input instanceof ComplexValueReference )
    {
      final ComplexValueReference complexValueReference = (ComplexValueReference) input;
      final String reference = complexValueReference.getReference();

      try
      {
        final URL url = new URL( reference );
        return url;
      }
      catch( final MalformedURLException e )
      {
        try
        {
          final URI uri = new URI( reference );
          return uri;
        }
        catch( final URISyntaxException e2 )
        {
          throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.1" ), e2 ); //$NON-NLS-1$
        }
      }
    }
    else
    {
      /* BoundingBoxType. */
      final BoundingBoxType boundingBox = (BoundingBoxType) input;
      return boundingBox;
    }
  }

  public static Object parseComplexValue( final ComplexValueType complexValue ) throws SimulationException
  {
    final String mimeType = complexValue.getFormat();
    final List<Object> content = complexValue.getContent();
    if( content.size() == 0 )
      return null;

    final Object firstContent = content.get( 0 );
    final String textContent;
    if( firstContent instanceof String )
    {
      textContent = (String) firstContent;
    }
    else if( firstContent instanceof Element )
    {
      final DOMSource domSource = new DOMSource( (Element) firstContent );
      final TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer;
      try
      {
        transformer = factory.newTransformer();
        final StringWriter writer = new StringWriter();
        transformer.transform( domSource, new StreamResult( writer ) );
        textContent = writer.toString();
      }
      catch( final Exception e )
      {
        throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.2" ), e ); //$NON-NLS-1$
      }
    }
    else
    {
      throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.2" ) ); //$NON-NLS-1$
    }

    // distinguish by mime type, default to binary
    if( TYPE_GML.equals( mimeType ) )
    {
      final StringReader reader = new StringReader( textContent );
      final InputSource inputSource = new InputSource( reader );

      try
      {
        final String schema = complexValue.getSchema();
        if( schema == null || schema.isEmpty())
        {
          return GmlSerializer.createGMLWorkspace( inputSource, null, null, null );
        }

        final URL schemaLocationHint = new URL( schema );
        return GmlSerializer.createGMLWorkspace( inputSource, schemaLocationHint, null, null );
      }
      catch( final Exception e )
      {
        throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.2" ), e ); //$NON-NLS-1$
      }
    }
    else
    {
      try
      {
        // parse as hexBinary
        // TODO: why not base64 encoded byte[]?
        final byte[] bytes = DatatypeConverter.parseHexBinary( textContent );

        final File file = FileUtilities.createNewUniqueFile( "complexValue_", FileUtilities.TMP_DIR ); //$NON-NLS-1$
        FileUtils.writeByteArrayToFile( file, bytes );

        return file.toURI().toURL();
      }
      catch( final IOException e )
      {
        throw new SimulationException( Messages.getString( "org.kalypso.service.wps.utils.simulation.WPSSimulationDataProvider.3" ), e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * @see org.kalypso.simulation.core.ISimulationDataProvider#hasID(java.lang.String)
   */
  @Override
  public boolean hasID( final String id )
  {
    return m_inputList.containsKey( id );
  }
}