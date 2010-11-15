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

import de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlTextLayer extends AbstractChartLayer
{
  private final ITextStyle m_style;

  private final String m_text;

  public ZmlTextLayer( final String id, final String text, final ITextStyle style )
  {
    m_text = text;
    m_style = style;

    setId( id );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    if( !isVisible() )
      return;

    final ICoordinateMapper mapper = getCoordinateMapper();

    /**
     * determine center point of screen
     */
    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final double domainCenter = domainRange.getMin().doubleValue() + (domainRange.getMax().doubleValue() - domainRange.getMin().doubleValue()) / 2.0;
    final double targetCenter = targetRange.getMin().doubleValue() + (targetRange.getMax().doubleValue() - targetRange.getMin().doubleValue()) / 2.0;

    final Point center = new Point( domainAxis.numericToScreen( domainCenter ), targetAxis.numericToScreen( targetCenter ) );

    /*
     * FIXME - implement helper class to configure gc (set and disposing styles and color, perhaps drawing of figures,
     * too)
     */
// final FontData fontData = m_style.toFontData();
// final Font font = new Font( gc.getDevice(), fontData );
// final Color color = new Color( gc.getDevice(), m_style.getTextColor() );

    try
    {
      m_style.apply( gc );
// gc.setFont( font );
// gc.setForeground( color );
// gc.setAlpha( m_style.getAlpha() );

      /** compute textExtend */
      final Point extent = gc.textExtent( m_text );
      gc.drawText( m_text, center.x - extent.x / 2, center.y - extent.y / 2 );
    }
    finally
    {
// font.dispose();
// color.dispose();
    }
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
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

}
