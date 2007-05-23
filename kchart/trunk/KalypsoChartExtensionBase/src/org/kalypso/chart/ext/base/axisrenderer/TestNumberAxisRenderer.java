package org.kalypso.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.renderer.AbstractAxisRenderer;
import org.kalypso.chart.framework.util.InsetsHelper;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;


/**
 * @author burtscher * IAxisRenderer-Implementation for displaying numeric values
 */
public class TestNumberAxisRenderer extends AbstractAxisRenderer<Number>
{
  private NumberFormat m_nf = NumberFormat.getInstance();


  private List<Number> m_ticks;

  /**
   * @param foreground
   *          Color of axis, ticks and text
   * @param lineWidth
   *          Width of ticks and axis line
   * @param tickLength
   * @param tickLabelInsets
   *          <ul>
   *          <li>top: distance between ticklabel and axis</li>
   *          <li>bottom: distance between ticklabel and outside</li>
   *          <li>left: distance between ticklabel and previous ticklabel</li>
   *          <li>right: distance between ticklabel and next ticklabel</li>
   *          </ul>
   * @param maxDigits
   * @param labelInsets
   * @param gap
   *          space between axis and component border
   */
  public TestNumberAxisRenderer( final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
  {
    super( rgbForeground, rgbBackground, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );
    m_nf.setMinimumFractionDigits( 0 );
    m_nf.setMaximumFractionDigits( 20 );

  }

  /**
   * calculates the size of the biggest tick label by assuming that the width of all labels is strictly monotonic - this
   * means that either the lowest or the highest axis value has the biggest label
   */
  public Point calcTickLabelSize( GCWrapper gc, final IAxis<Number> axis )
  {
	Point tickLabelSize=new Point(10,10);  
	//Vielleicht wurden zuvor schon mal ticks ausgerechnet - dann kann man die verwenden
	List<Number> tickMap = getTickMap().get( axis );
	
	
	
	if ( tickMap !=null && tickMap.size()>0)
	{
		NumberTickCalculator.testNumberFormat(tickMap.toArray(new Double[]{}));
		tickLabelSize=getTextExtent(gc, m_nf.format(tickMap.get(tickMap.size()-1)), getFontDataTickLabel());
		System.out.println("Biggest Tick: "+m_nf.format(tickMap.get(0))+" "+tickMap.get(0));
	}
	else
	{
		IDataRange<Number> range=axis.getDataRange(); 
		  
	    final double logicalMin = range.getMin().doubleValue();
	    final double logicalMax = range.getMax().doubleValue();

	   

	    FontData fontDataTick = getFontDataTickLabel();
	    
	    
	    final Point fromTextExtent = getTextExtent( gc, logicalMin, fontDataTick );
	    final Point toTextExtent = getTextExtent( gc, logicalMax, fontDataTick );
	    tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

	}
	

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;
    return new Point( x, y );
  }

  
  /**
   * doubleBuffered painting
   */
  public void paint( GCWrapper gc, final IAxis<Number> axis, final Rectangle screen )
  {
	    Device dev = gc.getDevice();
	    Color fgColor = new Color( dev, getRgbForeground() );
	    Color bgColor = new Color( dev, getRgbBackground() );
	    gc.setForeground( fgColor );
	    gc.setBackground( bgColor );

	    // paint axis line
	    int[] coords = createAxisSegment( axis, screen );
	    assert coords != null && coords.length == 4;
	    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

	    // final Collection<Double> ticks = calcTicks( gc, axis );
	    m_ticks = calcTicks( gc, axis);
	    drawTicks( gc, axis, coords[0], coords[1], m_ticks );

	    drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3] );

	    fgColor.dispose();
	    bgColor.dispose();
  }

  

  private List<Number> calcTicks(GCWrapper gc, IAxis<Number> axis)
{
	return NumberTickCalculator.calcTicks(gc, axis, true);
}

