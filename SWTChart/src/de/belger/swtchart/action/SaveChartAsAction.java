package de.belger.swtchart.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.belger.swtchart.ChartCanvas;

/**
 * @author gernot
 */
public class SaveChartAsAction extends Action
{
  private final static String[] FILTER_EXT = new String[]
  { "*.bmp", "*.ico", "*.jpg", "*.gif", "*.png", "*.tif" };

  private final static String[] FILTER_NAMES = new String[]
  { "Windows Bitmap", "Windows Icon", "JPEG", "GIF", "PNG", "TIFF" };

  private final static Map<String, Integer> EXT_MAP = new HashMap<String, Integer>();
  static
  {
    EXT_MAP.put( "bmp", SWT.IMAGE_BMP );
    EXT_MAP.put( "ico", SWT.IMAGE_ICO );
    EXT_MAP.put( "jpg", SWT.IMAGE_JPEG );
    EXT_MAP.put( "gif", SWT.IMAGE_GIF );
    EXT_MAP.put( "png", SWT.IMAGE_PNG );
    EXT_MAP.put( "tif", SWT.IMAGE_TIFF );
  }

  private final ChartCanvas m_chart;

  public SaveChartAsAction( final ChartCanvas chart )
  {
    super( "Export image...", AS_PUSH_BUTTON );

    m_chart = chart;

    setToolTipText( "Exports the chart as an image." );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void run()
  {
    final Display display = m_chart.getDisplay();
    final Shell activeShell = display.getActiveShell();
    final FileDialog dialog = new FileDialog( activeShell, SWT.SAVE );
    dialog.setFilterExtensions( FILTER_EXT );
    dialog.setFilterNames( FILTER_NAMES );
    dialog.setText( "Export chart" );

    final String path = dialog.open();
    if( path == null )
      return;

    int format = -1;
    for( final Map.Entry<String, Integer> entry : EXT_MAP.entrySet() )
    {
      if( path.endsWith( entry.getKey() ) )
        format = entry.getValue();
    }

    // save image as file
    // TODO: show nice dialog to select widht/height
    final int width = 1000;
    final int height = 1000;
    final Image img = m_chart.paintImage( display, new Rectangle( 0, 0, width, height ) );

    final ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[]
    { img.getImageData() };
    loader.logicalScreenWidth = width;
    loader.logicalScreenHeight = height;

    int mboxstyle = SWT.OK | SWT.ICON_INFORMATION;
    String mboxmsg = "Image succesfully exported to file:\n" + path;
    try
    {
      if( format != -1 )
        // TODO: save with progress bar in another thread
        loader.save( path, format );
      else
      {
        mboxstyle = SWT.OK | SWT.ICON_WARNING;
        mboxmsg = "Unsupported image format.\nFile not saved.";
      }
    }
    catch( final Throwable e )
    {
      e.printStackTrace();

      mboxstyle = SWT.OK | SWT.ICON_ERROR;
      mboxmsg = "Error while exporting image:\n" + e.getMessage();
    }
    finally
    {
      img.dispose();
    }

    final MessageBox mbox = new MessageBox( activeShell, mboxstyle );
    mbox.setText( getText() );
    mbox.setMessage( mboxmsg );
    mbox.open();
  }

}
