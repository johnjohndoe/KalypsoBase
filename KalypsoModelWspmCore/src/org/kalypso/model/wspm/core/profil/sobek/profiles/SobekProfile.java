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
package org.kalypso.model.wspm.core.profil.sobek.profiles;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;

/**
 * A sobek profile.
 * 
 * @author Holger Albert
 */
public class SobekProfile
{
  /**
   * The data for the file 'profile.dat'.
   */
  private final SobekProfileDat m_profileDat;

  /**
   * The data for the file 'profile.def'.
   */
  private final SobekProfileDef m_profileDef;

  /**
   * The constructor.
   * 
   * @param profileDat
   *          The data for the file 'profile.dat'.
   * @param profileDef
   *          The data for the file 'profile.def'.
   */
  public SobekProfile( final SobekProfileDat profileDat, final SobekProfileDef profileDef )
  {
    m_profileDat = profileDat;
    m_profileDef = profileDef;
  }

  public IStatus validate( )
  {
    if( m_profileDat == null )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "No data for the file 'profile.dat' available..." );

    if( m_profileDef == null )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "No data for the file 'profile.def' available..." );

    final IStatus statusDat = m_profileDat.validate();
    if( !statusDat.isOK() )
      return statusDat;

    final IStatus statusDef = m_profileDef.validate();
    if( !statusDef.isOK() )
      return statusDef;

    final String di = m_profileDat.getDi();
    final String id = m_profileDef.getId();
    if( !di.equals( id ) )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The ids of the cross section definition does not match..." );

    // TODO Further checks...

    return Status.OK_STATUS;
  }

  /**
   * This function serializes the data for the file 'profile.dat'.
   * 
   * @return The data for the file 'profile.dat'.
   */
  public String serializeProfileDat( )
  {
    return m_profileDat.toString();
  }

  /**
   * This function serializes the data for the file 'profile.def'.
   * 
   * @return The data for the file 'profile.def'.
   */
  public String serializeProfileDef( )
  {
    return m_profileDef.toString();
  }
}