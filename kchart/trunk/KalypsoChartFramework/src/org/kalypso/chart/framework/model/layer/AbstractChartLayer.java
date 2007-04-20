package org.kalypso.chart.framework.model.layer;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.legend.ILegendItem;
import org.kalypso.chart.framework.model.legend.LegendItem;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.styles.ILayerStyle;



/**
 * @author alibu
 *
 */
public abstract class AbstractChartLayer<T_domain extends Comparable, T_target extends Comparable> implements IChartLayer
{

  protected boolean m_isVisible=true;
  protected ILayerStyle m_style=null;
  protected String m_title="";
  protected String m_id="";
  protected String m_description="";
  protected boolean m_isActive=false;
  protected IAxis<T_target> m_targetAxis=null;
  protected IAxis<T_domain> m_domainAxis=null;
  protected IDataContainer<T_domain, T_target> m_dataContainer=null;

  
  public AbstractChartLayer()
  {
	  
  }
  
  
  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis<T_domain> getDomainAxis( )
  {
    return m_domainAxis;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getId()
   */
  public String getId( )
  {
	  return m_id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getStyle()
   */
  public ILayerStyle getStyle( )
  {
    return m_style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetAxis()
   */
  public IAxis<T_target> getTargetAxis( )
  {
    return m_targetAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getVisibility()
   */
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return m_isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public void setActive( boolean isActive)
  {
	  m_isActive=isActive;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setDescription(java.lang.String)
   */
  public void setDescription( String description )
  {
    m_description=description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setTitle(java.lang.String)
   */
  public void setTitle( String title )
  {
    m_title = title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setID(java.lang.String)
   */
  public void setId( String id )
  {
	  m_id = id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setStyle(org.kalypso.swtchart.chart.styles.ILayerStyle)
   */
  public void setStyle( ILayerStyle style )
  {
    m_style=style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  public void setVisible( boolean isVisible )
  {
    m_isVisible=isVisible;

  }
  
  public IDataContainer getDataContainer()
  {
	  return m_dataContainer;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem()
   */
  public ILegendItem getLegendItem()
  {
	ILegendItem l=null;
    Image img=new Image(Display.getCurrent(), 20, 20);
    drawIcon(img, 20, 20);
    ImageData id=img.getImageData();
    img.dispose();
    l=new LegendItem(null, getTitle(), id);
    return l;
  }

}
