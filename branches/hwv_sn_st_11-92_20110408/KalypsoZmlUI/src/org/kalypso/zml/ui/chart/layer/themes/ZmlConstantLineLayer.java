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

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.utils.ConfigUtils;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.metadata.IMetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.chart.layer.boundaries.IMetadataLayerBoundary;
import org.kalypso.zml.ui.chart.layer.boundaries.KodBoundaryLayerProvider;
import org.kalypso.zml.ui.chart.layer.boundaries.MetadataLayerBoundaryBuilder;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.TextFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

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

  protected ZmlConstantLineLayer( final ILayerProvider provider, final IStyleSet styleSet, final boolean calculateRange )
  {
    super( provider, styleSet );

    m_calculateRange = calculateRange;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
      m_handler.dispose();

    super.dispose();
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#onObservationChanged()
   */
  @Override
  public void onObservationChanged( )
  {
    try
    {
      updateDescriptors();
    }
    catch( final XmlException e )
    {
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }

    getEventHandler().fireLayerContentChanged( this );
  }

  public boolean isCalculateRange( )
  {
    return m_calculateRange;
  }

  /**
   * @see de.openali.odysseus.chart.factory.layer.AbstractChartLayer#isLegend()
   */
  @Override
  public boolean isLegend( )
  {
    return false;
  }

  public void setCalculateRange( final boolean calculateRange )
  {
    m_calculateRange = calculateRange;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    if( !m_calculateRange || ArrayUtils.isEmpty( m_descriptors ) )
    {
      return null;
    }

    Number max = -Double.MAX_VALUE;
    Number min = Double.MAX_VALUE;
    if( m_calculateRange )
    {
      for( final ZmlConstantLineBean descriptor : m_descriptors )
      {
        max = Math.max( max.doubleValue(), descriptor.getValue().doubleValue() );
        min = Math.min( max.doubleValue(), descriptor.getValue().doubleValue() );
      }
    }

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    try
    {
      if( ArrayUtils.isEmpty( m_descriptors ) )
        return;

      final Rectangle screenRect = gc.getClipping();
      final int[] screens = getScreenValues();

      for( final ZmlConstantLineBean descriptor : m_descriptors )
      {
        final int screenValue = getTargetAxis().numericToScreen( descriptor.getValue() );
        final ILineStyle lineStyle = descriptor.getLineStyle();

        final PolylineFigure polylineFigure = new PolylineFigure();
        polylineFigure.setStyle( lineStyle );
        polylineFigure.setPoints( new Point[] { new Point( 0, screenValue ), new Point( screenRect.width, screenValue ) } );
        polylineFigure.paint( gc );

        if( descriptor.isShowLabel() )
          paintDescriptorLabel( gc, screenRect, screens, descriptor, screenValue, lineStyle );
      }
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }
  }

  private void paintDescriptorLabel( final GC gc, final Rectangle screenRect, final int[] screens, final ZmlConstantLineBean descriptor, final int screenValue, final ILineStyle lineStyle )
  {
    final ITextStyle textStyle = descriptor.getTextStyle();
    final String text = descriptor.getLabel();

    // Needs to apply style here, else the extent is not correctly calculated
    textStyle.apply( gc );
    final Point extent = gc.textExtent( text );

    final TextFigure textFigure = new TextFigure();
    textFigure.setStyle( textStyle );

    // FIXME: choose text position on line depending on alignment set in text-style
    if( canDrawLabel( screens, screenValue, extent.y ) )
    {
      textFigure.setText( text );

      // final ALIGNMENT alignment = textStyle.getAlignment();
      // FIXME: textStyle alignment is not correctly implemented and is not an attribute of the corresponding
      // xml-element
      final ALIGNMENT alignment = ALIGNMENT.RIGHT;
      final int left = calculateLeftPosition( extent, screenRect, alignment );
      final int top = (int) (screenValue - extent.y / 2f - lineStyle.getWidth());

      textFigure.setPoint( new Point( left, top ) );
      textFigure.paint( gc );
    }
  }

  private int calculateLeftPosition( final Point extent, final Rectangle screenRect, final ALIGNMENT alignment )
  {
    // TODO: we would like to configure some kind of buffer/insets

    // FIXME: get insets from outside for all directions
    final int insetsRight = 7;

    switch( alignment )
    {
      case LEFT:
        // FIXME: check if the screen rect is correctly set here -> is the width of the axis an issue?
        return screenRect.x;

      case CENTER:
        return screenRect.x + (int) (screenRect.width / 2.0 - extent.x / 2.0);

      case RIGHT:
        // fall through
      default:
        return screenRect.x + screenRect.width - extent.x - insetsRight;
    }
  }

  private boolean canDrawLabel( final int[] screens, final int value, final int size )
  {
    for( final int x : screens )
    {
      if( x > value && x < value + size )
        return false;
    }
    return true;
  }

  private int[] getScreenValues( )
  {
    final int[] screens = new int[m_descriptors.length];
    for( int i = 0; i < m_descriptors.length; i++ )
    {
      screens[i] = getTargetAxis().numericToScreen( m_descriptors[i].getValue() );
    }
    return screens;
  }

  private void updateDescriptors( ) throws XmlException, IOException
  {
    final IObservation observation = m_handler.getObservation();
    if( Objects.isNull( observation ) )
    {
      m_descriptors = null;
      return;
    }

    final IMetadataLayerBoundary[] boundaryLayers = buildBoundaries( observation );

    final Set<ZmlConstantLineBean> descriptors = new LinkedHashSet<ZmlConstantLineBean>();
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
  private IMetadataLayerBoundary[] buildBoundaries( final IObservation observation ) throws XmlException, IOException
  {
    final MetadataList metadata = observation.getMetadataList();
    final IParameterContainer parameters = getProvider().getParameterContainer();
    if( Objects.isNotNull( parameters ) )
    {
      final MetadataLayerBoundaryBuilder builder = new MetadataLayerBoundaryBuilder( metadata, parameters, getStyleSet() );
      builder.execute( new NullProgressMonitor() );

      return builder.getBoundaries( m_handler.getTargetAxisId() );
    }

    final URL url = ConfigUtils.findCentralConfigLocation( "layers/grenzwerte/alarmstufen.kod" ); //$NON-NLS-1$

    final KodBoundaryLayerProvider provider = new KodBoundaryLayerProvider( metadata, url, getDataHandler().getTargetAxisId() );
    return provider.getBoundaries();
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setDataHandler(org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler)
   */
  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_handler != null )
      m_handler.dispose();

    m_handler = handler;
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setLabelDescriptor(java.lang.String)
   */
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

    final IObservation observation = getDataHandler().getObservation();
    if( observation == null )
      return m_labelDescriptor;

    return ObservationTokenHelper.replaceTokens( m_labelDescriptor, observation, getDataHandler().getValueAxis() );
  }
}
