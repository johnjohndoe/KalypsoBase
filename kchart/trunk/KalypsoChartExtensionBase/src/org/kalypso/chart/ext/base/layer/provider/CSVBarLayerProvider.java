package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueData;
import org.kalypso.chart.ext.base.layer.DefaultBarLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;



/**
 * @author alibu
 *
 */
public class CSVBarLayerProvider<T_domain extends Comparable, T_target extends Comparable> extends AbstractLayerProvider
{


  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;

    

    	  String domainAxisId = m_lt.getMapper().getDomainAxisRef().getRef();
    	  String targetAxisId = m_lt.getMapper().getTargetAxisRef().getRef();

          IAxis<T_domain> domainAxis = m_model.getAxisRegistry().getAxis( domainAxisId );
          IAxis<T_target> targetAxis = m_model.getAxisRegistry().getAxis( targetAxisId);

       
        
          
          
          
          AbstractDomainIntervalValueData<T_domain, T_target> data=new AbstractDomainIntervalValueData<T_domain, T_target>()
          {
        	
			public boolean openData()
			{
				//TODO: umschreiben, damit auch urls verwendet werden können
		          FileReader fr;
				try
				{
					fr = new FileReader(m_file);
				
		          
		          BufferedReader br=new BufferedReader(fr);
		          String s="";
		          while ((s=br.readLine())!=null)
		          {
		                String[] cols=s.split( "  *");
		                //YearString
		                if (cols.length>=2)
		                {
		                  String ys=cols[0];
		                  //Datum zerpflücken (Bsp: 0510190530)
		                  //TODO: Auslagern in Toolbox-ähnliche Klasse
		                  int year=2000+Integer.parseInt( ys.substring( 0, 2 )) ;
		                  int month=Integer.parseInt( ys.substring( 2, 4 ))-1;
		                  int day=Integer.parseInt( ys.substring( 4, 6 )) ;
		                  int hour=Integer.parseInt( ys.substring( 6, 8 )) ;
		                  int minute=Integer.parseInt( ys.substring( 8, 10 )) ;
		                  
		                  //echte Daten aus EiongabeDatei
		                  Calendar calData=Calendar.getInstance();
		                  calData.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
		                  calData.set( Calendar.YEAR, year );
		                  calData.set( Calendar.MONTH, month );
		                  calData.set( Calendar.DAY_OF_MONTH, day );
		                  calData.set( Calendar.HOUR_OF_DAY, hour );
		                  calData.set( Calendar.MINUTE, minute );
		                  calData.set( Calendar.SECOND, 0 );
		                  calData.set( Calendar.MILLISECOND, 0 );
		                  
		                  
		                  //Startwert für Interval
		                  Calendar calStart=(Calendar) calData.clone();
		                  calStart.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
		                  calStart.set(Calendar.MINUTE, 0);
		                  calStart.set(Calendar.HOUR_OF_DAY, 0);
		                  
		                  //Endwert für Interval
		                  Calendar calEnd=(Calendar) calStart.clone();
		                  calEnd.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
		                  //wichtig, damit die Zeiten richtig sind
		                  
		                  calEnd.add(Calendar.DAY_OF_MONTH, +1);
		                  
		                  m_domainValues.add((T_domain) calData);
		                  m_domainIntervalStartValues.add((T_domain) calStart);
		                  m_domainIntervalEndValues.add((T_domain) calEnd);
		                  m_targetValues.add((T_target) (Object) Double.parseDouble( cols[1]));
		                }
		          }
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NumberFormatException e)
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

			public IDataRange<T_domain> getDomainRange() {
				return new ComparableDataRange<T_domain>(m_domainValues);
			}

			public IDataRange<T_target> getTargetRange() {
				return new ComparableDataRange<T_target>(m_targetValues);
			}


        	  
          };
          data.setInputFile(new File(m_pc.getParameterValue( "url", m_lt.getId())));
          icl = new DefaultBarLayer<T_domain, T_target>( data, domainAxis, targetAxis );
          icl.setTitle( m_lt.getTitle() );
     
          return icl;
  }



}
