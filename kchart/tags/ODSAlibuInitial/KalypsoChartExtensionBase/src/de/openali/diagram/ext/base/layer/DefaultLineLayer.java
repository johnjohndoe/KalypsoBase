package de.openali.diagram.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.ext.base.data.AbstractDomainValueData;
import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.data.impl.ComparableDataRange;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;


/**
 * @author alibu
 *
 */
public class DefaultLineLayer<T_domain extends Comparable, T_target extends Comparable> extends AbstractChartLayer
{

  private AbstractDomainValueData<T_domain, T_target> m_dataContainer;


@SuppressWarnings("unchecked")
public DefaultLineLayer( AbstractDomainValueData<T_domain, T_target> dataContainer, IAxis<T_domain> domainAxis, IAxis<T_target> targetAxis)
  {
	m_domainAxis=domainAxis;
    m_targetAxis=targetAxis;
    m_dataContainer=dataContainer;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img, int width, int height )
  {	
	  GC gc=new GC(img);
	  GCWrapper gcw=new GCWrapper(gc);
	  
	  ArrayList<Point> path=new ArrayList<Point>();
	  
	  path.add(new Point(0,height/2));
	  path.add(new Point(width/5,height/2));
	  path.add(new Point(width/5*2,height/4));
	  path.add(new Point(width/5*3,height/4*3));
	  path.add(new Point(width/5*4,height/2));
	  path.add(new Point(width,height/2));
	  	
	  IStyledElement element = m_style.getElement(SE_TYPE.LINE, 1);
	  
	  element.setPath(path);
	  element.paint(gcw);
	  
	  gcw.dispose();
	  gc.dispose();
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  @SuppressWarnings("unchecked")
  public IDataRange getDomainRange( )
  {
	  if (m_dataContainer != null )
	  {
		  m_dataContainer.open();
		  return new ComparableDataRange<T_domain>(m_dataContainer.getDomainData());
	  }
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
		  return new ComparableDataRange<T_target>(m_dataContainer.getTargetData());
	  }
	  return null;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper, org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
public void paint( GCWrapper gc )
  {
	  if (m_dataContainer != null )
	  {
		  m_dataContainer.open();
		  List<T_domain> m_domainData = m_dataContainer.getDomainData();
		  List<T_target> m_targetData = m_dataContainer.getTargetData();
	      ArrayList<Point> path=new ArrayList<Point>();
	      IStyledElement line=m_style.getElement( SE_TYPE.LINE, 1);
	      for( int i=0; i<m_domainData.size();i++)
	      {
	    	  T_domain domVal=m_domainData.get(i);
	    	  T_target targetVal=m_targetData.get(i);
	    	  int domScreen=getDomainAxis().logicalToScreen(domVal);
	    	  int valScreen=getTargetAxis().logicalToScreen(targetVal);
	    	  path.add(new Point(domScreen, valScreen));
	      }
	      line.setPath(path);
	      line.paint(gc);
	  }
	  else
	  {
		  Logger.logWarning(Logger.TOPIC_LOG_GENERAL, "Layer "+m_id+" has not yet been opened");
	  }
  }

}
