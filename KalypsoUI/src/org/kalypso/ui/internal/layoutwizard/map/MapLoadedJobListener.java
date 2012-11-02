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
package org.kalypso.ui.internal.layoutwizard.map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.widgets.IWidget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public class MapLoadedJobListener extends JobChangeAdapter
{
  private final IMapPanel m_panel;

  private final GM_Envelope m_wishBoundingBox;

  private final String m_selectFid;

  private final String m_panFid;

  private final IWidget m_widget;

  public MapLoadedJobListener( final IMapPanel panel, final GM_Envelope wishBoundingBox, final String selectFid, final String panFid, final IWidget widget )
  {
    m_panel = panel;
    m_selectFid = selectFid;
    m_panFid = panFid;
    m_wishBoundingBox = wishBoundingBox;
    m_widget = widget;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void done( final IJobChangeEvent event )
  {
    final IStatus result = event.getResult();
    if( result.isOK() )
      handleMapLoadingSuccesfully();
    else
      handleMapLoadingFailure();
  }

  private void handleMapLoadingFailure( )
  {
    // ignore at the moment
  }

  private void handleMapLoadingSuccesfully( )
  {
    selectFeature();

    setBoundingBox();

    m_panel.getWidgetManager().addWidget( m_widget );
  }

  private void selectFeature( )
  {
    final IKalypsoTheme activeTheme = m_panel.getMapModell().getActiveTheme();
    if( !(activeTheme instanceof IKalypsoFeatureTheme) )
      return;

    final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;

    final Feature feature = findSelectFeature( kft );
    if( feature == null )
      return;

    final CommandableWorkspace workspace = kft.getWorkspace();

    final EasyFeatureWrapper easyFeatureWrapper = new EasyFeatureWrapper( workspace, feature );

    final EasyFeatureWrapper[] easyArray = new EasyFeatureWrapper[] { easyFeatureWrapper };
    m_panel.getSelectionManager().setSelection( easyArray );
  }

  private void setBoundingBox( )
  {
    final GM_Envelope boundingBox = determineBoundingBox();
    m_panel.setBoundingBox( boundingBox, false );
  }

  private GM_Envelope determineBoundingBox( )
  {
    final GM_Envelope wishBox = getWishBox();
    // May happen if map is closed while being loaded
    if( wishBox == null )
      return null;

    final IMapModell mapModell = m_panel.getMapModell();
    if( mapModell == null )
      return null;

    final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
    if( !(activeTheme instanceof IKalypsoFeatureTheme) )
      return m_wishBoundingBox;

    final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;

    final Feature panFeature = findPanFeature( kft );
    if( panFeature == null )
      return wishBox;

    // REMARK: should'nt we use the bounding box of the feature instead of the default geometry? Or even let the user
    // define it?
    final GM_Object defaultGeometryProperty = panFeature.getDefaultGeometryPropertyValue();
    final GM_Point centroid = defaultGeometryProperty == null ? null : defaultGeometryProperty.getCentroid();

    return centroid == null ? wishBox : wishBox.getPaned( centroid );
  }

  private GM_Envelope getWishBox( )
  {
    if( m_wishBoundingBox == null )
    {
      final IMapModell mapModell = m_panel.getMapModell();
      if( mapModell == null )
        return null;

      final GM_Envelope fullExtent = mapModell.getFullExtentBoundingBox();
      final double buffer = Math.max( fullExtent.getWidth(), fullExtent.getHeight() ) * 0.025;
      return fullExtent.getBuffer( buffer );
    }

    return m_wishBoundingBox;
  }

  private Feature findSelectFeature( final IKalypsoFeatureTheme theme )
  {
    if( m_selectFid == null )
      return getFirstFeature( theme );

    if( m_selectFid.isEmpty() )
      return null;

    final CommandableWorkspace workspace = theme.getWorkspace();
    return workspace.getFeature( m_selectFid );
  }

  private Feature findPanFeature( final IKalypsoFeatureTheme theme )
  {
    if( m_panFid == null || m_panFid.isEmpty() )
      return null;

    return theme.getWorkspace().getFeature( m_panFid );
  }

  private Feature getFirstFeature( final IKalypsoFeatureTheme theme )
  {
    final FeatureList featureList = theme.getFeatureListVisible( null );
    if( featureList.isEmpty() )
      return null;

    final Object object = featureList.get( 0 );
    return FeatureHelper.getFeature( theme.getWorkspace(), object );
  }

}
