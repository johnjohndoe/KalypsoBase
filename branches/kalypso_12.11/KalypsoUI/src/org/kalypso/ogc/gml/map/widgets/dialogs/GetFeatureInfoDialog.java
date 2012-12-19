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
package org.kalypso.ogc.gml.map.widgets.dialogs;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * The get feature info dialog.
 * 
 * @author Holger Albert
 */
public class GetFeatureInfoDialog extends PopupDialog
{
  /**
   * The job change listener.
   */
  private final IJobChangeListener m_listener = new JobChangeAdapter()
  {
    @Override
    public void done( final IJobChangeEvent event )
    {
      handleJobDone( event );
    }
  };

  /**
   * The WMS theme.
   */
  private final KalypsoWMSTheme m_wmsTheme;

  /**
   * The x coordinate.
   */
  private final double m_x;

  /**
   * The y coordinate.
   */
  private final double m_y;

  /**
   * The browser.
   */
  private Browser m_browser;

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The parent shell.
   * @param wmsTheme
   *          The WMS theme.
   * @param x
   *          The x coordinate.
   * @param y
   *          The y coordinate.
   */
  public GetFeatureInfoDialog( final Shell parentShell, final KalypsoWMSTheme wmsTheme, final double x, final double y )
  {
    super( parentShell, SWT.RESIZE, true, true, true, false, false, Messages.getString( "GetFeatureInfoDialog_0" ), null ); //$NON-NLS-1$

    m_wmsTheme = wmsTheme;
    m_x = x;
    m_y = y;
    m_browser = null;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Create the main composite. */
    final Composite main = (Composite)super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Set the title text. */
    setTitleText( Messages.getString( "GetFeatureInfoDialog.0" ) ); //$NON-NLS-1$

    /* Create the browser. */
    m_browser = new Browser( main, SWT.BORDER );
    m_browser.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Initialize. */
    initialize();

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.PopupDialog#getDialogSettings()
   */
  @Override
  protected IDialogSettings getDialogSettings( )
  {
    return DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getCanonicalName() );
  }

  /**
   * This function initalizes the dialog.
   */
  private void initialize( )
  {
    /* If there was no WMS theme provided show only a notice. */
    if( m_wmsTheme == null )
    {
      setTitleText( Messages.getString( "GetFeatureInfoDialog_1" ) ); //$NON-NLS-1$
      return;
    }

    /* Create the job. */
    final GetFeatureInfoJob job = new GetFeatureInfoJob( m_wmsTheme, m_x, m_y );
    job.setSystem( true );
    job.setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, true );
    job.setProperty( IProgressConstants.KEEP_PROPERTY, false );
    job.addJobChangeListener( m_listener );

    /* Schedule it. */
    job.schedule();
  }

  protected void handleJobDone( final IJobChangeEvent event )
  {
    if( m_browser == null || m_browser.isDisposed() )
      return;

    final Display display = m_browser.getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        handleJobDoneInternal( event );
      }
    } );
  }

  protected void handleJobDoneInternal( final IJobChangeEvent event )
  {
    setTitleText( Messages.getString( "GetFeatureInfoDialog.1" ) ); //$NON-NLS-1$

    /* Get the result. */
    final IStatus result = event.getResult();
    if( !result.isOK() )
    {
      setErrorText( result.getMessage() );
      return;
    }

    /* Get the job. */
    final Job job = event.getJob();
    if( !(job instanceof GetFeatureInfoJob) )
      return;

    /* Cast. */
    final GetFeatureInfoJob task = (GetFeatureInfoJob)job;

    /* Get the feature info. */
    final String featureInfo = task.getFeatureInfo();
    if( featureInfo == null )
    {
      setErrorText( Messages.getString( "GetFeatureInfoDialog.2" ) ); //$NON-NLS-1$
      return;
    }

    /* Set the response text into the browser. */
    setHtmlText( featureInfo );
  }

  private void setErrorText( final String text )
  {
    if( m_browser == null || m_browser.isDisposed() )
      return;

    // TODO Make nice html...
    m_browser.setText( StringEscapeUtils.escapeHtml4( text ) );
  }

  private void setHtmlText( final String text )
  {
    if( m_browser == null || m_browser.isDisposed() )
      return;

    m_browser.setText( text );
  }
}