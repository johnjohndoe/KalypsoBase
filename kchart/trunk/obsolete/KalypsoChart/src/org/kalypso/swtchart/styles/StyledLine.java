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
package org.kalypso.swtchart.styles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 *
 */
public class StyledLine
{
  GCWrapper m_gc;
  Device m_dev;
  int[] m_path;
  
  int m_width=2;
  int m_borderWidth=0;
  
  
  public StyledLine(GCWrapper gc, Device dev)
  {
    m_gc=gc;
    m_dev=dev;
  }

  public void setPath(int[] path)
  {
    m_path=path;
  }
  
  public void paint()
  {
    m_gc.m_gc.setAntialias(SWT.ON);
    
    m_gc.setForeground(new Color(m_dev, 0,0,0));      
    m_gc.setBackground(m_dev.getSystemColor(SWT.COLOR_GREEN));
    if (m_borderWidth>0)
    {
      m_gc.setLineWidth(m_width+2*m_borderWidth);
      m_gc.drawPolyline(m_path);
    }
    
    m_gc.setForeground(new Color(m_dev, 255,0,0));      
    m_gc.setLineWidth(m_width);
    m_gc.drawPolyline(m_path);

   
  }
  
  
  
  
  
  
}
