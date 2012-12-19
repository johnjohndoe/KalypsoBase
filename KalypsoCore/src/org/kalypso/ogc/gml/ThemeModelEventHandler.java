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
package org.kalypso.ogc.gml;

import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree.model.feature.event.FeaturesChangedModellEvent;
import org.kalypsodeegree.model.feature.event.IGMLWorkspaceModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Helper class that decides how to invalidate a {@link KalypsoFeatureTheme} if the underlying workspace model is
 * changed.
 * 
 * @author Gernot Belger
 */
class ThemeModelEventHandler
{
  private final ModellEvent m_event;

  private final KalypsoFeatureTheme m_theme;

  static FeatureThemeInvalidation calculateInvalidation( final KalypsoFeatureTheme theme, final ModellEvent event )
  {
    return new ThemeModelEventHandler( theme, event ).calculateInvalidation();
  }

  public ThemeModelEventHandler( final KalypsoFeatureTheme theme, final ModellEvent event )
  {
    m_theme = theme;
    m_event = event;
  }

  private FeatureThemeInvalidation calculateInvalidation( )
  {
    if( m_event instanceof IGMLWorkspaceModellEvent )
    {
      /* my workspace ? */
      final GMLWorkspace changedWorkspace = ((IGMLWorkspaceModellEvent)m_event).getGMLWorkspace();
      final CommandableWorkspace themeWorkspace = m_theme.getWorkspace();

      if( themeWorkspace != null && changedWorkspace != themeWorkspace && changedWorkspace != themeWorkspace.getWorkspace() )
      {
        /* not my workspace */
        return null;
      }

      if( m_event instanceof FeaturesChangedModellEvent )
      {
        final FeaturesChangedModellEvent featuresChangedModellEvent = (FeaturesChangedModellEvent)m_event;
        return handleFeaturesChanged( featuresChangedModellEvent );
      }
      else if( m_event instanceof FeatureStructureChangeModellEvent )
      {
        final FeatureStructureChangeModellEvent fscme = (FeatureStructureChangeModellEvent)m_event;
        return handleStructureChanged( fscme );
      }
    }

    /* unknown event type, invalidate everything */
    return new FeatureThemeInvalidation( null, true );
  }

  private FeatureThemeInvalidation handleFeaturesChanged( final FeaturesChangedModellEvent featuresChangedModellEvent )
  {
    final Feature[] features = featuresChangedModellEvent.getFeatures();

    final FeatureList featureList = m_theme.getFeatureList();
    final boolean isSingleFeature = m_theme.isSingleFeature();

    // HACK: for single-feature lists (see flag), we must invalidate the list ourselves.
    if( isSingleFeature )
    {
      // TODO: we do not know which one of the changed features is the right one... (ses FIXME below)
      // So we just invalidate all features in this (singleton) list
      for( final Feature feature : features )
        featureList.invalidate( feature );
    }

    if( features.length > 100 )
    {
      // OPTIMIZATION: as List#contains is quite slow, we generally repaint if the number of changed features
      // is too large.
      return new FeatureThemeInvalidation( null, true );
    }

    /* Calculate bbox of changed features */

    // REMARK: this may not result in the correct result, as we also need the envelope before the change here...
    GM_Envelope invalidBox = null;
    for( final Feature feature : features )
    {
      if( featureList.contains( feature ) || featureList.contains( feature.getId() ) )
      {
        final GM_Envelope envelope = feature.getEnvelope();
        if( invalidBox == null )
          invalidBox = envelope;
        else
          invalidBox = invalidBox.getMerged( envelope );
      }
    }

    if( invalidBox == null )
    {
      /* nothing to invalidate */
      // TODO: wrong: what, if the old feature had a bbox before?
      return null;
    }

    // TODO: buffer: does not work well for points, or fat-lines
    return new FeatureThemeInvalidation( invalidBox, true );
  }

  private FeatureThemeInvalidation handleStructureChanged( final FeatureStructureChangeModellEvent fscme )
  {
    final Feature[] parents = fscme.getParentFeatures();
    final boolean isMyParent = isMyParent( parents );
    if( !isMyParent )
      return null;

    switch( fscme.getChangeType() )
    {
      case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD:
      {
        // FIXME: cannot possibly work when new linked features (string links) are added....

        // OPTIMIZATION: invalidate bbox of really changed features (for add we do not need the old extent)
        // also, only invalidate full extent if freshly added features are outside the old full extent
        // REMARK: this extremely improves performance adding 2d elements in Kalypso1D2D
        final Feature[] changedFeatures = fscme.getChangedFeatures();
        if( changedFeatures.length == 0 )
          return new FeatureThemeInvalidation( null, true );

        final GM_Envelope envelope = FeatureHelper.getEnvelope( changedFeatures );

        if( envelope == null )
          return new FeatureThemeInvalidation( null, false );

        final boolean shouldInvalidateExtents = !isInsideFullExtent( envelope );

        return new FeatureThemeInvalidation( envelope, shouldInvalidateExtents );
      }

      case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE:
        // fall through
      case FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_MOVE:
        // fall through
      default:
        return new FeatureThemeInvalidation( m_theme.getFullExtent(), true );
    }
  }

  private boolean isInsideFullExtent( final GM_Envelope envelope )
  {
    // REMARK: use internal full extent ot avoid recalculation of extent here, because that may cause a major performance leak.
    // If the extent is currently not known, returning false is not bad, because the extent needs to be recalculate anyways.
    final GM_Envelope fullExtent = m_theme.getFullExtentInternal();
    if( fullExtent == null )
      return false;

    return fullExtent.contains( envelope );
  }

  private boolean isMyParent( final Feature[] parents )
  {
    final FeatureList featureList = m_theme.getFeatureList();

    for( final Feature parent : parents )
    {
      if( featureList.getOwner() == parent )
        return true;
    }

    return false;
  }
}