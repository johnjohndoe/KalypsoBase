/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.core.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * Helper methods for {@link org.eclipse.core.runtime.IStatus}.
 *
 * @author thuel
 */
public final class StatusUtilities
{
  /**
   * A status mask representing the combination of all available stati. Usefull e.g. for
   * {@link org.eclipse.jface.dialogs.ErrorDialog#openError(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String, org.eclipse.core.runtime.IStatus, int)}
   * .
   */
  public static final int ALL_STATUS_MASK = IStatus.OK | IStatus.CANCEL | IStatus.INFO | IStatus.WARNING | IStatus.ERROR;

  private StatusUtilities( )
  {
    // wird nicht instantiiert
  }

  /**
   * Generates MultiStatus to make sure that each line of the message can be seen as a single line in an errorDialog.
   * Severety, pluginId and code remain the same for each Status in the MultiStatus.
   */
  public static MultiStatus createMultiStatusFromMessage( final int severity, final String pluginId, final int code, final String message, final String delim, final Throwable throwable )
  {
    final StringTokenizer strTok = new StringTokenizer( message == null ? "<unknown>" : message, delim );
    final Collection<IStatus> stati = new ArrayList<IStatus>( strTok.countTokens() - 1 > 0 ? strTok.countTokens() - 1 : 0 );

    String sMainMessage;
    if( strTok.hasMoreTokens() )
    {
      sMainMessage = strTok.nextToken();
      while( strTok.hasMoreTokens() )
        stati.add( new Status( severity, pluginId, code, strTok.nextToken(), null ) );
    }
    else
      sMainMessage = message;

    // Child ohne Message generieren, damit der MultiStatus auf jeden Fall über
    // die severity informiert ist
    stati.add( new Status( severity, pluginId, code, "", null ) );

    final IStatus[] childStati = stati.toArray( new IStatus[stati.size()] );
    return new MultiStatus( pluginId, code, childStati, sMainMessage, throwable );
  }

  /**
   * Returns the message of the given status.
   * <p>
   * If the status is a multi-status, it recursively creates a string with all includes child-stati, separated by
   * line-breaks.
   * </p>
   */
  public static String messageFromStatus( final IStatus status )
  {
    return createStringFromStatus( status, 0 );
  }

  /**
   * Returns the message form the given status. If the status is a multi-status, it recursively creates a string with
   * all includes child-stati, separated by line-breaks
   *
   * @param currentDepth
   *          Amout of tabs with wich the message will be indentated
   */
  private static String createStringFromStatus( final IStatus status, final int currentDepth )
  {
    final StringBuffer tabBuffer = new StringBuffer();
    for( int i = 0; i < currentDepth; i++ )
      tabBuffer.append( '\t' );

    if( !status.isMultiStatus() )
    {
      final StringBuffer statusBuffer = new StringBuffer( tabBuffer.toString() );
      statusBuffer.append( status.getMessage() );
      final Throwable exception = status.getException();
      if( exception != null )
      {
        final String localizedMessage = exception.getLocalizedMessage();
        if( localizedMessage != null )
        {
          statusBuffer.append( "\n\t" );
          statusBuffer.append( tabBuffer.toString() );
          statusBuffer.append( localizedMessage );
        }
      }

      return statusBuffer.toString();
    }

    final StringBuffer buffer = new StringBuffer( tabBuffer.toString() );
    buffer.append( status.getMessage() );
    buffer.append( '\n' );

    final IStatus[] children = status.getChildren();
    for( final IStatus element : children )
    {
      buffer.append( createStringFromStatus( element, currentDepth + 1 ) );
      buffer.append( '\n' );
    }

    return buffer.toString();
  }

  /**
   * Transforms any exception into an {@link IStatus}object.
   * <p>
   * If the exception is an {@link InvocationTargetException}the inner exception is wrapped instead.
   * </p>
   * <p>
   * If the exception is a {@link CoreException}its status is returned.
   * </p>
   *
   * @throws NullPointerException
   *           If <code>t</code> is null.
   */
  public static IStatus statusFromThrowable( final Throwable t )
  {
    return statusFromThrowable( t, null );
  }

