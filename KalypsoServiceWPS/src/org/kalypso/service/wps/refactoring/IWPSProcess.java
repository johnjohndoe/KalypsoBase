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
package org.kalypso.service.wps.refactoring;

import java.util.List;
import java.util.Map;

import net.opengeospatial.wps.ExecuteResponseType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author kuch
 *
 */
public interface IWPSProcess
{
  enum ProcessStatus
  {
    NONE,
    ACCEPED,
    STARTED,
    SUCEEDED,
    FAILED
  }

  String getTitle( );
 
  String getStatusDescription( );

  Integer getPercentCompleted( );

  /**
   * Commonly used system property for the location of the WPS endpoint. Not every WPS client might use this one.
   */
  public static final String SYSTEM_PROP_WPS_ENDPOINT = "org.kalypso.service.wps.service";

  /**
   * Starts the execution of this process.
   */
  void startProcess( Map<String, Object> inputs, List<String> outputs, IProgressMonitor monitor ) throws CoreException;

  // TODO: return own interface instead of binding type in order to support different WPS versions
  /**
   * returns <code>null</code>, as long as this process has not yet been started.
   */
  ExecuteResponseType getExecuteResponse( ) throws CoreException;
  
  ProcessStatus getProcessStatus( );
  
  Object[] getResult( String id );
}