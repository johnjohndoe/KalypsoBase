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
package org.kalypso.chart.ext.observation.mapper.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageDescriptor;
import org.kalypso.chart.ext.observation.mapper.MarkerIconMapper;

import de.openali.odysseus.chart.factory.provider.AbstractMapperProvider;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;

/**
 * @author burtscher1
 */
public class MarkerIconMapperProvider extends AbstractMapperProvider
{

  /**
   * @see de.openali.odysseus.chart.factory.provider.IMapperProvider#getMapper()
   */
  @Override
  public IRetinalMapper getMapper( ) throws ConfigurationException
  {
    final LinkedHashMap<String, ImageDescriptor> string2Image = new LinkedHashMap<String, ImageDescriptor>();

    final Map<String, String> mapping = getParameterContainer().getParameterMap( "mapping" );
    for( final Entry<String, String> e : mapping.entrySet() )
    {
      // ImageLoader il = new ImageLoader();
      // ImageData id = ChartFactoryUtilities.loadImageData( getContext(), e.getValue(), 0, 0 );
      ImageDescriptor id = null;
      try
      {
        id = ImageDescriptor.createFromURL( new URL( getContext(), e.getValue() ) );
        string2Image.put( e.getKey(), id );
      }
      catch( final MalformedURLException e1 )
      {
        throw new ConfigurationException( "Invalid URL: " + getContext().toString() + e.getValue(), e1 );
      }
    }

    final MarkerIconMapper mim = new MarkerIconMapper( getId(), string2Image );
    return mim;
  }
}
