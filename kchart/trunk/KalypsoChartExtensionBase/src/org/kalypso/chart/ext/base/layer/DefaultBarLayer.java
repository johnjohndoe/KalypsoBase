package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueData;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.AbstractChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;



/**
 * @author alibu
 *
 */
public class DefaultBarLayer<T_domain extends Comparable, T_target extends Comparable> extends AbstractChartLayer
{

  private AbstractDomainIntervalValueData<T_domain, T_target> m_dataContainer;


public DefaultBarLayer( AbstractDomainIntervalValueData<T_domain, T_target> dataContainer, IAxis<T_domain> domAxis, IAxis<T_target> valAxis)
  {
	m_domainAxis=domAxis;
    m_targetAxis=valAxis;
    m_dataContainer=dataContainer;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img, int width, int height )
  {
	  GC gc=new GC(img);
	  GCWrapper gcw=new GCWrapper(gc);
	  IStyledElement element = m_style.getElement(SE_TYPE.POLYGON, 1);
	  
	  ArrayList<Point> path=new ArrayList<Point>();
	  path.add(new Point(0,height));
	  path.add(new Point(0,height/2));
	  path.add(new Point(width/2,height/2));
	  path.add(new Point(width/2,height ));
	  element.setPath(path);
	  element.paint(gcw);
	  
	  
	  path=new ArrayList<Point>();
	  path.add(new Point(width/2,height));
	  path.add(new Point(width/2,0));
	  path.add(new Point(width,0 ));
	  path.add(new Point(width,height));
	  element.setPath(path);
	  element.paint(gcw);
	  
	  gcw.dispose();
	  gc.dispose();
	  
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetRange()
   */
  public IDataRange getTargetRange( )
  {
	  if (m_dataContainer != null )
	  {
		  m_dataContainer.open();
		  return new ComparableDataRange<T_target>(m_dataContainer.getTargetValues());
	  }
	  return null;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper, org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
public void paint( GCWrapper gc)
  {
	  if (m_dataContainer!=null)
	  {
		  m_dataContainer.open();
		  List<T_domain> m_domainStartComponent = m_dataContainer.getDomainDataIntervalStart();
		  List<T_domain> m_domainEndComponent = m_dataContainer.getDomainDataIntervalEnd();
		  List<T_target> m_targetComponent = m_dataContainer.getTargetValues();
		  
	      ArrayList<Point> path=new ArrayList<Point>();
	      IStyledElement poly=m_style.getElement( SE_TYPE.POLYGON, 1);
	      
	      for(int i=0;i<m_domainStartComponent.size();i++)
	      {
	        
	    	T_domain startValue = m_domainStartComponent.get(i);
	    	T_domain endValue = m_domainEndComponent.get(i);
	    	T_target targetValue = m_targetComponent.get(i);
	    	
	
	        if (m_domainAxis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL )
	        {
	          path.add( new Point( m_domainAxis.logicalToScreen( startValue), m_targetAxis.zeroToScreen() ) );
	          path.add( new Point( m_domainAxis.logicalToScreen( startValue), m_targetAxis.logicalToScreen(  targetValue ) ) );
	          path.add( new Point( m_domainAxis.logicalToScreen( endValue), m_targetAxis.logicalToScreen( targetValue ) ) );
	          path.add( new Point( m_domainAxis.logicalToScreen( endValue), m_targetAxis.zeroToScreen() ) );
	        }
	        else
	        {
	          path.add( new Point( m_targetAxis.zeroToScreen(  ), m_domainAxis.logicalToScreen( startValue) ) );
	          path.add( new Point( m_targetAxis.logicalToScreen(targetValue ), m_domainAxis.logicalToScreen( startValue) ) );
	          path.add( new Point( m_targetAxis.logicalToScreen( targetValue ),  m_domainAxis.logicalToScreen( endValue) ) );
	          path.add( new Point( m_targetAxis.zeroToScreen( ), m_domainAxis.logicalToScreen( endValue) ) );
	        }
	        poly.setPath( path );
	        poly.paint( gc);
	      }
	  }
  }
}
