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

import java.io.PrintWriter;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;

/**
 * This class containes data of a tabulated sobek profile, which will be stored in the file 'profile.def'.
 * 
 * @author Holger Albert
 */
public class SobekProfileDef
{
  /**
   * The id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id is referenced from a line in the file 'profile.dat'.
   */
  private final String m_id;

  /**
   * The name of the cross section definition.
   */
  private final String m_nm;

  private final ISobekProfileDefData m_data;

// /**
// * The type of the cross section (0=table).
// */
// private final int m_ty;

  /**
   * @param id
   *          The id of the cross section definition. <strong>NOTE:</strong> This id is referenced from a line in the
   *          file profile.dat.
   * @param nm
   *          The name of the cross section definition.
   */
  public SobekProfileDef( final String id, final String nm, final ISobekProfileDefData data )
  {
    m_id = id;
    m_nm = nm;
    m_data = data;
  }

  /**
   * This function returns the id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id is referenced from a line in the file profile.dat.
   * 
   * @return The id of the cross section definition.
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * This function returns the name of the cross section definition.
   * 
   * @return The name of the cross section definition.
   */
  public String getNm( )
  {
    return m_nm;
  }

  /**
   * This function returns the type of the cross section (0=table).
   * 
   * @return The type of the cross section (0=table).
   */
  public int getTy( )
  {
    return m_data.getType();
  }

  /**
   * This function validates the contained data.
   * 
   * @return A status.
   */
  public IStatus validate( )
  {
    if( m_id == null || m_id.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The id of the cross section definition is mandatory..." ); //$NON-NLS-1$

    if( m_nm == null || m_nm.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The name of the cross section definition is mandatory..." ); //$NON-NLS-1$

    return Status.OK_STATUS;
  }

  /**
   * This function serializes the data for the file 'profile.def'.
   * 
   * @return The data for the file 'profile.def'.
   */
  public void serialize( final PrintWriter writer )
  {
    writer.format( Locale.PRC, "CRDS id '%s' nm '%s' ty %d ", m_id, m_nm, m_data.getType() ); //$NON-NLS-1$

    m_data.writeContent( writer );

    writer.print( "crds" ); //$NON-NLS-1$
  }

  public ISobekProfileDefData getData( )
  {
    return m_data;
  }
}