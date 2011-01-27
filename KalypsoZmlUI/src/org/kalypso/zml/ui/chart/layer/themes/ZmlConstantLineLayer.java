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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.metadata.IMetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.chart.layer.boundaries.IMetadataLayerBoundary;
import org.kalypso.zml.ui.chart.layer.boundaries.MetadataLayerBoundaryBuilder;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlConstantLineLayer extends AbstractLineLayer implements IZmlLayer, Cloneable
{
  private ZmlConstantLineBean[] m_descriptors = new ZmlConstantLineBean[] {};

  private boolean m_calculateRange = false;

  private final IParameterContainer m_parameters;

  private final IStyleSet m_styleSet;

  private IZmlLayerDataHandler m_handler;

  private IObservation m_lastObservation;

  private String m_labelDescriptor;

  protected ZmlConstantLineLayer( final IParameterContainer parameters, final IStyleSet styleSet, final boolean calculateRange )
  {
    super( null, null );

    m_parameters = parameters;
    m_styleSet = styleSet;
    m_calculateRange = calculateRange;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public ZmlConstantLineLayer clone( )
  {
    return new ZmlConstantLineLayer( m_parameters, m_styleSet, m_calculateRange );
  }

  public boolean isCalculateRange( )
  {
    return m_calculateRange;
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
    updateDescriptors();

    if( ArrayUtils.isEmpty( m_descriptors ) )
      return;

    final int screenSize = gc.getClipping().width;
    final int[] screens = getScreenValues();

    for( final ZmlConstantLineBean descriptor : m_descriptors )
    {
      final int screenValue = getTargetAxis().numericToScreen( descriptor.getValue() );
      final ILineStyle lineStyle = descriptor.getLineStyle();

      final PolylineFigure polylineFigure = new PolylineFigure();
      polylineFigure.setStyle( lineStyle );
      polylineFigure.setPoints( new Point[] { new Point( 0, screenValue ), new Point( screenSize, screenValue ) } );
      polylineFigure.paint( gc );

      if( descriptor.isShowLabel() )
      {
        getTextFigure().setStyle( descriptor.getTextStyle() );
        final String text = descriptor.getLabel();
        final Point extent = gc.textExtent( text );
        if( canDrawLabel( screens, screenValue, extent.y ) )
        {
          final Point leftTopPoint = new Point( screenSize - extent.x - 1, screenValue - extent.y / 2 - lineStyle.getWidth() );
          drawText( gc, text, leftTopPoint );
        }
      }
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

  private void updateDescriptors( )
  {
    final IObservation observation = m_handler.getObservation();
    if( observation == null )
    {
      m_descriptors = null;
      return;
    }
    else if( m_lastObservation == observation )
      return;

    final MetadataList metadata = observation.getMetadataList();
    final MetadataLayerBoundaryBuilder builder = new MetadataLayerBoundaryBuilder( metadata, m_parameters, m_styleSet );
    builder.execute( new NullProgressMonitor() );

    final IMetadataLayerBoundary[] boundaryLayers = builder.getBoundaries( m_handler.getTargetAxisId() );

    final Set<ZmlConstantLineBean> descriptors = new LinkedHashSet<ZmlConstantLineBean>();
    for( final IMetadataLayerBoundary boundaryLayer : boundaryLayers )
    {
      final IMetadataBoundary boundary = boundaryLayer.getBoundary();
      final ZmlConstantLineBean descriptor = new ZmlConstantLineBean( boundaryLayer.getLabel(), boundary.getValue(), boundaryLayer.getLineStyle(), boundaryLayer.getTextStyle(), true );
      descriptors.add( descriptor );
    }

    m_descriptors = descriptors.toArray( new ZmlConstantLineBean[] {} );
    m_lastObservation = observation;

    getEventHandler().fireLayerContentChanged( this );
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
