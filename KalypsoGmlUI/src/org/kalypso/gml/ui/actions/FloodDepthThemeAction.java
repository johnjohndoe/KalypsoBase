package org.kalypso.gml.ui.actions;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.WizardDialog2;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.wizard.flooddepth.FloodDepthWizard;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ui.action.AddThemeCommand;

public class FloodDepthThemeAction implements IObjectActionDelegate
{
  private ISelection m_selection;

  private IWorkbenchPart m_targetPart;

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart( final IAction action, final IWorkbenchPart targetPart )
  {
    m_targetPart = targetPart;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    /* retrieve selected theme, abort if none */
    IKalypsoFeatureTheme theme = null;
    if( m_selection instanceof IStructuredSelection )
    {
      for( final Object selectedObject : ((IStructuredSelection) m_selection).toList() )
      {
        if( selectedObject instanceof IKalypsoFeatureTheme )
          theme = (IKalypsoFeatureTheme) selectedObject;
      }
    }

    /* Check precondition */
    final Shell shell = m_targetPart == null ? null : m_targetPart.getSite().getShell();

    if( theme == null )
    {
      MessageDialog.openWarning( shell, "Fliesstiefen ermitteln", "Es wurden keine Dreiecke (Polygongeometrien mit 3 Punkten) in der Selektion gefunden." );
      return;
    }

    final URL meshUrl = theme.getWorkspace().getContext();

    final FloodDepthWizard intersectWizard = new FloodDepthWizard( meshUrl );

    /* show intersection wizard */
    final WizardDialog2 dialog = new WizardDialog2( shell, intersectWizard );
    dialog.setRememberSize( true );
    if( !(dialog.open() == Window.OK) )
      return;

    final IFile shapeFile = intersectWizard.getShapeFile();
    // add shape layer to map
    final IMapModell mapModell = theme.getMapModell();
    if( mapModell instanceof GisTemplateMapModell )
    {
      try
      {
        final URL url = ResourceUtilities.createURL( shapeFile );
        final AddThemeCommand command = new AddThemeCommand( (GisTemplateMapModell) mapModell, shapeFile.getName(), "shape", "featureMember", url.toExternalForm() );

        final ICommandTarget target = findCommandTarget();
        if( target != null )
          target.postCommand( command, null );
      }
      catch( final Exception e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoGmlUIPlugin.getDefault().getLog().log( status );
        ErrorDialog.openError( shell, "Fliesstiefen ermitteln", "Ergebis konnte nicht in Karte aufgenommen werden", status );
      }
    }
  }

  private ICommandTarget findCommandTarget( )
  {
    // REMARK: the first two lookups should work but they dont, because
    // the active part is allways the ResourceNavigator
    final ICommandTarget target = (ICommandTarget) m_targetPart.getAdapter( ICommandTarget.class );
    if( target != null )
      return target;

    final IContributedContentsView contribView = (IContributedContentsView) m_targetPart.getAdapter( IContributedContentsView.class );
    if( contribView != null )
      return (ICommandTarget) contribView.getContributingPart().getAdapter( ICommandTarget.class );

    final IWorkbenchPage page = m_targetPart.getSite().getPage();
    final IEditorPart activeEditor = page.getActiveEditor();
    if( activeEditor != null )
      return (ICommandTarget) activeEditor.getAdapter( ICommandTarget.class );
    
    return null;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
    m_selection = selection;
  }

}
