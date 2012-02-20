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
package org.kalypsodeegree_impl.model.feature;

import org.kalypso.contribs.javax.xml.namespace.QNameUnique;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.filterencoding.AbstractFilter;

/**
 * Filter implementation that evaluates to <code>true</code> if a {@link Feature} has a certain feature type.
 *
 * @author Gernot Belger
 */
public class FeatureTypeFilter extends AbstractFilter
{
  private final QNameUnique m_typename;

  private final QNameUnique m_localTypename;

  private final boolean m_acceptIfSubstituting;

  public FeatureTypeFilter( final IFeatureType ft, final boolean acceptIfSubstituting )
  {
    this( ft.getQName(), ft.getLocalQName(), acceptIfSubstituting );
  }

  public FeatureTypeFilter( final QNameUnique typename, final QNameUnique localQName, final boolean acceptIfSubstituting )
  {
    m_typename = typename;
    m_localTypename = localQName;
    m_acceptIfSubstituting = acceptIfSubstituting;
  }

  @Override
  public boolean evaluate( final Feature feature )
  {
    return matchesType( feature );
  }

  @Override
  public StringBuffer toXML( )
  {
    throw new UnsupportedOperationException();
  }

  public boolean matchesType( final Feature f )
  {
    if( f == null )
      return false;

    final IFeatureType featureType = f.getFeatureType();
    if( m_localTypename != null && m_localTypename == featureType.getLocalQName() )
      return true;

    /* If only a local typename was given, no further search necessary */
    if( m_typename == null )
      return false;

    if( m_typename == featureType.getQName() )
      return true;

    if( m_acceptIfSubstituting )
      return GMLSchemaUtilities.substitutes( featureType, m_typename, m_localTypename );

    return false;
  }
}