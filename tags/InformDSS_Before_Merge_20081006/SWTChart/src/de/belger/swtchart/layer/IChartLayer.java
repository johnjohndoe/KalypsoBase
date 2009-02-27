package de.belger.swtchart.layer;

import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.belger.swtchart.EditInfo;
import de.belger.swtchart.axis.AxisRange;

/**
 * @author Belger
 */
public interface IChartLayer
{
  public static final Rectangle2D MINIMAL_RECT = new Rectangle2D.Double( Double.NaN, Double.NaN, Double.NaN, Double.NaN );

  /** Returns true, if the layer should be visible when it is first shown. */
  public boolean getInitialVisibility( );

  /** Each layer must have a domain range */
  public AxisRange getDomainRange( );

  /** Each layer must have a value range */
  public AxisRange getValueRange( );

  /**
   * Compute and return the maximal extent of this layer with respect to its domain and value range. In any direction in
   * which the maximal extent cannot be determined, return {@link java.lang.Double#NaN}. In nothing is shown, return
   * {@link #MINIMAL_RECT}.
   */
  public Rectangle2D getBounds( );

  /**
   * Test if the layer has an handle at the given point and return appropriate information
   */
  public EditInfo getHoverInfo( final Point point );

  /**
   * Paint information for the user about dragging a handle of this layer.
   * 
   * @param data
   *          the data entry of the {@link EditInfo}return by {@link #getHoverInfo(Point)}
   */
  public void paintDrag( final GCWrapper gc, final Point editing, final Object hoverData );

  /** User friendly name of this layer */
  public String toString( );

  /** Paints a represantation of this layer in the legend */
  public void paintLegend( final GCWrapper gc );

  /** Draw this layer */
  public void paint( final GCWrapper gc );

  /** @return true, if this layer isn't painting anything */
  public boolean isNotPainting( );

  /**
   * Moves a handle of this layer to a given point.
   * 
   * @param point
   *          The target of the edit operation
   * @param data
   *          the data entry of the {@link EditInfo}return by {@link #getHoverInfo(Point)}
   */
  public void edit( final Point point, final Object data );

  /**
   * @param data
   *          the data entry of the {@link EditInfo}return by {@link #getHoverInfo(Point)}
   */
  public void setActivePoint( final Object data );

  public String getId( );
  
  public boolean alwaysAllowsEditing();

  /**
   * return the position of this layer on the canvas (paint order)
   * </p>
   * high value represents foreground
   * </p>
   * the value zero allows the IProfilLayerProvider to order the layeres by itself
   */
  public int getZOrder( );



  
}
