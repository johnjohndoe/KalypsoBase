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
package org.kalypso.services.processing.ogcwrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.opengeospatial.wps.ProcessBriefType;

/**
 * Objects of this class wrap a {@link Capabilities} object in order to provide easy access to contained information.
 * 
 * @author skurzbach
 */
public class Capabilities
{
  private final net.opengeospatial.wps.Capabilities m_capabilities;

  public Capabilities( final net.opengeospatial.wps.Capabilities capabilities )
  {
    m_capabilities = capabilities;
  }

  public ServiceIdentification getServiceIdentification( )
  {
    return new ServiceIdentification( m_capabilities.getServiceIdentification() );
  }

  public ServiceProvider getServiceProvider( )
  {
    return new ServiceProvider( m_capabilities.getServiceProvider() );
  }

  public OperationsMetadata getOperationsMetadata( )
  {
    return new OperationsMetadata( m_capabilities.getOperationsMetadata() );
  }

  public List<ProcessBrief> getProcessOfferings( )
  {
    final List<ProcessBriefType> processOfferings = m_capabilities.getProcessOfferings().getProcess();
    final List<ProcessBrief> resultList = new ArrayList<ProcessBrief>();
    for( ProcessBriefType processBrief : processOfferings )
    {
      resultList.add( new ProcessBrief( processBrief ) );
    }
    return Collections.unmodifiableList( resultList );
  }

  public String getVersion( )
  {
    return m_capabilities.getVersion();
  }

  public net.opengeospatial.wps.Capabilities getWPSCapabilities( )
  {
    return m_capabilities;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + getVersion().hashCode();
    result = PRIME * result + getOperationsMetadata().hashCode();
    result = PRIME * result + getProcessOfferings().hashCode();
    result = PRIME * result + getServiceIdentification().hashCode();
    result = PRIME * result + getServiceProvider().hashCode();
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    boolean result = obj instanceof Capabilities;
    if( result )
    {
      final Capabilities other = (Capabilities) obj;
      result = getOperationsMetadata().equals( other.getOperationsMetadata() ) && getProcessOfferings().equals( other.getProcessOfferings() )
          && getServiceIdentification().equals( other.getServiceIdentification() ) && getServiceProvider().equals( other.getServiceProvider() ) && getVersion().equals( other.getVersion() );
    }
    return result;
  }
}
