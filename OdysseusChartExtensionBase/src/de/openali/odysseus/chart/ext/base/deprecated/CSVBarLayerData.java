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
package de.openali.odysseus.chart.ext.base.deprecated;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author Gernot Belger
 */
final class CSVBarLayerData extends AbstractDomainIntervalValueData
{
  private URL m_url;

  public void setInputURL( final URL url )
  {
    m_url = url;
  }

  public URL getInputURL( )
  {
    return m_url;
  }

  private static Calendar createDate( final String s )
  {
    // Datum zerpflücken (Bsp: 0510190530)
    // TODO: Auslagern in Toolbox-ähnliche Klasse
    final int year = 2000 + Integer.parseInt( s.substring( 0, 2 ) );
    final int month = Integer.parseInt( s.substring( 2, 4 ) ) - 1;
    final int day = Integer.parseInt( s.substring( 4, 6 ) );
    final int hour = Integer.parseInt( s.substring( 6, 8 ) );
    final int minute = Integer.parseInt( s.substring( 8, 10 ) );

    // echte Daten aus EiongabeDatei
    final Calendar calData = Calendar.getInstance();
    calData.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) ); //$NON-NLS-1$
    calData.set( Calendar.YEAR, year );
    calData.set( Calendar.MONTH, month );
    calData.set( Calendar.DAY_OF_MONTH, day );
    calData.set( Calendar.HOUR_OF_DAY, hour );
    calData.set( Calendar.MINUTE, minute );
    calData.set( Calendar.SECOND, 0 );
    calData.set( Calendar.MILLISECOND, 0 );
    return calData;
  }

  private static Number createNumber( final String s )
  {
    return Double.parseDouble( s );
  }

  @Override
  public boolean openData( )
  {
    // TODO: umschreiben, damit auch urls verwendet werden können
    try
    {
      final URL url = getInputURL();
      final InputStream is = url.openStream();
      final InputStreamReader isr = new InputStreamReader( is );

      final List<Object> domainValues = new ArrayList<>();
      final List<Object> domainIntervalStartValues = new ArrayList<>();
      final List<Object> domainIntervalEndValues = new ArrayList<>();
      final List<Object> targetValues = new ArrayList<>();

      final BufferedReader br = new BufferedReader( isr );
      String s = ""; //$NON-NLS-1$
      int count = 0;
      String domType = null;

      while( (s = br.readLine()) != null && s.trim() != "" ) //$NON-NLS-1$
      {
        final String[] cols = s.split( "  *" ); //$NON-NLS-1$
        // erste Zeile: Überschrift
        if( count == 0 )
        {
          domType = cols[0];
        }
        else
        {
          if( cols.length >= 2 )
          {
            Object domStart = null;
            Object domEnd = null;
            Object domVal = null;
            if( domType.equals( "DATE" ) ) //$NON-NLS-1$
            {
              final Calendar calVal = createDate( cols[0] );

              // Startwert für Interval
              final Calendar calStart = (Calendar) calVal.clone();
              calStart.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) ); //$NON-NLS-1$
              calStart.set( Calendar.MINUTE, 0 );
              calStart.set( Calendar.HOUR_OF_DAY, 0 );

              // Endwert für Interval
              final Calendar calEnd = (Calendar) calStart.clone();
              calEnd.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) ); //$NON-NLS-1$
              // wichtig, damit die Zeiten richtig sind

              calEnd.add( Calendar.DAY_OF_MONTH, +1 );

              domVal = calVal;
              domStart = calStart;
              domEnd = calEnd;

            }
            else
            {
              final Number numVal = createNumber( cols[0] );
              domVal = numVal;
              domStart = new Double( numVal.doubleValue() - 0.5 );
              domEnd = new Double( numVal.doubleValue() + 0.5 );
            }

            domainValues.add( domVal );
            domainIntervalStartValues.add( domStart );
            domainIntervalEndValues.add( domEnd );
            targetValues.add( createNumber( cols[1] ) );

          }
          setDomainIntervalEndValues( domainIntervalEndValues );
          setDomainIntervalStartValues( domainIntervalStartValues );
          setDomainValues( domainValues );
          setTargetValues( targetValues );
        }
        count++;

      }
      br.close();
      isr.close();
      is.close();
    }
    catch( final FileNotFoundException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( final NumberFormatException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    final Object[] domainStart = getDomainDataIntervalStart();
    final Object[] domainEnd = getDomainDataIntervalEnd();
    final Object[] merged = new Object[domainStart.length + domainEnd.length];
    for( int i = 0; i < domainStart.length; i++ )
    {
      merged[i] = domainStart[i];
      merged[i + domainStart.length] = domainEnd[i];
    }
    return DataRange.createFromComparable( merged );
  }

  @Override
  public IDataRange<Object> getTargetRange( )
  {
    return DataRange.createFromComparable( getTargetValues() );
  }
}