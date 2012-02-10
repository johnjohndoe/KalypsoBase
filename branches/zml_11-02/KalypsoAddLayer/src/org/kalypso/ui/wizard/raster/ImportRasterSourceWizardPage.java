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
package org.kalypso.ui.wizard.raster;

import java.net.URL;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;

/**
 * @author Nadja Peiler
 */
public class ImportRasterSourceWizardPage extends WizardPage
{
  private IProject m_project;

  private IPath m_stylePath;

  private String m_styleName;

  private boolean m_checkDefaultStyle = true;

  protected ImportRasterSourceWizardPage( final String pageName )
  {
    super( pageName );

    setTitle( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizard.0" ) ); //$NON-NLS-1$
    final String radioLabel = Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.7" ); //$NON-NLS-1$
    setDescription( String.format( "Please choose an existing .sld file or check '%s' to create a new one next to the data file.", radioLabel ) );
    setImageDescriptor( ImageProvider.IMAGE_KALYPSO_ICON_BIG );

    setPageComplete( true );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite topComposite = new Composite( parent, SWT.NULL );
    topComposite.setFont( parent.getFont() );
    initializeDialogUnits( parent );
    topComposite.setLayout( new GridLayout() );
    setControl( topComposite );

    final Control styleGroup = createStyleGroup( topComposite );
    styleGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private Control createStyleGroup( final Composite parent )
  {
    final Group styleGroup = new Group( parent, SWT.NULL );
    styleGroup.setText( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.3" ) ); //$NON-NLS-1$

    styleGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    styleGroup.setLayout( new GridLayout( 3, false ) );

    final Button checkDefaultStyleButton = new Button( styleGroup, SWT.CHECK );
    checkDefaultStyleButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );
    checkDefaultStyleButton.setSelection( m_checkDefaultStyle );
    checkDefaultStyleButton.setText( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.7" ) ); //$NON-NLS-1$

    final Label styleLabel = new Label( styleGroup, SWT.NONE );
    styleLabel.setText( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.4" ) ); //$NON-NLS-1$

    final Text styleTextField = new Text( styleGroup, SWT.BORDER );
    styleTextField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    styleTextField.setEditable( false );

    final Button browseButton = new Button( styleGroup, SWT.PUSH );
    browseButton.setText( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.5" ) ); //$NON-NLS-1$
    browseButton.setLayoutData( new GridData( GridData.END ) );

    final Label styleNameLabel = new Label( styleGroup, SWT.NONE );
    styleNameLabel.setText( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.6" ) ); //$NON-NLS-1$

    final Combo styleNameCombo = new Combo( styleGroup, SWT.READ_ONLY );
    styleNameCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    new Label( styleGroup, SWT.NONE ).setText( "" ); //$NON-NLS-1$

    browseButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleBrowseButtonSelected( styleTextField, styleNameCombo );
      }
    } );

    styleNameCombo.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final String text = styleNameCombo.getText();
        handleStyleNameComboSelected( text );
      }
    } );

    checkDefaultStyleButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        setCheckDefaultStyle( checkDefaultStyleButton.getSelection() );
        if( checkDefaultStyleButton.getSelection() )
        {
          styleLabel.setEnabled( false );
          styleTextField.setEnabled( false );
          browseButton.setEnabled( false );
          styleNameLabel.setEnabled( false );
          styleNameCombo.setEnabled( false );
        }
        else
        {
          styleLabel.setEnabled( true );
          styleTextField.setEnabled( true );
          browseButton.setEnabled( true );
          styleNameLabel.setEnabled( true );
          styleNameCombo.setEnabled( true );
        }
        validate();
      }

    } );

    return styleGroup;
  }

  KalypsoResourceSelectionDialog createResourceDialog( final String[] fileResourceExtensions )
  {
    return new KalypsoResourceSelectionDialog( getShell(), m_project, Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.8" ), fileResourceExtensions, m_project, new ResourceSelectionValidator() ); //$NON-NLS-1$
  }

  protected void setCheckDefaultStyle( final boolean selection )
  {
    m_checkDefaultStyle = selection;
  }

  protected void handleStyleNameComboSelected( final String styleName )
  {
    m_styleName = styleName;
  }

  public void setProject( final IProject project )
  {
    m_project = project;
  }

  void validate( )
  {
    setErrorMessage( null );
    setMessage( null );
    setPageComplete( true );

    final StringBuffer error = new StringBuffer();
    if( !m_checkDefaultStyle )
    {
      if( m_stylePath != null )
      {
        setPageComplete( true );
      }
      else
      {
        error.append( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.10" ) ); //$NON-NLS-1$
        setPageComplete( false );
      }

      if( m_styleName != null )
      {
        setPageComplete( true );
      }
      else
      {
        error.append( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizardPage.11" ) ); //$NON-NLS-1$
        setPageComplete( false );
      }
    }

    if( error.length() > 0 )
      setErrorMessage( error.toString() );
  }

  public boolean checkDefaultStyle( )
  {
    return m_checkDefaultStyle;
  }

  public IPath getStylePath( )
  {
    return m_stylePath;
  }

  public String getStyleName( )
  {
    return m_styleName;
  }

  protected void handleBrowseButtonSelected( final Text styleTextField, final Combo styleNameCombo )
  {
    final KalypsoResourceSelectionDialog dialog = createResourceDialog( new String[] { "sld" } ); //$NON-NLS-1$
    if( !(dialog.open() == Window.OK) )
      return;

    final Object[] result = dialog.getResult();
    final Path resultPath = (Path) result[0];
    styleTextField.setText( resultPath.toString() );
    m_stylePath = resultPath;

    try
    {
      final IPath basePath = m_project.getLocation();
      final String styleURLAsString = basePath.toFile().toURI().toURL() + m_stylePath.removeFirstSegments( 1 ).toString();
      final URL styleURL = new URL( styleURLAsString );
      final StyledLayerDescriptor styledLayerDescriptor = SLDFactory.createSLD( styleURL );
      // TODO: move into helper
      final Layer[] layers = styledLayerDescriptor.getLayers();
      final Vector<String> styleNameVector = new Vector<String>();
      for( final Layer layer : layers )
      {
        final Style[] styles = layer.getStyles();
        for( final Style element : styles )
          styleNameVector.add( element.getName() );
      }
      final String[] styleNames = new String[styleNameVector.size()];
      for( int k = 0; k < styleNameVector.size(); k++ )
        styleNames[k] = styleNameVector.get( k );

      styleNameCombo.setItems( styleNames );
      styleNameCombo.select( 0 );
      m_styleName = styleNames[0];
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final Status status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), "Failed to read SLD file", e );
      new StatusDialog( getShell(), status, "Select Style" ).open();
    }

    validate();
  }
}