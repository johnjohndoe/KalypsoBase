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

package org.kalypso.ui.wizard.shape;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.transformation.CRSHelper;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypso.transformation.ui.listener.CRSSelectionListener;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * FIXME:
 * <ul>
 * <li>Allow to edit the path-fields</li>
 * <li>Refaktor a good abstraction for resource chooser</li>
 * <li>dialog settings!</li>
 * <li>if shape is selected, preselect sld with same name</li>
 * <li></li>
 * </ul>
 * 
 * @author kuepfer
 */
public class ImportShapeFileImportPage extends WizardPage
{
  enum StyleImport
  {
    useDefault("Use default", "Use a style from the default style registry. You will be not able to change this style later on."),
    generateDefault(Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.9" ), "Copy a style from the default style registry next to the shape file."), //$NON-NLS-1$
    selectExisting("Select existing", "Select an existing SLD-File from your project.");

    private final String m_label;

    private final String m_tooltip;

    private StyleImport( final String label, final String tooltip )
    {
      m_label = label;
      m_tooltip = tooltip;
    }

    public String getTooltip( )
    {
      return m_tooltip;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString( )
    {
      return m_label;
    }
  }

  // constants
  private static final int SIZING_TEXT_FIELD_WIDTH = 250;

  private static final String[] EMPTY_STYLE_NAMES = new String[] { "<No style file selected>" };

  private static final String[] FEATURETYPE_STYLE_NAMES = new String[] { "<FeatureTypeStyle - contains only one style>" };

  private static final String DATA_STYLE_IMPORT = "radioStyleImport"; //$NON-NLS-1$

  private CRSSelectionPanel m_crsPanel;

  private IProject m_project;

  // style
  protected String m_styleName;

  private StyleImport m_styleImportType = StyleImport.generateDefault;

  protected ViewerFilter m_filter;

  private IPath m_sourcePath;

  private String m_crs;

  private IPath m_stylePath;

  private Control[] m_styleControls;

  private Text m_styleTextField;

  private final Button[] m_styleImportRadios = new Button[StyleImport.values().length];

  public ImportShapeFileImportPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    setDescription( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.1" ) ); //$NON-NLS-1$
    setPageComplete( false );
  }

  @Override
  public void createControl( final Composite parent )
  {
    initializeDialogUnits( parent );

    final Composite topComposite = new Composite( parent, SWT.NULL );
    topComposite.setLayout( new GridLayout() );
    topComposite.setFont( parent.getFont() );

    createSourceGroup( topComposite );
    setControl( topComposite );
  }

  private void createSourceGroup( final Composite parent )
  {
    // shape source
    final Group fileGroup = new Group( parent, SWT.NULL );
    fileGroup.setLayout( new GridLayout( 3, false ) );
    fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    fileGroup.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.2" ) ); //$NON-NLS-1$

    final Label sourceFileLabel = new Label( fileGroup, SWT.NONE );
    sourceFileLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.3" ) ); //$NON-NLS-1$

    createSourceFileChooser( fileGroup );
    createCrsChooser( fileGroup );

    // style
    final Group styleGroup = new Group( parent, SWT.NULL );
    styleGroup.setLayout( new GridLayout( 3, false ) );
    styleGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    styleGroup.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.5" ) ); //$NON-NLS-1$

    createStyleChooser( styleGroup );
  }

  private void createSourceFileChooser( final Composite parent )
  {
    final Text sourceFileText = new Text( parent, SWT.BORDER );
    final GridData sourceFileTextData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    sourceFileTextData.minimumWidth = SIZING_TEXT_FIELD_WIDTH;
    sourceFileText.setLayoutData( sourceFileTextData );
    if( m_sourcePath != null )
      sourceFileText.setText( m_sourcePath.toOSString() );
    sourceFileText.setEditable( false );
    sourceFileText.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        handleSourcePathModified( sourceFileText.getText() );
      }
    } );

