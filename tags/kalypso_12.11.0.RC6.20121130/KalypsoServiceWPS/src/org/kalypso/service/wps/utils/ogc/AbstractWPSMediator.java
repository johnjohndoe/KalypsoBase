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
package org.kalypso.service.wps.utils.ogc;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.AssertionFailedException;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;

/**
 * @author kurzbach
 */
public class AbstractWPSMediator<V10 extends Object, V04 extends Object>
{
  private final WPS_VERSION m_version;

  private Object m_collegue;

  /**
   * Creates a mediator for the collegue with given WPS version.
   */
  public AbstractWPSMediator( final WPS_VERSION version )
  {
    org.eclipse.core.runtime.Assert.isNotNull( version );
    m_version = version;
  }

  /**
   * Creates a mediator for the collegue. Checks the version string to determine the WPS version. Must be either 0.4.0
   * or 1.0.0
   */
  public AbstractWPSMediator( final String version )
  {
    this( WPSUtilities.WPS_VERSION.getValue( version ) );
  }

  /**
   * Creates a mediator for the collegue. Checks the package identifier to determine the version.
   */
  public AbstractWPSMediator( final Object collegue )
  {
    final String className = collegue.getClass().getName();
    if( className.startsWith( "net.opengeospatial.wps" ) ) //$NON-NLS-1$
    {
      m_version = WPSUtilities.WPS_VERSION.V040;
    }
    else if( className.startsWith( "net.opengis.wps._1_0" ) ) //$NON-NLS-1$
    {
      m_version = WPSUtilities.WPS_VERSION.V100;
    }
    else
    {
      throw new AssertionFailedException( "Could not determine version from collegue class: " + className ); //$NON-NLS-1$
    }
    m_collegue = collegue;
  }

  /**
   * Returns the version of the collegue
   */
  public WPS_VERSION getVersion( )
  {
    return m_version;
  }

  /**
   * Returns the collegue as version 1.0.0
   */
  @SuppressWarnings("unchecked")
  public V10 getV10( )
  {
    return (V10) m_collegue;
  }

  /**
   * Returns the collegue as version 0.4.0
   */
  @SuppressWarnings("unchecked")
  public V04 getV04( )
  {
    return (V04) m_collegue;
  }

  /**
   * Marshalls the object according to the collegue's version
   */
  public String marshall( final Object responseObject ) throws JAXBException, IOException
  {
    switch( getVersion() )
    {
      case V040:
        return MarshallUtilities.marshall( responseObject, WPS_VERSION.V040 );
      case V100:
        return MarshallUtilities.marshall( responseObject, WPS_VERSION.V100 );
    }
    return null;
  }

}
