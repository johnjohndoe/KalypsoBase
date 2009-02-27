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
package org.kalypso.swtchart.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.ConfigurationType;
import org.ksp.chart.configuration.LayerType;
import org.ksp.chart.configuration.ObjectFactory;
import org.ksp.chart.configuration.StyleType;
import org.ksp.chart.configuration.TableType;
import org.w3c.dom.Node;

/**
 * @author alibu
 */
public class ConfigLoader
{
  public final static ObjectFactory OF = new ObjectFactory();

  public final static JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  private String m_configPath;

  private HashMap<String, ChartType> m_charts = new HashMap<String, ChartType>();

  private HashMap<String, AxisType> m_axes = new HashMap<String, AxisType>();

  private HashMap<String, LayerType> m_layers = new HashMap<String, LayerType>();

  private HashMap<String, StyleType> m_styles = new HashMap<String, StyleType>();

  private HashMap<String, TableType> m_tables = new HashMap<String, TableType>();

  private ConfigurationType m_config;

  /**
   * @param path
   *          file system path or url where the configuration file can be found
   */
  public ConfigLoader( String path ) throws JAXBException
  {
    m_configPath = path;
    loadFile();
  }

  
  /**
   * Creates a Configuration-Dokument from a Node
   */
  public ConfigLoader( Node node ) throws JAXBException
  {
      Object o = JC.createUnmarshaller().unmarshal(node);
      if( o instanceof JAXBElement )
      {
        Object child = ((JAXBElement) o).getValue();
        m_config = (ConfigurationType) child;
      }
      else if( o instanceof ConfigurationType )
      {
        m_config = (ConfigurationType) o;
      }
      Logger.trace( "Configuration geladen" );
      Logger.trace( "loadFile End" );
      divideObjects();
  }
  

  /**
   * reads and loads the configuration file
   */
  public void loadFile( ) throws JAXBException
  {
    File file = new File( m_configPath );
    if( file.exists() )
    {
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
      Logger.trace( "Configuration geladen" );
      Logger.trace( "loadFile End" );
      divideObjects();
    }
    else
    {
      Logger.trace( "ERROR: Der Pfad zur Konfigurationsdatei konnte nicht aufgelöst werden. Bitte überprüfen Sie die Einstellungen. \n" + m_configPath );
    }
  }

  /**
   * @return marshalled configuration
   */
  public ConfigurationType getConfiguration( )
  {
    return m_config;
  }

  public void divideObjects( )
  {
    List<Object> chartOrLayerOrAxis = m_config.getChartOrLayerOrAxis();
    for( Object object : chartOrLayerOrAxis )
    {
      if( object instanceof ChartType )
      {
        ChartType o = (ChartType) object;
        m_charts.put( o.getName(), o );
      }
      else if( object instanceof AxisType )
      {
        AxisType o = (AxisType) object;
        m_axes.put( o.getName(), o );
      }
      else if( object instanceof LayerType )
      {
        LayerType o = (LayerType) object;
        m_layers.put( o.getName(), o );
      }
      else if( object instanceof StyleType )
      {
        StyleType o = (StyleType) object;
        m_styles.put( o.getName(), o );
      }
      else if( object instanceof TableType )
      {
        TableType o = (TableType) object;
        m_tables.put( o.getName(), o );
      }
    }
  }

  public HashMap<String, AxisType> getAxes( )
  {
    return m_axes;
  }

  public HashMap<String, ChartType> getCharts( )
  {
    return m_charts;
  }

  public ConfigurationType getConfig( )
  {
    return m_config;
  }

  public String getConfigPath( )
  {
    return m_configPath;
  }

  public HashMap<String, LayerType> getLayers( )
  {
    return m_layers;
  }

  public HashMap<String, StyleType> getStyles( )
  {
    return m_styles;
  }

  public HashMap<String, TableType> getTables( )
  {
    return m_tables;
  }

  public static void writeConfig( final ConfigurationType config, final OutputStreamWriter writer ) throws JAXBException
  {
    final Marshaller marshaller = JC.createMarshaller();
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.setProperty( Marshaller.JAXB_ENCODING, writer.getEncoding() );

    final JAXBElement<ConfigurationType> configElement = OF.createConfiguration( config );
    marshaller.marshal( configElement, writer );
  }

  public static ConfigurationType loadConfig( final IStorage configFile ) throws CoreException, JAXBException, IOException
  {
    InputStream is = null;
    try
    {
      is = new BufferedInputStream( configFile.getContents() );
      ConfigurationType type = loadConfig( is );
      is.close();

      return type;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  public static ConfigurationType loadConfig( final InputStream inputStream ) throws JAXBException
  {
    final Unmarshaller unmarshaller = ConfigLoader.JC.createUnmarshaller();
    final Object o = unmarshaller.unmarshal( inputStream );

    if( o instanceof JAXBElement )
      return (ConfigurationType) ((JAXBElement) o).getValue();
    else if( o instanceof ChartType )
      return (ConfigurationType) o;

    return null;
  }

  public static ConfigurationType loadConfig( final URL configUrl ) throws IOException, JAXBException
  {
    InputStream is = null;
    try
    {
      is = new BufferedInputStream( configUrl.openStream() );
      ConfigurationType type = loadConfig( is );
      is.close();

      return type;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }
}
