package de.openali.odysseus.chart.framework.util.img;

import java.awt.Insets;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * Creates an simple legend from an IChartModel. It displays its elements as lines, where each line corresponds to a
 * layer title or a legend entry. The image size is calculated automatically, depending on style setting and default
 * icon sizes. It does neither display title or description of the chart nor the description of layers.
 * 
 * @author burtscher
 */
public final class SimpleLegendImageFactory
{
  private SimpleLegendImageFactory( )
  {
  }

  /**
   * @param chart
   *          the chart for which the legend is created
   * @param dev
   *          an swt device
   * @param layerStyle
   *          style for layer text
   * @param legendEntryStyle
   *          style for legend entry text
   * @param lineBorder
   *          Insets to be placed around each lines
   * @param defaultIconSize
   *          default size used for all icons - except for those who need more space
   */
  public static ImageData createLegendImage( final IChartModel chart, final Device dev, final ITextStyle layerStyle, final ITextStyle legendEntryStyle, final Insets lineBorder, final Point defaultIconSize )
  {
    final Image tmpImg = new Image( dev, 10, 10 );
    final GC tmpGc = new GC( tmpImg );

    final ILayerManager layerManager = chart.getLayerManager();

    // max Breite eines Icons
    int maxIconWidth = 0;
    // Mmax Höhe eines Icons
    int maxIconHeight = 0;
    // Anzahl vorhandener Layer
    int layercnt = 0;
    // Anzahl vorhandener Icons
    int iconcnt = 0;
    // Höhe aller Icons zusammen
    int allIconsSize = 0;
    // Gesamthöhe des Bildes
    int totalHeight = 0;
    // Gesamtbreite des Bildes
    int totalWidth = 0;
    // Platz zwischen Icon und Beschreibung
    final int spacer = 5;

    /*
     * Analyse: wie groß ist das größte Icon, wieviele Icons gibt es, etc
     */
    for( final IChartLayer layer : layerManager.getLayers() )
    {
      if( !layer.isLegend() )
        continue;

      layerStyle.apply( tmpGc );
      String layerText = layer.getTitle();
      if( layerText == null )
        layerText = "";
      final Point layerTextExt = tmpGc.textExtent( layerText );

      totalHeight += lineBorder.bottom + lineBorder.top + layerTextExt.y;

      final int layerLineWidth = layerTextExt.x + lineBorder.left + lineBorder.right;
      if( layerLineWidth > totalWidth )
      {
        totalWidth = layerLineWidth;
      }

      final ILegendEntry[] legendEntries = layer.getLegendEntries();
      for( final ILegendEntry le : legendEntries )
      {
        totalHeight += lineBorder.bottom + lineBorder.top;

        final Point iconSize = le.computeSize( defaultIconSize );
        if( iconSize.x > maxIconWidth )
        {
          maxIconWidth = iconSize.x;
        }
        if( iconSize.y > maxIconHeight )
        {
          maxIconHeight = iconSize.y;
        }
        allIconsSize += iconSize.y;

        legendEntryStyle.apply( tmpGc );
        String legendText = le.getDescription();
        if( legendText == null )
          legendText = "";

        final Point legendTextExt = tmpGc.textExtent( legendText );
        totalHeight += Math.max( iconSize.x, legendTextExt.y );

        final int legendLineWidth = legendTextExt.x + iconSize.x + spacer + lineBorder.left + lineBorder.right;
        if( legendLineWidth > totalWidth )
        {
          totalWidth = legendLineWidth;
        }

        iconcnt++;
      }
      layercnt++;
    }

    tmpGc.dispose();
    tmpImg.dispose();

    // echtes Image erzeugen
    final Image img = new Image( dev, totalWidth, totalHeight );
    final GC gc = new GC( img );

    int yOffset = 0;
    final int xOffset = lineBorder.left;
    final int textXOffset = xOffset + maxIconWidth + spacer;

    for( final IChartLayer layer : layerManager.getLayers() )
    {
      if( !layer.isLegend() )
        continue;

      layerStyle.apply( gc );
      String layerText = layer.getTitle();
      if( layerText == null )
        layerText = "";
      final Point layerTextExt = gc.textExtent( layerText );

      yOffset += lineBorder.top;
      gc.drawText( layerText, xOffset, yOffset );

      yOffset += lineBorder.bottom + layerTextExt.y;
      for( final ILegendEntry le : layer.getLegendEntries() )
      {
        yOffset += lineBorder.top;
        legendEntryStyle.apply( gc );
        String legendText = le.getDescription();
        if( legendText == null )
          legendText = "";
        final Point legendTextExt = gc.textExtent( legendText );

        final ImageData symbol = le.getSymbol( defaultIconSize );

        final int lineHeight = Math.max( symbol.height, legendTextExt.y );

        final int iconYOffset = yOffset + Double.valueOf( Math.ceil( Math.abs( lineHeight - symbol.height ) / 2 ) ).intValue();
        final Image iconImage = new Image( dev, symbol );
        gc.drawImage( iconImage, xOffset, iconYOffset );
        iconImage.dispose();

        final int textYOffset = yOffset + Double.valueOf( Math.ceil( Math.abs( lineHeight - legendTextExt.y ) / 2 ) ).intValue();

        gc.drawText( legendText, textXOffset, textYOffset );

        yOffset += lineHeight + lineBorder.bottom;
      }

    }

    final ImageData id = img.getImageData();

    gc.dispose();
    img.dispose();

    return id;

  }
}
