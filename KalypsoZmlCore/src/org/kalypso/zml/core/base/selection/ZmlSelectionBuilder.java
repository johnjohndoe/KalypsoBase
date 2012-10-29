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
package org.kalypso.zml.core.base.selection;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.util.ZmlLink;
import org.kalypso.ogc.sensor.view.ObservationViewHelper;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.IZmlSourceElement;
import org.kalypso.zml.core.base.TSLinkWithName;
import org.kalypso.zml.core.base.obsprovider.MultipleSourceElement;
import org.kalypso.zml.core.base.obsprovider.ZmlPlainObsProvider;

/**
 * @author Dirk Kuch
 */
public final class ZmlSelectionBuilder
{
  private ZmlSelectionBuilder( )
  {

  }

  public static IMultipleZmlSourceElement[] getSelection( final SelectionChangedEvent event )
  {
    return getSelection( (IStructuredSelection) event.getSelection() );

  }

  public static IMultipleZmlSourceElement[] getSelection( final IStructuredSelection selection )
  {
    final Iterator< ? > iterator = selection.iterator();

    final Set<IZmlSourceElement> items = new LinkedHashSet<>();

    while( iterator.hasNext() )
    {
      final Object obj = iterator.next();
      if( obj instanceof IRepositoryItem )
      {
        final IRepositoryItem item = (IRepositoryItem) obj;
        if( item.hasAdapter( IObservation.class ) )
        {
          final IZmlSourceElement[] sources = toSourceElement( item );
          if( ArrayUtils.isNotEmpty( sources ) )
          {
            Collections.addAll( items, sources );
          }
        }
      }
      else if( obj instanceof ZmlLink )
      {
        final ZmlLink link = (ZmlLink) obj;
        final IObservation observation = link.getObservationFromPool();
        if( Objects.isNull( observation ) )
          continue;

        final IZmlSourceElement[] sources = toSourceElement( observation, null );
        if( ArrayUtils.isNotEmpty( sources ) )
        {
          Collections.addAll( items, sources );
          continue;
        }
      }
      else if( obj instanceof IObservation )
      {
        final IZmlSourceElement[] sources = toSourceElement( (IObservation) obj, null );
        if( ArrayUtils.isNotEmpty( sources ) )
        {
          Collections.addAll( items, sources );
          continue;
        }
      }
      else if( obj instanceof IAdaptable )
      {
        final IAdaptable adapter = (IAdaptable) obj;

        final IZmlSourceElement element = (IZmlSourceElement) adapter.getAdapter( IZmlSourceElement.class );
        if( Objects.isNotNull( element ) )
        {
          items.add( element );
          continue;
        }

        final TSLinkWithName link = (TSLinkWithName) adapter.getAdapter( TSLinkWithName.class );
        if( Objects.isNotNull( link ) )
        {
          items.add( link );
          continue;
        }

        final IObsProvider provider = (IObsProvider) adapter.getAdapter( IObsProvider.class );
        if( Objects.isNotNull( provider ) )
        {
          final IZmlSourceElement[] sources = toSourceElement( provider );
          if( ArrayUtils.isNotEmpty( sources ) )
          {
            Collections.addAll( items, sources );
            continue;
          }
        }

        final IObservation observation = (IObservation) adapter.getAdapter( IObservation.class );
        if( Objects.isNotNull( observation ) )
        {
          final IZmlSourceElement[] sources = toSourceElement( observation, null );
          if( ArrayUtils.isNotEmpty( sources ) )
          {
            Collections.addAll( items, sources );
            continue;
          }
        }
      }
    }

    return packToMultiple( items.toArray( new IZmlSourceElement[] {} ) );
  }

  private static IMultipleZmlSourceElement[] packToMultiple( final IZmlSourceElement[] sources )
  {
    final Map<String, IMultipleZmlSourceElement> resultSet = new HashMap<>();

    for( final IZmlSourceElement source : sources )
    {
      final String type = source.getIdentifier();

      IMultipleZmlSourceElement multiple = resultSet.get( type );
      if( Objects.isNull( multiple ) )
      {
        multiple = new MultipleSourceElement( type );
        resultSet.put( type, multiple );
      }

      multiple.add( source );
    }

    return resultSet.values().toArray( new MultipleSourceElement[] {} );
  }

  private static IZmlSourceElement[] toSourceElement( final IRepositoryItem item )
  {
    final IObservation observation = (IObservation) item.getAdapter( IObservation.class );
    if( Objects.isNull( observation ) )
      return new IZmlSourceElement[] {};

    final DateRange dateRange = ObservationViewHelper.makeDateRange( item );

    return toSourceElement( observation, new ObservationRequest( dateRange ) );
  }

  private static IZmlSourceElement[] toSourceElement( final IObservation observation, final IRequest request )
  {
    final Set<IZmlSourceElement> sources = new HashSet<>();

    final IAxis[] valueAxes = AxisUtils.findValueAxes( observation.getAxes(), false );
    for( final IAxis axis : valueAxes )
    {
      final String type = axis.getType();
      sources.add( new ZmlPlainObsProvider( type, observation, request, getPriority( type ) ) );
    }

    return sources.toArray( new IZmlSourceElement[] {} );
  }

  private static int getPriority( final String type )
  {
    switch( type )
    {
      case "W": //$NON-NLS-1$
        return 10;
      case "Q"://$NON-NLS-1$
        return 20;
      case "N"://$NON-NLS-1$
        return 30;
      case "V"://$NON-NLS-1$
        return 40;
      case "E"://$NON-NLS-1$
        return 50;
      case "T"://$NON-NLS-1$
        return 60;
      case "W_NN"://$NON-NLS-1$
        return 70;
      case "WECHMANN_E"://$NON-NLS-1$
        return 80;
      case "WECHMANN_SCHALTER_V"://$NON-NLS-1$
        return 90;
    }

    System.out.println( String.format( "ZmlSelectionBuilder.getPriority(String type) - missing type: %s", type ) ); //$NON-NLS-1$

    return type.hashCode();
  }

  private static IZmlSourceElement[] toSourceElement( final IObsProvider provider )
  {
    // FIXME lazy loading
    final IObservation observation = provider.getObservation();
    if( Objects.isNull( observation ) )
      return new IZmlSourceElement[] {};

    return toSourceElement( observation, new ObservationRequest() );
  }
}