  /**
   * Transforms any exception into an {@link IStatus}object.
   * <p>
   * If the exception is an {@link InvocationTargetException}the inner exception is wrapped instead.
   * </p>
   * <p>
   * If the exception is a {@link CoreException}its status is returned.
   * </p>
   *
   * @param message
   *          [optional] used as message for newly created status if specified
   * @throws NullPointerException
   *           If <code>t</code> is null.
   */
  public static IStatus statusFromThrowable( final Throwable t, final String message, final Object... args )
  {
    if( message != null )
    {
      final MultiStatus status = new MultiStatus( EclipseRCPContributionsPlugin.getID(), 0, String.format( message, args ), null );
      status.add( statusFromThrowable( t ) );
      return status;
    }

    if( t instanceof InvocationTargetException )
      return statusFromThrowable( ((InvocationTargetException) t).getTargetException(), message );
    if( t instanceof CoreException )
      return ((CoreException) t).getStatus();

    String msg = t.getLocalizedMessage();
    if( msg == null )
    {
      if( t.getCause() != null && t.getCause() != t )
        return statusFromThrowable( t.getCause(), t.toString() );

      // beser t.toString, weil manche Exceptions dann doch noch mehr verraten
      // z.B. ValidationException
      msg = t.toString();// "<Keine weitere Information vorhanden>";
    }

    return new Status( IStatus.ERROR, EclipseRCPContributionsPlugin.getID(), 0, msg, t );
  }

  public static String messageFromThrowable( final Throwable t )
  {
    final String msg = t.getLocalizedMessage();
    if( msg != null )
      return msg;

    // beser t.toString, weil manche Exceptions dann doch noch mehr verraten
    // z.B. ValidationException
    return t.toString();// "<Keine weitere Information vorhanden>";
  }

  /**
   * Creates a status based on the list of stati. If the list is empty, it returns the <code>Status.OK_STATUS</code>. If
   * the list contains just one status, then it is returned. If the list contains more than one status, a MultiStatus is
   * returned.
   *
   * @param message
   *          only used when creating the MultiStatus
   */
  public static IStatus createStatus( final List<IStatus> stati, final String message, final Object... args )
  {
    if( stati.size() == 0 )
      return Status.OK_STATUS;

    if( stati.size() == 1 )
      return stati.get( 0 );

    return new MultiStatus( EclipseRCPContributionsPlugin.getID(), 0, stati.toArray( new IStatus[stati.size()] ), String.format( message, args ), null );
  }

  /**
   * Creates a status based on the list of stati. If the list is empty, it returns the <code>Status.OK_STATUS</code>.
   * If the list contains just one status, then it is returned. If the list contains more than one status, a MultiStatus
   * is returned.
   * 
   * @param message
   *          only used when creating the MultiStatus
   */
  public static IStatus createStatus( final IStatus[] stati, final String message, final Object... args )
  {
    return createStatus( Arrays.asList( stati ), String.format( message, args ) );
  }

  /**
   * Creates a status with given severity, message, and throwable
   */
  public static IStatus createStatus( final int severity, final String message, final Throwable t )
  {
    return new Status( severity, EclipseRCPContributionsPlugin.getID(), -1, message, t );
  }

  /**
   * Creates a status with given severity, message, code and throwable
   */
  public static IStatus createStatus( final int severity, final int code, final String message, final Throwable t )
  {
    return new Status( severity, EclipseRCPContributionsPlugin.getID(), code, message, t );
  }

  /**
   * Creates an error-status with given message and null throwable.
   * @deprecated: Do not use! It is just too often misused... (using exception as argument does not add the exception into the status!). Use {@link Status#Status(int, String, String)} instead.
   */
  @Deprecated
  public static IStatus createErrorStatus( final String errorMessage, final Object... args )
  {
    return createStatus( IStatus.ERROR, String.format( errorMessage, args ), null );
  }

