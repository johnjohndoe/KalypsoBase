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
package de.renew.workflow.utils;

/**
 * The scenario configuration.
 * 
 * @author Holger Albert
 */
public class ScenarioConfiguration
{
  /**
   * The derived folder. All derived scenarios will be saved in it within a parent scenario.
   */
  private final String m_derivedFolder;

  /**
   * The ignore folders. This folders should be ignored, according to their given role.
   */
  private final IgnoreFolder[] m_ignoreFolders;

  /**
   * The constructor.
   * 
   * @param derivedFolder
   *          The derived folder. All derived scenarios will be saved in it within a parent scenario.
   * @param ignoreFolders
   *          The ignore folders. This folders should be ignored, according to their given role.
   */
  public ScenarioConfiguration( final String derivedFolder, final IgnoreFolder[] ignoreFolders )
  {
    m_derivedFolder = derivedFolder;
    m_ignoreFolders = ignoreFolders;
  }

  /**
   * This function returns the derived folder. All derived scenarios will be saved in it within a parent scenario.
   * 
   * @return The derived folder. All derived scenarios will be saved in it within a parent scenario.
   */
  public String getDerivedFolder( )
  {
    return m_derivedFolder;
  }

  /**
   * This function returns the ignore folders. This folders should be ignored, according to their given role.
   * 
   * @return The ignore folders. This folders should be ignored, according to their given role.
   */
  public IgnoreFolder[] getIgnoreFolders( )
  {
    return m_ignoreFolders;
  }
}