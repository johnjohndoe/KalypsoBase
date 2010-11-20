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
package org.kalypso.ogc.sensor.timeseries.envelope;

import java.io.File;
import java.util.Calendar;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.IObservationConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.proxy.AutoProxyFactory;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.util.ZmlLink;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

/**
 * @author doemming
 */
public final class TranProLinFilterUtilities
{
  private TranProLinFilterUtilities( )
  {
    throw new UnsupportedOperationException();
  }

  public static void transformAndWrite( final IObservation baseObservation, final Calendar dateBegin, final Calendar dateEnd, final double operandBegin, final double operandEnd, final String operator, final String axisTypes, final int statusToMerge, final File resultFile, final String name, final IRequest request ) throws SensorException
  {
    if( resultFile == null )
      return; // nothing to do

    final IObservation resultObservation = transform( baseObservation, dateBegin, dateEnd, operandBegin, operandEnd, operator, axisTypes, statusToMerge );
    final MetadataList metadataList = resultObservation.getMetadataList();
    final String oldName = metadataList.getProperty( IObservationConstants.MD_NAME );
    metadataList.setProperty( IObservationConstants.MD_NAME, name );
    ZmlFactory.writeToFile( resultObservation, resultFile, request );
    metadataList.setProperty( IObservationConstants.MD_NAME, oldName );
  }

  public static void transformAndWrite( final IObservation baseObservation, final Calendar dateBegin, final Calendar dateEnd, final double operandBegin, final double operandEnd, final String operator, final String axisTypes, final int statusToMerge, final ZmlLink targetLink, final String name, final IRequest request ) throws SensorException, CoreException
  {
    final IObservation resultObservation = transform( baseObservation, dateBegin, dateEnd, operandBegin, operandEnd, operator, axisTypes, statusToMerge );
    final MetadataList metadataList = resultObservation.getMetadataList();
    final String oldName = metadataList.getProperty( IObservationConstants.MD_NAME );
    metadataList.setProperty( IObservationConstants.MD_NAME, name );
    targetLink.saveObservation( resultObservation, request );
    metadataList.setProperty( IObservationConstants.MD_NAME, oldName );
  }

  public static IObservation transform( final IObservation baseObservation, final Calendar dateBegin, final Calendar dateEnd, final double operandBegin, final double operandEnd, final String operator, final String axisTypes, final int statusToMerge ) throws SensorException
  {
    final TranProLinFilter filter = new TranProLinFilter( dateBegin.getTime(), dateEnd.getTime(), operator, operandBegin, operandEnd, statusToMerge, axisTypes );
    filter.initFilter( null, baseObservation, null );
    return AutoProxyFactory.getInstance().proxyObservation( filter );
  }

// final String name = getName( baseObservation, suffix );
  public static String getName( final IObservation baseObservation, final String suffix )
  {
    final String name = baseObservation.getName();
    if( name == null )
      return suffix;

    return name + suffix;
  }
}
