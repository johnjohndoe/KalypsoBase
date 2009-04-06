/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.debug.zml;

import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.manipulator.IObservationManipulator;
import org.kalypso.ogc.sensor.timeseries.TimeserieConstants;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTableFactory;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTableSet;
import org.xml.sax.InputSource;

/**
 * A default observation with a fake WQ-Table used for debugging purposes.
 * 
 * @author schlienger
 */
public class SimpleWQTableObsManipulator implements IObservationManipulator
{
  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
      throws CoreException
  {
  // empty
  }

  /**
   * @see org.kalypso.ogc.sensor.manipulator.IObservationManipulator#manipulate(org.kalypso.ogc.sensor.IObservation,
   *      java.lang.Object)
   */
  public void manipulate( final IObservation obs, final Object data ) throws SensorException
  {
    // do not overwrite existing table
    if( obs.getMetadataList().containsKey( TimeserieConstants.MD_WQTABLE ) )
      return;
    
    final IAxis[] axes = obs.getAxisList();

    // only do this for W/Q/V timeseries
    if( ObservationUtilities.hasAxisOfType( axes, TimeserieConstants.TYPE_WATERLEVEL )
        || ObservationUtilities.hasAxisOfType( axes, TimeserieConstants.TYPE_VOLUME )
        || ObservationUtilities.hasAxisOfType( axes, TimeserieConstants.TYPE_RUNOFF ) )
    {
      final InputStream stream = getClass().getResourceAsStream( "/org/kalypso/debug/zml/wqtable.xml" );
      try
      {
        final WQTableSet tableSet = WQTableFactory.parse( new InputSource( stream ) );
        stream.close();

        final String mdWQ = WQTableFactory.createXMLString( tableSet );
        obs.getMetadataList().setProperty( TimeserieConstants.MD_WQTABLE, mdWQ );
      }
      catch( final Exception e )
      {
        Logger.getLogger( getClass().getName() ).warning( "Could not load wq-table: " + e.getLocalizedMessage() );
      }
      finally
      {
        IOUtils.closeQuietly( stream );
      }
    }
  }
}
