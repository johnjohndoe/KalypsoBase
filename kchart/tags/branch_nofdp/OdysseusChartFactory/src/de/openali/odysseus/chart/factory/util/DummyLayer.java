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
package de.openali.odysseus.chart.factory.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author burtscher1
 */
public class DummyLayer implements IChartLayer
{
  private boolean m_isVisible = true;

  private String m_title = "";

  private String m_id = "";

  private String m_description = "";

  private boolean m_isActive = false;

  private final LayerEventHandler m_handler = new LayerEventHandler();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private ICoordinateMapper m_coordinateMapper;

  public DummyLayer( )
  {
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis getDomainAxis( )
  {
    if( getCoordinateMapper() != null )
      return getCoordinateMapper().getDomainAxis();
    else
      return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getId()
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetAxis()
   */
  public IAxis getTargetAxis( )
  {
    if( getCoordinateMapper() != null )
      return getCoordinateMapper().getTargetAxis();
    else
      return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getVisibility()
   */
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return m_isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public void setActive( final boolean isActive )
  {
    m_isActive = isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setDescription(java.lang.String)
   */
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setTitle(java.lang.String)
   */
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setID(java.lang.String)
   */
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  public void setVisible( final boolean isVisible )
  {
    m_isVisible = isVisible;
    m_handler.fireLayerVisibilityChanged( this );
  }

  @SuppressWarnings("unchecked")
  public IDataContainer getDataContainer( )
  {
    return null;
  }

  @SuppressWarnings("unchecked")
  public void setDataContainer( final IDataContainer data )
  {
    // nothing to do;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( String id )
  {
    return m_data.get( id );
  }

  public void addListener( ILayerEventListener l )
  {
    m_handler.addListener( l );
  }

  public void removeListener( ILayerEventListener l )
  {
    m_handler.removeListener( l );
  }

  public LayerEventHandler getEventHandler( )
  {
    return m_handler;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image)
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public void drawIcon( final Image img )
  {
    GC gc = new GC( img );
    gc.setBackground( img.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.fillRectangle( img.getBounds() );
    gc.dispose();
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  public void paint( GC gc )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  public IDataRange<Number> getDomainRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  public IDataRange<Number> getTargetRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getCoordinateMapper()
   */
  public ICoordinateMapper getCoordinateMapper( )
  {
    return m_coordinateMapper;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setCoordinateMapper(org.kalypso.chart.framework.model.mapper.ICoordinateMapper)
   */
  public void setCoordinateMapper( ICoordinateMapper coordinateMapper )
  {
    m_coordinateMapper = coordinateMapper;
  }

  public Map<String, ImageData> getSymbolMap( )
  {
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#init()
   */
  public void init( )
  {
    // nothing to do
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  public void dispose( )
  {
    // nothing to do
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  public ILegendEntry[] getLegendEntries( )
  {
    return new ILegendEntry[0];
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#setMappers(java.util.Map)
   */
  @SuppressWarnings("unchecked")
  public void setMappers( Map<String, IMapper> mapperMap )
  {
    // nothing to do

  }

}
