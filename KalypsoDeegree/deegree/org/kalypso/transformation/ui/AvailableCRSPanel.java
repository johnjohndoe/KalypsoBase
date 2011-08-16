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
package org.kalypso.transformation.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.deegree.i18n.Messages;
import org.kalypso.transformation.CRSHelper;
import org.kalypso.transformation.crs.CoordinateSystemFactory;
import org.kalypso.transformation.crs.ICoordinateSystem;
import org.kalypso.transformation.ui.listener.IAvailableCRSPanelListener;
import org.kalypso.transformation.ui.provider.CRSLabelProvider;
import org.kalypso.transformation.ui.validators.CRSInputValidator;

/**
 * This class represents a panel with elements for managing the coordinate systems.
 * 
 * @author Holger Albert
 */
public class AvailableCRSPanel extends Composite implements IJobChangeListener
{
  /**
   * The list of available crs panel listener.
   */
  private final List<IAvailableCRSPanelListener> m_listener;

  /**
   * The list viewer of the coordinate systems. Null, if no controls has been created.
   */
  protected ListViewer m_viewer;

  /**
   * A hash of the displayed coordinate systems.
   */
  protected Map<String, ICoordinateSystem> m_coordHash;

  /**
   * The construtor.
   */
  public AvailableCRSPanel( final Composite parent, final int style )
  {
    super( parent, style );

    m_listener = new ArrayList<IAvailableCRSPanelListener>();
    m_viewer = null;
    m_coordHash = new HashMap<String, ICoordinateSystem>();

    /* Create the controls. */
    createControls();
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Set the layout data. */
    final GridLayout gridLayout = Layouts.createGridLayout();
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    super.setLayout( gridLayout );

    /* Create the main group for the panel. */
    final Group main = new Group( this, SWT.NONE );
    main.setLayout( new GridLayout( 3, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    main.setText( Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.0" ) ); //$NON-NLS-1$

    /* Create the combo. */
    m_viewer = new ListViewer( main, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
    final GridData viewerData = new GridData( SWT.FILL, SWT.FILL, true, false, 3, 0 );
    viewerData.heightHint = 200;
    m_viewer.getList().setLayoutData( viewerData );
    m_viewer.setContentProvider( new ArrayContentProvider() );
    m_viewer.setLabelProvider( new CRSLabelProvider( false ) );
    m_viewer.setSorter( new ViewerSorter() );
    m_viewer.setInput( new String[] { Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.1" ) } ); //$NON-NLS-1$

    /* Create the info image. */
    final Label imageLabel = new Label( main, SWT.NONE );
    imageLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );

    /* Set the image. */
    final ImageDescriptor imgDesc = ImageDescriptor.createFromURL( getClass().getResource( "resources/info.gif" ) ); //$NON-NLS-1$
    final Image infoImage = imgDesc.createImage();
    imageLabel.setImage( infoImage );

    m_viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        /* Get the code of the selected coordinate system. */
        final String selectedCRS = getSelectedCRS();
        if( selectedCRS == null )
        {
          /* Modify the tooltip. */
          imageLabel.setToolTipText( "" ); //$NON-NLS-1$
          return;
        }

        /* Modify the tooltip. */
        imageLabel.setToolTipText( CRSHelper.getTooltipText( selectedCRS ) );
      }
    } );

    /* Create the button. */
    final Button removeButton = new Button( main, SWT.PUSH );
    removeButton.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );
    removeButton.setText( Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.4" ) ); //$NON-NLS-1$

    /* Add a listener. */
    removeButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleRemovePressed();
      }
    } );

