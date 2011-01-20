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
package de.openali.odysseus.chart.factory.util;

import org.apache.commons.lang.NotImplementedException;
import org.apache.xmlbeans.XmlException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.w3c.dom.Node;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.factory.layer.PlainLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chartconfig.x020.ChartDocument;
import de.openali.odysseus.chartconfig.x020.DerivedLayerType;
import de.openali.odysseus.chartconfig.x020.LayerDocument;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.ParameterType;
import de.openali.odysseus.chartconfig.x020.ParametersType;
import de.openali.odysseus.chartconfig.x020.ProviderType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;

/**
 * @author Dirk Kuch
 */
public final class LayerTypeHelper
{
  private LayerTypeHelper( )
  {
  }

  /**
   * layer type will be extended by given parameters
   */
  public static void appendParameters( final LayerType type, final ParametersType parameters )
  {
    if( parameters == null )
      return;

    final ProviderType provider = type.getProvider();
    if( provider == null )
      return;

    final ParametersType baseType = provider.getParameters();

    final ParameterType[] array = parameters.getParameterArray();
    for( final ParameterType parameter : array )
    {
      final ParameterType baseParameter = getParamter( baseType, parameter.getName() );
      baseParameter.setValue( parameter.getValue() );
    }
  }

  /**
   * @return parameter with name
   */
  private static ParameterType getParamter( final ParametersType baseType, final String name )
  {
    final ParameterType[] parameters = baseType.getParameterArray();
    for( final ParameterType parameter : parameters )
    {
      if( parameter.getName().equals( name ) )
        return parameter;
    }

    final ParameterType parameter = baseType.addNewParameter();
    parameter.setName( name );

    return parameter;
  }

  /**
   * clone basic layer type and append / extend by derived layer type definition / redifiniton
   */
  public static LayerType cloneLayerType( final DerivedLayerType derivedLayerType, final LayerType baseLayerType )
  {
    final LayerType clonedLayerType = (LayerType) baseLayerType.copy();
    clonedLayerType.setId( derivedLayerType.getId() );

    LayerTypeHelper.appendParameters( clonedLayerType, derivedLayerType.getParameters() );

    if( derivedLayerType.isSetTitle() )
      clonedLayerType.setTitle( derivedLayerType.getTitle() );

    if( derivedLayerType.isSetDescription() )
      clonedLayerType.setDescription( derivedLayerType.getDescription() );

    if( derivedLayerType.isSetStyles() )
      clonedLayerType.setStyles( derivedLayerType.getStyles() );

    return clonedLayerType;
  }

  public static ReferencableType getParentNode( final LayerType baseLayerType )
  {
    final Node node = baseLayerType.getDomNode();
    final Node parentNode = node.getParentNode().getParentNode();

    final String localName = parentNode.getLocalName();
    try
    {
      if( "Layer".equals( localName ) ) //$NON-NLS-1$
      {
        final LayerDocument layerDocument = LayerDocument.Factory.parse( parentNode );

        return layerDocument.getLayer();
      }
      else if( "Chart".equals( localName ) ) //$NON-NLS-1$
      {
        final ChartDocument document = ChartDocument.Factory.parse( parentNode );

        return document.getChart();
      }
    }
    catch( final XmlException e )
    {
      OdysseusChartFactory.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    throw new NotImplementedException();
  }

  public static ILayerProvider getLayerTypeProvider( final IExtensionLoader loader, final LayerType layerType )
  {
    final ProviderType providerType = layerType.getProvider();
    if( providerType == null )
    {
      return new PlainLayerProvider();
    }

    final ILayerProvider provider = loader.getExtension( ILayerProvider.class, providerType.getEpid() );
    if( provider == null )
      throw new IllegalStateException( String.format( "LayerProvider not found: %s", providerType.getEpid() ) ); //$NON-NLS-1$

    return provider;
  }

}
