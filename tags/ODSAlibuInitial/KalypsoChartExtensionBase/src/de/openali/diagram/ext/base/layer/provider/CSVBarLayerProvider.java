package de.openali.diagram.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import de.openali.diagram.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.diagram.ext.base.layer.DefaultBarLayer;
import de.openali.diagram.factory.configuration.exception.LayerProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.xsd.LayerType;
import de.openali.diagram.factory.provider.ILayerProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.mapper.IAxis;


/**
 * @author alibu
 *
 */
public class CSVBarLayerProvider<T_domain extends Comparable, T_target extends Comparable> implements ILayerProvider
{

  private LayerType m_lt;
  private IDiagramModel m_model;

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;

    IParameterContainer ph=DiagramFactoryUtilities.createParameterContainer(m_lt.getProvider(), m_lt.getId());
    

    	  String domainAxisId = m_lt.getMapper().getDomainAxisRef().getRef();
    	  String valueAxisId = m_lt.getMapper().getValueAxisRef().getRef();

          IAxis<T_domain> domainAxis = m_model.getAxisRegistry().getAxis( domainAxisId );
          IAxis<T_target> targetAxis = m_model.getAxisRegistry().getAxis( valueAxisId);

       
        
          
          
          
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
		                  Calendar calStart=Calendar.getInstance();

		                  calStart.set( Calendar.YEAR, year );
		                  calStart.set( Calendar.MONTH, month );
		                  calStart.set( Calendar.DAY_OF_MONTH, day );
		                  calStart.set( Calendar.HOUR_OF_DAY, hour );
		                  calStart.set( Calendar.MINUTE, minute );
		                  calStart.set( Calendar.SECOND, 0 );
		                  calStart.set( Calendar.MILLISECOND, 0 );
		                  
		                  Calendar calEnd=(Calendar) calStart.clone();
		                  //wichtig, damit die Zeiten richtig sind
		                  calStart.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
		                  calEnd.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
		                  
		                  calEnd.add(Calendar.DAY_OF_MONTH, +1);
		                  
		                  m_domainDataIntervalStart.add((T_domain) calStart);
		                  m_domainDataIntervalEnd.add((T_domain) calEnd);
		                  m_targetData.add((T_target) (Object) Double.parseDouble( cols[1]));
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
        	  
          };
          data.setInputFile(new File(ph.getParameterValue( "url", m_lt.getId())));
          
//        
          icl = new DefaultBarLayer<T_domain, T_target>( data, domainAxis, targetAxis );
          icl.setTitle( m_lt.getTitle() );
     
          return icl;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.ChartView, org.ksp.chart.configuration.LayerType)
   */
  public void init( final IDiagramModel model, final LayerType lt)
  {
    m_lt = lt;
    m_model = model;
  }


}
