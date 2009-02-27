package org.kalypso.chart.ui.editor.commandhandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.ChartImageFactory;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ui.SafeSaveDialog;

public class ExportHandler extends AbstractHandler
{

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );

    if( chartPart == null )
      return null;

    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );

    final SafeSaveDialog dia = new SafeSaveDialog( shell );

    dia.setFilterExtensions( new String[] { "*.png", "*.jpg", "*.bmp" } );

    final String filename = dia.open();
    if( filename != null )
    {
      final ChartComposite chart = chartPart.getChartComposite();

      final Rectangle bounds = chart.getBounds();
      final ImageLoader il = new ImageLoader();
      final ImageData id = ChartImageFactory.createChartImage( chart, Display.getCurrent(), bounds.width, bounds.height );
      il.data = new ImageData[] { id };
      int format = -1;
      final String formatString = filename.substring( filename.lastIndexOf( "." ) + 1 ).toLowerCase();

      if( formatString.equals( "png" ) )
        format = SWT.IMAGE_PNG;
      else if( formatString.equals( "bmp" ) )
        format = SWT.IMAGE_BMP;
      else if( formatString.equals( "jpg" ) )
        format = SWT.IMAGE_JPEG;

      if( format != -1 )
        il.save( filename, format );
      else
      {
        final MessageDialog ed = new MessageDialog( shell, "Datei konnte nicht gespeichert werden.", null, "Das angegebene Format wird nicht unterstützt", MessageDialog.NONE, new String[] { "OK" }, 1 );
        ed.open();
      }
    }
    return null;
  }
}
