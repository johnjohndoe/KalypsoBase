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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.view.ObservationViewHelper;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.obsprovider.MultipleObsProvider;
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

    final Set<IRepositoryItem> items = new LinkedHashSet<>();
    final Set<IObsProvider> providers = new LinkedHashSet<>();

    while( iterator.hasNext() )
    {
      final Object obj = iterator.next();
      if( obj instanceof IRepositoryItem )
      {
        final IRepositoryItem item = (IRepositoryItem) obj;
        if( item.hasAdapter( IObservation.class ) )
        {
          items.add( item );
        }
      }
      else if( obj instanceof IAdaptable )
      {
        final IAdaptable adapter = (IAdaptable) obj;

        final IObsProvider provider = (IObsProvider) adapter.getAdapter( IObsProvider.class );
        if( Objects.isNotNull( provider ) )
        {
          providers.add( provider );
          continue;
        }

        final IObservation observation = (IObservation) adapter.getAdapter( IObservation.class );
        if( Objects.isNotNull( observation ) )
        {
          providers.add( new PlainObsProvider( observation, null ) );
          continue;
        }
      }
    }

    final Map<String, IMultipleZmlSourceElement> sources = new HashMap<String, IMultipleZmlSourceElement>();
    fill( sources, items.toArray( new IRepositoryItem[] {} ) );
    fill( sources, providers.toArray( new IObsProvider[] {} ) );

    return sources.values().toArray( new IMultipleZmlSourceElement[] {} );
  }

  private static void fill( final Map<String, IMultipleZmlSourceElement> sources, final IObsProvider[] providers )
  {
    for( final IObsProvider provider : providers )
    {
      // FIXME lazy loading
      final IObservation observation = provider.getObservation();
      if( Objects.isNull( observation ) )
        continue;

      final IAxis[] valueAxes = AxisUtils.findValueAxes( observation.getAxes(), false );
      for( final IAxis axis : valueAxes )
      {
        final String type = axis.getType();
        IMultipleZmlSourceElement multiple = sources.get( type );
        if( Objects.isNull( multiple ) )
        {
          multiple = new MultipleObsProvider( type );
          sources.put( type, multiple );
        }

        multiple.add( new ZmlPlainObsProvider( multiple.getIdentifier(), observation, new ObservationRequest() ) );
      }
    }

  }

  private static void fill( final Map<String, IMultipleZmlSourceElement> sources, final IRepositoryItem[] items )
  {
    for( final IRepositoryItem item : items )
    {
      final IObservation observation = (IObservation) item.getAdapter( IObservation.class );
      if( Objects.isNull( observation ) )
        continue;

      // TODO refactor ObservationViewHelper in KalypsoUI - to get rid of KalypsoUI dependency in KalypsoZmlCore
      final DateRange dateRange = ObservationViewHelper.makeDateRange( item );

      final IAxis[] valueAxes = AxisUtils.findValueAxes( observation.getAxes(), false );
      for( final IAxis axis : valueAxes )
      {
        final String type = axis.getType();
        IMultipleZmlSourceElement multiple = sources.get( type );
        if( Objects.isNull( multiple ) )
        {
          multiple = new MultipleObsProvider( type );
          sources.put( type, multiple );
        }

        multiple.add( new ZmlPlainObsProvider( multiple.getIdentifier(), observation, new ObservationRequest( dateRange ) ) );
      }
    }

  }
}
