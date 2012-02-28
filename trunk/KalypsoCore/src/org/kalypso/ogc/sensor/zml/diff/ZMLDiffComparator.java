/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.zml.diff;

import java.io.InputStream;
import java.util.Date;

import org.kalypso.commons.diff.IDiffComparator;
import org.kalypso.commons.diff.IDiffLogger;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.xml.sax.InputSource;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class ZMLDiffComparator implements IDiffComparator
{
  public ZMLDiffComparator( )
  {
  }

  @Override
  public boolean diff( final IDiffLogger logger, final Object content, final Object content2 ) throws Exception
  {
    boolean result = false;
    final InputStream i1 = (InputStream) content;
    final InputStream i2 = (InputStream) content2;
    final IObservation obs1 = ZmlFactory.parseXML( new InputSource( i1 ), null ); //$NON-NLS-1$
    final IObservation obs2 = ZmlFactory.parseXML( new InputSource( i2 ), null ); //$NON-NLS-1$
    // result |= diffMetadata( logger, obs1.getMetadataList(), obs2.getMetadataList() );
    // result |= diffAxis( logger, obs1.getAxisList(), obs2.getAxisList() );
    logger.block();
    logger.log( DIFF_INFO, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.2" ) ); //$NON-NLS-1$
    final boolean valuesResult = diffValues( logger, obs1, obs2 );
    logger.unblock( valuesResult );

    result |= valuesResult;

    return result;
  }

  private boolean diffValues( final IDiffLogger logger, final IObservation obs1, final IObservation obs2 ) throws SensorException
  {
    boolean result = false;

    final double differenceAll = 0;

    /* Check equals length of series */
    final ITupleModel values1 = obs1.getValues( null );
    final ITupleModel values2 = obs2.getValues( null );

    final int length1 = values1.size();
    final int length2 = values2.size();
    if( length1 != length2 )
    {
      logger.log( IDiffComparator.DIFF_CONTENT, Messages.getString( "org.kalypso.ogc.sensor.zml.diff.ZMLDiffComparator.3" ) + length1 + " : " + length2 ); //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }

    if( length1 == 0 )
      return false;

    final IAxis[] axes1 = obs1.getAxes();
    final IAxis[] axes2 = obs2.getAxes();

    final IAxis dateAxis1 = ObservationUtilities.findAxisByClass( axes1, Date.class );
    final IAxis dateAxis2 = ObservationUtilities.findAxisByClass( axes2, Date.class );

    final IAxis valueAxis1 = findValueAxesNoWechmann( axes1 );
    final IAxis valueAxis2 = findValueAxesNoWechmann( axes2 );

    // TODO: alle Achsen
    // Log-Ausgabe um Info zu betroffener Achse erweitern

    final ZmlAxisComparator axisComparator = new ZmlAxisComparator( values1, dateAxis1, valueAxis1, values2, dateAxis2, valueAxis2, logger );
    final boolean axisResult = axisComparator.compare();

    result = axisResult;

    axisComparator.logDifferences( logger );

    return result;
  }

  // /**
  // * @param logger
  // * @param axisList
  // * @param axisList2
  // */
  // private boolean diffAxis( IDiffLogger logger, IAxis[] axisList, IAxis[] axisList2 )
  // {
  // final List list1 = new ArrayList();
  // final List list2 = new ArrayList();
  // for( int i = 0; i < axisList.length; i++ )
  // list1.add( axisList[i].getName() + ":" + axisList[i].getType() + ":" + axisList[i].getUnit() + ":"
  // + axisList[i].getClass().getName() );
  // for( int i = 0; i < axisList2.length; i++ )
  // list2.add( axisList2[i].getName() + ":" + axisList2[i].getType() + ":" + axisList2[i].getUnit() + ":"
  // + axisList2[i].getClass().getName() );
  // return DiffUtils.diffIgnoreOrder( logger, list1, list2, " Axen " );
  // }

  // /**
  // *
  // * @param logger
  // * @param metadataList
  // * @param metadataList2
  // * @return diff
  // */
  // private boolean diffMetadata( IDiffLogger logger, MetadataList metadataList, MetadataList metadataList2 )
  // {
  // List list1 = new ArrayList();
  // List list2 = new ArrayList();
  // for( Iterator iter = metadataList.keySet().iterator(); iter.hasNext(); )
  // {
  // final String key = (String)iter.next();
  // final String property = metadataList.getProperty( key );
  // list1.add( key + " = " + property );
  // }
  // for( Iterator iter = metadataList2.keySet().iterator(); iter.hasNext(); )
  // {
  // final String key = (String)iter.next();
  // final String property = metadataList2.getProperty( key );
  // list2.add( key + " = " + property );
  // }
  // return DiffUtils.diffIgnoreOrder( logger, list1, list2, "Metadaten" );
  // }

  /**
   * Finds ONE value axis, not Wechmann-E and not Wechmann-V.<br/>
   * A bit hacky, what to do in the (theoretical) case of two real value axes?
   */
  private static IAxis findValueAxesNoWechmann( final IAxis[] axes )
  {
    final IAxis[] allValueAxes = AxisUtils.findValueAxes( axes );
    for( final IAxis axis : allValueAxes )
    {
      final String type = axis.getType();
      if( !ITimeseriesConstants.TYPE_WECHMANN_E.equals( type ) && !ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V.equals( type ) )
        return axis;
    }

    return null;
  }

}
