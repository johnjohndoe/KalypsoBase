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
package org.kalypso.ui.editor.actions;

import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Utility class to help with actions on features.
 * 
 * @author Gernot Belger
 */
public class FeatureActionUtilities
{
  private FeatureActionUtilities( )
  {
    // will not get instantiated
  }

  /**
   * Search for a suitable name of the new feature action.
   * <p>
   * The name is searched as follow:
   * </p>
   * <p>
   * First the name-annotations, if no token replace takes place.
   * </p>
   * <p>
   * Second the label-annotations, if no token replace takes place.
   * </p>
   * <p>
   * Last, the local part of the feature type qname.
   * </p>
   */
  public static String newFeatureActionLabel( final IFeatureType featureType )
  {
    final IAnnotation annotation = featureType.getAnnotation();

    if( annotation != null && !FeatureHelper.hasReplaceTokens( featureType, IAnnotation.ANNO_NAME ) )
    {
      final String name = annotation.getValue( IAnnotation.ANNO_NAME );
      if( name != null )
        return name;
    }

    if( annotation != null && !FeatureHelper.hasReplaceTokens( featureType, IAnnotation.ANNO_LABEL ) )
    {
      final String name = annotation.getValue( IAnnotation.ANNO_LABEL );
      if( name != null )
        return name;
    }

    return featureType.getQName().getLocalPart();
  }

}