  /**
   * Creates an info-status with given message and null throwable.
   */
  public static IStatus createInfoStatus( final String infoMessage, final Object... args )
  {
    return createStatus( IStatus.INFO, String.format( infoMessage, args ), null );
  }

  /**
   * Creates a warning-status with given message and null throwable.
   */
  public static IStatus createWarningStatus( final String warningMessage, final Object... args )
  {
    return createStatus( IStatus.WARNING, String.format( warningMessage, args ), null );
  }

  /**
   * Creates a ok-status with given message and null throwable.
   */
  public static IStatus createOkStatus( final String message, final Object... args )
  {
    return createStatus( IStatus.OK, String.format( message, args ), null );
  }

  /**
   * Wraps the given status in a new status with the given severity. If the given status has already the given severity,
   * then it is simply returned.
   *
   * @param status
   *          the status to wrap
   * @param severity
   *          the desired severity
   * @param severityMask
   *          the severity-mask for which the wrapping takes place. If the given status does not match this
   *          severity-mask, no wrap takes place
   */
  public static IStatus wrapStatus( final IStatus status, final int severity, final int severityMask )
  {
    if( status.matches( severity ) || !status.matches( severityMask ) )
      return status;

    final IStatus newStatus;
    if( status.isMultiStatus() )
    {
      newStatus = new MultiStatus( status.getPlugin(), status.getCode(), ((MultiStatus) status).getChildren(), status.getMessage(), status.getException() )
      {
        @Override
        public int getSeverity( )
        {
          return severity;
        }
      };
    }
    else
    {
      newStatus = new Status( severity, status.getPlugin(), status.getCode(), status.getMessage(), status.getException() )
      {
        @Override
        public int getSeverity( )
        {
          return severity;
        }
      };
    }

    return newStatus;
  }

  /** Prints the stack trace of a status (iv available) and of all of its children. */
  public static void printStackTraces( final IStatus status )
  {
    final Throwable exception = status.getException();
    if( exception != null )
      exception.printStackTrace();

    final IStatus[] children = status.getChildren();
    for( final IStatus child : children )
      printStackTraces( child );
  }

  /**
   * Opens an error dialog on the given status.
   *
   * @param showMultipleDialogs
   *          If true, a multi-status will be shown within multiple message boxes, on e for each child of the
   *          multi-status. Else, only one dialog pops-up.
   * @return See
   *         {@link ErrorDialog#openError(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String, org.eclipse.core.runtime.IStatus)}
   */
  public static int openErrorDialog( final Shell shell, final String title, final String message, final IStatus status, final boolean showMultipleDialogs )
  {
    if( !status.isMultiStatus() || !showMultipleDialogs )
      return ErrorDialog.openError( shell, title, message, status );

    final IStatus[] children = ((MultiStatus) status).getChildren();
    for( final IStatus child : children )
    {
      final int result = ErrorDialog.openError( shell, title, message, child );
      if( result == Window.CANCEL )
        return result;
    }

    return Window.OK;
  }

  /**
   * @see #openSpecialErrorDialog(Shell, String, String, IStatus, int, boolean)
   */
  public static int openSpecialErrorDialog( final Shell shell, final String title, final String message, final IStatus status, final boolean showMultipleDialogs )
  {
    final int displayMask = IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR;
    return openSpecialErrorDialog( shell, title, message, status, showMultipleDialogs, displayMask );
  }

  /**
   * @see #openSpecialErrorDialog(Shell, String, String, IStatus, int, boolean)
   */
  public static int openSpecialErrorDialog( final Shell shell, final String title, final String message, final IStatus status, final boolean showMultipleDialogs, int displayMask )
  {
    return openSpecialErrorDialog( shell, title, message, status, displayMask, showMultipleDialogs );
  }

