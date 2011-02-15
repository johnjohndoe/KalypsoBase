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

import java.math.BigDecimal;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;

/**
 * This class containes data of a sobek profile, which will be stored in the file 'profile.dat'.<br>
 * <br>
 * <strong>NOTE:</strong><br>
 * It represents a cross section point (the intersection point of the cross section with the riverline).
 * 
 * @author Holger Albert
 */
public class SobekProfileDat
{
  /**
   * The id of the cross section location.
   */
  private final String m_id;

  /**
   * The id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id maps to a data block in the file 'profile.def'.
   */
  private final String m_di;

  /**
   * The reference level 1.
   */
  private final BigDecimal m_rl;

  /**
   * The reference level 2.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   */
  private final BigDecimal m_ll;

  /**
   * The surface level right.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   */
  private final BigDecimal m_rs;

  /**
   * The surface level left.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   */
  private final BigDecimal m_ls;

  /**
   * The constructor.
   * 
   * @param id
   *          The id of the cross section location.
   * @param di
   *          The id of the cross section definition. <strong>NOTE:</strong> This id maps to a data block in the file
   *          profile.def.
   * @param rl
   *          The reference level 1.
   * @param ll
   *          The reference level 2. <strong>NOTE:</strong> Optional
   * @param rs
   *          The surface level right. <strong>NOTE:</strong> Optional
   * @param ls
   *          The surface level left. <strong>NOTE:</strong> Optional
   */
  public SobekProfileDat( final String id, final String di, final BigDecimal rl, final BigDecimal ll, final BigDecimal rs, final BigDecimal ls )
  {
    m_id = id;
    m_di = di;
    m_rl = rl;
    m_ll = ll;
    m_rs = rs;
    m_ls = ls;
  }

  /**
   * This function returns the id of the cross section location.
   * 
   * @return The id of the cross section location.
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * This function returns the id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id maps to a data block in the file profile.def.
   * 
   * @return The id of the cross section definition.
   */
  public String getDi( )
  {
    return m_di;
  }

  /**
   * This function returns the reference level 1.
   * 
   * @return The reference level 1.
   */
  public BigDecimal getRl( )
  {
    return m_rl;
  }

  /**
   * This function returns the reference level 2.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   * 
   * @return The reference level 2.
   */
  public BigDecimal getLl( )
  {
    return m_ll;
  }

  /**
   * This function returns the surface level right.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   * 
   * @return The surface level right.
   */
  public BigDecimal getRs( )
  {
    return m_rs;
  }

  /**
   * This function returns the surface level left.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * Optional
   * 
   * @return The surface level left.
   */
  public BigDecimal getLs( )
  {
    return m_ls;
  }

  /**
   * This function validates the contained data.
   * 
   * @return A status.
   */
  public IStatus validate( )
  {
    if( m_id == null || m_id.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The id of the cross section location is mandatory..." ); //$NON-NLS-1$

    if( m_di == null || m_di.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The id of the cross section definition is mandatory..." ); //$NON-NLS-1$

    if( m_rl == null )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The reference level 1 is mandatory..." ); //$NON-NLS-1$

    return Status.OK_STATUS;
  }

  /**
   * This function serializes the data for the file 'profile.dat'.
   * 
   * @return The data for the file 'profile.dat'.
   */
  public String serialize( )
  {
    /* Create a string builder. */
    final StringBuilder line = new StringBuilder();

    /* Build the line. */
    line.append( String.format( Locale.PRC, "CRSN id '%s' di '%s' rl %.2f ", m_id, m_di, m_rl ) ); //$NON-NLS-1$
    if( m_ll != null )
      line.append( String.format( Locale.PRC, "ll %.2f ", m_ll ) ); //$NON-NLS-1$
    if( m_rs != null )
      line.append( String.format( Locale.PRC, "rs %.2f ", m_rs ) ); //$NON-NLS-1$
    if( m_ls != null )
      line.append( String.format( Locale.PRC, "ls %.2f ", m_ls ) ); //$NON-NLS-1$

    line.append( String.format( Locale.PRC, "crsn" ) ); //$NON-NLS-1$

    return line.toString();
  }
}