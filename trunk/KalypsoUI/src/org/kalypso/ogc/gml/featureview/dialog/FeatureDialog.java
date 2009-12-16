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
package org.kalypso.ogc.gml.featureview.dialog;

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.ogc.gml.featureview.maker.CachedFeatureviewFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewHelper;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * This dialog is able to edit existing features.
 * 
 * @author Holger Albert
 */
public class FeatureDialog extends Dialog
{
  /**
   * Feature change listener.
   */
  private IFeatureChangeListener m_changeListener = new IFeatureChangeListener()
  {
    /**
     * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#featureChanged(org.kalypso.commons.command.ICommand)
     */
    public void featureChanged( ICommand changeCommand )
    {
      try
      {
        /* Execute the commands. */
        m_workspace.postCommand( changeCommand );
      }
      catch( Exception ex )
      {
        /* Log the error message. */
        KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
      }
    }

    /**
     * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#openFeatureRequested(org.kalypsodeegree.model.feature.Feature,
     *      org.kalypso.gmlschema.property.IPropertyType)
     */
    public void openFeatureRequested( Feature feature, IPropertyType pt )
    {
    }
  };

  /**
   * Modell listener.
   */
  private ModellEventListener m_modelListener = new ModellEventListener()
  {
    /**
     * @see org.kalypsodeegree.model.feature.event.ModellEventListener#onModellChange(org.kalypsodeegree.model.feature.event.ModellEvent)
     */
    public void onModellChange( ModellEvent modellEvent )
    {
      if( modellEvent.isType( ModellEvent.FEATURE_CHANGE ) )
      {
        final FeatureComposite featureComposite = getFeatureComposite();
        final Control control = featureComposite == null ? null : featureComposite.getControl();
        if( control != null && !control.isDisposed() )
        {
          PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable()
          {
            public void run( )
            {
              /* Update. */
              if( !control.isDisposed() )
                featureComposite.updateControl();
            }
          } );
        }
      }
    }
  };

  /**
   * The title of the dialog.
   */
  private String m_title;

  /**
   * The workspace of the feature. It is a new commandable workspace, because we want be able to make only the changes
   * of this dialog undone.
   */
  protected CommandableWorkspace m_workspace;

  /**
   * The feature, which should be edited.
   */
  private Feature m_feature;

  /**
   * The feature composite.
   */
  protected FeatureComposite m_featureComposite;

  /**
   * The URL to a gft, if one should be used. May be null.
   */
  private URL m_gftUrl;

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The parent shell, or null to create a top-level shell.
   * @param title
   *          The title of the dialog.
   * @param feature
   *          The feature, which should be edited.
   * @param gftUrl
   *          The URL to a gft, if one should be used. May be null.
   */
  public FeatureDialog( Shell parentShell, String title, Feature feature, URL gftUrl )
  {
    super( parentShell );

    m_title = title;
    m_workspace = new CommandableWorkspace( feature.getWorkspace() );
    m_feature = feature;
    m_featureComposite = null;
    m_gftUrl = gftUrl;
  }

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The object that returns the current parent shell.
   * @param title
   *          The title of the dialog.
   * @param feature
   *          The feature, which should be edited.
   * @param gftUrl
   *          The URL to a gft, if one should be used. May be null.
   */
  public FeatureDialog( IShellProvider parentShell, String title, Feature feature, URL gftUrl )
  {
    super( parentShell );

    m_title = title;
    m_workspace = new CommandableWorkspace( feature.getWorkspace() );
    m_feature = feature;
    m_featureComposite = null;
    m_gftUrl = gftUrl;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( m_title );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the feature composite. */
    CachedFeatureviewFactory featureviewFactory = new CachedFeatureviewFactory( new FeatureviewHelper() );
    if( m_gftUrl != null )
      featureviewFactory.addView( m_gftUrl );
    m_featureComposite = new FeatureComposite( null, null, featureviewFactory );

    /* Add the listeners. */
    m_featureComposite.addChangeListener( m_changeListener );
    m_feature.getWorkspace().addModellListener( m_modelListener );

    /* Set the feature, which is edited. */
    m_featureComposite.setFeature( m_feature );

    /* Create the control. */
    m_featureComposite.createControl( main, SWT.NONE );
    main.layout( true, true );

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    /* Deinitialize everything. */
    deInit();

    super.okPressed();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
   */
  @Override
  protected void cancelPressed( )
  {
    while( m_workspace.canUndo() )
    {
      try
      {
        m_workspace.undo();
      }
      catch( Exception ex )
      {
        /* Log the error message. */
        KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( ex ) );
      }
    }

    /* Deinitialize everything. */
    deInit();

    super.cancelPressed();
  }

  /**
   * This function returns the feature composite, in which the edited feature is edited.<br>
   * If the controls of this dialog are not created yet or an error has occured creating them, this function returns
   * null.
   * 
   * @return The feature composite or null.
   */
  protected FeatureComposite getFeatureComposite( )
  {
    return m_featureComposite;
  }

  /**
   * This function deinitializes the dialog.
   */
  private void deInit( )
  {
    if( m_featureComposite != null )
    {
      m_featureComposite.removeChangeListener( m_changeListener );
      m_featureComposite.dispose();
    }

    if( m_feature != null )
    {
      GMLWorkspace workspace = m_feature.getWorkspace();
      workspace.removeModellListener( m_modelListener );
    }

    m_changeListener = null;
    m_modelListener = null;
    m_title = null;
    m_workspace = null;
    m_feature = null;
    m_featureComposite = null;
  }
}