/**
   * @return Array of 4 int-Values: startX, startY, endX, endY - where startX/Y are the start coordinates for the axis
   *         line and endX/Y its end coordinates within the given Rectangle
   */
  private int[] createAxisSegment( final IAxis<Number> axis, final Rectangle screen )
  {
    int startX;
    int startY;
    int endX;
    int endY;

    int gap=getGap();
    
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + gap;
      else
        startY = screen.y + screen.height - 1 - gap;
      endY = startY;

      if( axis.getDirection() == DIRECTION.NEGATIVE )
      {
        int tmp = startX;
        startX = endX;
        endX = tmp;
      }
    }
    else
    {
      startY = screen.y;
      endY = screen.y + screen.height;

      if( axis.getPosition() == POSITION.RIGHT )
        startX = screen.x + gap;
      else
        startX = screen.x + screen.width - 1 - gap;
      endX = startX;

      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        int tmp = startY;
        startY = endY;
        endY = tmp;
      }
    }

    return new int[] { startX, startY, endX, endY };
  }

  /**
   * draws the Axis ticks into the given GCWrapper
   */
  private void drawTicks( GCWrapper gc, IAxis<Number> axis, int startX, int startY, List<Number> ticks )
  {
    if( gc == null || axis == null || ticks == null )
    {
      return;
    }

    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    

    int textX = 0;
    int textY = 0;
    
    int tickLength=getTickLength();
    FontData fontDataTick = getFontDataTickLabel();
    Insets tickLabelInsets = getTickLabelInsets();

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      int y1 = startY;
      int y2 = y1 + tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 + tickLabelInsets.top;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      int y1 = startY;
      int y2 = y1 - tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 - tickLabelInsets.top - tickExtent.y;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      int x1 = startX;
      int x2 = x1 - tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 - tickExtent.x - tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      int x1 = startX;
      int x2 = x1 + tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 + tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
  }

  /**
   * @return Insets-object, transformed in accordance to axis position
   */
  private Insets getConvertedInsets( final IAxis<Number> axis, final Insets insets )
  {
    // POSITION BOTTOM is the default order for the insets
    InsetsHelper ihelper = new InsetsHelper( insets );

    if( axis.getPosition() == POSITION.TOP )
      ihelper = ihelper.mirrorTopBottom();
    if( axis.getPosition() == POSITION.LEFT )
      ihelper = ihelper.hor2vert();
    if( axis.getPosition() == POSITION.RIGHT )
      ihelper = ihelper.hor2vert().mirrorLeftRight();

    return ihelper;
  }
  
  


  /**
   * draws the axis' main line
   */
  private void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private void drawAxisLabel( GCWrapper gc, IAxis<Number> axis, int x1, int y1, int x2, int y2 )
  {
    // ChartUtilities.resetGC( gc.m_gc, dev );

    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getFontDataLabel() );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      if( tickLabelExtent == null )
      {
        return;
      }

      int x = 0;
      int y = 0;
      
      int tickLength=getTickLength();
      int lineWidth=getLineWidth();
      
      Insets labelInsets = getLabelInsets();
      Insets tickLabelInsets=getTickLabelInsets();

      Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= textExtent.y + labelInsets.bottom + tickLength + lineWidth + tickLabelInsets.top + tickLabelExtent.y + tickLabelInsets.bottom + labelInsets.bottom;// +
          else
            y += tickLength + lineWidth + tickLabelInsets.top + tickLabelExtent.y + tickLabelInsets.bottom + labelInsets.top;// +
        }
        else
        {
          x = x1;
          y = Math.abs( y2 - y1 ) / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x -= lineWidth + tickLength + tickLabelInsets.right + tickLabelExtent.x + tickLabelInsets.left + labelInsets.bottom + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            x += lineWidth + tickLength + tickLabelInsets.right + tickLabelExtent.x + tickLabelInsets.left + labelInsets.bottom + textExtent.y;
            y -= textExtent.x / 2;
          }
          tr.translate( x, y );
          tr.rotate( rotation );
          tr.translate( -x, -y );

        }
        if( tr != null )
        {
          gc.setTransform( tr );
        }
        drawText( gc, axis.getLabel(), x, y, getFontDataLabel() );
      }
      finally
      {
        if( tr != null )
          tr.dispose();
      }
    }
  }

  /**
   * calculates the extent of a Double using a certain fontdata
   */
  @Override
  protected Point getTextExtent( GCWrapper gc, Number value, FontData fd )
  {
    String label = "";
    try
    {
      label = m_nf.format( value );
    }
    catch( java.lang.IllegalArgumentException e )
    {
      e.printStackTrace();
    }
    return getTextExtent( gc, label, fd );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getTicks(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public Double[] getGridTicks( IAxis<Number> axis )
  {
	HashMap<IAxis<Number>, List<Number>> tickMap = getTickMap();
    List<Number> ticks = tickMap.get( axis );
    if( ticks == null )
    {
      GC gc = new GC( Display.getDefault() );
      GCWrapper gcw = new GCWrapper( gc );
      calcTicks( gcw, axis);
      ticks = tickMap.get( axis );
      gcw.dispose();
      gc.dispose();
    }
    return (Double[]) ticks.toArray(new Double[0]);
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<Number> axis )
  {
    int width = 0;
    int label = 0;

    // Testutensilien erzeugen
    Display dev = Display.getCurrent();
    Image img = new Image( dev, 1, 1 );
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    Point tickLabelSize = calcTickLabelSize( gcw, axis );

    if( tickLabelSize == null )
    {
      return 0;
    }

    Insets tickLabelInsets=getTickLabelInsets();
    Insets labelInsets=getLabelInsets();
    
    
    // TickLabels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += tickLabelInsets.top + tickLabelInsets.bottom;
      width += labelInsets.top + labelInsets.bottom;
      label = tickLabelSize.y;
    }
    else
    {
      width += tickLabelInsets.left + tickLabelInsets.right;
      width += labelInsets.left + labelInsets.right;
      label = tickLabelSize.x;
    }
    width += label;
    width += getGap();
    width += getLineWidth();
    width += getTickLength();

    // Jetzt noch den Label-Text
    Point labelExtent = getTextExtent( gcw, axis.getLabel(), getFontDataLabel());
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }
  
  public void setNumberFormat(NumberFormat nf)
  {
	  m_nf=nf;
  }


  
}
