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
package de.openali.odysseus.chart.factory.config;

import java.net.URL;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.parameters.impl.XmlbeansParameterContainer;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chartconfig.x020.ParametersType;
import de.openali.odysseus.chartconfig.x020.ProviderType;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractChartFactory
{
  private final IChartModel m_model;

  private final IReferenceResolver m_resolver;

  private final IExtensionLoader m_loader;

  private final URL m_context;

  protected static final String CONFIGURATION_TYPE_KEY = "de.openali.odysseus.chart.factory.configurationType";

  /**
   * Keys for saving providers in chart elements
   */
  public static final String LAYER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.layerprovider";

  protected AbstractChartFactory( final IChartModel model, final IReferenceResolver resolver, final IExtensionLoader loader, final URL context )
  {
    m_model = model;
    m_resolver = resolver;
    m_loader = loader;
    m_context = context;
  }

  protected IChartModel getModel( )
  {
    return m_model;
  }

  protected IReferenceResolver getResolver( )
  {
    return m_resolver;
  }

  protected IExtensionLoader getLoader( )
  {
    return m_loader;
  }

  protected URL getContext( )
  {
    return m_context;
  }

  protected IParameterContainer createParameterContainer( final String ownerId, final ProviderType pt )
  {
    ParametersType parameters = null;
    if( pt != null )
      parameters = pt.getParameters();

    final IParameterContainer pc = new XmlbeansParameterContainer( ownerId, pt.getEpid(), parameters );

    return pc;
  }

}
