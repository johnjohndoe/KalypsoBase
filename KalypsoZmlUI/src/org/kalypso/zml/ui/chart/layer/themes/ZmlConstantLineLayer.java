/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.chart.layer.themes;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlConstantLineLayer extends AbstractLineLayer
{
  private final ZmlConstantLineBean[] m_descriptors;

  private boolean m_calculateRange = false;

  public boolean isCalculateRange( )
  {
    return m_calculateRange;
  }

  public void setCalculateRange( final boolean calculateRange )
  {
    m_calculateRange = calculateRange;
  }

  public ZmlConstantLineLayer( final ZmlConstantLineBean[] descriptors, final boolean calculateRange )
  {
    super( null, null );
    m_descriptors = descriptors == null ? new ZmlConstantLineBean[] {} : descriptors;
    m_calculateRange = calculateRange;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    if( !m_calculateRange || m_descriptors == null || m_descriptors.length == 0 )
    {
      return null;
    }
    Number max = -Double.MAX_VALUE;
    Number min = Double.MAX_VALUE;
    if( m_calculateRange )
    {
      for( final ZmlConstantLineBean descriptor : m_descriptors )
      {
        max = Math.max( max.doubleValue(), descriptor.getValue().doubleValue() );
        min = Math.min( max.doubleValue(), descriptor.getValue().doubleValue() );
      }
    }

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final int screenSize = gc.getClipping().width;
    final int[] screens = getScreenValues();

    for( final ZmlConstantLineBean descriptor : m_descriptors )
    {
      final int screenValue = getTargetAxis().numericToScreen( descriptor.getValue() );
      final ILineStyle als = descriptor.getLineStyle();

      final PolylineFigure polylineFigure = new PolylineFigure();
      polylineFigure.setStyle( als );
      polylineFigure.setPoints( new Point[] { new Point( 0, screenValue ), new Point( screenSize, screenValue ) } );
      polylineFigure.paint( gc );

      if( descriptor.isShowLabel() )
      {
        getTextFigure().setStyle( descriptor.getTextStyle() );
        final String text = descriptor.getLabel();
        final Point ext = gc.textExtent( text );
        if( canDrawLabel( screens, screenValue, ext.y ) )
        {
          final Point leftTopPoint = new Point( screenSize - ext.x - 1, screenValue - ext.y / 2 - als.getWidth() );
          drawText( gc, text, leftTopPoint );
        }
      }
    }
  }

  private final boolean canDrawLabel( final int[] screens, final int value, final int size )
  {
    for( final int x : screens )
    {
      if( x > value && x < value + size )
        return false;
    }
    return true;
  }

  private final int[] getScreenValues( )
  {
    final int[] screens = new int[m_descriptors.length];
    for( int i = 0; i < m_descriptors.length; i++ )
    {
      screens[i] = getTargetAxis().numericToScreen( m_descriptors[i].getValue() );
    }
    return screens;
  }
}
