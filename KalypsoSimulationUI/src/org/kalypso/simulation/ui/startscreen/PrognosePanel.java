/*--------------- Kalypso-Header --------------------------------------------------------------------

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
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.startscreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.swt.graphics.FontUtilities;
import org.kalypso.model.xml.Modellist;
import org.kalypso.model.xml.ModellistType;
import org.kalypso.model.xml.ObjectFactory;
import org.kalypso.model.xml.ModellistType.ModelType;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.actions.StartCalcWizardDelegate;
import org.kalypso.simulation.ui.calccase.ModelSynchronizer;
import org.kalypso.simulation.ui.dialogs.OrganisePrognosesDialog;
import org.kalypso.ui.KalypsoGisPlugin;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
public class PrognosePanel
{
  private Modellist m_modellist = null;

  private String m_errorMessage;

  private final Map m_imageHash = new HashMap();

  private final ModelLabelProvider m_labelProvider = new ModelLabelProvider();

  private final FontUtilities m_fontUtils = new FontUtilities();

  private final URL m_location;

  private Label m_imageLabel;

  private Composite m_control;

  private ModelType m_model;

  public PrognosePanel( final URL modellistLocation )
  {
    m_location = modellistLocation;

    try
    {
      final InputSource inputSource = new InputSource( modellistLocation.openStream() );
      m_modellist = (Modellist)new ObjectFactory().createUnmarshaller().unmarshal( inputSource );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      m_errorMessage = e.getLocalizedMessage();
    }
  }

  public void dispose()
  {
    m_labelProvider.dispose();
    m_fontUtils.dispose();
  }

  public Composite createControl( final Composite parent, final IWorkbenchWindow window )
  {
    final Display display = parent.getDisplay();
    final Color background = display.getSystemColor( SWT.COLOR_WHITE );

    m_control = new Composite( parent, SWT.NONE );

    final GridLayout gridLayout = new GridLayout( 2, false );
    //    gridLayout.horizontalSpacing = 20;
    //    gridLayout.verticalSpacing = 20;
    //    gridLayout.marginHeight = 20;
    //    gridLayout.marginWidth = 20;
    m_control.setLayout( gridLayout );

    if( m_modellist == null )
    {
      final Label label = new Label( parent, SWT.CENTER );
      label.setLayoutData( new GridData( GridData.FILL_BOTH ) );
      label.setText( "Die Modellliste konnte nicht geladen werden: " + m_errorMessage );
      return m_control;
    }

    final Label mainImageLabel = new Label( m_control, SWT.NONE );
    final GridData mainLabelgridData = new GridData();
    mainLabelgridData.horizontalSpan = 2;
    mainLabelgridData.horizontalAlignment = GridData.CENTER;
    mainLabelgridData.grabExcessHorizontalSpace = true;
    mainLabelgridData.verticalSpan = 1;
    mainLabelgridData.verticalAlignment = GridData.FILL;
    mainImageLabel.setLayoutData( mainLabelgridData );

    try
    {
      final String mainImageName = m_modellist.getMainImage();
      final URL mainImageURL = new URL( m_location, mainImageName );
      final Image mainImage = new Image( display, mainImageURL.openStream() );
      mainImageLabel.setImage( mainImage );
    }
    catch( MalformedURLException e )
    {
      e.printStackTrace();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }

    final Label headingLabel = new Label( m_control, SWT.SINGLE );
    final GridData headingGridData = new GridData( GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 2 );
    headingLabel.setLayoutData( headingGridData );
    final Font headingFont = m_fontUtils.createChangedFontData( headingLabel.getFont().getFontData(), 10, SWT.BOLD,
        headingLabel.getDisplay() );
    headingLabel.setFont( headingFont );
    headingLabel.setBackground( background );
    headingLabel.setText( "Einzugsgebiet:" );

    ////////////////
    // LEFT PANEL //
    ////////////////
    final Composite leftPanel = new Composite( m_control, SWT.NO_MERGE_PAINTS );
    leftPanel.setLayout( new GridLayout() );
    leftPanel.setLayoutData( new GridData( GridData.FILL_VERTICAL ) );
    leftPanel.setBackground( background );

    final List list = new List( leftPanel, SWT.SINGLE );
    final GridData listGridData = new GridData( GridData.BEGINNING, GridData.BEGINNING, false, true );
    list.setLayoutData( listGridData );
    final Font font = list.getFont();
    final Font listfont = m_fontUtils.createChangedFontData( font.getFontData(), 10, SWT.NONE, list.getDisplay() );
    list.setFont( listfont );

    final ListViewer viewer = new ListViewer( list );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( m_labelProvider );

    final Label separator = new Label( leftPanel, SWT.NONE );
    separator.setText( "" );

    final Font buttonfont = m_fontUtils.createChangedFontData( font.getFontData(), 3, SWT.NONE, list.getDisplay() );

    final Button startButton = new Button( leftPanel, SWT.PUSH | SWT.FLAT );
    startButton.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    startButton.setText( "Vorhersage starten" );
    startButton.setToolTipText( "Hochwasser-Vorhersage starten" );
    startButton.setFont( buttonfont );
    startButton.addSelectionListener( new SelectionAdapter()
    {
      public void widgetSelected( final SelectionEvent e )
      {
        final IProject project = updateModel( window );
        if( project != null )
          StartCalcWizardDelegate.openCalculactionWizardOnProject( window, project );
      }
    } );

    final Button organizeButton = new Button( leftPanel, SWT.PUSH | SWT.FLAT );
    organizeButton.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    organizeButton.setFont( buttonfont );
    organizeButton.setText( "Vorhersagen verwalten" );
    organizeButton.setToolTipText( "Hochwasser-Vorhersage verwalten" );
    organizeButton.addSelectionListener( new SelectionAdapter()
    {
      public void widgetSelected( final SelectionEvent e )
      {
        final IProject project = updateModel( window );
        if( project != null )
          new OrganisePrognosesDialog( parent.getShell(), project ).open();
      }
    } );

    ////////////////
    // RIGHT SIDE //
    ///////////////
    m_imageLabel = new Label( m_control, SWT.NONE );
    final GridData imageGridData = new GridData( GridData.HORIZONTAL_ALIGN_CENTER );
    imageGridData.verticalSpan = 2;
    m_imageLabel.setLayoutData( imageGridData );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ModellistType.ModelType model = (ModelType)( (IStructuredSelection)event.getSelection() )
            .getFirstElement();

        setModel( model );
      }
    } );

    // create content
    viewer.setInput( m_modellist.getModel() );
    viewer.setSelection( new StructuredSelection( m_modellist.getModel().get( 0 ) ) );

    return m_control;
  }

  protected void setModel( final ModellistType.ModelType model )
  {
    m_model = model;

    final Image oldImage = m_imageLabel.getImage();

    ImageData imageData = (ImageData)m_imageHash.get( model );
    if( imageData == null )
    {
      try
      {
        final URL imageURL = new URL( m_location, model.getImage() );
        final InputStream openStream = imageURL.openStream();
        if( openStream != null )
        {
          imageData = new ImageData( openStream );
          m_imageHash.put( model, imageData );
        }
      }
      catch( MalformedURLException e )
      {
        e.printStackTrace();
      }
      catch( IOException e )
      {
        e.printStackTrace();
      }
    }

    final Image newImage = imageData == null ? null : new Image( m_control.getDisplay(), imageData );
    m_imageLabel.setImage( newImage );

    if( oldImage != null )
      oldImage.dispose();

    m_control.layout();
    m_control.redraw();
  }

  protected IProject updateModel( final IWorkbenchWindow window ) throws IllegalStateException
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();
    final String modelName = m_model.getName();
    final IProject project = root.getProject( modelName );

    final WorkspaceModifyOperation operation = new WorkspaceModifyOperation()
    {
      protected void execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException,
          InterruptedException
      {
        final File serverRoot = KalypsoGisPlugin.getDefault().getServerModelRoot();

        if( serverRoot == null )
          throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0,
              "Kein serverseitiges Modellverzeichnis definiert. Prognose kann nicht gestartet werden.", null ) );

        final File serverProject = new File( serverRoot, modelName );
        if( !serverProject.exists() )
          throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0,
              "Servermodell existiert nicht! Prognose kann nicht gestartet werden.", null ) );

        final ModelSynchronizer synchronizer = new ModelSynchronizer( project, serverProject );
        synchronizer.updateLocal( monitor );
      }
    };
    
    final IStatus status = RunnableContextHelper.execute( window, false, true, operation );
    if( !status.isOK() )
    {
      ErrorDialog.openError( window.getShell(), "Vorhersage starten", "Modell konnte nicht aktualisiert werden", status );
      return null;
    }

    return project;
  }
}