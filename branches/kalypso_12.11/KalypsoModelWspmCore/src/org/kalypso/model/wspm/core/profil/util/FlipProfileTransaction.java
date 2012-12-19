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
package org.kalypso.model.wspm.core.profil.util;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;

/**
 * mirror the profiles points (axis 0.0)
 *
 * @author Dirk Kuch
 */
public class FlipProfileTransaction implements IProfileTransaction
{
  private static final String[] VEGETATION_ROUGHNESS_PROPERTIES = new String[] { IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY,
      IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST };

  @Override
  public IStatus execute( final IProfile profile )
  {
    final IComponent[] components = profile.getPointProperties();
    final IProfileRecord[] records = profile.getPoints();
    final int size = records.length;

    IProfileRecord ptrStack = records[0];

    for( int index = 0; index < size / 2; index++ )
    {
      final IProfileRecord clone = records[index];

      final int n = size - 1 - index;

      final IProfileRecord r0 = records[index];
      final IProfileRecord rn = records[n];

      for( int component = 0; component < components.length; component++ )
      {
        final String id = components[component].getId();

        if( IWspmPointProperties.POINT_PROPERTY_BREITE.equals( id ) )
        {
          final Double value = (Double) r0.getValue( component ) * -1;
          r0.setValue( component, (Double) rn.getValue( component ) * -1 );
          rn.setValue( component, value );
        }
        else if( ArrayUtils.contains( VEGETATION_ROUGHNESS_PROPERTIES, id ) )
        {
          final Object value = ptrStack.getValue( component );

          r0.setValue( component, records[n - 1].getValue( component ) );
          rn.setValue( component, value );
        }
        else
        {
          final Object value = r0.getValue( component );
          r0.setValue( component, rn.getValue( component ) );
          rn.setValue( component, value );
        }
      }

      ptrStack = clone;
    }

    if( size / 2 * 2 < size )
    {
      final IProfileRecord middle = records[size / 2];
      final Double breite = middle.getBreite();
      middle.setBreite( breite * -1 );
    }

    return Status.OK_STATUS;
  }

}
