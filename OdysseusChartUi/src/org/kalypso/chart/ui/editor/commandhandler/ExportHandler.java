package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.chart.ui.editor.ui.SafeSaveDialog;
import org.kalypso.chart.ui.i18n.Messages;

import de.openali.odysseus.chart.framework.util.img.ChartPainter;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Alexander Burtscher
 * @author Holger Albert (streamlined it a bit)
 */
public class ExportHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );

    final IChartComposite chartComposite = ChartHandlerUtilities.getChart( context );
    if( chartComposite == null )
    {
      final MessageDialog ed = new MessageDialog( shell, Messages.getString( "org.kalypso.chart.ui.editor.commandhandler.ExportHandler.2" ), null, Messages.getString( "org.kalypso.chart.ui.editor.commandhandler.ExportHandler.3" ), MessageDialog.NONE, new String[] { "OK" }, 1 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      ed.open();
      return null;
    }

    final SafeSaveDialog dia = new SafeSaveDialog( shell );
    dia.setFilterExtensions( new String[] { "*.png", "*.jpg", "*.bmp" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final String filename = dia.open();
    if( filename == null || filename.length() == 0 )
      return null;

    final Rectangle bounds = ((Composite) chartComposite).getBounds();
    final ImageData id = ChartPainter.createChartImageData( chartComposite.getChartModel(), bounds, new NullProgressMonitor() );

    final ImageLoader il = new ImageLoader();
    il.data = new ImageData[] { id };

    int format = -1;
    final String formatString = filename.substring( filename.lastIndexOf( "." ) + 1 ).toLowerCase(); //$NON-NLS-1$

    if( formatString.equals( "png" ) ) //$NON-NLS-1$
      format = SWT.IMAGE_PNG;
    else if( formatString.equals( "bmp" ) ) //$NON-NLS-1$
      format = SWT.IMAGE_BMP;
    else if( formatString.equals( "jpg" ) ) //$NON-NLS-1$
      format = SWT.IMAGE_JPEG;

    if( format == -1 )
    {
      final MessageDialog ed = new MessageDialog( shell, Messages.getString( "org.kalypso.chart.ui.editor.commandhandler.ExportHandler.0" ), null, Messages.getString( "org.kalypso.chart.ui.editor.commandhandler.ExportHandler.1" ), MessageDialog.NONE, new String[] { "OK" }, 1 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      ed.open();
      return null;
    }

    /* Save. */
    il.save( filename, format );

    return null;
  }
}