    /* Create the button. */
    final Button addButton = new Button( main, SWT.PUSH );
    addButton.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );
    addButton.setText( Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.5" ) ); //$NON-NLS-1$

    /* Add a listener. */
    addButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleAddPressed( e.display );
      }
    } );
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( final Layout layout )
  {
    /* Ignore user set layouts, only layout datas are permitted. */
  }

  /**
   * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
   */
  @Override
  public void setEnabled( final boolean enabled )
  {
    super.setEnabled( enabled );

    if( m_viewer != null && !m_viewer.getControl().isDisposed() )
      m_viewer.getControl().setEnabled( enabled );
  }

  /**
   * This function disposes the images.
   */
  @Override
  public void dispose( )
  {
    super.dispose();
  }

  /**
   * This function sets the available coordinate systems.
   * 
   * @param preferenceCodes
   *          An array of codes from coordinate systems. Make sure, they are ; seperated.
   */
  public void setAvailableCoordinateSystems( final String preferenceCodes )
  {
    if( m_viewer == null || m_viewer.getControl().isDisposed() )
      return;

    if( preferenceCodes == null || preferenceCodes.length() == 0 )
    {
      m_coordHash.clear();
      m_viewer.setInput( null );
      return;
    }

    /* Disable. */
    setEnabled( false );

    /* The codes of the coordinate systems as array. */
    final String[] codes = preferenceCodes.split( ";" ); //$NON-NLS-1$

    /* Start the job. */
    final CRSInitializeJob initCRSJob = new CRSInitializeJob( "CRSInitializeJob", codes ); //$NON-NLS-1$
    initCRSJob.setSystem( true );

    /* Add myself as a listener. */
    initCRSJob.addJobChangeListener( this );

    /* Schedule. */
    initCRSJob.schedule();
  }

  /**
   * This function returns the codes of all coordinate systems in the list and returns them as a ; seperated string.
   * 
   * @return The codes of the coordinate systems in the list as a ; seperated string.
   */
  public String getAvailableCoordinateSystems( )
  {
    /* Memory for the keys. */
    String preferenceCodes = ""; //$NON-NLS-1$

    /* Get the iterator for the keys. */
    final Iterator<String> iterator = m_coordHash.keySet().iterator();

    /* Iterate over the keys. */
    while( iterator.hasNext() )
    {
      /* Get the next key. */
      final String key = iterator.next();

      /* Add it to the string. */
      preferenceCodes = preferenceCodes + key;

      /* Make sure, the codes are ; seperated. */
      if( iterator.hasNext() )
        preferenceCodes = preferenceCodes + ";"; //$NON-NLS-1$
    }

    return preferenceCodes;
  }

  /**
   * This function sets the selection of the panel.
   * 
   * @param selection
   *          The selection.
   */
  public void setSelectedCRS( final String selectedCRS )
  {
    if( m_viewer != null )
    {
      final ICoordinateSystem coordinateSystem = m_coordHash.get( selectedCRS );
      if( coordinateSystem != null )
        m_viewer.setSelection( new StructuredSelection( coordinateSystem ) );
    }
  }

  /**
   * This function returns the code of the selected coordinate system.
   * 
   * @return The code of the selected coordinate system or null, if none is selected.
   */
  public String getSelectedCRS( )
  {
    /* Get the selection. */
    final ISelection selection = m_viewer.getSelection();

    /* If not empty and the right type, the code is returned. */
    if( !selection.isEmpty() && selection instanceof IStructuredSelection )
    {
      /* Cast. */
      final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      /* Get the selected element. */
      final Object selectedElement = structuredSelection.getFirstElement();

      /* Check type. */
      if( selectedElement instanceof ICoordinateSystem )
      {
        /* Cast. */
        final ICoordinateSystem coordinateSystem = (ICoordinateSystem) selectedElement;

        /* Return the code of the selected coordinate system. */
        return coordinateSystem.getCode();
      }
    }

    return null;
  }

  /**
   * This function adds a available crs panel listener.
   * 
   * @param listener
   *          The available crs panel listener.
   */
  public void addAvailableCRSPanelListener( final IAvailableCRSPanelListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a available crs panel listener.
   * 
   * @param listener
   *          The available crs panel listener.
   */
  public void removeAvailableCRSPanelListener( final IAvailableCRSPanelListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function adds a selection changed listener.
   * 
   * @param listener
   *          The selection changed listener.
   */
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    if( m_viewer != null )
      m_viewer.addSelectionChangedListener( listener );
  }

  /**
   * This function removes a selection changed listener.
   * 
   * @param listener
   *          The selection changed listener.
   */
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    if( m_viewer != null )
      m_viewer.removeSelectionChangedListener( listener );
  }

  /**
   * This function informs listeners about the coordinate systems, being initialized.
   * 
   * @param codes
   *          The list of codes of the coordinate systems.
   */
  protected void fireCoordinateSystemsInitialized( final String[] codes )
  {
    for( int i = 0; i < m_listener.size(); i++ )
    {
      /* Get the available crs panel listener. */
      final IAvailableCRSPanelListener availableCRSPanelListener = m_listener.get( i );

      /* Notify it about the initializing. */
      availableCRSPanelListener.coordinateSystemsInitialized( codes );
    }
  }

  /**
   * This function removes the selected coordinate system.
   */
  protected void handleRemovePressed( )
  {
    /* Get the selected coordinate system. */
    final String selectedCRS = getSelectedCRS();
    if( selectedCRS == null )
      return;

    /* Get the selected coordinate system. */
    final ICoordinateSystem coordinateSystem = m_coordHash.get( selectedCRS );
    if( coordinateSystem == null )
      return;

    /* Remove the selected coordinate system. */
    m_coordHash.remove( selectedCRS );

    /* Now remove it from the viewer, too. */
    m_viewer.remove( coordinateSystem );

    /* Reset the selection. */
    m_viewer.setSelection( new StructuredSelection() );

    /* Tell listeners, that a structure change has occured. */
    fireCoordinateSystemRemoved( coordinateSystem.getCode() );
  }

  /**
   * This function informs listeners about the coordinate system, which was removed.
   * 
   * @param code
   *          The code of the coordinate system.
   */
  private void fireCoordinateSystemRemoved( final String code )
  {
    for( int i = 0; i < m_listener.size(); i++ )
    {
      /* Get the available crs panel listener. */
      final IAvailableCRSPanelListener availableCRSPanelListener = m_listener.get( i );

      /* Notify it about the removal. */
      availableCRSPanelListener.coordinateSystemRemoved( code );
    }
  }

  /**
   * This function adds a new coordinate system.
   * 
   * @param display
   *          The display.
   */
  protected void handleAddPressed( final Display display )
  {
    try
    {
      /* Create the dialog for entering the EPSG code coordinate system. */
      final InputDialog dialog = new InputDialog( display.getActiveShell(), Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.10" ), Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.11" ), "EPSG:", new CRSInputValidator() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final int open = dialog.open();
      if( open == Window.CANCEL )
        return;

      /* The entered coordinate system. */
      final String code = dialog.getValue();

      /* Create it, the code should be already validated. */
      final ICoordinateSystem coordinateSystem = CoordinateSystemFactory.getCoordinateSystem( code );

      /* Actualize the hash. */
      m_coordHash.put( coordinateSystem.getCode(), coordinateSystem );

      /* Add it to the viewer. */
      m_viewer.add( coordinateSystem );

      /* Set the selection. */
      m_viewer.setSelection( new StructuredSelection( coordinateSystem ) );

      /* Tell listeners, that a structure change has occured. */
      fireCoordinateSystemAdded( coordinateSystem.getCode() );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * This function informs listeners about the coordinate system, which was added.
   * 
   * @param code
   *          The code of the coordinate system.
   */
  private void fireCoordinateSystemAdded( final String code )
  {
    for( int i = 0; i < m_listener.size(); i++ )
    {
      /* Get the available crs panel listener. */
      final IAvailableCRSPanelListener availableCRSPanelListener = m_listener.get( i );

      /* Notify it about the addition. */
      availableCRSPanelListener.coordinateSystemAdded( code );
    }
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void aboutToRun( final IJobChangeEvent event )
  {
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void awake( final IJobChangeEvent event )
  {
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void done( final IJobChangeEvent event )
  { /* Get the display. */
    final Display display = getDisplay();

    /* Create a UI job. */
    final UIJob uiJob = new UIJob( display, "AvailableCRSPanelRefreshJob" ) //$NON-NLS-1$
    {
      /**
       * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
       */
      @Override
      public IStatus runInUIThread( IProgressMonitor monitor )
      {
        try
        {
          /* If no monitor is given, create a null progress monitor. */
          if( monitor == null )
            monitor = new NullProgressMonitor();

          /* Monitor. */
          monitor.beginTask( Messages.getString( "org.kalypso.transformation.ui.AvailableCRSPanel.14" ), 100 ); //$NON-NLS-1$

          /* Get the job. */
          final Job job = event.getJob();
          if( !(job instanceof CRSInitializeJob) )
          {
            /* Monitor. */
            monitor.worked( 100 );

            return Status.OK_STATUS;
          }

          /* Cast. */
          final CRSInitializeJob initCRSJob = (CRSInitializeJob) job;

          /* If the viewer is already disposed, do nothing any more. */
          if( m_viewer == null || m_viewer.getControl().isDisposed() )
          {
            /* Monitor. */
            monitor.worked( 100 );

            /* Remove myself as a listener. */
            initCRSJob.removeJobChangeListener( AvailableCRSPanel.this );

            return Status.OK_STATUS;
          }

          /* Get the hash of them. */
          final Map<String, ICoordinateSystem> coordHash = initCRSJob.getCoordHash();
          if( coordHash == null || coordHash.size() == 0 )
          {
            /* Clear the hash. */
            m_coordHash.clear();

            /* Set the input. */
            m_viewer.setInput( null );

            /* Remove myself as a listener. */
            initCRSJob.removeJobChangeListener( AvailableCRSPanel.this );

            /* Enable. */
            setEnabled( true );

            /* Monitor. */
            monitor.worked( 100 );

            return Status.OK_STATUS;
          }

          /* Store the hash. */
          m_coordHash = coordHash;

          /* Set the input. */
          m_viewer.setInput( coordHash.values().toArray( new ICoordinateSystem[] {} ) );

          /* Notify it about the initializing. */
          fireCoordinateSystemsInitialized( coordHash.keySet().toArray( new String[] {} ) );

          /* Remove myself as a listener. */
          initCRSJob.removeJobChangeListener( AvailableCRSPanel.this );

          /* Enable. */
          setEnabled( true );

          /* Monitor. */
          monitor.worked( 100 );

          return Status.OK_STATUS;
        }
        catch( final Exception ex )
        {
          ex.printStackTrace();

          return StatusUtilities.statusFromThrowable( ex );
        }
        finally
        {
          /* Monitor. */
          monitor.done();
        }
      }
    };

    /* Execute the UI job. */
    uiJob.schedule();
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void running( final IJobChangeEvent event )
  {
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void scheduled( final IJobChangeEvent event )
  {
  }

  /**
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void sleeping( final IJobChangeEvent event )
  {
  }
}