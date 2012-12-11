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
package org.kalypso.zml.ui.chart.layer.themes;

import java.awt.Insets;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.utils.ConfigUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.metadata.IMetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.base.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.chart.layer.boundaries.IMetadataLayerBoundary;
import org.kalypso.zml.ui.chart.layer.boundaries.KodBoundaryLayerProvider;
import org.kalypso.zml.ui.chart.layer.boundaries.MetadataLayerBoundaryBuilder;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlConstantLineLayer extends AbstractLineLayer implements IZmlLayer
{
  private ZmlConstantLineBean[] m_descriptors = new ZmlConstantLineBean[] {};

  private boolean m_calculateRange = false;

  private IZmlLayerDataHandler m_handler;

  private String m_labelDescriptor;

  public ZmlConstantLineLayer( final IZmlLayerProvider provider, final IStyleSet styleSet, final boolean calculateRange, final URL context )
  {
    super( provider, styleSet );
    m_calculateRange = calculateRange;

    setup( context );
  }

  @Override
  public IZmlLayerProvider getProvider( )
  {
    return (IZmlLayerProvider) super.getProvider();
  }

  private void setup( final URL context )
  {
    final IZmlLayerProvider provider = getProvider();
    final ZmlObsProviderDataHandler handler = new ZmlObsProviderDataHandler( this, provider.getTargetAxisId() );
    try
    {
      handler.load( provider, context );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    setDataHandler( handler );

  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
      m_handler.dispose();

    super.dispose();
  }

  @Override
  public void onObservationChanged( final ContentChangeType type )
  {
    updateDescriptors();

    getEventHandler().fireLayerContentChanged( this, type );
  }

  public boolean isCalculateRange( )
  {
    return m_calculateRange;
  }

  @Override
  public boolean isLegend( )
  {
    return false;
  }

  public void setCalculateRange( final boolean calculateRange )
  {
    m_calculateRange = calculateRange;
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return null;
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    if( !m_calculateRange || ArrayUtils.isEmpty( m_descriptors ) )
    {
      return null;
    }

    Double max = -Double.MAX_VALUE;
    Double min = Double.MAX_VALUE;
    if( m_calculateRange )
    {
      for( final ZmlConstantLineBean descriptor : m_descriptors )
      {
        max = Math.max( max, descriptor.getValue().doubleValue());
        min = Math.min( min, descriptor.getValue().doubleValue());
      }
    }

    return new DataRange ( min, max );
  }

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    try
    {
      if( ArrayUtils.isEmpty( m_descriptors ) )
        return;

      for( final ZmlConstantLineBean descriptor : m_descriptors )
      {
        final int screenValue = getTargetAxis().logicalToScreen( descriptor.getValue() );
        final PolylineFigure polylineFigure = new PolylineFigure();
        polylineFigure.setStyle( descriptor.getLineStyle() );
        polylineFigure.setPoints( new Point[] { new Point( 0, screenValue ), new Point( getDomainAxis().getScreenHeight(), screenValue ) } );
        polylineFigure.paint( gc );

        if( descriptor.isShowLabel() )
        {
          final TitleTypeBean titleType = new TitleTypeBean( null );
          titleType.setLabel( descriptor.getLabel() );
          titleType.setTextStyle( descriptor.getTextStyle() );
          titleType.setPositionHorizontal( ALIGNMENT.RIGHT );
          titleType.setInsets( new Insets( 0, 0, 0, 10 ) );
          titleType.setTextAnchorX( ALIGNMENT.RIGHT );
          titleType.setTextAnchorY( ALIGNMENT.BOTTOM );
          final GenericChartLabelRenderer labelRenderer = new GenericChartLabelRenderer( titleType );
          labelRenderer.paint( gc, new Rectangle( 0, screenValue, getDomainAxis().getScreenHeight(), -1 ) );
        }
      }
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  private void updateDescriptors( )
  {
    if( Objects.isNull( m_handler ) )
      return;

    final IObservation observation = (IObservation) m_handler.getAdapter( IObservation.class );
    if( Objects.isNull( observation ) )
    {
      m_descriptors = null;
      return;
    }

    final IMetadataLayerBoundary[] boundaryLayers = buildBoundaries( observation );

    final Set<ZmlConstantLineBean> descriptors = new LinkedHashSet<>();
    for( final IMetadataLayerBoundary boundaryLayer : boundaryLayers )
    {
      final IMetadataBoundary boundary = boundaryLayer.getBoundary();
      final ZmlConstantLineBean descriptor = new ZmlConstantLineBean( boundaryLayer.getLabel(), boundary.getValue(), boundaryLayer.getLineStyle(), boundaryLayer.getTextStyle(), true );
      descriptors.add( descriptor );
    }

    m_descriptors = descriptors.toArray( new ZmlConstantLineBean[] {} );
  }

  /**
   * differ between generic global .kod alarmstufen definition and special alarmstufen.kod
   */
  private IMetadataLayerBoundary[] buildBoundaries( final IObservation observation )
  {

    final MetadataList metadata = observation.getMetadataList();

    /**
     * *urks* special handling for alarmstufen layer!
     */
    if( StringUtils.containsIgnoreCase( getIdentifier(), "alarmstufe" ) ) //$NON-NLS-1$
    {
      try
      {
        final URL url = ConfigUtils.findCentralConfigLocation( "layers/grenzwerte/alarmstufen.kod" ); //$NON-NLS-1$

        final KodBoundaryLayerProvider provider = new KodBoundaryLayerProvider( metadata, url, getDataHandler().getTargetAxisId() );
        final IMetadataLayerBoundary[] boundaries = provider.getBoundaries();
        if( !Arrays.isEmpty( boundaries ) )
          return boundaries;
      }
      catch( final Exception ex )
      {
        ex.printStackTrace();
      }
    }

    final IParameterContainer parameters = getProvider().getParameterContainer();
    if( Objects.isNotNull( parameters ) )
    {
      final MetadataLayerBoundaryBuilder builder = new MetadataLayerBoundaryBuilder( metadata, parameters, getStyleSet() );
      builder.execute( new NullProgressMonitor() );

      return builder.getBoundaries( m_handler.getTargetAxisId() );
    }

    return new IMetadataLayerBoundary[] {};

  }

  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_handler != null )
      m_handler.dispose();

    m_handler = handler;
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    m_labelDescriptor = labelDescriptor;
  }

  @Override
  public String getTitle( )
  {
    if( m_labelDescriptor == null )
      return super.getTitle();

    final IObservation observation = (IObservation) getDataHandler().getAdapter( IObservation.class );
    if( observation == null )
      return m_labelDescriptor;

    final IAxis valueAxis = getDataHandler().getValueAxis();
    if( valueAxis == null )
      return m_labelDescriptor;

    return ObservationTokenHelper.replaceTokens( m_labelDescriptor, observation, valueAxis );
  }
}
