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
package org.kalypso.ogc.gml.map.widgets.providers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * Feature provider, which uses a qname, which all returned features must substitute.
 * 
 * @author Holger Albert
 */
public class QNameFeaturesProvider implements IFeaturesProvider
{
  /**
   * The qname, which must be substituted.
   */
  private final QName m_qname;

  /**
   * The constructor.
   * 
   * @param qname
   *          The qname, which must be substituted.
   */
  public QNameFeaturesProvider( final QName qname )
  {
    m_qname = qname;
  }

  @Override
  public EasyFeatureWrapper[] getFeatures( final IMapPanel mapPanel )
  {
    /* Get all themes. */
    final IKalypsoTheme[] allThemes = mapPanel.getMapModell().getAllThemes();

    /* The list of found features. */
    final List<EasyFeatureWrapper> foundfeatures = new ArrayList<>();

    /* Check each theme. */
    for( final IKalypsoTheme theme : allThemes )
      inspectTheme( foundfeatures, theme );

    return foundfeatures.toArray( new EasyFeatureWrapper[foundfeatures.size()] );
  }

  /**
   * This function inspects one theme.
   * 
   * @param found
   *          To this list, the found features will be added.
   * @param theme
   *          A kalypso theme.
   */
  private void inspectTheme( final List<EasyFeatureWrapper> found, final IKalypsoTheme theme )
  {
    /* Kalypso feature theme. */
    if( theme instanceof IKalypsoFeatureTheme )
    {
      handleTheme( found, (IKalypsoFeatureTheme)theme );
      return;
    }

    /* Abstract cascading theme. */
    if( theme instanceof IKalypsoCascadingTheme )
    {
      final IKalypsoCascadingTheme cascadingTheme = (IKalypsoCascadingTheme)theme;
      final IKalypsoTheme[] themes = cascadingTheme.getAllThemes();
      for( final IKalypsoTheme ct : themes )
        inspectTheme( found, ct );

      return;
    }

    /* Others are not handled at the moment. */
  }

  /**
   * This function handles one theme.
   * 
   * @param found
   *          To this list, the found features will be added.
   * @param theme
   *          A kalypso feature theme.
   */
  private void handleTheme( final List<EasyFeatureWrapper> found, final IKalypsoFeatureTheme theme )
  {
    /* Get the workspace. */
    final CommandableWorkspace workspace = theme.getWorkspace();

    /* Get all visible features. */
    final FeatureList featureList = theme.getFeatureListVisible( null );

    if( featureList != null )
    {
      for( final Object object : featureList )
      {
        Feature feature = null;
        if( object instanceof Feature )
          feature = (Feature)object;
        else if( object instanceof String )
          feature = workspace.getFeature( (String)object );
        else
          continue;

        final IFeatureType targetFeatureType = feature.getFeatureType();
        if( GMLSchemaUtilities.substitutes( targetFeatureType, m_qname ) )
          found.add( new EasyFeatureWrapper( workspace, feature ) );
      }
    }
  }
}