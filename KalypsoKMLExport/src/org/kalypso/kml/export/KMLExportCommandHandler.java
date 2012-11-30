package org.kalypso.kml.export;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.kml.export.constants.IKMLExportSettings;
import org.kalypso.kml.export.wizard.WizardGoogleExport;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;

public class KMLExportCommandHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    // ??
    final File targetFile = (File) context.getVariable( IKMLExportSettings.CONST_TARGET_FILE );

    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    /* get mapView instance */
    final IMapPanel mapPanel = MapHandlerUtils.getMapPanelChecked( context );

    /* call google earth export wizard */
    final WizardGoogleExport wizard = new WizardGoogleExport( mapPanel, targetFile );

    final WizardDialog dialog = new WizardDialog( shell, wizard );
    dialog.open();

    /* return settings */
    return wizard.getExportedSettings();
  }
}