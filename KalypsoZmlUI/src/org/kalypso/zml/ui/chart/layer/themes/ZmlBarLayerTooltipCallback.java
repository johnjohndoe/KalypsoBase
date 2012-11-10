/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.zml.ui.chart.layer.themes;

import org.eclipse.swt.SWT;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;

import de.openali.odysseus.chart.ext.base.layer.BarPaintManager.ITooltipCallback;
import de.openali.odysseus.chart.ext.base.layer.TooltipFormatter;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;

/**
 * @author Gernot Belger
 */
class ZmlBarLayerTooltipCallback implements ITooltipCallback
{
  @Override
  public String buildTooltip( final EditInfo info )
  {
    final IObservationValueContainer data = (IObservationValueContainer)info.getData();

    // TODO: configure formatting via layer parameters?

    final String[] formats = new String[] { "%s", "%s", "%s" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final int[] alignments = new int[] { SWT.LEFT, SWT.RIGHT, SWT.LEFT };
    final TooltipFormatter formatter = new TooltipFormatter( null, formats, alignments );

    final IAxis[] axes = data.getAxes();
    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) || AxisUtils.isStatusAxis( axis ) )
        continue;

      try
      {
        // FIXME: improve -> depends on data type; format double and dates correctly, etc...
        // FIXME: use same mechanism as is used by table
        formatter.addLine( axis.getName(), data.get( axis ), axis.getUnit() );
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }
    }

    return formatter.format();
  }
}