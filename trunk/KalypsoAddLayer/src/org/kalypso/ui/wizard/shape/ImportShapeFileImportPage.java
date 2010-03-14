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

/*
 * Created on 31.01.2005
 *
 */
package org.kalypso.ui.wizard.shape;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.transformation.CRSHelper;
import org.kalypso.transformation.ui.CRSSelectionListener;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.xml.XMLParsingException;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;

/**
 * @author kuepfer
 */
public class ImportShapeFileImportPage extends WizardPage implements SelectionListener, ModifyListener, KeyListener
{
  // constants
  private static final int SIZING_TEXT_FIELD_WIDTH = 250;

  // widgets
  private Group m_group;

  private Label m_sourceFileLabel;

  private Text m_sourceFileText;

  private Composite m_topComposite;

  private Button m_browseButton;

  // mapping

  private IPath m_relativeSourcePath;

  private CRSSelectionPanel m_crsPanel;

  private IProject m_project;

  // style
  private Text styleTextField;

  private Button browseButton2;

  protected Path stylePath;

  protected Combo styleNameCombo;

  protected String styleName;

  private boolean checkDefaultStyle = false;

  private Button checkDefaultStyleButton;

  private Label styleNameLabel;

  private Label styleLabel;

  protected ViewerFilter m_filter;

  /**
   * @param pageName
   */
  public ImportShapeFileImportPage( final String pageName )
  {
    super( pageName );
    setDescription( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.0") ); //$NON-NLS-1$
    setPageComplete( false );
  }

  /**
   * @param pageName
   * @param title
   * @param titleImage
   */
  public ImportShapeFileImportPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );
    setDescription( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.1") ); //$NON-NLS-1$
    setPageComplete( false );
  }

