package org.kalypso.afgui.internal.ui.workflow;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.ITaskGroup;

/**
 * @author Stefan Kurzbach
 */
public class WorkflowLabelProvider extends ColumnLabelProvider
{
  private final static String IMAGE_TASK = "taskDefault"; //$NON-NLS-1$

  private final ImageRegistry m_imageRegistry = new ImageRegistry();

  private final Font m_fontTask;

  private final Font m_fontActiveTask;

  private final WorkflowControl m_workflowControl;

  public WorkflowLabelProvider( final WorkflowControl workflowControl )
  {
    m_workflowControl = workflowControl;

    final ImageDescriptor taskImage = KalypsoAFGUIFrameworkPlugin.getImageDescriptor( "icons/nuvola_select/task.png" ); //$NON-NLS-1$

    m_imageRegistry.put( IMAGE_TASK, taskImage );

    m_fontTask = JFaceResources.getDialogFont();
    m_fontActiveTask = JFaceResources.getBannerFont();
  }

  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof ITask )
    {
      final ITask task = (ITask) element;
      final String uri = task.getURI();

      final Image image = m_imageRegistry.get( uri );
      if( image != null )
        return image;

      final ICommandImageService cis = (ICommandImageService) PlatformUI.getWorkbench().getService( ICommandImageService.class );
      final ImageDescriptor imageDescriptor = cis.getImageDescriptor( uri );
      if( imageDescriptor == null )
        return m_imageRegistry.get( IMAGE_TASK );

      m_imageRegistry.put( uri, imageDescriptor );

      return m_imageRegistry.get( uri );
    }

    return null;
  }

  @Override
  public String getText( final Object element )
  {
    if( element instanceof ITask )
    {
      final String taskName = ((ITask) element).getName();
      if( !StringUtils.isBlank( taskName ) )
        return taskName;

      try
      {
        final Command command = findCommand( (ITask) element );
        if( command != null && command.isDefined() )
          return command.getName();
      }
      catch( final NotDefinedException e )
      {
        // will not happen, we just checked
      }

      return taskName;
    }

    return null;
  }

  /* tries to find a command for the given taks. Only defined commands get returned. */
  private Command findCommand( final ITask element )
  {
    final String commandID = element.getURI();
    final ICommandService cs = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
    final Command command = cs.getCommand( commandID );
    if( command.isDefined() )
      return command;

    return null;
  }

  @Override
  public void dispose( )
  {
    m_imageRegistry.dispose();

    super.dispose();
  }

  @Override
  public Font getFont( final Object element )
  {
    if( element instanceof ITaskGroup )
    {
      if( m_workflowControl.getTaskExecutor().getActiveTask() == element )
        return m_fontActiveTask;
      else
        return m_fontTask;
    }
    else
    {
      if( m_workflowControl.getTaskExecutor().getActiveTask() == element )
        return m_fontActiveTask;
      else
        return m_fontTask;
    }
  }

  @Override
  public String getToolTipText( final Object element )
  {
    if( element instanceof ITask )
    {
      final ITask task = (ITask) element;
      final String tooltip = task.getTooltip();
      if( StringUtils.isBlank( tooltip ) )
      {
        /* Try to get description of command instead */
        try
        {
          final Command command = findCommand( (ITask) element );
          if( command != null && command.isDefined() )
            return command.getDescription();
        }
        catch( final NotDefinedException e )
        {
          // will not happen, we just checked
        }
      }

      return tooltip;
    }

    return null;
  }
}
