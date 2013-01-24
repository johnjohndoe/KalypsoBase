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
package org.kalypso.ogc.sensor.zml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.commons.bind.NamespacePrefixMap;
import org.kalypso.commons.factory.FactoryException;
import org.kalypso.commons.parser.impl.DateParser;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.AxisType;
import org.kalypso.zml.AxisType.ValueArray;
import org.kalypso.zml.MetadataListType;
import org.kalypso.zml.MetadataType;
import org.kalypso.zml.ObjectFactory;
import org.kalypso.zml.Observation;

/**
 * @author Gernot Belger
 */
class ObservationMarshaller
{
  private static Logger LOG = Logger.getLogger( ObservationMarshaller.class.getName() );

  /**
   * FIXME: Use {@link javax.xml.bind.DatatypeConverter#printDate(java.util.Calendar)} instead.
   */
  private static final String XML_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";//$NON-NLS-1$

  static final NamespacePrefixMap ZML_PREFIX_MAPPER = new NamespacePrefixMap( "zml.kalypso.org" );
  static
  {
    ZML_PREFIX_MAPPER.addMapping( "filters.zml.kalypso.org", "filters" );
  }

  private static final Comparator<MetadataType> METADATA_COMPERATOR = new Comparator<MetadataType>()
  {
    @Override
    public int compare( final MetadataType o1, final MetadataType o2 )
    {
      return o1.getName().compareTo( o2.getName() );
    }
  };

  private final ObjectFactory m_factory = new ObjectFactory();

  /** Timezone with that all dates are written. */
  private final TimeZone m_timezone = KalypsoCorePlugin.getDefault().getTimeZone();

  private final IObservation m_input;

  private final IRequest m_request;

  public ObservationMarshaller( final IObservation input, final IRequest request )
  {
    m_input = input;
    m_request = request;
  }

