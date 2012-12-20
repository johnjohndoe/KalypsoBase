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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * @author Dejan Antanaskovic, <a href="mailto:dejan.antanaskovic@tuhh.de">dejan.antanaskovic@tuhh.de</a>
 * @author Dirk Kuch
 */
public class NativeObservationZrxAdapter extends AbstractObservationImporter
{
  public static final String SOURCE_ID = "source://native.observation.zrx.import"; //$NON-NLS-1$

  private static Pattern ZRX_HEADER_PATTERN = Pattern.compile( "#.*" ); //$NON-NLS-1$

  private static Pattern ZRX_DATA_PATTERN = Pattern.compile( "([0-9]{12,14})\\s+(-??[0-9]+(.[0-9]*))\\s*" ); //$NON-NLS-1$

  private static Pattern ZRX_SNAME_PATTERN = Pattern.compile( "(#\\S*SNAME)(\\w+)(\\|\\*\\|\\S*)" ); //$NON-NLS-1$

  @Override
  protected List<NativeObservationDataSet> parse( final File source, final TimeZone timeZone, final boolean continueWithErrors, final IStatusCollector stati ) throws IOException
  {
    final List<NativeObservationDataSet> datasets = new ArrayList<>();

    final DateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmm" ); //$NON-NLS-1$
    final DateFormat sdf2 = new SimpleDateFormat( "yyyyMMddHHmmss" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );
    sdf2.setTimeZone( timeZone );

    final FileReader fileReader = new FileReader( source );
    final LineNumberReader reader = new LineNumberReader( fileReader );

    String lineIn = null;
    while( (lineIn = reader.readLine()) != null )
    {
      if( !continueWithErrors && getErrorCount() > getMaxErrorCount() )
        return datasets;

      try
      {
        Matcher matcher = ZRX_DATA_PATTERN.matcher( lineIn );
        if( matcher.matches() )
        {
          Date date = null;
          Double value = null;
          if( matcher.group( 1 ).length() == 14 ) // date with seconds
          {
            try
            {
              date = sdf2.parse( matcher.group( 1 ) );
            }
            catch( final Exception e )
            {
              stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationZrxAdapter_0" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
              tickErrorCount();
            }
          }
          else
          {
            try
            {
              date = sdf.parse( matcher.group( 1 ) );
            }
            catch( final Exception e )
            {
              stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationZrxAdapter_1" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
              tickErrorCount();
            }
          }
          try
          {
            value = new Double( matcher.group( 2 ) );
          }
          catch( final Exception e )
          {
            stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationZrxAdapter_2" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
            tickErrorCount();
          }

          datasets.add( new NativeObservationDataSet( date, value, KalypsoStati.BIT_OK, SOURCE_ID ) );
        }
        else
        {
          matcher = ZRX_HEADER_PATTERN.matcher( lineIn );
          if( matcher.matches() )
          {
            matcher = ZRX_SNAME_PATTERN.matcher( lineIn );
            if( matcher.matches() )
            {
              // m_sName = matcher.group( 2 );
            }
          }
          else
          {
            stati.add( IStatus.WARNING, String.format( Messages.getString( "NativeObservationZrxAdapter_3" ), reader.getLineNumber(), lineIn ) ); //$NON-NLS-1$
            tickErrorCount();
          }
        }
      }
      catch( final Exception e )
      {
        stati.add( IStatus.ERROR, String.format( Messages.getString( "NativeObservationZrxAdapter_4" ), reader.getLineNumber(), e.getLocalizedMessage() ) ); //$NON-NLS-1$
        tickErrorCount();
      }
    }

    return datasets;
  }
}