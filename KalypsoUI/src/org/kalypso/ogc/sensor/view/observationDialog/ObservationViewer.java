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
package org.kalypso.ogc.sensor.view.observationDialog;

import java.awt.Frame;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.action.ActionButton;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
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
import org.kalypso.ogc.sensor.view.propertySource.ObservationPropertySource;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * ObservationViewer
 * 
 * @author schlienger (23.05.2005)
 */
public class ObservationViewer extends Composite
{
  /**
   * If this style is used, the properties viewer is not shown.
   */
  public static final int HIDE_PROPERTIES = 1 << 1;

  private static final String SETTINGS_WEIGHTS_BOTTOM = "weightsBottom"; //$NON-NLS-1$

  private static final String SETTINGS_WEIGHTS_MAIN = "weightsMain"; //$NON-NLS-1$

  private Text m_txtHref;

  private Button m_showRadioButton;

  private SimplePropertySheetViewer m_mdViewer;

  private final DiagView m_diagView = new DiagView();

  private ObservationChart m_chart;

  private final TableView m_tableView = new TableView();

  private ObservationTable m_table;

  private URL m_context = null;

  private Object m_input = null;

  private boolean m_show = true;

  private final IDialogSettings m_settings;

  private final Clipboard m_clipboard;

  public ObservationViewer( final Composite parent, final int style, final boolean header, final IObservationAction[] buttons, final IDialogSettings settings, final Clipboard clipboard )
  {
    super( parent, style );

    m_settings = settings;
    m_clipboard = clipboard;
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
  private Control createControlsForm( final Composite parent, final IObservationAction[] actions )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( actions.length, false ) );

    for( final IObservationAction action : actions )
    {
      action.init( this );

      final Button button = ActionButton.createButton( null, group, action );
      button.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    }

    return group;
  }

  private Control createHeaderForm( final Composite parent )
  {
    final Group header = new Group( parent, SWT.NONE );
    header.setLayout( new GridLayout( 4, false ) );

    // 1. HREF
    final Label lblObs = new Label( header, SWT.LEFT );
    lblObs.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.0" ) ); //$NON-NLS-1$
    lblObs.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );

    m_txtHref = new Text( header, SWT.BORDER | SWT.MULTI | SWT.WRAP );
    m_txtHref.setSize( 400, m_txtHref.getSize().y );
    m_txtHref.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    m_txtHref.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusLost( final FocusEvent e )
      {
        updateInput();
      }
    } );

    final ChooseZmlAction chooseLocalZmlAction = new ChooseZmlAction( this, Messages.getString( "ObservationViewer.0" ) ) //$NON-NLS-1$
    {
      @Override
      protected IContainer getBaseDir( )
      {
        final IFile contextIFile = ResourceUtilities.findFileFromURL( getContext() );
        return contextIFile.getParent();
      }
    };
    chooseLocalZmlAction.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.1" ) ); //$NON-NLS-1$
    ActionButton.createButton( null, header, chooseLocalZmlAction ).setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    final ChooseZmlAction chooseProjectZmlAction = new ChooseZmlAction( this, Messages.getString( "ObservationViewer.1" ) ) //$NON-NLS-1$
    {
      @Override
      protected IContainer getBaseDir( )
      {
        return ResourceUtilities.findProjectFromURL( getContext() );
      }
    };
    chooseProjectZmlAction.setText( Messages.getString( "ObservationViewer.2" ) ); //$NON-NLS-1$
    ActionButton.createButton( null, header, chooseProjectZmlAction ).setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    // 2. Anzeige
    m_showRadioButton = new Button( header, SWT.CHECK );
    m_showRadioButton.setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewer.8" ) ); //$NON-NLS-1$
    m_showRadioButton.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_BEGINNING ) );
    m_showRadioButton.setSelection( false );
    m_showRadioButton.addSelectionListener( new SelectionListener()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        updateInput();
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

    final Composite chartComp = new Composite( parent, SWT.NO_BACKGROUND | SWT.EMBEDDED | SWT.BORDER );
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

    final Composite tableComp = new Composite( form, SWT.NO_BACKGROUND | SWT.EMBEDDED );
    final Frame vFrame = SWT_AWT.new_Frame( tableComp );
    vFrame.setVisible( true );
    vFrame.add( m_table );

    final int[] mainWeights = getWeightsFromSettings( new int[] { 2, 5 }, SETTINGS_WEIGHTS_MAIN );
    form.setWeights( mainWeights );
    addWeightsListener( form, SETTINGS_WEIGHTS_MAIN );

    if( (getStyle() & HIDE_PROPERTIES) != 0 )
      form.setMaximizedControl( tableComp );
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

  void updateInput( )
  {
    final String href = m_txtHref.getText();
    final boolean show = m_showRadioButton.getSelection();

    setInput( href, show );
  }

  private void updateViewer( )
  {
    m_diagView.removeAllItems();
    m_tableView.removeAllItems();

    if( m_txtHref != null )
      m_txtHref.setText( StringUtils.EMPTY );

    final IObservation obs = getObservation();
    if( obs == null )
      return;

    m_mdViewer.setInput( new ObservationPropertySource( obs ) );

    if( m_show )
    {
      final PlainObsProvider pop = new PlainObsProvider( obs, null );

      final ItemData itd = new ObsView.ItemData( true, null, null, true );
      m_diagView.addObservation( pop, ObservationTokenHelper.TOKEN_AXISNAME_OBSNAME, itd );
      m_tableView.addObservation( pop, ObservationTokenHelper.TOKEN_AXISNAME_AXISUNIT, itd );
    }
  }

  private IObservation getObservation( )
  {
    if( m_input == null )
      return null;

    if( m_input instanceof IObservation )
      return (IObservation)m_input;

    if( m_input instanceof String )
      return readObservation( (String)m_input );

    return null;
  }

  private IObservation readObservation( final String href )
  {
    m_txtHref.setText( ZmlURL.getIdentifierPart( href ) );

    try
    {
      if( StringUtils.isBlank( href ) )
        return null;

      final URL url = UrlResolverSingleton.resolveUrl( m_context, href );
      return ZmlFactory.parseXML( url );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      return null;
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return null;
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

  URL getContext( )
  {
    return m_context;
  }

  public Clipboard getClipboard( )
  {
    return m_clipboard;
  }
}