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
package org.kalypso.service.wps.client.exceptions;

/**
 * An exception, that indicates, that something went wrong with the WPS.
 * 
 * @author Holger Albert
 */
public class WPSException extends Exception
{
  /**
   * The constructor.
   */
  public WPSException( )
  {
  }

  /**
   * The constructor.
   * 
   * @param message
   *            The detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public WPSException( String message )
  {
    super( message );
  }

  /**
   * The constructor.
   * 
   * @param cause
   *            The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *            indicates that the cause is nonexistent or unknown.)
   */
  public WPSException( Throwable cause )
  {
    super( cause );
  }

  /**
   * The constructor.
   * 
   * @param message
   *            The detail message. The detail message is saved for later retrieval by the getMessage() method.
   * @param cause
   *            The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *            indicates that the cause is nonexistent or unknown.)
   */
  public WPSException( String message, Throwable cause )
  {
    super( message, cause );
  }
}