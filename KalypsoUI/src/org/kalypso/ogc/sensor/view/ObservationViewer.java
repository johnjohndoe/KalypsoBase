/*
 * --------------- Kalypso-Header --------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.view;

import java.awt.Frame;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.jfreechart.ChartFactory;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.ogc.sensor.template.ObsView.ItemData;
import org.kalypso.ogc.sensor.view.observationDialog.IObservationAction;
import org.kalypso.ogc.sensor.view.propertySource.ObservationPropertySource;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;

/**
 * ObservationViewer
 * 
 * @author schlienger (23.05.2005)
 */
public class ObservationViewer extends Composite
{
  private static final String SETTINGS_WEIGHTS_BOTTOM = "weightsBottom";

  private static final String SETTINGS_WEIGHTS_MAIN = "weightsMain";

  private Label m_lblObs;

  protected Text m_txtHref;

  protected Text m_txtFilter;

  protected Text m_txtRange;

  private Button m_btnSelectObsLocal;

  private Button m_btnSelectObsProject;

  Button showRadioButton;

  private SimplePropertySheetViewer m_mdViewer;

  private final DiagView m_diagView = new DiagView();

  private ObservationChart m_chart;

  private final TableView m_tableView = new TableView();

  private ObservationTable m_table;

  URL m_context = null;

  private Object m_input = null;

  private boolean m_show = true;

  private final IDialogSettings m_settings;

  public ObservationViewer( final Composite parent, final int style, final boolean header, final IObservationAction[] buttons, final IDialogSettings settings )
  {
    super( parent, style );
    m_settings = settings;
    m_tableView.setAlphaSort( false );

    createControl( header, buttons );
  }

  private void createControl( final boolean withHeader, final IObservationAction[] buttons )
  {
    final GridLayout gridLayout = new GridLayout( 1, false );
    setLayout( gridLayout );

    final Composite main = new Composite( this, SWT.NONE );
    main.setLayout( new GridLayout() );

    main.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    if( withHeader )
    {
      final Control headerForm = createHeaderForm( main );
      headerForm.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

      m_show = false;
    }

    if( buttons.length > 0 )
    {
      final Control controlsForm = createControlsForm( main, buttons );
      controlsForm.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    }

    final SashForm bottom = new SashForm( main, SWT.HORIZONTAL );
    bottom.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    createMetadataAndTableForm( bottom );
    createDiagramForm( bottom );

    final int[] bottomWeights = getWeightsFromSettings( new int[] { 1, 3 }, SETTINGS_WEIGHTS_BOTTOM );
    bottom.setWeights( bottomWeights );

    addWeightsListener( bottom, SETTINGS_WEIGHTS_BOTTOM );
  }

