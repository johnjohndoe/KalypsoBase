/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.profil.sobek.struct;

/**
 * Represents one line of the sobek struct.dat file.
 * 
 * @author Gernot Belger
 */
public class SobekStructDat
{
  private final String m_id;

  private final String m_name;

  private final String m_dd;

  public SobekStructDat( final String id, final String name, final String dd )
  {
    m_id = id;
    m_name = name;
    m_dd = dd;
  }

  public String getID( )
  {
    return m_id;
  }

  public String getName( )
  {
    return m_name;
  }

  public String getDD( )
  {
    return m_dd;
  }

  /**
   * This function serializes the data for the file 'profile.dat'.
   * 
   * @return The data for the file 'profile.dat'.
   */
  public String serialize( )
  {
    // todo: maybe use ca/cj/cm for controlled elements

    return String.format( "STRU id '%s' nm '%s' dd '%s' stru", m_id, m_name, m_dd ); //$NON-NLS-1$
  }

}