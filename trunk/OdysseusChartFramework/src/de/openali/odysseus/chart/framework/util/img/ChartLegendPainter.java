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
package de.openali.odysseus.chart.framework.util.img;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author Dirk Kuch
 */
public class ChartLegendPainter
{
  private final IChartModel m_model;

  private ITextStyle m_style = StyleUtils.getDefaultTextStyle();

  private final int m_maxImageWidth;

  private Point m_iconSize = new Point( 9, 9 );

  private final Point m_itemSpacer = new Point( 5, 8 );

  private final ILegendPaintStrategy m_strategy;

  public ChartLegendPainter( final IChartModel model, final int maximalImageWidth )
  {
    this( model, maximalImageWidth, new DefaultLegendStrategy() );
  }

  public ChartLegendPainter( final IChartModel model, final int maximalImageWidth, final ILegendPaintStrategy strategy )
  {
    m_model = model;
    m_maxImageWidth = maximalImageWidth;
    m_strategy = strategy;
  }

  public void setTextStyle( final ITextStyle style )
  {
    m_style = style;
  }

  public void setIconSize( final Point size )
  {
    m_iconSize = size;
  }

  public void setItemSpacer( final Point size )
  {
    m_iconSize = size;
  }

  public Point getSize( )
  {
    if( m_model.isHideLegend() )
      return new Point( 0, 0 );

    return m_strategy.getSize( this );
  }

  public Image createImage( )
  {
    if( m_model.isHideLegend() )
      return null;

    return m_strategy.createImage( this );
  }

  Point getSpacer( )
  {
    return new Point( 2, 0 );
  }

  IChartLayer[] getLayers( )
  {
    final Set<IChartLayer> visible = new LinkedHashSet<IChartLayer>();

    final IChartLayer[] layers = m_model.getLayerManager().getLayers();
    for( final IChartLayer layer : layers )
    {
      Collections.addAll( visible, getLayers( layer ) );
    }

    return visible.toArray( new IChartLayer[] {} );
  }

  private IChartLayer[] getLayers( final IChartLayer layer )
  {
    final Set<IChartLayer> visible = new LinkedHashSet<IChartLayer>();

    if( layer instanceof IExpandableChartLayer )
    {
      final IChartLayer[] children = ((IExpandableChartLayer) layer).getLayerManager().getLayers();
      for( final IChartLayer child : children )
      {
        Collections.addAll( visible, getLayers( child ) );
      }
    }
    else if( layer.isLegend() )
      visible.add( layer );

    return visible.toArray( new IChartLayer[] {} );
  }

  public int getMaximumWidth( )
  {
    return m_maxImageWidth;
  }

  public Point getIconSize( )
  {
    return m_iconSize;
  }

  public Point getItemSpacer( )
  {
    return m_itemSpacer;
  }

  public ITextStyle getTextStyle( )
  {
    return m_style;
  }

}
