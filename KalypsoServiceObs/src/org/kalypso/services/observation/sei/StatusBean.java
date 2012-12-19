/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.services.observation.sei;

import java.io.Serializable;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Dirk Kuch
 */
public class StatusBean implements Serializable
{
  private Integer m_severity;

  private String m_plugin;

  private String m_message;

  public StatusBean( )
  {
    this( IStatus.CANCEL, "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public StatusBean( final Integer severity, final String plugin, final String message )
  {
    setSeverity( severity );
    setPlugin( plugin );
    setMessage( message );
  }

  public void setMessage( final String message )
  {
    m_message = message;
  }

  public String getMessage( )
  {
    return m_message;
  }

  public void setPlugin( final String plugin )
  {
    m_plugin = plugin;
  }

  public String getPlugin( )
  {
    return m_plugin;
  }

  public void setSeverity( final Integer severity )
  {
    m_severity = severity;
  }

  public Integer getSeverity( )
  {
    return m_severity;
  }

}