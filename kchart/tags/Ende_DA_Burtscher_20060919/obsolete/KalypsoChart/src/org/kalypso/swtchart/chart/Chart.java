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
package org.kalypso.swtchart.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.swt.widgets.SizedComposite;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.component.AxisComponent;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.registry.AxisRegistry;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistryEventListener;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.ILayerManager;
import org.kalypso.swtchart.chart.layer.impl.DataRange;

/**
 * @author schlienger
 */
public class Chart extends Composite implements IAxisRegistryEventListener, Listener, ILayerManager
{
  private final IAxisRegistry m_axisRegistry = new AxisRegistry();

  /** my layers */
  private final List<IChartLayer> m_layers = new ArrayList<IChartLayer>();

  /** axis --> List of layers */
  private final Map<IAxis< ? >, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis< ? >, List<IChartLayer>>();

  /** axis pos --> axis placeholder */
  private final Map<POSITION, Composite> m_axisPlaces = new HashMap<POSITION, Composite>();

  /** drawing space for the plot itself */
  private Plot m_plot;

  private boolean m_autoscale = false;

  private boolean m_hideUnusedAxes = false;

  private final Color m_white;

  private final Color m_black;

  public Chart( final Composite parent, final int style )
  {
    super( parent, style );

    m_white = new Color( null, 255, 255, 255 );
    m_black = new Color( null, 0, 0, 0 );

    setBackground( m_white );
    createControl( this );

    addListener( SWT.Resize, this );

    m_axisRegistry.addAxisRegistryEventListener( this );
  }

