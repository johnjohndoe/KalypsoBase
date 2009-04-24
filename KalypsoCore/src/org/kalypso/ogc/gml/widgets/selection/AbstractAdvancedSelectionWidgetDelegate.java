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
package org.kalypso.ogc.gml.widgets.selection;

import java.awt.Graphics;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;


/**
 * @author kuch
 *
 */
public abstract class AbstractAdvancedSelectionWidgetDelegate implements IAdvancedSelectionWidgetDelegate
{
  private final IAdvancedSelectionWidget m_widget;

  private final IAdvancedSelectionWidgetDataProvider m_provider;

  public AbstractAdvancedSelectionWidgetDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider )
  {
    m_widget = widget;
    m_provider = provider;
  }

  protected IAdvancedSelectionWidget getWidget( )
  {
    return m_widget;
  }

  protected IAdvancedSelectionWidgetDataProvider getDataProvider( )
  {
    return m_provider;
  }
  
  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.IAdvancedSelectionWidgetDelegate#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    try
    {
      // underlying features
      final GM_Point point = m_widget.getCurrentGmPoint();
      final Feature[] features = m_provider.query( point, 0.1, getWidget().getEditMode() );
      
      // highlight these features
      for( final Feature feature : features )
      {
        highlightUnderlying(feature, g);
      }
    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  protected abstract void highlightUnderlying( final Feature feature, final Graphics g );

}
