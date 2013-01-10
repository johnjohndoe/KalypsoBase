/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.core.diagram.data;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;

import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

/**
 * @author Dirk Kuch
 */
public class MetadataRequestHandler implements IRequestHandler
{
  private static final Pattern PATTERN_METADATA_DATE = new Pattern( "^metadata\\:" ); //$NON-NLS-1$

  private final IParameterContainer m_parameters;

  public MetadataRequestHandler( final IParameterContainer parameters )
  {
    m_parameters = parameters;
  }

  @Override
  public IRequest getArguments( final MetadataList metadata )
  {
    final String keyStart = getKey( "start" ); //$NON-NLS-1$
    final String keyEnd = getKey( "end" ); //$NON-NLS-1$

    final Date from = getDate( metadata, keyStart, "startOffset" ); //$NON-NLS-1$
    final Date to = getDate( metadata, keyEnd, "endOffset" ); //$NON-NLS-1$

    return new ObservationRequest( new DateRange( from, to ) );
  }

  private Date getDate( final MetadataList metadata, final String key, final String offset )
  {
    if( StringUtils.isBlank( key ) )
      return null;

    if( key.startsWith( "metadata:" ) )//$NON-NLS-1$
    {
      final Date base = getFromMetadata( metadata, key );
      if( base == null )
        return null;

      return doAdjust( base, offset ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return null;
  }

  private Date doAdjust( final Date base, final String offset )
  {
    final String strDuration = getKey( offset );
    if( StringUtils.isBlank( strDuration ) )
      return base;

    final boolean positive = isPositiveOffset( strDuration );
    final Period duration = getDuration( strDuration );

    final DateTime toAdjust = new DateTime( base.getTime() );
    DateTime adjusted = null;
    if( positive )
      adjusted = toAdjust.plus( duration );
    else
      adjusted = toAdjust.minus( duration );

    return adjusted.toDate();
  }

  private Period getDuration( final String strDuration )
  {
    final PeriodFormatter formater = ISOPeriodFormat.standard();
    if( strDuration.startsWith( "-" ) ) //$NON-NLS-1$
      return formater.parsePeriod( strDuration.substring( 1 ) );
    else if( strDuration.startsWith( "+" ) ) //$NON-NLS-1$
      return formater.parsePeriod( strDuration.substring( 1 ) );

    return formater.parsePeriod( strDuration );
  }

  private boolean isPositiveOffset( final String strDuration )
  {
    if( strDuration.startsWith( "-" ) )
      return false;

    return true;
  }

  private Date getFromMetadata( final MetadataList metadata, final String url )
  {
    final RETokenizer tokenizer = new RETokenizer( PATTERN_METADATA_DATE, url );

    final String key = tokenizer.nextToken();

    final String property = metadata.getProperty( key );
    if( StringUtils.isEmpty( property ) )
      return null;

    final Calendar calendar = DatatypeConverter.parseDate( property );

    return calendar.getTime();
  }

  private String getKey( final String key )
  {
    return m_parameters.getParameterValue( key, null );
  }
}