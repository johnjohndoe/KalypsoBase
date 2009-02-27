package de.openali.diagram.framework.model.layer;

import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.data.IDataContainer;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.legend.ILegendItem;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.styles.ILayerStyle;

/**
 * @author burtscher an IChartLayer represents a (visual) layer of the chart; it can be assigned to up to 2 axes to
 *         translate logical data into screen values
 */
public interface IChartLayer
{
  // TODO: add get/set ID: this is needed to get the back-reference where this laer came from (for examlpe from the LayerType)
  
  
	
	/**
	   * Setzt die ID des Layers; die ID wird verwendet, um das Layer im Diagram zu referenzieren
	   */
	public void setId( final String name );
	
	/**
	   * Gibt die ID des Layers zurück; die ID wird verwendet, um das Layer im Diagram zu referenzieren
	   */
	public String getId( );
	
	
	
	
  /**
   * sets the layers title (which will be shown in the legend)
   */
  public void setTitle( final String name );

  /**
   * @return the layers title
   */
  public String getTitle( );

  /**
   * sets a description for the layer
   */
  public void setDescription( String description );

  /**
   * @return the layers description
   */
  public String getDescription( );

  /**
   * @return the IAxis object used with domain data
   */
  public IAxis getDomainAxis( );

  /**
   * @return DataRange object describing minimal and maximal domain values
   */
  public IDataRange getDomainRange( );

  /**
   * @return the IAxis object used with value data
   */
  public IAxis getTargetAxis( );

  /**
   * @return DataRange object describing minimal and maximal target values (to remain inside the elsewise used
   *         vocabulary: the "target values" should be refered to as "value values" which sounds a bit strange...)
   */
  public IDataRange getTargetRange( );

  /**
   * TODO: remove this from IChartLayer interface ILegendItem should be constructed upon layers, client should always
   * use drawIcon and getName only the legend should know about legend-items
   * 
   * @return ILegendItem which describes the Layers content and style
   */
  public ILegendItem getLegendItem( );

  /**
   * draws a picture of a given size into the given Image; the icon will be used for the legend to describe the layers
   * visual representation
   * TODO: remove widht and height parameters!
   * Use img.getBounds instead
   */
  public void drawIcon( final Image img, final int width, final int height );

  /**
   * @return true if the layer is set active, false otherwise
   */
  public boolean isActive( );

  /**
   * @return the layer style
   */
  public ILayerStyle getStyle( );

  /**
   * sets the layer's style
   */
  public void setStyle( final ILayerStyle style );

  /**
   * draws the layer using the given GCWrapper and Device
   */
  public void paint( final GCWrapper gc);

  public void setVisibility( final boolean isVisible );

  public boolean getVisibility( );
  
  public IDataContainer getDataContainer();
}