  private final void createControl( final Composite parent )
  {
    final GridLayout gridLayout = new GridLayout( 3, false );
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    super.setLayout( gridLayout );
    setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final int axisContainerStyle = SWT.NONE;

    // 1.1
    final Label lbl11 = new Label( parent, SWT.NONE );
    lbl11.setVisible( false );

    // 1.2
    final Composite topAxes = new SizedComposite( parent, axisContainerStyle );
    topAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData tad = new GridData( SWT.FILL, SWT.NONE, true, false );
    topAxes.setLayoutData( tad );
    m_axisPlaces.put( POSITION.TOP, topAxes );

    // 1.3
    final Label lbl13 = new Label( parent, SWT.NONE );
    lbl13.setVisible( false );

    // 2.1
    final Composite leftAxes = new SizedComposite( parent, axisContainerStyle );
    leftAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData lad = new GridData( SWT.NONE, SWT.FILL, false, true );
    leftAxes.setLayoutData( lad );
    m_axisPlaces.put( POSITION.LEFT, leftAxes );

    // 2.2
    m_plot = new Plot( this, parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED | SWT.NO_FOCUS );
    m_plot.setLayout( new FillLayout() );
    final GridData pad = new GridData( SWT.FILL, SWT.FILL, true, true );
    m_plot.setLayoutData( pad );
    m_plot.setBackground( parent.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

    // 2.3
    final Composite rightAxes = new SizedComposite( parent, axisContainerStyle );
    rightAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData rad = new GridData( SWT.NONE, SWT.FILL, false, true );
    rightAxes.setLayoutData( rad );
    m_axisPlaces.put( POSITION.RIGHT, rightAxes );

    // 3.1
    final Label lbl31 = new Label( parent, SWT.NONE );
    lbl31.setVisible( false );

    // 3.2
    final Composite bottomAxes = new SizedComposite( parent, axisContainerStyle );
    bottomAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData bad = new GridData( SWT.FILL, SWT.NONE, true, false );
    bottomAxes.setLayoutData( bad );
    m_axisPlaces.put( POSITION.BOTTOM, bottomAxes );

    // 3.3
    final Label lbl33 = new Label( parent, SWT.NONE );
    lbl33.setVisible( false );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    m_axis2Layers.clear();
    m_axisRegistry.clear();
    m_axisPlaces.clear();

    super.dispose();

    m_white.dispose();
    m_black.dispose();
  }

  /**
   * No Layout can be set on this chart. It manages its children and the layout on its own.
   * 
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( final Layout layout )
  {
    checkWidget();
  }

  public IAxisRegistry getAxisRegistry( )
  {
    return m_axisRegistry;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistryEventListener#onAxisAdded(org.kalypso.swtchart.axis.IAxis)
   */
  public void onAxisAdded( final IAxis axis )
  {
    final Composite parent = m_axisPlaces.get( axis.getPosition() );
    final AxisComponent component = new AxisComponent( axis, parent, SWT.NONE );
    m_axisRegistry.setComponent( axis, component );

    layout();
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistryEventListener#onAxisRemoved(org.kalypso.swtchart.axis.IAxis)
   */
  public void onAxisRemoved( final IAxis axis )
  {
    layout();
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( final Event event )
  {
    if( event.type == SWT.Resize )
    {
      // layout();
      // redraw();
    }
  }

  /**
   * @see org.kalypso.swtchart.layer.ILayerManager#addLayer(org.kalypso.swtchart.layer.IChartLayer)
   */
  public void addLayer( final IChartLayer layer )
  {
    m_layers.add( layer );
    updateAxisLayerMap( layer, true );

    if( m_autoscale )
      autoscale( new IAxis[] { layer.getDomainAxis(), layer.getValueAxis() } );

    redraw();
  }

  private void updateAxisLayerMap( final IChartLayer layer, final boolean bAdding )
  {
    List<IChartLayer> domList = m_axis2Layers.get( layer.getDomainAxis() );
    List<IChartLayer> valList = m_axis2Layers.get( layer.getValueAxis() );

    if( bAdding )
    {
      // mapping for domain axis
      if( domList == null )
      {
        domList = new ArrayList<IChartLayer>();
        m_axis2Layers.put( layer.getDomainAxis(), domList );
      }
      domList.add( layer );

      // mapping for value axis
      if( valList == null )
      {
        valList = new ArrayList<IChartLayer>();
        m_axis2Layers.put( layer.getValueAxis(), valList );
      }
      valList.add( layer );

      // axis-components must be visible
      m_axisRegistry.getComponent( layer.getDomainAxis() ).setVisible( true );
      m_axisRegistry.getComponent( layer.getValueAxis() ).setVisible( true );

      /**
       * TODO: das sollte an einem besseren Ort passieren; ausserdem ist das ein HACK, weil immer nur das aktuelle Layer
       * betrachtet wird
       */

      IDataRange[] dr = new DataRange[1];
      dr[0] = layer.getDomainRange();
      IDataRange[] vr = new DataRange[1];
      vr[0] = layer.getValueRange();

    }
    else
    {
      // remove domain mapping
      if( domList != null )
        domList.remove( layer );

      // remove value mapping
      if( valList != null )
        valList.remove( layer );

      // eventually hide axes
      if( m_hideUnusedAxes )
      {
        if( domList == null || domList.size() == 0 )
          m_axisRegistry.getComponent( layer.getDomainAxis() ).setVisible( false );
        if( valList == null || valList.size() == 0 )
          m_axisRegistry.getComponent( layer.getValueAxis() ).setVisible( false );
      }
    }
  }

  public void autoscale( IAxis[] axes )
  {
    if( axes == null )
      axes = m_axisRegistry.getAxes();

    for( final IAxis axis : axes )
    {
      final List<IChartLayer> layers = m_axis2Layers.get( axis );
      if( layers == null )
        continue;

      final List<IDataRange> ranges = new ArrayList<IDataRange>( layers.size() );

      for( final IChartLayer layer : layers )
      {
        final IDataRange range = getRangeFor( layer, axis );
        if( range != null )
          ranges.add( range );
      }

      axis.autorange( ranges.toArray( new IDataRange[ranges.size()] ) );
    }
  }

  private IDataRange getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    if( axis == layer.getDomainAxis() )
      return layer.getDomainRange();
    else if( axis == layer.getValueAxis() )
      return layer.getValueRange();
    else
      return null;
  }

  /**
   * @see org.kalypso.swtchart.layer.ILayerManager#removeLayer(org.kalypso.swtchart.layer.IChartLayer)
   */
  public void removeLayer( final IChartLayer layer )
  {
    updateAxisLayerMap( layer, false );

    m_layers.remove( layer );

    // TODO: das LegendItem muss gelöscht werden

    if( m_autoscale )
      autoscale( null );

    redraw();
  }

  public void setAutoscale( boolean b )
  {
    m_autoscale = b;

    if( m_autoscale )
      autoscale( null );
  }

  public void setHideUnusedAxes( boolean b )
  {
    m_hideUnusedAxes = b;

    final IAxis[] axes = m_axisRegistry.getAxes();
    for( int i = 0; i < axes.length; i++ )
    {
      final IAxis axis = axes[i];
      final List<IChartLayer> list = m_axis2Layers.get( axis );
      if( list == null || list.size() == 0 )
      {
        final IAxisComponent comp = m_axisRegistry.getComponent( axis );
        comp.setVisible( !m_hideUnusedAxes );
      }
    }

    redraw();
  }

  /**
   * Gibt die Liste der Layer zurück
   */
  public List<IChartLayer> getLayers( )
  {
    return m_layers;
  }

  public Plot getPlot( )
  {
    return m_plot;
  }

  /**
   * TODO: Vordergrund und Hintergrundfarbe sind im Moment noch Hartcodiert
   */

  /**
   * Gibt die VordergrundFarbe des Charts zurück
   */
  public Color getFGColor( )
  {
    return m_black;
  }

  /**
   * Gibt die HintergrundFarbe des Charts zurück
   */
  public Color getBGColor( )
  {
    return m_white;
  }

  public void repaint( )
  {
    m_plot.repaint();

    final IAxisRegistry axisRegistry = getAxisRegistry();
    final IAxis[] axes = axisRegistry.getAxes();
    for( final IAxis axis : axes )
    {
      /*
       * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisComponent gecastet werden - sonst gibts keinen
       * Zugriff auf die Paint-Sachen
       */
      final AxisComponent ac = (AxisComponent) axisRegistry.getComponent( axis );
      ac.redraw();
    }
  }

}
