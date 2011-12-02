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
package org.kalypso.model.wspm.core.profil.filter;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.observation.result.IRecord;

/**
 * @author Gernot Belger
 */
public abstract class AbstractProfilePointFilter implements IProfilePointFilter, IExecutableExtension
{
  private String m_id;

  private String m_name;

  private String m_description;

  private String m_usageHint;

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getId()
   */
  @Override
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getName()
   */
  @Override
  public String getName( )
  {
    return m_name;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getUsageHint()
   */
  @Override
  public String getUsageHint( )
  {
    return m_usageHint;
  }

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  @Override
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    m_id = config.getAttribute( "id" ); //$NON-NLS-1$
    m_name = config.getAttribute( "name" ); //$NON-NLS-1$
    m_description = config.getAttribute( "description" ); //$NON-NLS-1$
    m_usageHint = config.getAttribute( "usageHint" ); //$NON-NLS-1$
  }

  protected boolean isBetweenMarkers( final IProfil profil, final IRecord point, final IProfilPointMarker leftMarker, final IProfilPointMarker rightMarker )
  {
    final IRecord[] points = profil.getPoints();
    if( points.length < 1 )
      return false;

    final IRecord leftPoint = leftMarker == null ? points[0] : leftMarker.getPoint();
    final IRecord rightPoint = rightMarker == null ? points[points.length - 1] : rightMarker.getPoint();

    final int leftPointIndex = ArrayUtils.indexOf( points, leftPoint );
    final int rightPointIndex = ArrayUtils.indexOf( points, rightPoint );

    final int index = ArrayUtils.indexOf( points, point );

    final int left = Math.min( leftPointIndex, rightPointIndex );
    final int right = Math.max( leftPointIndex, rightPointIndex );

    if( left == right && index == right )
      return true;

    return left <= index && index < right;
  }

}