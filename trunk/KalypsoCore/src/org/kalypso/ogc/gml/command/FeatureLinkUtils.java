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
package org.kalypso.ogc.gml.command;

import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * Some helper methods for easy handling of linked features.
 * 
 * @author Gernot Belger
 */
public final class FeatureLinkUtils
{
  private FeatureLinkUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Inserts or sets a link to a feature inside the same workspace.
   * 
   * @param pos
   *          Insert position. If <code>-1</code> the new element is inserted at the end of the list.
   */
  public static void insertLink( final Feature feature, final IRelationType linkRelation, final int pos, final String linkedFeatureID ) throws Exception
  {
    // test if feature exists in workspace
    final GMLWorkspace workspace = feature.getWorkspace();
    if( workspace.getFeature( linkedFeatureID ) == null )
      throw new Exception( "tried to link a feature that does not exist in the workspace" ); //$NON-NLS-1$

    final String href = "#" + linkedFeatureID; //$NON-NLS-1$

    if( linkRelation.isList() )
    {
      final FeatureList list = (FeatureList) feature.getProperty( linkRelation );
      list.insertLink( pos, href );
    }
    else
    {
      feature.setLink( linkRelation, href );
    }
  }
}
