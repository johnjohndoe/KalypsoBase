/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.core.diagram.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IResource;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * @author Dirk Kuch
 */
public class ChartTypeHandler
{
  private ChartConfigurationLoader m_loader;

  private final URL m_template;

  public ChartTypeHandler( final URL template )
  {
    m_template = template;
  }

  public ChartTypeHandler( final IResource templateFile ) throws MalformedURLException
  {
    this( templateFile.getLocationURI().toURL() );
  }

  public ChartType getChartType( ) throws XmlException, IOException
  {
    final ChartType[] charts = getLoader().getCharts();
    if( charts.length != 1 )
      throw new IllegalStateException();

    return charts[0];
  }

  public IReferenceResolver getReferenceResolver( ) throws XmlException, IOException
  {
    return getLoader();
  }

  private ChartConfigurationLoader getLoader( ) throws XmlException, IOException
  {
    if( m_loader != null )
      return m_loader;

    m_loader = new ChartConfigurationLoader( m_template );
    return m_loader;
  }

  public URL getContext( )
  {
    return m_template;
  }
}