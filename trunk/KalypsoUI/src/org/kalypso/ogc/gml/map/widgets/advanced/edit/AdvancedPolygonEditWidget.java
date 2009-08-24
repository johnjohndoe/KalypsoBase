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
package org.kalypso.ogc.gml.map.widgets.advanced.edit;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModeMultiDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModePointInsertDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModePointRemoveDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModeSingleDelegate;
import org.kalypso.ogc.gml.widgets.AbstractKeyListenerWidget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public class AdvancedPolygonEditWidget extends AbstractKeyListenerWidget implements IAdvancedEditWidget
{
  private enum EDIT_MODE
  {
    eMulti,
    eSingle,
    ePointInsert,
    ePointRemove;
  }

  private boolean m_leftMouseButtonPressed = false;

  private EDIT_MODE m_mode = EDIT_MODE.eMulti;

  Map<EDIT_MODE, IAdvancedEditWidgetDelegate> m_delegates = new HashMap<EDIT_MODE, IAdvancedEditWidgetDelegate>();

  private Point m_originPoint = null;

  private IAdvancedEditWidgetSnappedPoint[] m_snappedPointsAtOrigin = null;

  private final IAdvancedEditWidgetDataProvider m_provider;

  public AdvancedPolygonEditWidget( final IAdvancedEditWidgetDataProvider provider )
  {
    super( Messages.getString("org.kalypso.ogc.gml.map.widgets.advanced.edit.AdvancedPolygonEditWidget.0") ); //$NON-NLS-1$
    m_provider = provider;

    m_delegates.put( EDIT_MODE.eMulti, new AdvancedEditModeMultiDelegate( this, provider ) );
    m_delegates.put( EDIT_MODE.eSingle, new AdvancedEditModeSingleDelegate( this, provider ) );
    m_delegates.put( EDIT_MODE.ePointInsert, new AdvancedEditModePointInsertDelegate( this, provider ) );
    m_delegates.put( EDIT_MODE.ePointRemove, new AdvancedEditModePointRemoveDelegate( this, provider ) );

  }

  private IAdvancedEditWidgetDelegate getCurrentDelegate( )
  {
    return m_delegates.get( m_mode );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    paintToolTip( g );

    getCurrentDelegate().paint( g );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftPressed(java.awt.Point)
   */
  @Override
  public void leftPressed( final java.awt.Point p )
  {
    final GM_Point gmp = getCurrentGmPoint();
    if( gmp == null )
      return;

    m_leftMouseButtonPressed = true;

    try
    {
      m_originPoint = (Point) JTSAdapter.export( gmp );

      final Feature[] features = m_provider.query( gmp, 20 );
      if( ArrayUtils.isEmpty( features ) )
        return;

      // highlight detected feature points
      final Map<Geometry, Feature> mapGeometries = m_provider.resolveJtsGeometries( features );
      m_snappedPointsAtOrigin = resolveSnapPoints( mapGeometries );
    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final java.awt.Point p )
  {
    getCurrentDelegate().leftReleased( p );
    
    m_leftMouseButtonPressed = false;

    m_originPoint = null;
    m_snappedPointsAtOrigin = null;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    return String.format( Messages.getString("org.kalypso.ogc.gml.map.widgets.advanced.edit.AdvancedPolygonEditWidget.1"), getCurrentDelegate().getToolTip() ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IAdvancedEditWidget#getOriginPoint()
   */
  @Override
  public Point getOriginPoint( )
  {
    return m_originPoint;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IAdvancedEditWidget#getSnappedPointsAtOrigin()
   */
  @Override
  public IAdvancedEditWidgetSnappedPoint[] getSnappedPointsAtOrigin( )
  {
    return m_snappedPointsAtOrigin;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IAdvancedEditWidget#getIMapPanel()
   */
  @Override
  public IMapPanel getIMapPanel( )
  {
    return getMapPanel();
  }

  /**
   * Escape Key pressed? -> reset / deactivate widget
   * 
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    if( KeyEvent.VK_SPACE == keyCode )
    {
      switchMode();
    }

    super.keyReleased( e );
  }

  private void switchMode( )
  {
    if( EDIT_MODE.eMulti.equals( m_mode ) )
    {
      m_mode = EDIT_MODE.eSingle;
    }
    else if( EDIT_MODE.eSingle.equals( m_mode ) )
    {
      m_mode = EDIT_MODE.ePointInsert;
    }
    else if( EDIT_MODE.ePointInsert.equals( m_mode ) )
    {
      m_mode = EDIT_MODE.ePointRemove;
    }
    else if( EDIT_MODE.ePointRemove.equals( m_mode ) )
    {
      m_mode = EDIT_MODE.eMulti;
    }

    getMapPanel().repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IAdvancedEditWidget#isLeftMouseButtonPressed()
   */
  @Override
  public boolean isLeftMouseButtonPressed( )
  {
    return m_leftMouseButtonPressed;
  }

  public IAdvancedEditWidgetSnappedPoint[] resolveSnapPoints( final Map<Geometry, Feature> mapGeometries )
  {
    return AdvancedEditWidgetSnapper.findSnapPoints( mapGeometries, getOriginPoint(), getCurrentDelegate().getRange() );
  }
}
