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
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ui.SafeSaveDialog;

import de.openali.odysseus.chart.framework.util.img.ChartImageFactory;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

public class ExportHandler extends AbstractHandler
{
  private String m_filename;

  private final int count = 0;

  /**
   * saves an image of the current chart part to a file; if no filename has been set by setFilename(), a file dialog
   * will open; otherwise no dialog will appear; at the end of the function, the member variable holding the filename
   * will be reset; so if the filename is
   * 
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {

    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );

    if( chartPart == null )
    {
      return null;
    }
    final ChartComposite chart = chartPart.getChartComposite();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );


    if( chart != null )
    {
      // Damit der Dateiname auch von aussen gesetzt werden kann:
      if( m_filename == null )
      {
        final SafeSaveDialog dia = new SafeSaveDialog( shell );

        dia.setFilterExtensions( new String[] { "*.png", "*.jpg", "*.bmp" } );

        m_filename = dia.open();
      }
      if( m_filename != null )
      {

        final Rectangle bounds = chart.getBounds();
        final ImageLoader il = new ImageLoader();
        final ImageData id = ChartImageFactory.createChartImage( chart, Display.getCurrent(), bounds.width, bounds.height );
        il.data = new ImageData[] { id };

        int format = -1;
        final String formatString = m_filename.substring( m_filename.lastIndexOf( "." ) + 1 ).toLowerCase();

        if( formatString.equals( "png" ) )
          format = SWT.IMAGE_PNG;
        else if( formatString.equals( "bmp" ) )
          format = SWT.IMAGE_BMP;
        else if( formatString.equals( "jpg" ) )
          format = SWT.IMAGE_JPEG;

        if( format != -1 )
          il.save( m_filename, format );
        else
        {
          final MessageDialog ed = new MessageDialog( shell, "Datei konnte nicht gespeichert werden.", null, "Das angegebene Format wird nicht unterstützt", MessageDialog.NONE, new String[] { "OK" }, 1 );
          ed.open();
        }
      }

    }
    else
    {
      final MessageDialog ed = new MessageDialog( shell, "Kein Chart vorhangen", null, "Das Chart kann nicht gespeichert werden.", MessageDialog.NONE, new String[] { "OK" }, 1 );
      ed.open();
    }
    m_filename = null;
    return null;
  }

  /**
   * this function can be used by IExecutionListener to set a filename from outside; BEWARE: the filename is reset after
   * the image has been saved!!!!!!!!!!!!
   */
  public void setFilename( String filename )
  {
    m_filename = filename;

  }

}
