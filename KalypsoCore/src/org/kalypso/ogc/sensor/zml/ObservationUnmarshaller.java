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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.factory.FactoryException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.commons.parser.IParser;
import org.kalypso.commons.parser.ParserException;
import org.kalypso.commons.parser.impl.DateParser;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.zml.values.IZmlValues;
import org.kalypso.ogc.sensor.zml.values.ZmlArrayValues;
import org.kalypso.ogc.sensor.zml.values.ZmlLinkValues;
import org.kalypso.ogc.sensor.zml.values.ZmlTupleModel;
import org.kalypso.zml.AxisType;
import org.kalypso.zml.AxisType.ValueArray;
import org.kalypso.zml.AxisType.ValueLink;
import org.kalypso.zml.MetadataListType;
import org.kalypso.zml.MetadataType;
import org.kalypso.zml.Observation;
import org.xml.sax.InputSource;

/**
 * @author Gernot Belger
 */
class ObservationUnmarshaller implements ICoreRunnableWithProgress
{
  private final InputSource m_source;

  private final URL m_context;

  private IObservation m_observation;

  private MetadataList m_metadata;

  ObservationUnmarshaller( final InputSource source, final URL context )
  {
    m_source = source;
    m_context = context;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final StatusCollector stati = new StatusCollector( KalypsoCorePlugin.getID() );

    try
    {
      final Unmarshaller u = ZmlFactory.JC.createUnmarshaller();
      final Observation binding = (Observation) u.unmarshal( m_source );

      m_observation = doConvert( binding, stati );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();

      final String msg = Messages.getString("ObservationUnmarshaller.0"); //$NON-NLS-1$
      stati.add( IStatus.ERROR, msg, ex );
    }

    return stati.asMultiStatus( Messages.getString("ObservationUnmarshaller.1") ); //$NON-NLS-1$
  }

  public IObservation doConvert( final Observation binding, final StatusCollector stati )
  {
    try
    {
      // metadata
      stati.add( readMetadata( binding ) );
      final TimeZone timeZone = MetadataHelper.getTimeZone( m_metadata, "UTC" ); //$NON-NLS-1$

      // axes and values
      final List<AxisType> bindingAxes = binding.getAxis();
      final Map<IAxis, IZmlValues> valuesMap = new HashMap<>( bindingAxes.size() );

      final String data = binding.getData(); // data is optional and can be null

      for( int i = 0; i < bindingAxes.size(); i++ )
      {
        final AxisType bindingAxis = bindingAxes.get( i );
        final IParser parser = createParser( bindingAxis, timeZone );
        final IZmlValues values = binding2Values( bindingAxis, parser, data );
        final IAxis axis = new DefaultAxis( bindingAxis.getName(), bindingAxis.getType(), bindingAxis.getUnit(), parser.getObjectClass(), bindingAxis.isKey() );

        valuesMap.put( axis, values );
      }

      final ZmlTupleModel model = new ZmlTupleModel( valuesMap );

      final String contextHref = m_context != null ? m_context.toExternalForm() : ""; //$NON-NLS-1$
      return new SimpleObservation( contextHref, binding.getName(), m_metadata, model );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
      stati.add( IStatus.ERROR, Messages.getString("ObservationUnmarshaller.2"), ex ); //$NON-NLS-1$

      return null;
    }
  }

  private IStatus readMetadata( final Observation binding )
  {
    m_metadata = new MetadataList();
    m_metadata.put( IMetadataConstants.MD_NAME, binding.getName() );

    final MetadataListType bindingMeta = binding.getMetadataList();
    if( Objects.isNotNull( bindingMeta ) )
    {
      final List<MetadataType> mdList = bindingMeta.getMetadata();
      for( final MetadataType md : mdList )
      {
        final String value = guessMetaValue( md );
        m_metadata.put( md.getName(), value );
      }
    }

    return new Status( IStatus.OK, KalypsoCorePlugin.getID(), Messages.getString("ObservationUnmarshaller.3") ); //$NON-NLS-1$
  }

  private static String guessMetaValue( final MetadataType md )
  {
    if( md.getValue() != null )
      return md.getValue();

    if( md.getData() != null )
      return md.getData().replaceAll( XMLUtilities.CDATA_BEGIN_REGEX, StringUtils.EMPTY ).replaceAll( XMLUtilities.CDATA_END_REGEX, StringUtils.EMPTY );

    return StringUtils.EMPTY;
  }

  private IParser createParser( final AxisType bindingAxis, final TimeZone timeZone ) throws FactoryException
  {
    final Properties props = PropertiesHelper.parseFromString( bindingAxis.getDatatype(), '#' );
    final String type = props.getProperty( "TYPE" ); //$NON-NLS-1$
    final String overrideFormat = props.getProperty( "FORMAT" ); //$NON-NLS-1$

    final IParser parser = ZmlParserFactory.createParser( type, overrideFormat );

    // if we have a date parser, set the right timezone to read the values
    if( parser instanceof DateParser )
      ((DateParser) parser).setTimezone( timeZone );

    return parser;
  }

  /**
   * Parses the values and create the corresponding objects.
   *
   * @param context
   *          context into which the original file exists
   * @param axisType
   *          binding object for axis
   * @param parser
   *          configured parser enabled for parsing the values according to axis spec
   * @param data
   *          [optional] contains the data-block if observation is block-inline
   * @return corresponding values depending on value axis type
   * @throws ParserException
   * @throws MalformedURLException
   * @throws IOException
   */
  private IZmlValues binding2Values( final AxisType axisType, final IParser parser, final String data ) throws ParserException, IOException, SensorException
  {
    final ValueArray va = axisType.getValueArray();
    if( va != null )
      return new ZmlArrayValues( va, parser );

    // loader for linked values, here we specify where base location is
    final ValueLink vl = axisType.getValueLink();
    if( vl != null )
      return new ZmlLinkValues( vl, parser, m_context, data );

    throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.14" ) + axisType.toString() ); //$NON-NLS-1$
  }

  public IObservation getObservation( )
  {
    return m_observation;
  }

}