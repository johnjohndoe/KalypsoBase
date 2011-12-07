package org.kalypso.afgui.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
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

      // http___www.tu-harburg.de_wb_kalypso_kb_workflow_test__ActivateScenario

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
      return ((ITask) element).getName();

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
      return task.getTooltip();
    }

    return null;
  }
}
