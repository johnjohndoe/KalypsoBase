package org.kalypso.ogc.gml.map.handlers.listener;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.map.handlers.MapScreenShotHandler;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.preferences.KalypsoScreenshotPreferencePage;

/**
 * This is a listener which is notified, when the screenshot command changes its state.
 * 
 * @author Holger Albert
 */
public final class MapScreenshotExecuteListener implements IExecutionListener
{
  /**
   * The constructor.
   */
  public MapScreenshotExecuteListener( )
  {
  }

  /**
   * @see org.eclipse.core.commands.IExecutionListener#notHandled(java.lang.String,
   *      org.eclipse.core.commands.NotHandledException)
   */
  public void notHandled( final String commandId, final NotHandledException exception )
  {
    if( "org.kalypso.ogc.gml.map.Screenshot".equals( commandId ) == false ) //$NON-NLS-1$
      return;
  }

  /**
   * @see org.eclipse.core.commands.IExecutionListener#postExecuteFailure(java.lang.String,
   *      org.eclipse.core.commands.ExecutionException)
   */
  public void postExecuteFailure( final String commandId, final ExecutionException exception )
  {
    if( "org.kalypso.ogc.gml.map.Screenshot".equals( commandId ) == false ) //$NON-NLS-1$
      return;

    /* Log the error message. */
    KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( exception ) );

    /* Show an error dialog. */
    ErrorDialog.openError( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.2"), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.3"), StatusUtilities.statusFromThrowable( exception ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
   */
  public void postExecuteSuccess( final String commandId, final Object returnValue )
  {
    if( "org.kalypso.ogc.gml.map.Screenshot".equals( commandId ) == false ) //$NON-NLS-1$
      return;

    if( !(returnValue instanceof File) )
      return;

    /* Cast to file. */
    final File file = (File) returnValue;

    /* Show the user a success dialog. */
    MessageDialog.openInformation( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.5"), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.6") + file.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.eclipse.core.commands.IExecutionListener#preExecute(java.lang.String,
   *      org.eclipse.core.commands.ExecutionEvent)
   */
  public void preExecute( final String commandId, final ExecutionEvent event )
  {
    if( "org.kalypso.ogc.gml.map.Screenshot".equals( commandId ) == false ) //$NON-NLS-1$
      return;

    final IPreferenceStore preferences = KalypsoGisPlugin.getDefault().getPreferenceStore();
    final String extension = preferences.getString( KalypsoScreenshotPreferencePage.KEY_SCREENSHOT_FORMAT );
    final String dir = preferences.getString( KalypsoScreenshotPreferencePage.KEY_SCREENSHOT_TARGET );

    File file = null;
    if( dir != null && !dir.equals( "" ) ) //$NON-NLS-1$
      file = new File( dir );

    /* Create the file dialog. */
    final FileDialog dialog = new FileDialog( PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.NONE );

    /* Set some dialog information. */
    dialog.setText( Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.9") ); //$NON-NLS-1$

    /* Set the initialize path, if available. */
    if( file != null )
      dialog.setFilterPath( file.getAbsolutePath() );

    /* Initialize with some settings. */
    dialog.setFilterExtensions( new String[] { "*." + extension } ); //$NON-NLS-1$
    dialog.setFilterNames( new String[] { extension.toUpperCase() + " - Images" } ); //$NON-NLS-1$

    /* Show the dialog. */
    final String result = dialog.open();

    /* If the user has canceled the dialog, do not execute the command. */
    if( result == null )
    {
      stopIt( event );
      return;
    }

    /* Create the target file. */
    File target = new File( result );

    /* Add extension. */
    if( !target.getName().endsWith( "." + extension ) ) //$NON-NLS-1$
      target = new File( target.getParentFile(), target.getName() + "." + extension ); //$NON-NLS-1$

    /* If the target exists already, give a warning and do only execute, if the user has confirmed it. */
    if( target.exists() )
    {
      /* Ask the user. */
      boolean confirmed = MessageDialog.openConfirm( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.14"), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.15") ); //$NON-NLS-1$ //$NON-NLS-2$

      /* If he has not confirmed, do not execute the command. */
      if( !confirmed )
      {
        stopIt( event );
        return;
      }
    }

    try
    {
      /* Create the empty file. */
      target.createNewFile();
    }
    catch( final IOException e )
    {
      /* Show an error dialog. */
      ErrorDialog.openError( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.16"), Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.17") + target.getName() + Messages.getString("org.kalypso.ogc.gml.map.handlers.listener.MapScreenshotExecuteListener.18"), StatusUtilities.statusFromThrowable( e ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      /* Stop executing ... */
      stopIt( event );

      /* ... and leave. */
      return;
    }

    /* If everything is okay, set the target. */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    context.addVariable( MapScreenShotHandler.CONST_TARGET_FILE, target );
  }

  /**
   * This function stops the execution of the command.
   */
  private void stopIt( final ExecutionEvent event )
  {
    /* Say the command handler, he shoud not execute the command. */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    context.addVariable( MapScreenShotHandler.CONST_SHOULD_EXECUTE_BOOLEAN, Boolean.FALSE );
  }
}