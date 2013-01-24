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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.jface.viewers.ITooltipProvider;

/**
 * The this label provider modifies some labels for handling themes, that have only one style.
 * 
 * @author Gernot Belger
 */
public class ThemeNodeLabelProvider extends LabelProvider implements IFontProvider, ITooltipProvider
{
  private ResourceManager m_resourceManager;

  /**
   * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_resourceManager != null )
    {
      m_resourceManager.dispose();
      m_resourceManager = null;
    }

    super.dispose();
  }

  /**
   * Lazy load the resource manager
   * 
   * @return The resource manager, create one if necessary
   */
  private ResourceManager getResourceManager( )
  {
    if( m_resourceManager == null )
      m_resourceManager = new LocalResourceManager( JFaceResources.getResources() );

    return m_resourceManager;
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    final IThemeNode node = (IThemeNode) element;
    return node.getLabel();
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    final IThemeNode node = (IThemeNode) element;
    final ImageDescriptor descriptor = node.getImageDescriptor();
    if( descriptor == null )
      return null;

    return (Image) getResourceManager().get( descriptor );
  }

  /**
   * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {
    final IThemeNode node = (IThemeNode) element;
    return ((IFontProvider) node).getFont( element );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.viewers.ITooltipProvider#getTooltip(java.lang.Object)
   */
  @Override
  public String getTooltip( final Object element )
  {
    final IThemeNode node = (IThemeNode) element;
    return node.getDescription();
  }
}