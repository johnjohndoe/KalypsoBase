/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.view.wq.diagram;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.AxisMapping;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewCurve;
import org.kalypso.ogc.sensor.diagview.DiagramAxis;
import org.kalypso.ogc.sensor.diagview.jfreechart.ChartFactory;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTableSet;

/**
 * @author schlienger
 */
public class WQRelationDiagramViewer extends Composite
{
  private ObservationChart m_chart;

  private final DiagView m_diagView = new DiagView( true );

  public WQRelationDiagramViewer( final Composite parent )
  {
    super( parent, SWT.RIGHT | SWT.EMBEDDED | SWT.BORDER );

    ControlUtils.addDisposeListener( this );

    try
    {
      m_chart = new ObservationChart( m_diagView );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      return;
    }

    final Frame vFrame = SWT_AWT.new_Frame( this );
    vFrame.add( ChartFactory.createChartPanel( m_chart ) );
    vFrame.setVisible( true );
  }

  @Override
  public void dispose( )
  {
    m_diagView.dispose();

    if( m_chart != null )
      m_chart.dispose();
  }

  public void setInput( final WQTableSet wqs ) throws SensorException
  {
    m_chart.clearChart();

    if( wqs == null )
      return;

    final String fromType = wqs.getFromType();
    final String toType = wqs.getToType();

    final String fromUnit = TimeseriesUtils.getUnit( fromType );
    final String toUnit = TimeseriesUtils.getUnit( toType );

    final DiagramAxis diagramAxisFrom = new DiagramAxis( fromType, "double", fromType, fromUnit, DiagramAxis.DIRECTION_VERTICAL, DiagramAxis.POSITION_LEFT, false ); //$NON-NLS-1$
    final DiagramAxis diagramAxisTo = new DiagramAxis( toType, "double", toType, toUnit, DiagramAxis.DIRECTION_HORIZONTAL, DiagramAxis.POSITION_BOTTOM, false ); //$NON-NLS-1$
    m_diagView.addAxis( diagramAxisFrom );
    m_diagView.addAxis( diagramAxisTo );

    final WQTable[] tables = wqs.getTables();
    for( final WQTable table : tables )
    {
      final IObservation obs = WQCurveFactory.createObservation( table );

      final AxisMapping[] axmaps = new AxisMapping[2];
      axmaps[0] = new AxisMapping( obs.getAxes()[0], diagramAxisFrom );
      axmaps[1] = new AxisMapping( obs.getAxes()[1], diagramAxisTo );

      final DiagViewCurve curve = new DiagViewCurve( m_diagView, new PlainObsProvider( obs, null ), obs.getName(),
          ColorUtilities.random(), null, axmaps );

      m_chart.getObservationPlot().addCurve( curve );
    }
  }
}
