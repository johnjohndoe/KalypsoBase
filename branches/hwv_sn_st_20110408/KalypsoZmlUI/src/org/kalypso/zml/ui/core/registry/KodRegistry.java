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
package org.kalypso.zml.ui.core.registry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jregex.util.io.WildcardFilter;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.utils.ConfigUtils;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.core.kod.KodUtils;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerType;

/**
 * @author Dirk Kuch
 */
public final class KodRegistry
{
  private static KodRegistry INSTANCE = null;

  Map<String, LayerType> m_map = new HashMap<String, LayerType>();

  private KodRegistry( ) throws IOException
  {
    final URL url = ConfigUtils.findCentralConfigLocation( "layers/default" );

    final File directory = new File( url.getFile() );
    final String[] kodFiles = directory.list( new WildcardFilter( "*.kod" ) );

    for( final String kod : kodFiles )
    {
      try
      {
        final File file = new File( directory, kod );
        final URL fileUrl = file.toURI().toURL();

        final ChartConfigurationLoader loader = new ChartConfigurationLoader( fileUrl );
        final ChartType[] charts = loader.getCharts();
        for( final ChartType chart : charts )
        {
          final LayerType[] layers = chart.getLayers().getLayerArray();
          for( final LayerType layer : layers )
          {
            final String parameter = KodUtils.getParameter( layer.getProvider(), "targetAxis" );//$NON-NLS-1$
            m_map.put( parameter, layer );
          }
        }
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }
  }

  public static KodRegistry getInstance( ) throws IOException
  {
    if( INSTANCE == null )
      INSTANCE = new KodRegistry();

    return INSTANCE;
  }

  public LayerType getLayer( final String type )
  {
    return m_map.get( type );
  }

}
