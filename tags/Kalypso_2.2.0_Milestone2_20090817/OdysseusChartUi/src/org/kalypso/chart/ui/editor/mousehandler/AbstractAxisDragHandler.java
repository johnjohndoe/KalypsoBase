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
package org.kalypso.chart.ui.editor.mousehandler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author burtscher1
 * 
 */
public abstract class AbstractAxisDragHandler implements IAxisDragHandler
{

  protected final ChartComposite m_chartComposite;

  protected int m_mouseDragStart = -1;

  protected int m_mouseDragEnd = -1;

  protected final Map<AxisCanvas, IAxis> m_axes = new HashMap<AxisCanvas, IAxis>();

  protected boolean m_applyOnAllAxes = false;

  public AbstractAxisDragHandler( final ChartComposite chartComposite )
  {
    m_chartComposite = chartComposite;

    // zugehörige Achsen rausfinden
    final IMapperRegistry reg = m_chartComposite.getChartModel().getMapperRegistry();
    final IAxis[] axes = reg.getAxes();
    for( final IAxis axis : axes )
    {
      final IAxisComponent component = reg.getComponent( axis );
      if( component != null )
      {
        final AxisCanvas ac = (AxisCanvas) component;
        m_axes.put( ac, axis );
      }
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( MouseEvent e )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    m_mouseDragStart = getPos( e );
  }

  protected int getPos( final MouseEvent e )
  {
    final AxisCanvas ac = getEventSource( e );
    final IAxis axis = m_axes.get( ac );
    if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      return e.x;
    }
    else
    {
      return e.y;
    }
  }

  protected AxisCanvas getEventSource( final MouseEvent e )
  {
    return (AxisCanvas) e.getSource();
  }

  public void keyPressed( KeyEvent e )
  {
    if( e.keyCode == SWT.ALT )
    {
      m_applyOnAllAxes = true;
    }
  }

  public void keyReleased( KeyEvent e )
  {
    if( e.keyCode == SWT.ALT )
    {
      m_applyOnAllAxes = false;
    }
  }
}
