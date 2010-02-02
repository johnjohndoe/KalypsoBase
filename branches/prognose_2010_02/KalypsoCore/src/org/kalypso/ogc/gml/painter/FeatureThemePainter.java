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
package org.kalypso.ogc.gml.painter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Gernot Belger
 */
class FeatureThemePainter implements IStylePainter
{
  private static final String STRING_PAINTING_STYLES = Messages.getString( "org.kalypso.ogc.gml.KalypsoFeatureTheme.1" );

  private final IKalypsoFeatureTheme m_theme;

  private final Boolean m_paintSelected;

  FeatureThemePainter( final IKalypsoFeatureTheme theme, final Boolean paintSelected )
  {
    m_theme = theme;
    m_paintSelected = paintSelected;
  }

  /**
   * @see org.kalypso.ogc.gml.painter.IStylePainter#paint(org.kalypso.ogc.gml.IPaintDelegate, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void paint( final IStylePaintable paintable, final IProgressMonitor monitor ) throws CoreException
  {
    final IKalypsoStyle[] styleforPaint = getStylesForPaint();

    final SubMonitor progress = SubMonitor.convert( monitor, STRING_PAINTING_STYLES, styleforPaint.length ); //$NON-NLS-1$

    final List<Feature> features = getFeatures( paintable.getBoundingBox() );

    for( final IKalypsoStyle style : styleforPaint )
    {
      final SubMonitor childProgress = progress.newChild( 1 );

      final IStylePainter painter = StylePainterFactory.create( style, features );
      painter.paint( paintable, childProgress );
    }

    ProgressUtilities.done( progress );
  }

  private List<Feature> getFeatures( final GM_Envelope bbox )
  {
    final GMLWorkspace workspace = m_theme.getWorkspace();

    /* Only paint features contained in the current bounding box */
    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
      return Collections.emptyList();

    final List< ? > visibleFeatures = featureList.query( bbox, null );

    return new ResolvedFeatureList( workspace, visibleFeatures );
  }

  private IKalypsoStyle[] getStylesForPaint( )
  {
    final IKalypsoStyle[] styles = m_theme.getStyles();

    final List<IKalypsoStyle> normalStyles = new ArrayList<IKalypsoStyle>( styles.length );
    final List<IKalypsoStyle> selectionStyles = new ArrayList<IKalypsoStyle>( styles.length );
    for( final IKalypsoStyle style : styles )
    {
      if( style.isUsedForSelection() )
        selectionStyles.add( style );
      else
        normalStyles.add( style );
    }

    /* If no selection style is present, we will paint with old HighlightGraphics stuff, so return normal styles. */
    if( m_paintSelected == null || m_paintSelected == false || selectionStyles.size() == 0 )
      return normalStyles.toArray( new IKalypsoStyle[normalStyles.size()] );

    return selectionStyles.toArray( new IKalypsoStyle[selectionStyles.size()] );
  }

}