  private void addWeightsListener( final SashForm form, final String settings )
  {
    final IDialogSettings dialogSettings = m_settings;

    form.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        if( dialogSettings == null )
          return;

        final int[] weights = form.getWeights();
        final String[] array = new String[weights.length];
        for( int i = 0; i < array.length; i++ )
          array[i] = Integer.toString( weights[i] );

        dialogSettings.put( settings, array );
      }
    } );
  }

  private int[] getWeightsFromSettings( final int[] defaultWeights, final String section )
  {
    if( m_settings == null )
      return defaultWeights;

    final String[] array = m_settings.getArray( section );
    if( array == null || array.length != defaultWeights.length )
      return defaultWeights;

    final int[] weights = new int[defaultWeights.length];
    try
    {
      for( int i = 0; i < weights.length; i++ )
        weights[i] = Integer.parseInt( array[i] );
      return weights;
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      return defaultWeights;
    }
  }

  /**
   * @param parent
   * @param buttonControls
   */
  private Control createControlsForm( final Composite parent, final IObservationAction[] buttonControls )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( buttonControls.length, false ) );

    for( final IObservationAction control : buttonControls )
    {
      final Button button = control.createButton( group );
      button.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );
    }

    return group;
  }

  private Control createHeaderForm( final Composite parent )
  {
    final Group header = new Group( parent, SWT.NONE );
    header.setLayout( new GridLayout( 4, false ) );

    // 1. HREF
    m_lblObs = new Label( header, SWT.LEFT );
    m_lblObs.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.0" ) ); //$NON-NLS-1$
    m_lblObs.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );

    m_txtHref = new Text( header, SWT.BORDER | SWT.MULTI | SWT.WRAP );
    m_txtHref.setSize( 400, m_txtHref.getSize().y );
    m_txtHref.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL ) );

    m_txtHref.addFocusListener( new FocusListener()
    {
      @Override
      public void focusGained( final FocusEvent e )
      {
        // nothing
      }

      @Override
      public void focusLost( final FocusEvent e )
      {
        final String filterText = m_txtFilter == null ? "" : m_txtFilter.getText(); //$NON-NLS-1$
        setInput( m_txtHref.getText(), filterText, getShow() );
      }
    } );

    m_btnSelectObsLocal = new Button( header, SWT.NONE );
    m_btnSelectObsLocal.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.1" ) ); //$NON-NLS-1$
    m_btnSelectObsLocal.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );
    m_btnSelectObsLocal.addSelectionListener( new SelectionListener()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        // hack to support local references (doemming)
        try
        {
          final IFile contextIFile = ResourceUtilities.findFileFromURL( m_context );
          final IContainer baseDir = contextIFile.getParent();
          final ResourceListSelectionDialog dialog = new ResourceListSelectionDialog( getShell(), baseDir, IResource.FILE, "*zml" ); //$NON-NLS-1$
          dialog.setBlockOnOpen( true );

          if( dialog.open() == Window.OK )
          {
            final Object[] result = dialog.getResult();
            if( result.length > 0 )
            {
              if( result[0] instanceof IFile )
              {
                final IFile r = (IFile) result[0];
                final URL url1 = m_context;
                final URL url2 = ResourceUtilities.createURL( r );

                final String href = FileUtilities.getRelativePathTo( url1.toExternalForm(), url2.toExternalForm() );
                if( href == null )
                  m_txtHref.setText( "" ); //$NON-NLS-1$
                else
                  m_txtHref.setText( href.substring( 1 ) );
                // refresh...
                final String filterText = m_txtFilter == null ? "" : m_txtFilter.getText(); //$NON-NLS-1$
                setInput( m_txtHref.getText(), filterText, getShow() );
              }
            }
          }
        }
        catch( final Exception e2 )
        {
          e2.printStackTrace();
        }
      }

      @Override
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        // nothing
      }
    } );
    m_btnSelectObsProject = new Button( header, SWT.NONE );
    m_btnSelectObsProject.setText( "Projekt..." ); //$NON-NLS-1$
    m_btnSelectObsProject.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );
    m_btnSelectObsProject.addSelectionListener( new SelectionListener()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        try
        {
          final IProject project = ResourceUtilities.findProjectFromURL( m_context );
          final IContainer baseDir = (project.getFolder( ".model" )).getParent(); //$NON-NLS-1$
          final ResourceListSelectionDialog dialog = new ResourceListSelectionDialog( getShell(), baseDir, IResource.FILE, "*zml" ); //$NON-NLS-1$
          dialog.setBlockOnOpen( true );
          if( dialog.open() == Window.OK )
          {
            final Object[] result = dialog.getResult();
            if( result.length > 0 )
            {
              if( result[0] instanceof IFile )
              {
                final IFile r = (IFile) result[0];
                final URL url1 = m_context;
                final URL url2 = ResourceUtilities.createURL( r );

                final String href = FileUtilities.getRelativePathTo( url1.toExternalForm(), url2.toExternalForm() );
                if( href == null )
                  m_txtHref.setText( "" ); //$NON-NLS-1$
                else
                  m_txtHref.setText( href );
                // refresh...
                setInput( m_txtHref.getText(), null, getShow() );
              }
            }
          }
        }
        catch( final Exception e2 )
        {
          e2.printStackTrace();
        }
      }

      @Override
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        // nothing
      }
    } );
    // 2. Anzeige
    showRadioButton = new Button( header, SWT.CHECK );
    showRadioButton.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.8" ) ); //$NON-NLS-1$
    showRadioButton.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );
    showRadioButton.setSelection( false );
    showRadioButton.addSelectionListener( new SelectionListener()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        // refresh...
        setInput( m_txtHref.getText(), null, showRadioButton.getSelection() );
      }

      @Override
      public void widgetDefaultSelected( final SelectionEvent e )
      {
        // nothing
      }
    } );

    return header;
  }

  private void createDiagramForm( final Composite parent )
  {
    try
    {
      m_chart = new ObservationChart( m_diagView );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      throw new IllegalStateException( e.getLocalizedMessage() );
    }

    final Composite chartComp = new Composite( parent, SWT.RIGHT | SWT.EMBEDDED );
    final Frame vFrame = SWT_AWT.new_Frame( chartComp );
    vFrame.add( ChartFactory.createChartPanel( m_chart ) );
    vFrame.setVisible( true );
  }

  private void createMetadataAndTableForm( final Composite parent )
  {
    final SashForm form = new SashForm( parent, SWT.VERTICAL );

    // METADATA
    m_mdViewer = new SimplePropertySheetViewer( form );

    // TABLE
    m_table = new ObservationTable( m_tableView, false, false );

    final Composite tableComp = new Composite( form, SWT.RIGHT | SWT.EMBEDDED );
    final Frame vFrame = SWT_AWT.new_Frame( tableComp );
    vFrame.setVisible( true );
    vFrame.add( m_table );

    final int[] mainWeights = getWeightsFromSettings( new int[] { 2, 5 }, SETTINGS_WEIGHTS_MAIN );
    form.setWeights( mainWeights );
    addWeightsListener( form, SETTINGS_WEIGHTS_MAIN );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    m_diagView.dispose();
    if( m_chart != null )
      m_chart.dispose();

    m_tableView.dispose();
    if( m_table != null )
      m_table.dispose();

    super.dispose();
  }

  void setInput( final String href, final String filter, final boolean show )
  {
    // 1. basic href
    String hereHref = href;

    // 2. plus filter stuff
    if( href.length() > 0 )
      hereHref = ZmlURL.insertFilter( hereHref, filter );

    setInput( hereHref, show );
  }

  private void updateViewer( )
  {
    // check type of input
    final IObservation obs;
    if( m_input == null )
      obs = null;
    else if( m_input instanceof IObservation )
    {
      obs = (IObservation) m_input;
    }
    else if( m_input instanceof String )
    {
      String href = (String) m_input;
      m_txtHref.setText( ZmlURL.getIdentifierPart( href ) );

      // always insert date-range info for loading
      if( href.length() > 0 )
      {
        href = (String) m_input;
      }
      final URL url;
      try
      {
        url = UrlResolverSingleton.resolveUrl( m_context, href );
      }
      catch( final MalformedURLException e )
      {
        return;
      }

      if( href.length() > 0 )
      {
        try
        {
          obs = ZmlFactory.parseXML( url );
        }
        catch( final SensorException e1 )
        {
          e1.printStackTrace();
          return;
        }
      }
      else
        obs = null;
    }
    else
      return;
    m_diagView.removeAllItems();
    m_tableView.removeAllItems();
    if( obs != null && m_show )
    {
      m_mdViewer.setInput( new ObservationPropertySource( obs ) );

      final PlainObsProvider pop = new PlainObsProvider( obs, null );

      final ItemData itd = new ObsView.ItemData( true, null, null, true );
      m_diagView.addObservation( pop, ObservationTokenHelper.TOKEN_AXISNAME_OBSNAME, itd );
      m_tableView.addObservation( pop, ObservationTokenHelper.TOKEN_AXISNAME_AXISUNIT, itd );
    }
    else if( obs != null )
    {
      m_mdViewer.setInput( new ObservationPropertySource( obs ) );
    }
  }

  /**
   * @param context
   */
  public void setContext( final URL context )
  {
    m_context = context;
  }

  /**
   * @param input
   */
  public void setInput( final Object input, final boolean show )
  {
    if( input != null && input.equals( m_input ) && show == m_show )
      return;
    m_input = input;
    m_show = show;
    updateViewer();
  }

  /**
   * @return the input
   */
  public Object getInput( )
  {
    return m_input;
  }

  /**
   * @return the show
   */
  public Boolean getShow( )
  {
    return m_show;
  }
}