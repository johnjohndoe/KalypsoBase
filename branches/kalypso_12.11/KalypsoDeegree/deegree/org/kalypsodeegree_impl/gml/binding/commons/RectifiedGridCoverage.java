/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.binding.commons;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * TODO: add setters/getters for the coverage-function
 * 
 * @author Dejan Antanaskovic, Gernot Belger
 */
public class RectifiedGridCoverage extends AbstractCoverage
{
  public static final QName QNAME = new QName( NS.GML3, "RectifiedGridCoverage" ); //$NON-NLS-1$

  public static final QName QNAME_PROP_GRID_DOMAIN = new QName( NS.GML3, "rectifiedGridDomain" ); //$NON-NLS-1$

  public RectifiedGridCoverage( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public RectifiedGridDomain getGridDomain( )
  {
    return getProperty( QNAME_PROP_GRID_DOMAIN, RectifiedGridDomain.class );
  }

  /**
   * Sets the grid domain, also updates the boundedBy property.
   * 
   * @param gridDomain
   *          The gridDomain to set.
   */
  public void setGridDomain( final RectifiedGridDomain gridDomain )
  {
    setProperty( QNAME_PROP_GRID_DOMAIN, gridDomain );

    try
    {
      final GM_Envelope envelope = gridDomain.getGM_Envelope( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      setProperty( QN_BOUNDED_BY, envelope );
      setEnvelopesUpdated();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }
}