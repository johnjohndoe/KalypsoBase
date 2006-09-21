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
package org.kalypso.swtchart.configuration;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.kalypso.jwsdp.JaxbUtilities;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.ObjectFactory;

/**
 * @author burtscher
 * 
 * loads a configuration file
 */
public class ConfigurationLoader
{
  private String m_configPath;

  private ConfigurationType m_config;

  public final static ObjectFactory OF = new ObjectFactory();

  public final static JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  /**
   * @param path file system path or url where the configuration file can be found
   */
  public ConfigurationLoader( String path ) throws JAXBException
  {
    m_configPath = path;
    loadFile();
  }

  /**
   *  reads and loads the configuration file
   */
  public void loadFile( ) throws JAXBException
  {
    System.out.println( "loadFile Start" );
    File file = new File( m_configPath );
    Object o = JC.createUnmarshaller().unmarshal( file );
    if( o instanceof JAXBElement )
    {
      Object child = ((JAXBElement) o).getValue();
      m_config = (ConfigurationType) child;
    }
    else if( o instanceof ConfigurationType )
    {
      m_config = (ConfigurationType) o;
    }
    System.out.println( "Configuration geladen" );
    System.out.println( "loadFile End" );
  }

  /**
   * @return marshalled configuration
   */
  public ConfigurationType getConfiguration( )
  {
    return m_config;
  }

}
