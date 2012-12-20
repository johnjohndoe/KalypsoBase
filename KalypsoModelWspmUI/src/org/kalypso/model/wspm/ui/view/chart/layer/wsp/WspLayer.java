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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import com.vividsolutions.jts.geom.Coordinate;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * Displays constant wsp lines in the cross section.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class WspLayer extends AbstractProfilTheme
{
  private final IPropertyChangeListener m_preferenceListener = new IPropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent event )
    {
      handlePreferencesChanged();
    }
  };

  private final IWspLayerData m_data;

  private WaterlevelRenderData[] m_renderData;

  private ILegendEntry[] m_legendEntries;

  private final ILineStyle m_style;

  private final ViewerFilter m_panelFilter;

  public WspLayer( final IProfile profile, final String layerId, final IProfilChartLayer[] childLayers, final ILayerStyleProvider styleProvider, final IWspLayerData data, final ICoordinateMapper< ? , ? > mapper, final ViewerFilter panelFilter )
  {
    super( profile, layerId, Messages.getString( "WspLayer.0" ), childLayers, mapper ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    m_panelFilter = panelFilter;

    final IPreferenceStore store = KalypsoModelWspmUIPlugin.getDefault().getPreferenceStore();
    store.addPropertyChangeListener( m_preferenceListener );

    m_style = styleProvider.getStyleFor( layerId + ILayerStyleProvider.LINE, ILineStyle.class );

    m_data = data;
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( ArrayUtils.isEmpty( m_legendEntries ) )
    {
      m_legendEntries = createLegendEntries();
    }
    return m_legendEntries;
  }

  private ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] { new WspLegendEntry( this, m_style ) };
  }

  @Override
  public IProfilView createLayerPanel( )
  {
    return new WspPanel( this, m_panelFilter );
  }

  @Override
  public void dispose( )
  {
    final IPreferenceStore store = KalypsoModelWspmUIPlugin.getDefault().getPreferenceStore();
    store.removePropertyChangeListener( m_preferenceListener );

    super.dispose();
  }

  /**
   * This function returns the wsp layer data.
   * 
   * @return The wsp layer data.
   */
  public IWspLayerData getData( )
  {
    return m_data;
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public EditInfo getHover( final Point pos )
  {
    final WaterlevelRenderData[] renderData = getRenderData();

    final IAxis< ? > domainAxis = getDomainAxis();
    final IDataRange<Double> domainRange = domainAxis.getNumericRange();

    /* The x positions. */
    final int xStart = domainAxis.numericToScreen( domainRange.getMin() );
    final int xEnd = domainAxis.numericToScreen( domainRange.getMax() );

    final ICoordinateMapper coordinateMapper = getCoordinateMapper();

    final Coordinate screenPos = new Coordinate( pos.x, pos.y );

    for( final WaterlevelRenderData data : renderData )
    {
      final WaterlevelRenderSegment segment = data.findSegment( screenPos, coordinateMapper );
      if( segment != null )
      {
        final ILineStyle hoverLineStyle = m_style;

        final IPaintable hoverFigure = segment.getHoverFigure( hoverLineStyle, xStart, xEnd, coordinateMapper );

        final String label = data.getLabel();

        final String tooltip = segment.formatTooltip( label );

        return new EditInfo( this, hoverFigure, null, null, tooltip, pos );
      }
    }

    return null;
  }

  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    final WaterlevelRenderData[] renderData = getRenderData();
    if( renderData.length == 0 )
      return null;

    final SortedSet<Double> values = new TreeSet<>();

    for( final WaterlevelRenderData data : renderData )
    {
      final double value = data.getValue();
      values.add( value );
    }

    final Double min = values.first();
    final Double max = values.last();
    return new DataRange<>( min, max );
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    // For now, we only fill on hover, as 1) the are is not nice yet 2) it overlaps the other water levels
    final IAreaStyle areaStyle = null;

    final ICoordinateMapper coordinateMapper = getCoordinateMapper();

    final WaterlevelRenderData[] renderData = getRenderData();
    for( final WaterlevelRenderData data : renderData )
    {
      data.paint( gc, m_style, areaStyle, coordinateMapper );
    }
  }

  private synchronized WaterlevelRenderData[] getRenderData( )
  {
    /* Lazy create water level data */
    if( m_renderData == null )
    {
      final WaterlevelsRenderWorker worker = new WaterlevelsRenderWorker( getProfil(), m_data );
      // TODO: keep status and show to user
      /* final IStatus status = */worker.execute();
      m_renderData = worker.getResult();
    }

    return m_renderData;
  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
    m_renderData = null;

    super.onProfilChanged( hint );
  }

  // TODO: all layers should have this method; instead, the event handler should not be visible from outside
  /**
   * Do not use, only needed for {@link org.kalypso.model.wspm.ui.view.chart.layer.wsp.utils.WaterLevelResultTree}.
   */
  public void invalidate( )
  {
    m_renderData = null;

    fireLayerContentChanged( ContentChangeType.value );
  }

  protected void handlePreferencesChanged( )
  {
    invalidate();
  }
}