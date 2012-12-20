/**
 * 
 */
package de.renew.workflow.contexts;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import de.renew.workflow.internal.i18n.Messages;

/**
 * Opens the map view on a given resource and activates a given layer
 * 
 * @author Stefan Kurzbach
 */
public class ViewContextHandler extends AbstractHandler implements IExecutableExtension
{
  public static final String CONTEXT_VIEW_ID = "org.kalypso.afgui.contexts.view"; //$NON-NLS-1$

  private String m_viewIDprimary;

  private String m_viewIDsecondary;

  public ViewContextHandler( )
  {
  }

  public ViewContextHandler( final String viewId )
  {
    initViewId( viewId );
  }

  private void initViewId( final String viewId )
  {
    /* reset */
    m_viewIDprimary = null;
    m_viewIDsecondary = null;

    if( StringUtils.isBlank( viewId ) )
      return;

    final String[] split = StringUtils.split( viewId, ":", 2 ); //$NON-NLS-1$
    m_viewIDprimary = split[0];

    if( split.length > 1 )
      m_viewIDsecondary = split[1];
  }

  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IWorkbenchWindow activeWorkbenchWindow = (IWorkbenchWindow) context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    if( activeWorkbenchWindow == null || m_viewIDprimary == null )
      return Status.CANCEL_STATUS;

    final IWorkbenchPage workbenchPage = activeWorkbenchWindow.getActivePage();
    try
    {
      /* final IViewPart view = */
      workbenchPage.showView( m_viewIDprimary, m_viewIDsecondary, IWorkbenchPage.VIEW_CREATE );
      return Status.OK_STATUS;
    }
    catch( final PartInitException e )
    {
      throw new ExecutionException( Messages.getString( "ViewContextHandler.0", m_viewIDprimary, m_viewIDsecondary ), e ); //$NON-NLS-1$
    }
  }

  @Override
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    if( data instanceof Map )
    {
      final Map< ? , ? > parameterMap = (Map< ? , ? >) data;
      final String viewId = (String) parameterMap.get( CONTEXT_VIEW_ID );
      initViewId( viewId );
    }
  }
}
