/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.contribs.ogc31;

import javax.xml.bind.JAXBContext;

import ogc31.www.opengis.net.gml.ObjectFactory;

import org.kalypso.jwsdp.JaxbUtilities;

/**
 * Just contains the singleton for the JAXBContext of the binding stuff used in this plugin
 * 
 * @author schlienger
 */
public final class KalypsoOGC31JAXBcontext
{
  public final static ObjectFactory GML3_FAC = new ObjectFactory();

  private static JAXBContext m_context = null;

  private static Class< ? >[] m_contextClass = null;

  /**
   * methode is intended to be used by parts that generate JAXB-contexts with many classes and need to collect them
   * 
   * @return classes needed for binding context
   */
  public static Class< ? >[] getContextClasses( )
  {
    if( m_contextClass == null )
    {
      m_contextClass = new Class[] {//  
      ogc31.www.opengis.net.gml.ObjectFactory.class// 
          , ogc31.www.opengis.net.swe.ObjectFactory.class//
          , ogc31.www.isotc211.org.gmd.ObjectFactory.class//
          , au.csiro.seegrid.xml.st.ObjectFactory.class };
    }
    return m_contextClass;
  }

  public static synchronized JAXBContext getContext( )
  {
    if( m_context == null )
    {
      // create the context on all of these factories else the binding won't work
      m_context = JaxbUtilities.createQuiet( getContextClasses() );
    }

    return m_context;
  }
}
