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
package org.kalypso.model.wspm.ui.view.chart;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.observation.result.IComponent;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author kimwerner
 */
public abstract class AbstractProfilTheme extends AbstractProfilLayer
{
  private final String m_id;

  private String m_title;

  private ILegendEntry[] m_combinedEntry;

  public AbstractProfilTheme( final IProfile profil, final String id, final String title, final IProfilChartLayer[] chartLayers, final ICoordinateMapper cm )
  {
    super( id, profil ); //$NON-NLS-1$

    /* *grml* AbstractProfileLayer overwrites getTitle() implementation */
    setTitle( title );
    m_title = title;

    m_id = id;

    // FIXME: bad! all layers should get their mapper in the constructor
    setCoordinateMapper( cm );

    if( chartLayers != null )
    {
      // FIXME: bad! all layers should get their mapper in the constructor
      for( final IChartLayer layer : chartLayers )
        layer.setCoordinateMapper( cm );

      getLayerManager().addLayer( chartLayers );
    }
  }

  /**
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return null;
  }

  /**
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return null;
  }

  @Override
  public EditInfo commitDrag( final Point point, final EditInfo dragStartData )
  {
    if( getTargetComponent() != null )
    {
      getProfil().getSelection().setActivePointProperty( getTargetComponent() );
    }
    final IProfilChartLayer layer = getActiveLayer();
    if( layer == null )
      return null;

    if( dragStartData.getPosition() == point )
    {
      layer.executeClick( dragStartData );
    }
    else
    {
      layer.executeDrop( point, dragStartData );
    }

    return null;
  }

  public IChartLayer[] getLegendNodes( )
  {
    return getLayerManager().getLayers();
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( ArrayUtils.isEmpty( m_combinedEntry ) )
    {
      final ILegendEntry[] entries = createLegendEntries();
      m_combinedEntry = entries;
    }
    return m_combinedEntry;
  }

  private ILegendEntry[] createLegendEntries( )
  {
    // TODO: implement combined legend entry and reuse
    final LegendEntry le = new LegendEntry( this, toString() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        for( final IChartLayer layer : getLayerManager().getLayers() )
        {
          final ILegendEntry[] les = layer.getLegendEntries();

          for( final ILegendEntry l : les )
          {
            // FIXME: i.e. no other implementations than LegendEntry are supported, this is ugly....
            if( l instanceof LegendEntry )
              ((LegendEntry)l).paintSymbol( gc, size );
          }
        }
      }
    };

    return new ILegendEntry[] { le };
  }

  /**
   * *grml* AbstractProfileLayer overwrites getTitle() implementation
   */
  @Override
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * *grml* AbstractProfileLayer overwrites getTitle() implementation
   */
  @Override
  public void setTitle( final String title )
  {
    super.setTitle( title );

    m_title = title;
  }

  @Override
  public EditInfo drag( final Point newPos, final EditInfo dragStartData )
  {
    final IProfilChartLayer layer = getActiveLayer();
    if( layer == null )
      return null;

    return layer.drag( newPos, dragStartData );
  }

  @Override
  public void executeClick( final EditInfo clickInfo )
  {
    final IProfilChartLayer layer = getActiveLayer();
    if( layer != null )
    {
      layer.executeClick( clickInfo );
    }
  }

  @Override
  public void executeDrop( final Point point, final EditInfo dragStartData )
  {
    final IProfilChartLayer layer = getActiveLayer();
    if( layer != null )
    {
      layer.executeDrop( point, dragStartData );
    }
  }

  protected final void fireLayerContentChanged( final ContentChangeType type )
  {
    getEventHandler().fireLayerContentChanged( this, type );
  }

  private IProfilChartLayer getActiveLayer( )
  {
    for( final IChartLayer l : getLayerManager().getLayers() )
    {
      if( l.isActive() && l instanceof IProfilChartLayer )
        return (IProfilChartLayer)l;
    }
    return null;
  }

  @Override
  public IComponent getDomainComponent( )
  {
    final IProfilChartLayer layer = getActiveLayer();
    return layer == null ? null : layer.getDomainComponent();
  }

  /**
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
//  @Override
//  public IDataRange<Number> getDomainRange( )
//  {
//    return null;
//  }

  /**
   * Empty implementation of getHover, because most themes do not have any hover elements but their children instead.
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    return null;
  }

  @Override
  public String getIdentifier( )
  {
    return m_id;
  }

  @Override
  public IComponent getTargetComponent( )
  {
    final IProfilChartLayer layer = getActiveLayer();
    return layer == null ? null : layer.getTargetComponent();
  }

  /**
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
//  @Override
//  public IDataRange<Number> getTargetRange( final IDataRange< ? > domainIntervall )
//  {
//    return null;
//  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
//    if( hint.isSelectionChanged() )
//    {
//      fireLayerContentChanged( ContentChangeType.value );
//    }
//    else
//    {
    for( final IChartLayer layer : getLayerManager().getLayers() )
    {
      if( layer instanceof IProfilChartLayer )
      {
        ((IProfilChartLayer)layer).onProfilChanged( hint );
      }
    }
//    }
  }

  @Override
  public void removeYourself( )
  {
    throw new UnsupportedOperationException( Messages.getString( "org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme.0" ) ); //$NON-NLS-1$
  }
}