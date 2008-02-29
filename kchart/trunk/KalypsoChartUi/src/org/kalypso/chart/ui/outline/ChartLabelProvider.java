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
package org.kalypso.chart.ui.outline;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.factory.util.DummyLayer;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.util.ChartPartUtil;

/**
 * @author Gernot Belger
 */
public class ChartLabelProvider extends LabelProvider implements ITableLabelProvider
{
  private final Map<IChartLayer, Image> m_images = new HashMap<IChartLayer, Image>();

  private final IChartPart m_chartPart;

  public ChartLabelProvider( final IChartPart chartPart )
  {
    m_chartPart = chartPart;
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    for( final Image img : m_images.values() )
      img.dispose();
    m_images.clear();
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    // Falls das Layer nicht erzeugt werden konnte sollte das in der Legende ersichtlich sein
    if( element instanceof DummyLayer )
      return "ERROR: could not load '" + ((IChartLayer) element).getTitle() + "'";
    else if( element instanceof IChartLayer )
      return ((IChartLayer) element).getTitle();
    return super.getText( element );
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IChartLayer )
    {
      final IChartLayer chartLayer = (IChartLayer) element;
      if( m_images.containsKey( chartLayer ) )
        return m_images.get( chartLayer );

      final Image img = new Image( m_chartPart.getChartComposite().getDisplay(), new Rectangle( 0, 0, 16, 16 ) );
      ChartPartUtil.drawLegendIcon( m_chartPart, chartLayer.getId(), img );
      m_images.put( chartLayer, img );
      return img;
    }

    return super.getImage( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage( final Object element, int columnIndex )
  {
    return getImage( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText( final Object element, final int columnIndex )
  {
    return getText( element );
  }
}
