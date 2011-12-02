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
package org.kalypso.ogc.gml.featureview.dialog.feature;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
public class FeatureDialog extends TitleAreaDialog
{
  /**
   * Feature change listener.
   */
  private IFeatureChangeListener m_changeListener = new IFeatureChangeListener()
  {
    /**
     * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#featureChanged(org.kalypso.commons.command.ICommand)
     */
    @Override
    public void featureChanged( ICommand changeCommand )
    {
      try
      {
        /* Execute the commands. */
        m_workspace.postCommand( changeCommand );

        /* Check, if the dialog is allowed to be completed. */
        checkDialogComplete();
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
    @Override
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
    @Override
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
            @Override
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
   * The validator for validating the entered values of the feature. May be null.
   */
  private IFeatureDialogValidator m_validator;

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
    m_validator = new PropertyFeatureDialogValidator();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( m_title );
    setTitle( m_title );

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
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar( Composite parent )
  {
    super.createButtonsForButtonBar( parent );

    /* Check, if the dialog is allowed to be completed. */
    checkDialogComplete();
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
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable( )
  {
    return true;
  }

  /**
   * This function checks, if the dialog is allowed to be completed.
   */
  protected void checkDialogComplete( )
  {
    /* Get the OK button. */
    Button okButton = getButton( IDialogConstants.OK_ID );

    /* First of all, it should be allowed to complete. */
    setErrorMessage( null );
    okButton.setEnabled( true );

    /* Without a validator, no validation is needed. */
    if( m_validator == null )
      return;

    /* Check the entered values. */
    IStatus status = m_validator.validate( m_feature );
    if( status.isOK() == true )
      return;

    /* Display the message. */
    setErrorMessage( buildErrorMessage( status, null ) );

    /* Deactivate the OK Button. */
    okButton.setEnabled( false );
  }

  /**
   * This function builds the error message.
   * 
   * @param status
   *          The status.
   * @param errorMessage
   *          The error message, so far.
   * @return The error message.
   */
  private String buildErrorMessage( IStatus status, String errorMessage )
  {
    /* Get the line separator. */
    String separator = System.getProperty( "line.separator", "\r\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    /* If it is no multi status, we end the recursion here. */
    if( status.isMultiStatus() == false )
    {
      /* If the error message is null or empty, it is enough to return the message of the status. */
      if( errorMessage == null || errorMessage.isEmpty() )
        return status.getMessage();

      /* If the error message is not null and not empty, we add a new line and then the message of the status. */
      return errorMessage + separator + status.getMessage();
    }

    /* Cast. */
    MultiStatus multiStatus = (MultiStatus) status;

    /* Get all children. */
    IStatus[] children = multiStatus.getChildren();
    for( int i = 0; i < children.length; i++ )
    {
      IStatus child = children[i];
      if( child.isOK() == false )
        errorMessage = buildErrorMessage( child, errorMessage );
    }

    return errorMessage;
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
    m_gftUrl = null;
    m_validator = null;
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
   * This function sets the validator for validating the entered values of the feature.
   * 
   * @param validator
   *          The validator for validating the entered values of the feature. May be null.
   */
  public void setValidator( IFeatureDialogValidator validator )
  {
    m_validator = validator;
  }
}