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
package org.kalypso.ui.views.map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterFinder;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.listeners.IMapPanelListener;
import org.kalypso.ogc.gml.map.listeners.MapPanelAdapter;
import org.kalypso.transformation.CRSHelper;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Point;

/**
 * This item displays map coordinates in the status line.
 * 
 * @author Dirk Kuch
 */
public class MapCoordinateStatusLineItem extends WorkbenchWindowControlContribution implements IAdapterEater<IMapPanel>
{
  static String MAP_POSITION_TEXT = "%.2f / %.2f"; //$NON-NLS-1$

  private static final IAdapterFinder<IMapPanel> m_initFinder = new EditorFirstAdapterFinder<>();

  private final IMapPanelListener m_panelListener = new MapPanelAdapter()
  {
    @Override
    public void onMouseMoveEvent( final IMapPanel source, final GM_Point gmPoint, final int mousex, final int mousey )
    {
      doScheduleUpdateLabelJob( gmPoint );
    }
  };

  private final AdapterPartListener<IMapPanel> m_adapterListener = new AdapterPartListener<>( IMapPanel.class, this, m_initFinder, m_initFinder );

  private Label m_label;

  private Composite m_composite;

  private IMapPanel m_panel;

  private final UpdateItemJob m_updateLabelJob = new UpdateItemJob( "Updating position label ...", this ); //$NON-NLS-1$

  private GM_Point m_gmPoint;

  public MapCoordinateStatusLineItem( )
  {
    m_updateLabelJob.setSystem( true );
  }

  @Override
  public void dispose( )
  {
    m_adapterListener.dispose();
    if( m_panel != null )
      m_panel.removeMapPanelListener( m_panelListener );

    super.dispose();
  }

  @Override
  protected Control createControl( final Composite parent )
  {
    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( GridLayoutFactory.fillDefaults().numColumns( 3 ).create() );

    final ImageHyperlink lnk = new ImageHyperlink( m_composite, SWT.NONE );
    final Image image = KalypsoGisPlugin.getImageProvider().getImage( ImageProvider.DESCRIPTORS.STATUS_LINE_SHOW_MAP_COORDS );
    lnk.setImage( image );
    lnk.setEnabled( false );
    lnk.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, true ) );

    m_label = new Label( m_composite, SWT.NONE );
    m_label.setToolTipText( Messages.getString( "org.kalypso.ui.views.map.MapCoordinateStatusLineItem.1" ) ); //$NON-NLS-1$
    final GridData gridData = new GridData( GridData.FILL, GridData.CENTER, true, true );
    gridData.widthHint = 175;
    m_label.setLayoutData( gridData );

    final Label imageLabel = new Label( m_composite, SWT.NONE );
    imageLabel.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, true ) );
    final Image infoImage = KalypsoGisPlugin.getImageProvider().getImage( ImageProvider.DESCRIPTORS.STATUS_LINE_SHOW_CRS_INFO );
    imageLabel.setImage( infoImage );

    /* Set the CRS info. */
    startCrsInfoJob( imageLabel );

    /* Add some listeners. */
    m_composite.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        handleLabelDisposed();
      }
    } );

    final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if( activePage != null )
      m_adapterListener.init( activePage );

    return m_composite;
  }

  protected void handleLabelDisposed( )
  {
    m_adapterListener.dispose();
  }

  @Override
  public void update( )
  {
    /* Check twice, perhaps m_label was disposed */
    if( m_label == null || m_label.isDisposed() )
      return;

    if( m_gmPoint == null )
      m_label.setText( StringUtils.EMPTY );
    else
    {
      final double x = m_gmPoint.getX();
      final double y = m_gmPoint.getY();

      m_label.setText( String.format( MapCoordinateStatusLineItem.MAP_POSITION_TEXT, x, y ) );
      m_label.getParent().layout();
    }

    super.update();
  }

  private void startCrsInfoJob( final Label infoLabel )
  {
    final Display display = infoLabel.getDisplay();

    /* Fetch CRS in separate, non-ui job, because it is a long running operation */
    final Job fetchCrsJob = new Job( "Fetching Kalypso CoordinateSystem" ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        /*
         * Check early for dispose, because item is created on workbench startup but soon disposed again, so fetching
         * srs is not necessary.
         */
        if( infoLabel.isDisposed() )
          return Status.OK_STATUS;

        final String coordinateSystem = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
        final UIJob uiJob = new UIJob( display, "Setting crs info" ) //$NON-NLS-1$
        {
          @Override
          public IStatus runInUIThread( final IProgressMonitor progress )
          {
            if( !infoLabel.isDisposed() )
              infoLabel.setToolTipText( CRSHelper.getTooltipText( coordinateSystem ) );
            return Status.OK_STATUS;
          }
        };
        uiJob.setSystem( true );
        uiJob.schedule();

        return Status.OK_STATUS;
      }
    };

    fetchCrsJob.setPriority( Job.LONG );
    fetchCrsJob.setSystem( true );
    fetchCrsJob.schedule();
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IMapPanel adapter )
  {
    if( !m_composite.isDisposed() )
      m_composite.setVisible( adapter != null );

    if( m_panel != null )
      m_panel.removeMapPanelListener( m_panelListener );

    m_panel = adapter;

    if( m_panel != null )
      m_panel.addMapPanelListener( m_panelListener );
  }

  protected void doScheduleUpdateLabelJob( final GM_Point gmPoint )
  {
    m_gmPoint = gmPoint;

    if( m_updateLabelJob != null )
      m_updateLabelJob.cancel();

    if( m_label != null && !m_label.isDisposed() )
      m_updateLabelJob.schedule();
  }
}