package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.kalypso.chart.ext.base.data.AbstractDomainValueData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.ksp.chart.factory.LayerType;



/**
 * @author alibu
 *
 */
public class CSVLineLayerProvider<T_domain extends Comparable, T_target extends Comparable> extends AbstractLayerProvider
{


  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;


	 String domainAxisId=m_lt.getMapper().getDomainAxisRef().getRef();
     String valueAxisId= m_lt.getMapper().getValueAxisRef().getRef();

     IAxis domAxis = m_model.getAxisRegistry().getAxis( domainAxisId );
     IAxis valAxis = m_model.getAxisRegistry().getAxis( valueAxisId);
	 
	 AbstractDomainValueData<T_domain, T_target> data=new AbstractDomainValueData<T_domain, T_target>()
	 {


		public boolean openData()
		{
			try
			{
	          FileReader fr;
    			fr = new FileReader(m_file);
			
	          BufferedReader br=new BufferedReader(fr);
	          String s="";
	          int count=0;
	          while ((s=br.readLine())!=null )
	          {
	                String[] cols=s.split( "  *");
	                //YearString
	                if (cols.length>=2)
	                {
	                  T_domain domainVal=null;
	                  T_target targetVal=null;
	                  try {
	                	  domainVal=(T_domain) (Object) (Double.parseDouble( cols[0].trim()));
	                	  targetVal=(T_target) (Object) Double.parseDouble( cols[1].trim());
	                	  if (domainVal!=null && targetVal!=null)
		                  {
		                	  m_domainData.add( domainVal);
			                  m_targetData.add( targetVal);
		                  }
	                  }
	                  catch(NumberFormatException e)
	                  {
	                	  Logger.logWarning(Logger.TOPIC_LOG_DATA, m_file.getName()+": Line "+count+" could not be parsed: \n"+s);
	                	  e.printStackTrace();
	                  }
	                }
	                count++;
	          }
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
			
		}

		public IDataRange getDomainRange() {
			return new ComparableDataRange<T_domain>(getDomainData());
		}

		public IDataRange getTargetRange() {
			return new ComparableDataRange<T_target>(getTargetData());
		}
		 
		 
	 };
	 	  String url=m_pc.getParameterValue( "url", m_lt.getId());
	 	  data.setInputFile(new File(url));
          icl = new DefaultLineLayer( data, domAxis, valAxis );
          icl.setTitle( m_lt.getTitle() );


    return icl;
  }

}