    final Button browseButton = new Button( parent, SWT.PUSH );
    browseButton.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.4" ) ); //$NON-NLS-1$
    browseButton.setLayoutData( new GridData( GridData.END ) );
    browseButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleSourceButtonSelected( sourceFileText );
      }
    } );
  }

  private void createCrsChooser( final Composite parent )
  {
    m_crsPanel = new CRSSelectionPanel( parent, SWT.NONE );
    m_crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    if( m_crs != null )
      m_crsPanel.setSelectedCRS( m_crs );

    m_crsPanel.addSelectionChangedListener( new CRSSelectionListener()
    {
      /**
       * @see org.kalypso.transformation.ui.CRSSelectionListener#selectionChanged(java.lang.String)
       */
      @Override
      protected void selectionChanged( final String selectedCRS )
      {
        handleCrsChanged( selectedCRS );
      }
    } );
  }

  private void createStyleChooser( final Composite parent )
  {
    final Composite radioPanel = new Composite( parent, SWT.NONE );
    radioPanel.setLayout( Layouts.createGridLayout() );
    radioPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    final StyleImport[] styleImportTypes = StyleImport.values();
    for( int i = 0; i < styleImportTypes.length; i++ )
      m_styleImportRadios[i] = addStyleRadio( radioPanel, styleImportTypes[i] );

    final Label styleLabel = new Label( parent, SWT.NONE );
    styleLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.6" ) ); //$NON-NLS-1$

    m_styleTextField = new Text( parent, SWT.BORDER );
    m_styleTextField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_styleTextField.setEditable( false );

    final Button styleBrowseButton = new Button( parent, SWT.PUSH );
    styleBrowseButton.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    styleBrowseButton.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.7" ) ); //$NON-NLS-1$

    final Label styleNameLabel = new Label( parent, SWT.NONE );
    styleNameLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.8" ) ); //$NON-NLS-1$

    final ComboViewer styleNameChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    styleNameChooser.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    styleNameChooser.setContentProvider( new ArrayContentProvider() );
    styleNameChooser.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element instanceof UserStyle )
        {
          final UserStyle userStyle = (UserStyle) element;
          final String title = userStyle.getTitle();
          if( StringUtils.isBlank( title ) )
            return userStyle.getName();
          return title;
        }

        return ObjectUtils.toString( element );
      }
    } );
    styleNameChooser.setInput( EMPTY_STYLE_NAMES );
    styleNameChooser.setSelection( new StructuredSelection( EMPTY_STYLE_NAMES[0] ) );

    m_styleControls = new Control[] { styleLabel, m_styleTextField, styleBrowseButton, styleNameLabel, styleNameChooser.getControl() };

    hookStyleChooserListeners( m_styleTextField, styleBrowseButton, styleNameChooser );

    for( final Control styleControl : m_styleControls )
      styleControl.setEnabled( m_styleImportType == StyleImport.selectExisting );
  }

  private Button addStyleRadio( final Composite parent, final StyleImport styleImport )
  {
    final Button radio = new Button( parent, SWT.RADIO );
    radio.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    radio.setText( styleImport.toString() );
    radio.setToolTipText( styleImport.getTooltip() );
    radio.setSelection( m_styleImportType == styleImport );
    radio.setData( DATA_STYLE_IMPORT, styleImport );

    radio.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleStyleImportRadioSelected( styleImport );
      }
    } );

    return radio;
  }

  private void hookStyleChooserListeners( final Text styleTextField, final Button styleBrowseButton, final ComboViewer styleNameChooser )
  {
    styleTextField.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        handleStylePathModified( styleTextField.getText(), styleNameChooser );
      }
    } );

    styleBrowseButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleStyleButtonSelected( styleTextField );
      }
    } );

    styleNameChooser.addSelectionChangedListener( new ISelectionChangedListener()
    {

      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleStyleNameSelected( selection.getFirstElement() );
      }
    } );
  }

  protected void handleStyleImportRadioSelected( final StyleImport styleImport )
  {
    m_styleImportType = styleImport;

    for( final Control styleControl : m_styleControls )
      styleControl.setEnabled( m_styleImportType == StyleImport.selectExisting );

    validate();
  }

  protected void handleStyleNameSelected( final Object style )
  {
    if( style instanceof Style )
      m_styleName = ((Style) style).getName();
    else if( style instanceof String )
      m_styleName = null;

    validate();
  }

  protected void handleStyleButtonSelected( final Text styleTextField )
  {
    final KalypsoResourceSelectionDialog dialog = createResourceDialog( new String[] { "sld" } ); //$NON-NLS-1$
    dialog.open();
    final Object[] result = dialog.getResult();
    if( result == null )
      return;

    final Path resultPath = (Path) result[0];
    styleTextField.setText( resultPath.toString() );
  }

  protected void handleCrsChanged( final String selectedCRS )
  {
    m_crs = selectedCRS;

    validate();
  }

  protected void handleSourceButtonSelected( final Text sourceFileText )
  {
    final KalypsoResourceSelectionDialog dialog = createResourceDialog( new String[] { "shp" } ); //$NON-NLS-1$
    if( m_filter != null )
      dialog.setViewerFilter( m_filter );
    dialog.open();

    final Object[] result = dialog.getResult();
    if( result == null )
      return;

    final Path resultPath = (Path) result[0];
    sourceFileText.setText( resultPath.toString() );
  }

  protected void handleSourcePathModified( final String text )
  {
    final IPath path = new Path( text );
    if( path.isValidPath( text ) )
      m_sourcePath = path;
    else
      m_sourcePath = null;

    // Try to find an .sld with the same filename -> preselect it
    if( m_stylePath == null )
    {
      final IFile shapeFile = getShapeFile();
      if( shapeFile != null )
      {
        final IPath sldPath = shapeFile.getFullPath().removeFileExtension().addFileExtension( "sld" );
        final IFile sldFile = ResourcesPlugin.getWorkspace().getRoot().getFile( sldPath );
        if( sldFile.exists() )
        {
          for( final Button styleRadio : m_styleImportRadios )
          {
            final StyleImport styleType = (StyleImport) styleRadio.getData( DATA_STYLE_IMPORT );
            styleRadio.setSelection( styleType == StyleImport.selectExisting );
          }

          handleStyleImportRadioSelected( StyleImport.selectExisting );
          m_styleTextField.setText( sldPath.toPortableString() );
        }
      }
    }

    validate();
  }

  private void reloadUserStyle( final ComboViewer styleNameChooser )
  {
    // TODO: we know at the moment, that this will only happen if user uses the dialog.
    // If we allow the user to modify the path, we need something more sophisticated to prevent too many reloads

    try
    {
      final Object[] styles = loadStyles();

      styleNameChooser.setInput( styles );

      if( styles.length == 0 )
        styleNameChooser.setSelection( StructuredSelection.EMPTY );
      else
        styleNameChooser.setSelection( new StructuredSelection( styles[0] ) );
    }
    catch( final CoreException e )
    {
      // FIXME:better error handling:show error in status composite

      e.printStackTrace();
    }
  }

  private Object[] loadStyles( ) throws CoreException
  {
    final IFile styleFile = getStyleFile();
    if( styleFile == null || !styleFile.exists() )
      return EMPTY_STYLE_NAMES;

    try
    {
      final Document doc = XMLTools.parse( styleFile );
      final Element documentElement = doc.getDocumentElement();
      if( StyledLayerDescriptor.ELEMENT_STYLEDLAYERDESCRIPTOR.equals( documentElement.getLocalName() ) )
      {
        final URL context = ResourceUtilities.createURL( styleFile );
        final StyledLayerDescriptor styledLayerDescriptor = SLDFactory.createStyledLayerDescriptor( context, documentElement );

        final Layer[] layers = styledLayerDescriptor.getLayers();
        final Collection<Style> allStyles = new ArrayList<Style>();
        for( final Layer layer : layers )
        {
          final Style[] styles = layer.getStyles();
          for( final Style style : styles )
            allStyles.add( style );
        }

        return allStyles.toArray( new Style[allStyles.size()] );
      }
      else if( FeatureTypeStyle.ELEMENT_FEATURETYPESTYLE.equals( documentElement.getLocalName() ) )
        return FEATURETYPE_STYLE_NAMES;
      else
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), "Failed to sld file", e );
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      throw new CoreException( status );
    }
  }

  protected void handleStylePathModified( final String stylePath, final ComboViewer styleNameChooser )
  {
    final IPath path = new Path( stylePath );
    if( path.isValidPath( stylePath ) )
      m_stylePath = path;
    else
      m_stylePath = null;

    validate();

    reloadUserStyle( styleNameChooser );
  }

  void validate( )
  {
    final IMessageProvider message = doValidate();
    if( message == null )
      setMessage( null );
    else
      setMessage( message.getMessage(), message.getMessageType() );
    setPageComplete( message == null );
  }

  private IMessageProvider doValidate( )
  {
    // shapeFile
    if( m_sourcePath == null )
      return new MessageProvider( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.13" ), ERROR ); //$NON-NLS-1$
    else
    {
      // TODO: check if path is a real file
    }

    // CoordinateSystem
    if( m_crs == null )
      return new MessageProvider( "Please choose a valid source coordinate system", ERROR );
    else if( !CRSHelper.isKnownCRS( m_crs ) )
      return new MessageProvider( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.12" ), ERROR ); //$NON-NLS-1$

    // User style
    if( m_styleImportType == StyleImport.selectExisting )
    {
      // TODO: check if path is a real file
      if( m_stylePath == null )
        return new MessageProvider( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.11" ), ERROR ); //$NON-NLS-1$
    }

    return null;
  }

  public void setViewerFilter( final ViewerFilter filter )
  {
    m_filter = filter;
  }

  public IPath getShapePath( )
  {
    return m_sourcePath;
  }

  public IFile getShapeFile( )
  {
    return PathUtils.toFile( m_sourcePath );
  }

  private KalypsoResourceSelectionDialog createResourceDialog( final String[] fileResourceExtensions )
  {
    return new KalypsoResourceSelectionDialog( getShell(), m_project, Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.14" ), fileResourceExtensions, m_project, new ResourceSelectionValidator() ); //$NON-NLS-1$
  }

  public String getCRS( )
  {
    return m_crsPanel.getSelectedCRS();
  }

  protected void setProjectSelection( final IProject project )
  {
    m_project = project;
  }

  public StyleImport getStyleImportType( )
  {
    return m_styleImportType;
  }

  public IPath getStylePath( )
  {
    return m_stylePath;
  }

  public IFile getStyleFile( )
  {
    return PathUtils.toFile( m_stylePath );
  }

  public String getStyleName( )
  {
    return m_styleName;
  }

  public String getRelativeShapePath( final IFile mapFile )
  {
    final IFile shapeFile = getShapeFile();
    if( shapeFile == null )
      return null;

    final IPath relativeShapePath = ResourceUtilities.makeRelativ( mapFile, shapeFile );
    if( "..".equals( relativeShapePath.segment( 0 ) ) )
      return UrlResolver.createProjectPath( m_sourcePath.removeFileExtension() );
    else
      return relativeShapePath.removeFileExtension().toPortableString();
  }

  public String getRelativeStylePath( final IFile mapFile )
  {
    return makeRelativeOrProjectRelative( mapFile, m_stylePath );
  }

  public static String makeRelativeOrProjectRelative( final IFile mapFile, final IPath path )
  {
    if( path == null )
      return null;

    final IFile relativeFile = PathUtils.toFile( path );

    final IPath relativeStylePath = ResourceUtilities.makeRelativ( mapFile, relativeFile );
    if( "..".equals( relativeStylePath.segment( 0 ) ) )
      return UrlResolver.createProjectPath( path );
    else
      return relativeStylePath.toPortableString();
  }
}
