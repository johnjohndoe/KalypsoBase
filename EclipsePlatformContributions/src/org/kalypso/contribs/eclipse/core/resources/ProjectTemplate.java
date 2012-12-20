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
package org.kalypso.contribs.eclipse.core.resources;

import java.net.URL;

/**
 * Objects of this class represent extensions of the org.kalypso.model.wspm.tuhh.core.demoProject extension-point.
 *
 * @author Gernot Belger
 */
public class ProjectTemplate
{
  private final String m_id;

  private final String m_label;

  private final String m_projectName;

  private final String m_icon;

  private final URL m_data;

  private final String m_description;

  public ProjectTemplate( final String id, final String label, final String projectName, final String description, final String icon, final URL data )
  {
    m_id = id;
    m_label = label;
    m_projectName = projectName;
    m_description = description;
    m_icon = icon;
    m_data = data;
  }

  public String getId( )
  {
    return m_id;
  }

  public String getLabel( )
  {
    return m_label;
  }

  public String getProjectName( )
  {
    return m_projectName;
  }

  public String getIcon( )
  {
    return m_icon;
  }

  public URL getData( )
  {
    return m_data;
  }

  public String getDescription( )
  {
    return m_description;
  }
}