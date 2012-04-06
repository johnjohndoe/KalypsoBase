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
package org.kalypso.ogc.gml.outline.nodes;

import org.apache.commons.lang3.StringUtils;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.command.ICommand;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;

/**
 * @author Gernot Belger
 */
public class WMSLayerNode extends AbstractThemeNode<Layer>
{
  private final WMSLayerLegendJob m_legendImageJob = new WMSLayerLegendJob( this );

  public WMSLayerNode( final IThemeNode parent, final Layer layer )
  {
    super( parent, layer );

    // FIXME
    // m_legendImageJob.schedule( 500 );
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    m_legendImageJob.dispose();
  }

  @Override
  public String getLabel( )
  {
    final Layer element = getElement();

    final String title = element.getTitle();
    if( StringUtils.isBlank( title ) )
      return element.getName();

    return title;
  }

  @Override
  public String getDescription( )
  {
    final Layer element = getElement();
    final String description = element.getAbstract();
    if( StringUtils.isEmpty( description ) )
      return null;

    return description;
  }

  @Override
  protected Object[] getElementChildren( )
  {
    final Layer element = getElement();
    return element.getLayer();
  }

  @Override
  public boolean isChecked( final Object element )
  {
    final KalypsoWMSTheme theme = getWMSTheme();
    if( theme == null )
    {
      /* should never happen */
      return false;
    }

    return theme.isLayerVisible( getElement().getName() );
  }

  @Override
  public ICommand setVisible( final boolean visible )
  {
    final KalypsoWMSTheme wmsTheme = getWMSTheme();
    if( wmsTheme == null )
      return super.setVisible( visible );

    final Layer element = getElement();
    final String name = element.getName();
    return new ChangeWMSLayerVisibilityCommand( wmsTheme, name, visible );
  }

  private KalypsoWMSTheme getWMSTheme( )
  {
    final IThemeNode parent = getParent();
    if( parent instanceof WMSThemeNode )
      return ((WMSThemeNode) parent).getTheme();

    if( parent instanceof WMSLayerNode )
      return ((WMSLayerNode) parent).getWMSTheme();

    return null;
  }

  @Override
  public boolean isGrayed( final Object element )
  {
    return false;
  }

  @Override
  public Image getLegendGraphic( final String[] whiteList, final boolean onlyVisible, final Font font )
  {
    final Image image = m_legendImageJob.getImage();
    if( image != null )
      return image;

    // FIXME
// if( m_legendImageJob.getState() != Job.RUNNING )
// m_legendImageJob.schedule( 250 );

    return null;
  }

  public String getStyle( )
  {
    final KalypsoWMSTheme wmsTheme = getWMSTheme();
    if( wmsTheme == null )
      return null;

    return wmsTheme.getStyle( getElement() );
  }
}