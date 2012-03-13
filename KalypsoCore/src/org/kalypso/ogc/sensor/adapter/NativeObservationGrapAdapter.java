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
package org.kalypso.ogc.sensor.adapter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author doemming
 */
public class NativeObservationGrapAdapter extends AbstractObservationImporter
{
  private final DateFormat m_grapDateFormat = new SimpleDateFormat( "dd MM yyyy HH mm ss" ); //$NON-NLS-1$

  private static final Pattern GRAP_PATTERN = Pattern.compile( "([0-9]{1,2}.+?[0-9]{1,2}.+?[0-9]{2,4}.+?[0-9]{1,2}.+?[0-9]{1,2}.[0-9 ]{1,2})(.*-?[0-9\\.]+)" ); //$NON-NLS-1$

  private static final int MAX_NO_OF_ERRORS = 30;

  final List<Date> m_dates = new ArrayList<Date>();

  final List<Double> m_values = new ArrayList<Double>();

  private int m_numberOfErrors = 0;

  @Override
  public IStatus doImport( final File source, final TimeZone timeZone, final String valueType, final boolean continueWithErrors )
  {

    m_grapDateFormat.setTimeZone( timeZone );

    final List<IStatus> stati = new ArrayList<>();

    try
    {
      Collections.addAll( stati, parse( source, continueWithErrors ) );
    }
    catch( final Exception ex )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), ex.getMessage() );
      stati.add( status );
    }

    setObservation( new SimpleObservation( "href", "titel", new MetadataList(), createTuppelModel( valueType ) ) ); //$NON-NLS-1$ //$NON-NLS-2$

    return StatusUtilities.createStatus( stati, "Grap Observation Import" );
  }

  private IStatus[] parse( final File source, final boolean continueWithErrors ) throws IOException
  {
    final List<IStatus> stati = new ArrayList<>();

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );

    try
    {
      String lineIn = null;

      while( (lineIn = reader.readLine()) != null )
      {
        if( !continueWithErrors && m_numberOfErrors > MAX_NO_OF_ERRORS )
          return stati.toArray( new IStatus[] {} );
        try
        {
          final Matcher matcher = GRAP_PATTERN.matcher( lineIn );
          if( !matcher.matches() )
          {
            final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "Line not parsable: %s", lineIn ) );
            throw new CoreException( status );
          }

          final Date date = parseDate( matcher.group( 1 ) );
          final Double value = NumberUtils.parseDouble( matcher.group( 2 ) );

          m_dates.add( date );
          m_values.add( value );
        }
        catch( final Throwable e )
        {
          final String message = e.getMessage();

          if( m_numberOfErrors < MAX_NO_OF_ERRORS )
            stati.add( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "Line #%d: %s", reader.getLineNumber(), message ) ) );

          m_numberOfErrors++;
        }
      }
    }
    finally
    {
      reader.close();
    }

    return stati.toArray( new IStatus[] {} );
  }

  private ITupleModel createTuppelModel( final String valueType )
  {
    final IAxis[] axis = createAxis( valueType );

    final Object[][] tupelData = new Object[m_dates.size()][2];
    for( int i = 0; i < m_dates.size(); i++ )
    {
      tupelData[i][0] = m_dates.get( i );
      tupelData[i][1] = m_values.get( i );
    }

    m_dates.clear();
    m_values.clear();

    return new SimpleTupleModel( axis, tupelData );
  }

  private static final Pattern DATE_PATTERN = Pattern.compile( "([0-9 ]{2}) ([0-9 ]{2}) ([0-9]{4}) ([0-9 ]{2}) ([0-9 ]{2}) ([0-9 ]{2})" ); //$NON-NLS-1$

  private Date parseDate( final String dateString ) throws ParseException, CoreException
  {
    final String formatedDate = dateString.replaceAll( "[:\\.]", " " ); //$NON-NLS-1$ //$NON-NLS-2$

    final Matcher dateMatcher = DATE_PATTERN.matcher( formatedDate );

    if( !dateMatcher.matches() )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), String.format( "Date not parsable: %s", dateString ) ) );
    }

    final StringBuffer buffer = new StringBuffer();
    for( int i = 1; i <= dateMatcher.groupCount(); i++ )
    {
      if( i > 1 )
        buffer.append( " " ); // separator //$NON-NLS-1$

      buffer.append( dateMatcher.group( i ).replaceAll( " ", "0" ) ); // //$NON-NLS-1$ //$NON-NLS-2$
      // correct
      // empty
      // fields
    }

    final String correctDate = buffer.toString();
    final Date date = m_grapDateFormat.parse( correctDate );

    return date;
  }

}