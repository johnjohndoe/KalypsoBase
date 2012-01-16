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
package org.kalypso.model.wspm.ui.commands;

import org.eclipse.swt.SWT;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.profil.wrappers.ProfilePointWrapper;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractProfilePointHandler extends AbstractChartHandler
{
  private Double m_breite;

  private ProfilePointWrapper m_point;

  private ProfileWrapper m_profile;

  public AbstractProfilePointHandler( final IChartComposite chart )
  {
    super( chart );
  }

  protected final Double getBreite( )
  {
    return m_breite;
  }

  protected final void setBreite( final Double breite )
  {
    m_breite = breite;
  }

  protected final ProfilePointWrapper getPoint( )
  {
    return m_point;
  }

  protected final void setPoint( final ProfilePointWrapper point )
  {
    m_point = point;
  }

  protected final ProfileWrapper getProfile( )
  {
    return m_profile;
  }

  protected final void setProfile( final ProfileWrapper profile )
  {
    m_profile = profile;
  }

  protected final AbstractProfilTheme findProfileTheme( final IChartComposite chart )
  {
    final IChartModel model = chart.getChartModel();

    final FindLayerVisitor visitor = new FindLayerVisitor( IWspmLayers.LAYER_GELAENDE );
    model.getLayerManager().accept( visitor );

    final IChartLayer layer = visitor.getLayer();

    return (AbstractProfilTheme) layer;
  }

  protected final boolean isOutOfRange( )
  {
    final ProfilePointWrapper p0 = getProfile().getFirstPoint();
    final ProfilePointWrapper pn = getProfile().getLastPoint();

    if( getBreite() < p0.getBreite() )
      return true;
    else if( getBreite() > pn.getBreite() )
      return true;

    return false;
  }

  protected final void doReset( )
  {
    setProfile( null );
    setBreite( null );
    setPoint( null );

    setCursor( SWT.CURSOR_ARROW );
  }

}
