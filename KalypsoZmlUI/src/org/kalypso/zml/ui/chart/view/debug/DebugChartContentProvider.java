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
package org.kalypso.zml.ui.chart.view.debug;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.zml.core.base.request.IRequestStrategy;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;

import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class DebugChartContentProvider implements ITreeContentProvider
{

  @Override
  public void dispose( )
  {
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
  }

  @Override
  public Object[] getElements( final Object inputElement )
  {
    return getChildren( inputElement );
  }

  @Override
  public Object[] getChildren( final Object element )
  {
    if( element instanceof ILayerContainer )
    {
      final ILayerContainer container = (ILayerContainer) element;
      final ILayerManager layerManager = container.getLayerManager();

      final Set<Object> children = new LinkedHashSet<>();
      Collections.addAll( children, getDebugProperties( container ) );
      Collections.addAll( children, layerManager.getLayers() );

      return children.toArray();
    }

    return new Object[] {};
  }

  private Object[] getDebugProperties( final ILayerContainer container )
  {
    if( !(container instanceof IChartLayer) )
      return new Object[] {};

    final IChartLayer layer = (IChartLayer) container;

    final Set<String> properties = new LinkedHashSet<>();

    final ICoordinateMapper mapper = layer.getCoordinateMapper();
    if( Objects.isNotNull( mapper ) )
    {
      final IAxis domainAxis = mapper.getDomainAxis();
      final IAxis targetAxis = mapper.getTargetAxis();

      if( Objects.isNotNull( domainAxis ) )
      {
        properties.add( String.format( "- domain axis: %s", domainAxis.getIdentifier() ) ); //$NON-NLS-1$
      }

      if( Objects.isNotNull( targetAxis ) )
      {
        properties.add( String.format( "- target axis: %s", targetAxis.getIdentifier() ) ); //$NON-NLS-1$
      }
    }

    final ILayerProvider provider = layer.getProvider();
    if( Objects.isNotNull( provider ) )
    {
      final IParameterContainer parameters = provider.getParameterContainer();
      if( Objects.isNotNull( parameters ) )
      {
        final String[] keys = parameters.keys();
        for( final String key : keys )
        {
          properties.add( String.format( "- parameter %s: %s", key, parameters.getParameterValue( key, "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }

    final IChartLayerFilter[] filters = layer.getFilters();
    for( final IChartLayerFilter filter : filters )
    {
      properties.add( String.format( "- filter: %s", filter.getClass().getSimpleName() ) ); //$NON-NLS-1$
    }

    if( layer instanceof IZmlLayer )
    {
      final IZmlLayer zmlLayer = (IZmlLayer) layer;
      final IZmlLayerDataHandler handler = zmlLayer.getDataHandler();
      if( Objects.isNotNull( handler ) )
      {
        properties.add( String.format( "- handler impl: %s", handler.getClass().getSimpleName() ) ); //$NON-NLS-1$

        final IObservation observation = (IObservation) handler.getAdapter( IObservation.class );
        if( Objects.isNotNull( observation ) )
        {
          properties.add( String.format( "   - observation: %s", observation.getHref() ) ); //$NON-NLS-1$
        }

        final IRequestStrategy requestStrategy = handler.getRequestStrategy();
        if( Objects.isNotNull( requestStrategy ) )
        {
          properties.add( String.format( "   - request strategy: %s", requestStrategy.getClass().getSimpleName() ) ); //$NON-NLS-1$
        }
      }

    }

    return properties.toArray();
  }

  @Override
  public Object getParent( final Object element )
  {
    if( element instanceof ILayerContainer )
      return ((ILayerContainer) element).getParent();

    return null;
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    if( element instanceof ILayerContainer )
    {
      final ILayerContainer container = (ILayerContainer) element;

      if( element instanceof IChartLayer )
        return true;

      final ILayerManager layerManager = container.getLayerManager();
      return ArrayUtils.isNotEmpty( layerManager.getLayers() );
    }

    return false;
  }

}
