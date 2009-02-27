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
package org.kalypso.swtchart.chart.axis.registry;

import java.util.Map;

import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author schlienger
 * @author burtscher
 * 
 * Interface describing a container for the chart axes; used
 * to ensure that only one axis is present for each DataType
 */


public interface IAxisRegistry extends IAxisRegistryEventProvider
{
  
  /**
   * @return true if an axis with the given identifier is already present in the registry, false elsewise
   */
  public boolean hasAxis( String identifier );

  /**
   * @return axis with the corresponding identifier
   */
  public IAxis getAxis( String identifier );

  /**
   * @return Array of all present axes
   */
  public IAxis[] getAxes( );

  /**
   * adds an axis to the registry
   */
  public void addAxis( IAxis axis );

  /**
   * removes an axis from the registry
   */
  public void removeAxis( IAxis axis );

  /**
   * removes all axes from the registry
   */
  public void clear( );

  /**
   * @return Array of all axes at the given position
   */
  public IAxis[] getAxesAt( IAxisConstants.POSITION pos );

  /**
   *    applies the axisVisitors mission
   */
  public void apply( IAxisVisitor axisVisitor );

  /**
   * @return renderer for the given axis. If first looks up the renderer that were explicitely registered for a given
   *         axis. If no renderer is found for that axis, it looks up the renderer based on the dataClass of the axis.
   *         If still no renderer is found, it tries to find a renderer for a super class of the axis dataClass.
   */
  public IAxisRenderer getRenderer( IAxis axis );

  /**
   * sets the AxisRenderer for a particular dataClass
   */
  public void setRenderer( Class< ? > dataClass, IAxisRenderer renderer );

  /**
   * sets the AxisRenderer for a particular axis, identified by - guess what - the axis' identifier
   */
  public void setRenderer( String identifier, IAxisRenderer renderer );

  /**
   * removes the renderer for a particular dataClass
   */
  public void unsetRenderer( Class< ? > dataClass );

  /**
   * removes the renderer for a particular axis 
   */
  public void unsetRenderer( String identifier );

  /**
   * @return the AxisComponent of the given axis or null if there isn't any
   */
  public IAxisComponent getComponent( IAxis axis );

  /**
   * sets the component for a particular axis
   */
  public void setComponent( IAxis axis, IAxisComponent comp );

  /**
   * @return map of Axis-AxisComponent-Pairs
   */
  public Map<IAxis, IAxisComponent> getComponents( );
}
