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
package org.kalypso.chart.ui.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.impl.ChartModel;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.chart.ui.editor.ChartEditorOutlinePage;
import org.kalypso.chart.ui.editor.mouse.DragZoomHandler;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartType;

/**
 * @author Thomas Jung
 */
public class ChartView extends ViewPart
{
  public static final String ID = "org.kalypso.chart.ui.view.ChartView";

  private Composite m_composite = null;

  private ChartConfigurationDocument m_configuration = null;

  private IChartModel m_chartModel = null;

  private ChartType m_chartType = null;

  private ChartConfigurationLoader m_chartConfigurationLoader = null;

  private final ChartEditorOutlinePage m_outlinePage = null;

  private ChartComposite m_chartComposite = null;

  private IFile m_input;

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( Composite parent )
  {
    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( new FillLayout() );

    updateControl();

  }

  public void setInput( final IFile input )
  {
    m_input = input;

    // prepare for exception
    m_chartType = null;
    m_configuration = null;

    try
    {

      m_chartConfigurationLoader = new ChartConfigurationLoader( m_input );
      m_configuration = m_chartConfigurationLoader.getChartConfigurationDocument();
      final ChartType[] charts = m_chartConfigurationLoader.getCharts();
      m_chartType = charts[0];

    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), "Load input", "Failed to load editor input", status );
    }

    updateControl();
  }

  private void updateControl( )
  {
    if( m_composite == null || m_composite.isDisposed() )
      return;

    m_chartModel = null;

    /* Reset controls */
    final Control[] children = m_composite.getChildren();
    for( final Control control : children )
      control.dispose();

    if( m_chartType == null )
    {
      final Label label = new Label( m_composite, SWT.NONE );
      label.setText( "No chart set" );
    }
    else
    {
      /* Create chart */
      final IFile file = m_input;
      try
      {
        final URL context = ResourceUtilities.createURL( file );

        m_chartModel = new ChartModel();

        try
        {
          m_chartConfigurationLoader = new ChartConfigurationLoader( file );
          ChartFactory.configureChartModel( m_chartModel, m_chartConfigurationLoader, m_chartType.getId(), context );
        }
        // TODO: provide message to user instead of eating the exceptions.
        catch( final ConfigurationException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final XmlException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        if( m_chartModel != null )
        {
          m_chartComposite = new ChartComposite( m_composite, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );
          final DragZoomHandler dragHandler = new DragZoomHandler( m_chartComposite );
          m_chartComposite.getPlot().addMouseListener( dragHandler );
          m_chartComposite.getPlot().addMouseMoveListener( dragHandler );
          m_chartComposite.getModel().setAutoscale( true );
        }
        // else: TODO: what?
      }
      catch( final MalformedURLException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    m_composite.layout();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    // TODO Auto-generated method stub

  }

}
