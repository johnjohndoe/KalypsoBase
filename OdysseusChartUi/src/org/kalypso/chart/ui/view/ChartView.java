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
package org.kalypso.chart.ui.view;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.chart.ui.editor.ChartEditorTreeOutlinePage;
import org.kalypso.chart.ui.editor.mousehandler.AxisDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.TooltipHandler;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * @author Thomas Jung
 */
public class ChartView extends ViewPart implements IChartPart, ISelectionListener
{
  public static final String ID = "org.kalypso.chart.ui.view.ChartView";

  private Composite m_composite = null;

  private IChartModel m_chartModel = null;

  private ChartType m_chartType = null;

  private ChartConfigurationLoader m_chartConfigurationLoader = null;

  private ChartComposite m_chartComposite = null;

  private IFile m_input;

  private PlotDragHandlerDelegate m_plotDragHandler;

  private TooltipHandler m_tooltipHandler;

  private AxisDragHandlerDelegate m_axisDragHandler;

  private ChartEditorTreeOutlinePage m_outlinePage;

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {

    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( new FillLayout() );
    getSite().getPage().addSelectionListener( this );

    updateControl();

  }

  public void setInput( final IFile input )
  {
    m_input = input;

    if( m_chartComposite != null )
    {
      m_chartComposite.dispose();
      m_chartComposite = null;
    }
    // prepare for exception
    m_chartType = null;

    try
    {
      m_chartConfigurationLoader = new ChartConfigurationLoader( m_input );
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
    {
      return;
    }

    m_chartModel = null;

    /* Reset controls */
    final Control[] children = m_composite.getChildren();
    for( final Control control : children )
    {
      control.dispose();
    }

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

        m_chartConfigurationLoader = new ChartConfigurationLoader( file );
        IExtensionLoader el = ChartExtensionLoader.getInstance();
        ChartFactory.configureChartModel( m_chartModel, m_chartConfigurationLoader, m_chartType.getId(), el, context );

        if( m_chartModel != null )
        {
          m_chartComposite = new ChartComposite( m_composite, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );

          // DragHandler erzeugen
          m_plotDragHandler = new PlotDragHandlerDelegate( m_chartComposite );
          m_axisDragHandler = new AxisDragHandlerDelegate( m_chartComposite );

          // TooltipHandler setzen - meldet sich selbst an
          m_tooltipHandler = new TooltipHandler( m_chartComposite );

          // Titel der View setzen
          setPartName( m_chartModel.getTitle() );

          // m_chartComposite.getChartModel().setAutoscale( true );
        }
        // else: TODO: what?
      }
      catch( final Exception e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoChartUiPlugin.getDefault().getLog().log( status );
        ErrorDialog.openError( getSite().getShell(), "Update input", "Failed to update view.", status );
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

  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartComposite()
   */
  public ChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartDragHandler()
   */
  public PlotDragHandlerDelegate getPlotDragHandler( )
  {
    return m_plotDragHandler;
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    if( m_axisDragHandler != null )
    {
      m_axisDragHandler.dispose();
    }

    if( m_plotDragHandler != null )
    {
      m_plotDragHandler.dispose();
    }

    if( m_tooltipHandler != null )
    {
      m_tooltipHandler.dispose();
    }
    if( m_outlinePage != null )
    {
      m_outlinePage.dispose();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( ChartComposite.class.equals( adapter ) )
    {
      if( m_chartComposite != null && !m_chartComposite.isDisposed() )
        return m_chartComposite;
      else
        return null;
    }

    if( IChartPart.class.equals( adapter ) )
    {
      return this;
    }

    if( IContentOutlinePage.class.equals( adapter ) )
    {
      if( m_outlinePage == null )
      {
        m_outlinePage = new ChartEditorTreeOutlinePage( this );
      }

      return m_outlinePage;
    }
    return super.getAdapter( adapter );
  }

  /**
   * Overridden in order to make public.
   * 
   * @see org.eclipse.ui.part.WorkbenchPart#setTitleImage(org.eclipse.swt.graphics.Image)
   */
  @Override
  public void setTitleImage( final Image titleImage )
  {
    super.setTitleImage( titleImage );
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getAxisDragHandler()
   */
  public AxisDragHandlerDelegate getAxisDragHandler( )
  {
    return m_axisDragHandler;
  }

  /**
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( IWorkbenchPart part, ISelection selection )
  {

    // TODO Auto-generated method stub
    if( selection instanceof ITreeSelection )
    {

      ITreeSelection ts = (ITreeSelection) selection;
      TreePath[] paths = ts.getPaths();
      for( TreePath treePath : paths )
      {
        Object ls = treePath.getLastSegment();
        if( ls instanceof IFile )
        {
          IFile f = (IFile) ls;
          if( f.getFileExtension().equals( "kod" ) )
          {
            try
            {
              f.refreshLocal( 1, null );
            }
            catch( CoreException e )
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            setInput( f );
          }
        }
      }
    }
  }
}