  public void marshall( final Writer writer ) throws SensorException
  {
    try
    {
      final Observation xml = createXML();
      final Marshaller marshaller = getMarshaller();
      marshaller.marshal( xml, writer );
    }
    catch( final JAXBException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
    catch( final FactoryException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
  }

  public void marshall( final OutputStream os ) throws SensorException
  {
    try
    {
      final Observation xml = createXML();
      final Marshaller marshaller = getMarshaller();
      marshaller.marshal( xml, os );
    }
    catch( final JAXBException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
    catch( final FactoryException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
  }

  public void marshall( final File file ) throws SensorException
  {
    OutputStream outs = null;
    try
    {
      outs = new BufferedOutputStream( new FileOutputStream( file ) );

      marshall( outs );

      outs.close();
    }
    catch( final IOException e )
    {
      LOG.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.30" ), e ); //$NON-NLS-1$

      throw new SensorException( e );
    }
    finally
    {
      IOUtils.closeQuietly( outs );
    }
  }

  public String asString( )
  {
    try
    {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      marshall( bos );
      bos.close();
      return new String( bos.toByteArray(), "UTF-8" );//$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return e.toString();
    }
  }

  private static Marshaller getMarshaller( ) throws JAXBException
  {
    return JaxbUtilities.createMarshaller( ZmlFactory.JC, true, null, ZML_PREFIX_MAPPER );
  }

  /**
   * Create an XML-Observation ready for marshalling.
   * 
   * @param timezone
   *          the time zone into which dates should be converted before serialised
   */
  private Observation createXML( ) throws FactoryException
  {
    try
    {
      // first of all fetch values
      final ITupleModel values = m_input.getValues( m_request );

      final MetadataList obsMetadataList = m_input.getMetadataList();

      final Observation obsType = m_factory.createObservation();

      obsType.setName( m_input.getName() );
      final String metaName = obsMetadataList.getProperty( IMetadataConstants.MD_NAME, null );
      if( metaName != null )
        obsType.setName( metaName );

      final MetadataListType metadataListType = createMetadata( obsMetadataList );
      obsType.setMetadataList( metadataListType );

      // sort axes, this is not needed from a XML view, but very useful when comparing marshaled files (e.g.
      // JUnit-Test)
      final List<AxisType> axisList = obsType.getAxis();
      final Comparator< ? super IAxis> axisComparator = new MarshallerAxisComparator();
      final SortedSet<IAxis> sortedAxis = new TreeSet<IAxis>( axisComparator );
      sortedAxis.addAll( Arrays.asList( m_input.getAxes() ) );

      for( final IAxis axis : sortedAxis )
      {
        if( axis.isPersistable() )
        {
          final AxisType axisType = buildAxis( values, axis );

          axisList.add( axisType );
        }
      }

      return obsType;
    }
    catch( final Exception e )
    {
      throw new FactoryException( e );
    }
  }

  private AxisType buildAxis( final ITupleModel values, final IAxis axis ) throws SensorException
  {
    final AxisType axisType = m_factory.createAxisType();

    final String xsdType = ZmlParserFactory.getXSDTypeFor( axis.getDataClass().getName() );

    axisType.setDatatype( xsdType );
    axisType.setName( axis.getName() );
    axisType.setUnit( axis.getUnit() );
    axisType.setType( axis.getType() );
    axisType.setKey( axis.isKey() );

    final ValueArray valueArrayType = m_factory.createAxisTypeValueArray();

    valueArrayType.setSeparator( ";" ); //$NON-NLS-1$
    valueArrayType.setValue( buildValueString( values, axis ) );

    axisType.setValueArray( valueArrayType );
    return axisType;
  }

  private MetadataListType createMetadata( final MetadataList obsMetadataList )
  {
    final MetadataListType metadataListType = m_factory.createMetadataListType();
    final List<MetadataType> metadataList = metadataListType.getMetadata();
    for( final Entry<Object, Object> entry : obsMetadataList.entrySet() )
    {
      final String mdKey = (String) entry.getKey();
      final String mdValue = (String) entry.getValue();

      final MetadataType mdType = m_factory.createMetadataType();
      mdType.setName( mdKey );

      // TRICKY: if this looks like an xml-string then pack it
      // into a CDATA section and use the 'data'-Element instead
      if( mdValue.startsWith( XMLUtilities.XML_HEADER_BEGIN ) )
        mdType.setData( XMLUtilities.encapsulateInCDATA( mdValue ) );
      else
        mdType.setValue( mdValue );

      metadataList.add( mdType );
    }

    Collections.sort( metadataList, METADATA_COMPERATOR );

    // write time zone info into meta data
    final MetadataType mdType = m_factory.createMetadataType();
    mdType.setName( ITimeseriesConstants.MD_TIMEZONE );
    mdType.setValue( m_timezone.getID() );
    // Check, if value already exists and remove first
    for( final Iterator<MetadataType> iterator = metadataList.iterator(); iterator.hasNext(); )
    {
      if( iterator.next().getName().equals( ITimeseriesConstants.MD_TIMEZONE ) )
        iterator.remove();
    }
    metadataList.add( mdType );

    return metadataListType;
  }

  /**
   * @return string that contains the serialized values
   */
  private String buildValueString( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    if( model.size() == 0 )
      return StringUtils.EMPTY;

    if( java.util.Date.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringDateAxis( model, axis );

    if( Number.class.isAssignableFrom( axis.getDataClass() ) || Boolean.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringNumberAxis( model, axis );

    if( String.class.isAssignableFrom( axis.getDataClass() ) )
      return buildStringAxis( model, axis );

    throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.21" ) ); //$NON-NLS-1$
  }

  private static String buildStringAxis( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();
    for( int i = 0; i < model.size(); i++ )
    {
      buffer.append( model.get( i, axis ) ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtils.chop( buffer.toString() );
  }

  private String buildStringDateAxis( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();
    final DateParser dateParser = getDateParser( m_timezone );

    for( int i = 0; i < model.size(); i++ )
    {
      buffer.append( dateParser.toString( model.get( i, axis ) ) ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtils.chop( buffer.toString() );
  }

  /**
   * Uses the default toString() method of the elements
   */
  private static String buildStringNumberAxis( final ITupleModel model, final IAxis axis ) throws SensorException
  {
    final StringBuffer buffer = new StringBuffer();

    for( int i = 0; i < model.size(); i++ )
    {
      final Object elt = model.get( i, axis );
      if( elt == null )
        LOG.warning( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.24" ) + i + Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.25" ) + axis ); //$NON-NLS-1$ //$NON-NLS-2$

      buffer.append( elt ).append( ";" ); //$NON-NLS-1$
    }

    return StringUtils.chop( buffer.toString() );
  }

  /**
   * Parser for the type <code>date</code>. It uses following format string:
   * 
   * <pre>
   *      yyyy-MM-dd'T'HH:mm:ss
   * </pre>
   * 
   * FIXME: we should use {@link javax.xml.bind.DatatypeConverter} instead
   */
  public static DateParser getDateParser( final TimeZone timezone )
  {
    final DateParser parser = new DateParser( XML_DATETIME_FORMAT );
    parser.setTimezone( timezone );

    return parser;
  }
}