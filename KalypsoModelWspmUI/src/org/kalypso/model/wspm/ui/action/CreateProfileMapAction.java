package org.kalypso.model.wspm.ui.action;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.resources.StringStorage;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.ui.editor.mapeditor.GisMapEditor;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureRelation;

public class CreateProfileMapAction extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked( event );
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    /* retrieve selected profile-collections, abort if none */
    final Map<Feature, IRelationType> selectedFeatures = new HashMap<>();
    for( final Object selectedObject : selection.toList() )
    {
      if( selectedObject instanceof IFeatureRelation )
      {
        final IFeatureRelation fate = (IFeatureRelation) selectedObject;
        final Feature parentFeature = fate.getOwner();

        selectedFeatures.put( parentFeature, fate.getPropertyType() );
      }
    }

    if( selectedFeatures.size() == 0 )
    {
      MessageDialog.openWarning( shell, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.CreateProfileMapAction.0" ), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.CreateProfileMapAction.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    final IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
    createAndOpenMap( activePart, selectedFeatures );

    return null;
  }

  public static void createAndOpenMap( final IWorkbenchPart activePart, final Map<Feature, IRelationType> selectedProfiles )
  {
    final UIJob uijob = new UIJob( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.CreateProfileMapAction.2" ) ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        try
        {
          final String title = guessTitle( selectedProfiles.keySet() );
          final String mapTemplate = createMapTemplate( selectedProfiles, title );
          if( mapTemplate == null )
            return Status.OK_STATUS;

          final String storageName = title == null ? "<unbekannt>.gmt" : title + ".gmt"; //$NON-NLS-1$ //$NON-NLS-2$
          final IPath storagePath = guessPath( activePart, storageName, selectedProfiles );

          final IWorkbenchPartSite activeSite = activePart.getSite();
          final IWorkbenchPage page = activeSite.getPage();
          final IWorkbench workbench = activeSite.getWorkbenchWindow().getWorkbench();

          final IEditorRegistry editorRegistry = workbench.getEditorRegistry();
          final IEditorDescriptor editorDescription = editorRegistry.findEditor( GisMapEditor.ID );

          final IEditorInput input = new StorageEditorInput( new StringStorage( mapTemplate, storagePath ) );

          page.openEditor( input, editorDescription.getId(), true );
        }
        catch( final CoreException e )
        {
          final IStatus status = e.getStatus();
          KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
          return status;
        }

        return Status.OK_STATUS;
      }
    };
    uijob.setUser( true );
    uijob.schedule();
  }

  static IPath guessPath( final IWorkbenchPart part, final String storageName, final Map<Feature, IRelationType> selectedProfiles ) throws CoreException
  {
    if( part instanceof IEditorPart )
    {
      final IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
      if( editorInput instanceof IStorageEditorInput )
      {
        final IStorage storage = ((IStorageEditorInput) editorInput).getStorage();
        final IPath fullPath = storage.getFullPath();
        if( fullPath != null )
        {
          final IPath parentPath = fullPath.removeLastSegments( 1 );
          return parentPath.append( storageName );
        }
      }
    }

    final Set<Entry<Feature, IRelationType>> entrySet = selectedProfiles.entrySet();
    for( final Entry<Feature, IRelationType> entry : entrySet )
    {
      final Feature feature = entry.getKey();
      final GMLWorkspace workspace = feature.getWorkspace();
      if( workspace != null )
      {
        final URL context = workspace.getContext();
        if( context != null )
        {

        }
      }
    }

    return null;
  }

  static String guessTitle( final Set<Feature> keySet )
  {
    if( keySet.isEmpty() )
      return null;

    final Feature firstFeature = keySet.iterator().next();
    return firstFeature.getName();
  }

  static String createMapTemplate( final Map<Feature, IRelationType> selectedProfiles, final String title ) throws CoreException
  {
    try
    {
      final Gismapview gismapview = GisTemplateHelper.createGisMapView( selectedProfiles, true );
      if( title != null )
      {
        gismapview.setName( title );
      }
      final StringWriter stringWriter = new StringWriter();
      GisTemplateHelper.saveGisMapView( gismapview, stringWriter, "UTF8" ); //$NON-NLS-1$
      stringWriter.close();

      return stringWriter.toString();
    }
    catch( final JAXBException e )
    {
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.CreateProfileMapAction.4" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    catch( final IOException e )
    {
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, e.getLocalizedMessage(), e );
      throw new CoreException( status );
    }
  }
}