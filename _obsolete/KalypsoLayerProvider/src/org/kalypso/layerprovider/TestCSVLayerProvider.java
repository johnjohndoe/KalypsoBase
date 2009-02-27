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
package org.kalypso.layerprovider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.kalypso.observation.result.Component;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.exception.LayerProviderException;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.LayerType;

/**
 * @author alibu
 */
public class TestCSVLayerProvider implements ILayerProvider
{

  private LayerType m_lt;

  private Chart m_chart;

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( final URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;
    final String configLayerId = m_lt.getName();

    final ParameterHelper ph = new ParameterHelper();
    ph.addParameters( m_lt.getParameters(), configLayerId );

    try
    {
      final String domainAxisId = ((AxisType) m_lt.getAxes().getDomainAxisRef().getRef()).getName();
      final String valueAxisId = ((AxisType) m_lt.getAxes().getValueAxisRef().getRef()).getName();

      final IAxis domAxis = m_chart.getAxisRegistry().getAxis( domainAxisId );
      final IAxis valAxis = m_chart.getAxisRegistry().getAxis( valueAxisId );

      final Component comp_date = new Component( "date", "Datum", "das Datum", "", "frame_datum", new QName( "Datum" ), null, null );
      final Component comp_pegel = new Component( "pegel", "Pegel", "der Pegelstand", "cmNN", "frame_pegel", new QName( "Wasserstand" ), null, null );
      final TupleResult result = new TupleResult( new Component[] { comp_date, comp_pegel } );

// TODO: umschreiben, damit auch urls verwendet werden können
      final String url = ph.getParameterValue( "url", configLayerId );
      final FileReader fr = new FileReader( url );

      final BufferedReader br = new BufferedReader( fr );
      String s = "";
      while( (s = br.readLine()) != null )
      {
        final IRecord record = result.createRecord();
        final String[] cols = s.split( "  *" );
        // YearString
        if( cols.length >= 2 )
        {
          final String ys = cols[0];
          // Datum zerpflücken (Bsp: 0510190530)
          // TODO: Auslagern in Toolbox-ähnliche Klasse
          final int year = 2000 + Integer.parseInt( ys.substring( 0, 2 ) );
          final int month = Integer.parseInt( ys.substring( 2, 4 ) ) - 1;
          final int day = Integer.parseInt( ys.substring( 4, 6 ) );
          final int hour = Integer.parseInt( ys.substring( 6, 8 ) );
          final int minute = Integer.parseInt( ys.substring( 8, 10 ) );
          final Calendar cal = Calendar.getInstance();

          cal.set( Calendar.YEAR, year );
          cal.set( Calendar.MONTH, month );
          cal.set( Calendar.DAY_OF_MONTH, day );
          cal.set( Calendar.HOUR_OF_DAY, hour );
          cal.set( Calendar.MINUTE, minute );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          // wichtig, damit die Zeiten richtig sind
          cal.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );

          record.setValue( comp_date, cal );
          record.setValue( comp_pegel, Integer.parseInt( cols[1] ) );
          result.add( record );
        }
      }
      final TreeMap<String, IComponent> map = new TreeMap<String, IComponent>();
      map.put( "domain", comp_date );
      map.put( "value", comp_pegel );
      icl = new TestBarLayer( result, map.get( "domain" ), map.get( "value" ), domAxis, valAxis );
      icl.setName( m_lt.getTitle() );
    }
    catch( final FileNotFoundException e )
    {
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }

    return icl;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.Chart,
   *      org.ksp.chart.configuration.LayerType)
   */
  public void init( final Chart chart, final LayerType lt )
  {
    m_lt = lt;
    m_chart = chart;
  }

}
