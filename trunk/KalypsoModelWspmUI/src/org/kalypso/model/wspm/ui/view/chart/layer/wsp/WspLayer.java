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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;
import org.kalypso.model.wspm.ui.view.chart.layer.wsp.utils.WaterLevelFilter;

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

  /**
   * @param fill
   *          True, if the area below the wsp lines should be filled. If there are more than one wsp line, this option
   *          should be false, because you could see only the most above line and its area.
   */
  public WspLayer( final IProfil profile, final String layerId, final ILayerStyleProvider styleProvider, final IWspLayerData data, final ICoordinateMapper mapper )
  {
    super( profile, layerId, Messages.getString( "WspLayer.0" ), null, mapper, styleProvider ); //$NON-NLS-1$

    final IPreferenceStore store = KalypsoModelWspmUIPlugin.getDefault().getPreferenceStore();
    store.addPropertyChangeListener( m_preferenceListener );

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
    final ILineStyle lineStyle = getLineStyle();

    return new ILegendEntry[] { new WspLegendEntry( this, lineStyle ) };
  }

  @Override
  public IProfilView createLayerPanel( )
  {
    return new WspPanel( this, new WaterLevelFilter() );
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

  @Override
  public EditInfo getHover( final Point pos )
  {
    final WaterlevelRenderData[] renderData = getRenderData();

    final IAxis domainAxis = getDomainAxis();
    final IDataRange<Number> domainRange = domainAxis.getNumericRange();

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
        final ILineStyle hoverLineStyle = getLineStyleHover();

        final IPaintable hoverFigure = segment.getHoverFigure( hoverLineStyle, xStart, xEnd, coordinateMapper );

        final String label = data.getLabel();

        final String tooltip = segment.formatTooltip( label );

        return new EditInfo( this, hoverFigure, null, null, tooltip, pos );
      }
    }

    return null;
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
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

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final ILineStyle lineStyle = getLineStyle();

    // For now, we only fill on hover, as 1) the are is not nice yet 2) it overlaps the other water levels
    final IAreaStyle areaStyle = null;

    final ICoordinateMapper coordinateMapper = getCoordinateMapper();

    final WaterlevelRenderData[] renderData = getRenderData();
    for( final WaterlevelRenderData data : renderData )
    {
      data.paint( gc, lineStyle, areaStyle, coordinateMapper );
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
  public void onProfilChanged( final ProfilChangeHint hint )
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