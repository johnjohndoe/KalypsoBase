package org.kalypso.kml.export;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.kalypso.kml.export.constants.IKMLExportSettings;
import org.kalypso.kml.export.i18n.Messages;
import org.kalypso.kml.export.wizard.WizardGoogleExport;
import org.kalypso.ui.views.map.MapView;

public class KMLExportCommandHandler implements IHandler
{

  @Override
  public void addHandlerListener( final IHandlerListener handlerListener )
  {
  }

  @Override
  public void dispose( )
  {
  }

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final File targetFile = (File) context.getVariable( IKMLExportSettings.CONST_TARGET_FILE );

    /* get mapView instance */
    // FIXME: get MapPanel from MapHandlerUtils and only give the panel to the exporter
    MapView mapView = null;

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchPage[] pages = workbench.getActiveWorkbenchWindow().getPages();
    for( final IWorkbenchPage page : pages )
    {
      final IViewReference[] references = page.getViewReferences();
      for( final IViewReference reference : references )
      {
        // get viewpart an check if it is instanceof mapview (mapView id's can differ
        final IWorkbenchPart part = reference.getPart( false );
        if( part instanceof MapView )
        {
          mapView = (MapView) part;
          break;
        }
      }
    }

    if( mapView == null )
      throw new IllegalStateException( Messages.GoogleEarthExportCommandHandler_0 );

    /* call google earth export wizard */
    final WizardGoogleExport wizard = new WizardGoogleExport( mapView, targetFile );
    wizard.init( workbench, null );

    final WizardDialog dialog = new WizardDialog( workbench.getDisplay().getActiveShell(), wizard );
    dialog.open();

    /* return settings */
    return wizard.getExportedSettings();
  }

  @Override
  public boolean isEnabled( )
  {
    return true;
  }

  @Override
  public boolean isHandled( )
  {
    return true;
  }

  @Override
  public void removeHandlerListener( final IHandlerListener handlerListener )
  {
  }
}