  /**
   * Opens an error dialog on the given status. Tweaks the error message.
   *
   * @param showMultipleDialogs
   *          If true, a multi-status will be shown within multiple message boxes, on e for each child of the
   *          multi-status. Else, only one dialog pops-up.
   * @return See
   *         {@link ErrorDialog#openError(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String, org.eclipse.core.runtime.IStatus, int)}
   */
  public static int openSpecialErrorDialog( final Shell shell, final String title, final String message, final IStatus status, final int displayMask, final boolean showMultipleDialogs )
  {
    if( !status.isMultiStatus() || !showMultipleDialogs )
    {
      final StringBuffer msg = new StringBuffer();
      switch( status.getSeverity() )
      {
        case IStatus.ERROR:
          msg.append( "Fehler" );
          break;
        case IStatus.WARNING:
          msg.append( "Warnung(en)" );
          break;
        case IStatus.INFO:
          msg.append( "Information(en)" );
          break;
      }

      msg.append( " bei der Bearbeitung von: \n" );
      msg.append( message );

      return ErrorDialog.openError( shell, title, msg.toString(), status, displayMask );
    }

    final IStatus[] children = ((MultiStatus) status).getChildren();
    for( final IStatus child : children )
    {
      final String msg;
      if( child instanceof DialogMultiStatus )
        msg = ((DialogMultiStatus) child).getDialogMessage();
      else
        msg = message;

      final int result = openSpecialErrorDialog( shell, title, msg, child, false, displayMask );
      if( result == Window.CANCEL )
        return result;
    }

    return Window.OK;
  }

  /**
   * Returns an (internationalized) string corresponding to the severity of the given status.
   * <p>
   * TODO: internationalize it
   */
  public static String getLocalizedSeverity( final IStatus status )
  {
    switch( status.getSeverity() )
    {
      case IStatus.OK:
        return "OK";
      case IStatus.INFO:
        return "INFO";
      case IStatus.WARNING:
        return "WARNUNG";
      case IStatus.ERROR:
        return "FEHLER";
      case IStatus.CANCEL:
        return "ABBRUCH";
      default:
        return "UNBEKANNT";
    }
  }

  /**
   * Creates a copy of the given status, changeing its severity to the given one.<br>
   * As the severity of a {@link MultiStatus} is defined by the severity of its children, the severities of all children
   * of the cloned status are set to the given status.
   *
   * @param One
   *          of {@link IStatus#OK}, ...
   */
  public static IStatus cloneStatus( final IStatus status, final int severity )
  {
    if( status.isMultiStatus() )
    {
      final IStatus[] children = status.getChildren();
      final IStatus[] newChildren = new IStatus[children.length];

      for( int i = 0; i < children.length; i++ )
        newChildren[i] = cloneStatus( children[i], severity );

      return new MultiStatus( status.getPlugin(), status.getCode(), newChildren, status.getMessage(), status.getException() );
    }

    return new Status( severity, status.getPlugin(), status.getCode(), status.getMessage(), status.getException() );
  }

  public static boolean equals( final IStatus status1, final IStatus status2 )
  {
    if( status1 == null || status2 == null )
      return status1 == null && status2 == null;

    final int severity1 = status1.getSeverity();
    final int severity2 = status1.getSeverity();
    if( severity1 != severity2 )
      return false;

    final int code1 = status1.getCode();
    final int code2 = status1.getCode();
    if( code1 != code2 )
      return false;

    final String message1 = status1.getMessage();
    final String message2 = status2.getMessage();
    if( !message1.equals( message2 ) )
      return false;

    final String plugin1 = status1.getPlugin();
    final String plugin2 = status2.getPlugin();
    if( !plugin1.equals( plugin2 ) )
      return false;

    final Throwable exception1 = status1.getException();
    final Throwable exception2 = status2.getException();
    if( exception1 != exception2 )
      return false;

    final IStatus[] children1 = status1.getChildren();
    final IStatus[] children2 = status2.getChildren();
    return Arrays.equals( children1, children2 );
  }

}