  /*
   * (non-Javadoc)
   *
   * @see wizard.eclipse.jface.dialogs.IDialogPage#createControl(wizard.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    m_topComposite = new Composite( parent, SWT.NULL );
    m_topComposite.setFont( parent.getFont() );

    initializeDialogUnits( parent );

    // WorkbenchHelp.setHelp(topComposite,
    // IHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

    m_topComposite.setLayout( new GridLayout() );
    m_topComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    // build wizard page
    createFileGroup( m_topComposite );
    setControl( m_topComposite );
  }

  private void createFileGroup( final Composite parent )
  {
    m_group = new Group( parent, SWT.NULL );
    final GridLayout topGroupLayout = new GridLayout();
    final GridData topGroupData = new GridData();
    topGroupLayout.numColumns = 3;
    topGroupData.horizontalAlignment = GridData.FILL;
    m_group.setLayout( topGroupLayout );
    m_group.setLayoutData( topGroupData );
    m_group.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.2") ); //$NON-NLS-1$
    m_sourceFileLabel = new Label( m_group, SWT.NONE );
    m_sourceFileLabel.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.3") ); //$NON-NLS-1$

    // Set width of source path field
    final GridData data0 = new GridData( GridData.FILL_HORIZONTAL );
    data0.widthHint = SIZING_TEXT_FIELD_WIDTH;

    m_sourceFileText = new Text( m_group, SWT.BORDER );
    m_sourceFileText.setLayoutData( data0 );
    m_sourceFileText.setEditable( false );
    m_sourceFileText.addModifyListener( this );

    m_browseButton = new Button( m_group, SWT.PUSH );
    m_browseButton.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.4") ); //$NON-NLS-1$
    m_browseButton.setLayoutData( new GridData( GridData.END ) );
    m_browseButton.addSelectionListener( this );

    m_crsPanel = new CRSSelectionPanel( parent, SWT.NONE );
    m_crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_crsPanel.addSelectionChangedListener( new CRSSelectionListener()
    {
      /**
       * @see org.kalypso.transformation.ui.CRSSelectionListener#selectionChanged(java.lang.String)
       */
      @Override
      protected void selectionChanged( final String selectedCRS )
      {
        validate();
      }
    } );

    m_group.pack();

    // style
    final Group styleGroup = new Group( parent, SWT.NULL );
    styleGroup.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.5") ); //$NON-NLS-1$

    final GridData data3 = new GridData();
    data3.horizontalAlignment = GridData.FILL;
    data3.grabExcessHorizontalSpace = true;
    styleGroup.setLayoutData( data3 );
    final GridLayout gridLayout1 = new GridLayout();
    gridLayout1.numColumns = 3;
    styleGroup.setLayout( gridLayout1 );

    styleLabel = new Label( styleGroup, SWT.NONE );
    styleLabel.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.6") ); //$NON-NLS-1$

    styleTextField = new Text( styleGroup, SWT.BORDER );
    final GridData data4 = new GridData();
    data4.horizontalAlignment = GridData.FILL;
    data4.grabExcessHorizontalSpace = true;
    styleTextField.setLayoutData( data4 );
    styleTextField.setEditable( false );

    browseButton2 = new Button( styleGroup, SWT.PUSH );
    browseButton2.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.7") ); //$NON-NLS-1$
    browseButton2.setLayoutData( new GridData( GridData.END ) );
    browseButton2.addSelectionListener( this );

    styleNameLabel = new Label( styleGroup, SWT.NONE );
    styleNameLabel.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.8") ); //$NON-NLS-1$

    styleNameCombo = new Combo( styleGroup, SWT.READ_ONLY );
    final GridData data5 = new GridData();
    data5.horizontalAlignment = GridData.FILL;
    data5.grabExcessHorizontalSpace = true;
    styleNameCombo.setLayoutData( data5 );
    styleNameCombo.addSelectionListener( this );

    // new SelectionAdapter()
    // {
    // public void widgetSelected( SelectionEvent e )
    // {
    // styleName = styleNameCombo.getText();
    // validate();
    // }
    // } );

    final Label dummyLabel = new Label( styleGroup, SWT.NONE );
    dummyLabel.setText( "" ); //$NON-NLS-1$

    checkDefaultStyleButton = new Button( styleGroup, SWT.CHECK );
    checkDefaultStyleButton.setSelection( checkDefaultStyle );
    checkDefaultStyleButton.addSelectionListener( this );

    final Label defaultStyleLabel = new Label( styleGroup, SWT.NONE );
    defaultStyleLabel.setText( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.9") ); //$NON-NLS-1$

  }

  void validate( )
  {
    setErrorMessage( null );
    boolean pageComplete = true;
    if( !checkDefaultStyle )
    {
      // styleName
      if( styleName != null )
      {
        // ok
      }
      else
      {
        setErrorMessage( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.10") ); //$NON-NLS-1$
        pageComplete = false;
      }

      // styleFile
      if( styleTextField.getText() != null && styleTextField.getText().length() > 0 )
      {
        // ok
      }
      else
      {
        setErrorMessage( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.11") ); //$NON-NLS-1$
        pageComplete = false;
      }
    }

    // CoordinateSystem
    if( checkCRS( m_crsPanel.getSelectedCRS() ) )
    {
      // ok
    }
    else
    {
      setErrorMessage( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.12") ); //$NON-NLS-1$
      pageComplete = false;
    }

    // shapeFile
    if( m_sourceFileText.getText() != null && m_sourceFileText.getText().length() > 0 )
    {
      // ok
    }
    else
    {
      setErrorMessage( Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.13") ); //$NON-NLS-1$
      pageComplete = false;
    }

    setPageComplete( pageComplete );
  }

  public void setViewerFilter( ViewerFilter filter )
  {
    m_filter = filter;
  }

  // SelectionListener
  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetSelected( final SelectionEvent e )
  {
    Button b;
    if( e.widget instanceof Button )
    {
      b = (Button) e.widget;
      if( b.equals( m_browseButton ) )
      {
        final KalypsoResourceSelectionDialog dialog = createResourceDialog( new String[] { "shp" } ); //$NON-NLS-1$
        if( m_filter != null )
          dialog.setViewerFilter( m_filter );

        dialog.open();
        final Object[] result = dialog.getResult();
        if( result != null )
        {
          final Path resultPath = (Path) result[0];
          m_sourceFileText.setText( resultPath.toString() );
          m_relativeSourcePath = resultPath;
        }
      }
      if( b.equals( browseButton2 ) )
      {
        final KalypsoResourceSelectionDialog dialog = createResourceDialog( new String[] { "sld" } ); //$NON-NLS-1$
        dialog.open();
        final Object[] result = dialog.getResult();
        if( result != null )
        {
          final Path resultPath = (Path) result[0];
          styleTextField.setText( resultPath.toString() );
          stylePath = resultPath;
          try
          {
            final IPath basePath = m_project.getLocation();
            final String styleURLAsString = basePath.toFile().toURI().toURL() + stylePath.removeFirstSegments( 1 ).toString();
            final URL styleURL = new URL( styleURLAsString );
            final Reader reader = new InputStreamReader( (styleURL).openStream() );
            final IUrlResolver2 resolver = new IUrlResolver2()
            {

              public URL resolveURL( final String href ) throws MalformedURLException
              {
                return UrlResolverSingleton.resolveUrl( styleURL, href );
              }

            };
            final StyledLayerDescriptor styledLayerDescriptor = SLDFactory.createSLD( resolver, reader );
            reader.close();
            final Layer[] layers = styledLayerDescriptor.getLayers();
            final Vector<String> styleNameVector = new Vector<String>();
            for( final Layer layer : layers )
            {
              final Style[] styles = layer.getStyles();
              for( final Style style : styles )
              {
                styleNameVector.add( style.getName() );
              }
            }
            final String[] styleNames = new String[styleNameVector.size()];
            for( int k = 0; k < styleNameVector.size(); k++ )
            {
              styleNames[k] = styleNameVector.get( k );
            }
            styleNameCombo.setItems( styleNames );
            styleNameCombo.select( 0 );
            styleName = styleNames[0];
          }
          catch( final MalformedURLException e1 )
          {
            e1.printStackTrace();
          }
          catch( final IOException ioEx )
          {
            ioEx.printStackTrace();
          }
          catch( final XMLParsingException xmlEx )
          {
            xmlEx.printStackTrace();
          }
        }
      }
      if( b.equals( checkDefaultStyleButton ) )
      {
        checkDefaultStyle = checkDefaultStyleButton.getSelection();
        if( checkDefaultStyleButton.getSelection() )
        {
          styleLabel.setEnabled( false );
          styleTextField.setEnabled( false );
          browseButton2.setEnabled( false );
          styleNameLabel.setEnabled( false );
          styleNameCombo.setEnabled( false );
        }
        else
        {
          styleLabel.setEnabled( true );
          styleTextField.setEnabled( true );
          browseButton2.setEnabled( true );
          styleNameLabel.setEnabled( true );
          styleNameCombo.setEnabled( true );
        }
      }
    }
    if( e.widget instanceof Combo )
    {
      if( e.widget == styleNameCombo )
      {
        styleName = styleNameCombo.getText();
      }
    }

    validate();
  }

  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetDefaultSelected( final SelectionEvent e )
  {
    // no default selection
  }

  // ModifyListener
  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText( final ModifyEvent e )
  {
    validate();
  }

  // KeyListener
  /**
   * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  public void keyPressed( final KeyEvent e )
  {
    final Widget w = e.widget;
    if( w instanceof Combo && e.character == SWT.CR )
    {
      validate();
    }
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  public void keyReleased( final KeyEvent e )
  {
    // do nothing
  }

  public File getShapeBaseFile( )
  {
    return new File( m_project.getLocation() + "/" + FileUtilities.nameWithoutExtension( m_relativeSourcePath.removeFirstSegments( 1 ).toString() ) ); //$NON-NLS-1$
  }

  public String getShapeBaseRelativePath( )
  {
    return "project:/" + FileUtilities.nameWithoutExtension( m_relativeSourcePath.removeFirstSegments( 1 ).toString() ); //$NON-NLS-1$
  }

  public IPath getShapePath( )
  {
    return m_relativeSourcePath;
  }

  KalypsoResourceSelectionDialog createResourceDialog( final String[] fileResourceExtensions )
  {
    return new KalypsoResourceSelectionDialog( getShell(), m_project, Messages.getString("org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.14"), fileResourceExtensions, m_project, new ResourceSelectionValidator() ); //$NON-NLS-1$
  }

  public String getCRS( )
  {
    return m_crsPanel.getSelectedCRS();
  }

  protected void setProjectSelection( final IProject project )
  {
    m_project = project;
  }

  private boolean checkCRS( final String customCRS )
  {
    return CRSHelper.isKnownCRS( customCRS );
  }

  public boolean checkDefaultStyle( )
  {
    return checkDefaultStyle;
  }

  public IPath getStylePath( )
  {
    return stylePath;
  }

  public String getStyleName( )
  {
    return styleName;
  }

  public void removeListeners( )
  {
    m_browseButton.removeSelectionListener( this );
    // m_checkCRS.removeSelectionListener( this );
  }
}
