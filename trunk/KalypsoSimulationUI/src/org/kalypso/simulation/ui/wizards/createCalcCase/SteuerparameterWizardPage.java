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
package org.kalypso.simulation.ui.wizards.createCalcCase;

import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.IProjectProvider;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.ogc.gml.featureview.maker.CachedFeatureviewFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewHelper;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * Wizard-Page zur Eingabe der Steuerparameter
 * 
 * @author belger
 */
public class SteuerparameterWizardPage extends WizardPage
{
  private final IProjectProvider m_projectProvider;

  private final CachedFeatureviewFactory m_fvFactory = new CachedFeatureviewFactory( new FeatureviewHelper() );

  private final FeatureComposite m_featureComposite = new FeatureComposite( null, new FeatureSelectionManager2(), m_fvFactory );

  private boolean m_update;

  private Button m_checkUpdate;

  protected GMLWorkspace m_workspace;

  private IFolder m_currentCalcCase;

  private Composite m_panel;

  private final boolean m_canGoBack;

  public SteuerparameterWizardPage( final IProjectProvider pp, final ImageDescriptor image, final boolean canGoBack )
  {
    super( "EditCalcCaseControlPage", Messages.getString( "org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage.0" ), image ); //$NON-NLS-1$ //$NON-NLS-2$
    m_canGoBack = canGoBack;

    final FeatureComposite featureComposite = m_featureComposite;
    m_featureComposite.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        try
        {
          changeCommand.process();
          // We know that we are the only one who changes our workspace, so calling updateControl is enough
          // else, we would have to register a workspace-listener
          featureComposite.updateControl();
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }

      @Override
      public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
      {
      }
    } );

    m_projectProvider = pp;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_featureComposite != null )
      m_featureComposite.dispose();

    if( m_workspace != null )
      m_workspace.dispose();
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    m_panel = new Composite( parent, SWT.NONE );
    m_panel.setLayout( new GridLayout() );

    createFeatureControl( m_panel );
    m_panel.layout();

    setControl( m_panel );
  }

  public void saveChanges( final IFolder folder, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage.1" ), 1000 ); //$NON-NLS-1$

    // SPEICHERN
    final IFile controlFile = folder.getFile( ModelNature.CONTROL_NAME );

    final GMLWorkspace workspace = m_workspace;
    final SetContentHelper thread = new SetContentHelper()
    {
      @Override
      public void write( final OutputStreamWriter w ) throws Throwable
      {
        GmlSerializer.serializeWorkspace( w, workspace );
      }
    };

    try
    {
      thread.setFileContents( controlFile, false, false, new NullProgressMonitor() );
    }
    finally
    {
      monitor.done();
    }
  }

  public boolean isUpdate( )
  {
    return m_update;
  }

  public void setUpdate( final boolean update )
  {
    if( m_update != update )
    {
      m_update = update;

      final Button checkUpdate = m_checkUpdate;
      if( checkUpdate != null && !checkUpdate.isDisposed() )
        checkUpdate.getDisplay().asyncExec( new Runnable()
        {
          @Override
          public void run( )
          {
            checkUpdate.setSelection( update );
          }
        } );
    }
  }

  /**
   * Setzt die aktuelle Rechenvariante, ist dort schon eine .calculation vorhanden, wird diese geladen, sonst die
   * default.
   * 
   * @param currentCalcCase
   */
  public void setFolder( final IFolder currentCalcCase )
  {
    m_currentCalcCase = currentCalcCase;

    final Composite panel = m_panel;
    if( panel != null && !panel.isDisposed() )
    {
      m_panel.getDisplay().asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          createFeatureControl( panel );
          panel.layout();
        }
      } );
    }
  }

  /**
   * Returns the current calc case folder, <code>null</code>, if not yet set.
   */
  public IFolder getFolder( )
  {
    return m_currentCalcCase;
  }

  protected void createFeatureControl( final Composite panel )
  {
    // dispose old control
    m_featureComposite.disposeControl();
    if( m_checkUpdate != null )
    {
      m_checkUpdate.dispose();
      m_checkUpdate = null;
    }
    final IProject project = m_projectProvider.getProject();
    if( project == null )
      return;

    // gleich mal den Workspace auf das default setzen
    try
    {
      final ModelNature nature = (ModelNature) project.getNature( ModelNature.ID );
      m_workspace = nature.loadOrCreateControl( m_currentCalcCase );
    }
    catch( final CoreException e )
    {
      // ignore, m_workspace stays null, this will be handle later
      e.printStackTrace();

      setErrorMessage( e.getLocalizedMessage() );
    }

    if( m_workspace != null )
    {
      final Feature f = m_workspace.getRootFeature();
      m_featureComposite.setFeature( f );
    }

    try
    {
      final URL viewURL = new URL( "platform:/resource/" + project.getName() + "/" + ModelNature.CONTROL_VIEW_PATH ); //$NON-NLS-1$ //$NON-NLS-2$
      m_fvFactory.addView( viewURL );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      // ignore, its 'just' bad configured
    }

    if( m_workspace == null )
    {
      setPageComplete( false );
      return;
    }

    m_featureComposite.createControl( panel, SWT.NONE );

    final Button checkUpdate = new Button( m_panel, SWT.CHECK );
    checkUpdate.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );
    checkUpdate.setText( Messages.getString( "org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage.2" ) ); //$NON-NLS-1$
    checkUpdate.setToolTipText( Messages.getString( "org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage.3" ) ); //$NON-NLS-1$
    checkUpdate.setSelection( m_update );
    m_checkUpdate = checkUpdate;

    checkUpdate.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        setUpdate( checkUpdate.getSelection() );
      }
    } );

    final FeatureComposite featureComposite = m_featureComposite;
    if( featureComposite != null )
    {
      featureComposite.setFeature( m_workspace.getRootFeature() );
      featureComposite.updateControl();
    }
  }

  /**
   * @see org.eclipse.jface.wizard.WizardPage#getPreviousPage()
   */
  @Override
  public IWizardPage getPreviousPage( )
  {
    return m_canGoBack ? super.getPreviousPage() : null;
  }
}