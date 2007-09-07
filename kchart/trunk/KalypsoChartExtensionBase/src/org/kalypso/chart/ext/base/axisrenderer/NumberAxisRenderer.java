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
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.renderer.AbstractAxisRenderer;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.chart.framework.util.InsetsHelper;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;


/**
 * @author burtscher * IAxisRenderer-Implementation for displaying numeric values
 */
public class NumberAxisRenderer extends AbstractAxisRenderer<Number>
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
  public NumberAxisRenderer( final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
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
		Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Biggest Tick: "+m_nf.format(tickMap.get(0))+" "+tickMap.get(0));
	}
	else
	{
		IDataRange<Number> range=axis.getDataRange(); 
		final double logicalMin, logicalMax;
		if (range!=null)
		{
		  logicalMin = range.getMin().doubleValue();
		  logicalMax = range.getMax().doubleValue();
		}
		else
		{
		  return new Point (1,0);
		}
	   

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


	    //Variable zum Zwischenspeichern der "Breite" des letzten Segments
	    int removeWidth=getGap();
	    //Gap rausrechnen
	    Rectangle newScreen=substractSegment( axis, screen, removeWidth );
	    
	    // paint axis line
	    removeWidth=drawAxisLine (gc, axis, newScreen);
	    
	    newScreen=substractSegment( axis, newScreen, removeWidth );
	    
	    m_ticks = calcTicks( gc, axis);
	    removeWidth=drawTicks( gc, axis, newScreen, m_ticks );

	    newScreen=substractSegment( axis, newScreen, removeWidth );
	    removeWidth=drawAxisLabel( gc, axis, newScreen );

	    //Breite des letzten Stücks sollte nur der Breite des Rest-Rechtecks entsprechen
	    fgColor.dispose();
	    bgColor.dispose();
  }

  /**
   * Substracts a segment by the width (given a vertical axis) of 'width' from a rectangle;
   */
  private Rectangle substractSegment(IAxis<?> axis, Rectangle rect, int width)
  {
    if (axis.getPosition()==POSITION.LEFT)
    {
      return new Rectangle(rect.x, rect.y, rect.width - width, rect.height);
    }
    else if (axis.getPosition()==POSITION.RIGHT)
    {
      return new Rectangle(rect.x+width, rect.y, rect.width - width, rect.height);
    }
    else if (axis.getPosition()==POSITION.BOTTOM)
    {
      return new Rectangle(rect.x, rect.y+width, rect.width , rect.height - width);
    }
    else if (axis.getPosition()==POSITION.TOP)
    {
      return new Rectangle(rect.x, rect.y, rect.width , rect.height - width);
    }
    //should never occur
    return null;
  }

  /**
   * draws the axis main line
   */
  private int drawAxisLine( GCWrapper gc, IAxis<Number> axis, Rectangle screen )
  {
    int lineWidth=getLineWidth();
    int x=0;
    int y=0;
    int width=0;
    int height=0;
    if (axis.getPosition()==POSITION.LEFT)
    {
      x=screen.x + screen.width - lineWidth;
      y=screen.y;
      height=screen.height;
      width=lineWidth;
    }
    else if (axis.getPosition()==POSITION.RIGHT)
    {
      x=screen.x;
      y=screen.y;
      height=screen.height;
      width=lineWidth;
    }
    else if (axis.getPosition()==POSITION.BOTTOM)
    {
      x=screen.x;
      y=screen.y;
      height=lineWidth;
      width=screen.x+screen.width;
    }
    else if (axis.getPosition()==POSITION.TOP)
    {
      x=screen.x;
      y=screen.y+screen.height-lineWidth;
      height=lineWidth;
      width=screen.x+screen.width;
    }
    //temporär Vorder- und Hintergrund austauschen - Rectangle-Hintergrund wird in Vordergrundfarbe gezeichnet
    Color bgColor=gc.getBackground();
    gc.setBackground( gc.getForeground() );
    gc.fillRectangle( x, y, width, height );
    gc.setBackground( bgColor );
    return lineWidth;
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
    int halfWidth=(int) Math.round( ( (double) getLineWidth() )/ ( (double) 2 ) );
    
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + gap+halfWidth;
      else
        startY = screen.y + screen.height - gap - halfWidth;
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
        startX = screen.x + gap + halfWidth;
      else
        startX = screen.x + screen.width - gap - halfWidth;
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
  private int drawTicks( GCWrapper gc, IAxis<Number> axis, Rectangle screen, List<Number> ticks )
  {
    if( gc == null || axis == null || ticks == null )
    {
      return 0;
    }
    ChartUtilities.resetGC( gc.m_gc );

    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    
    //speichert die maximale Labelgröße
    Point maxExtent=new Point(0,0);
    
    int textX = 0;
    int textY = 0;
    
    int tickLength=getTickLength();
    FontData fontDataTick = getFontDataTickLabel();
    Insets tickLabelInsets = getTickLabelInsets();

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      int y1 = screen.y;
      int y2 = y1 + tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );
        maxExtent=getMaxExtent( maxExtent, tickExtent );
        
        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 + tickLabelInsets.top;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      int y1 = screen.y + screen.height;
      int y2 = y1 - tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );
        maxExtent=getMaxExtent( maxExtent, tickExtent );
        
        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 - tickLabelInsets.top - tickExtent.y;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      int x1 = screen.x+screen.width;
      int x2 = x1 - tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );
        maxExtent=getMaxExtent( maxExtent, tickExtent );
        
        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 - tickExtent.x - tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      int x1 = screen.x;
      int x2 = x1 + tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );
        maxExtent=getMaxExtent( maxExtent, tickExtent );
        
        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 + tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, fontDataTick );
      }
    }
    
    
    //Benötigte Breite berechnen
    if (axis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
    {
      return tickLength + tickLabelInsets.top + maxExtent.y + tickLabelInsets.bottom;
    }
    else
    {
      return tickLength + tickLabelInsets.left + maxExtent.x + tickLabelInsets.right;
    }
  }
  
  /**
   * returns a point containing the max x and max y of both given points
   */
  private Point getMaxExtent(Point p1, Point p2)
  {
    return new Point(Math.max( p1.x, p2.x ), Math.max( p1.y, p2.y ));
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
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private int drawAxisLabel( GCWrapper gc, IAxis<Number> axis, Rectangle screen )
  {
     ChartUtilities.resetGC( gc.m_gc );
     int partWidth=0;
     
     
     
    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getFontDataLabel() );

      int x = 0;
      int y = 0;
      
      Insets labelInsets = getLabelInsets();

      Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = screen.width / 2 - textExtent.x / 2;
          y = screen.y;
          y += labelInsets.top;// 
          partWidth=labelInsets.top + textExtent.y + labelInsets.bottom;
        }
        else
        {
          
          y = screen.height / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            x = screen.width;
            rotation = -90;
            x -= labelInsets.bottom + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            x = screen.x;
            rotation = 90;
            x += screen.y + labelInsets.bottom + textExtent.y ;
            y -= textExtent.x / 2;
          }
          tr.translate( x, y );
          tr.rotate( rotation );
          tr.translate( -x, -y );
          
          partWidth=labelInsets.left + textExtent.x + labelInsets.right;

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
    
    return partWidth;
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
    int ticklabel = 0;

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
      ticklabel = tickLabelSize.y;
      if (axis.getPosition() == POSITION.BOTTOM)
        width += labelInsets.top;
      else
        width += labelInsets.bottom;
         
    }
    else
    {
      width += tickLabelInsets.left + tickLabelInsets.right;
      width += labelInsets.bottom;
      ticklabel = tickLabelSize.x;
    }
    width += ticklabel;
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
