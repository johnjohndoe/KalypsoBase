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

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.javax.xml.namespace.QNameUnique;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;


/**
 * @author Gernot Belger
 */
public class FilterByTypeStylePaintable implements IStylePaintable
{
  private final IStylePaintable m_paintable;

  private final QNameUnique m_featureTypeName;

  private final QNameUnique m_localFeatureTypeName;

  public FilterByTypeStylePaintable( final IStylePaintable paintable, final QName featureTypeName )
  {
    m_paintable = paintable;
    m_featureTypeName = QNameUnique.create( featureTypeName );
    m_localFeatureTypeName = m_featureTypeName.asLocal();
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    return m_paintable.getBoundingBox();
  }

  @Override
  public Double getScale( )
  {
    return m_paintable.getScale();
  }

  @Override
  public void paint( final DisplayElement displayElement, final IProgressMonitor monitor ) throws CoreException
  {
    m_paintable.paint( displayElement, monitor );
  }

  @Override
  public boolean shouldPaintFeature( final Feature feature )
  {
    /* Only paint features which applies to the given qname */
    if( !GMLSchemaUtilities.substitutes( feature.getFeatureType(), m_featureTypeName, m_localFeatureTypeName ) )
      return false;

    return m_paintable.shouldPaintFeature( feature );
  }

}
