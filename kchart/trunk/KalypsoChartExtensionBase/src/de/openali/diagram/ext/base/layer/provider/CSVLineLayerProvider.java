package de.openali.diagram.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import de.openali.diagram.ext.base.data.AbstractDomainValueData;
import de.openali.diagram.ext.base.layer.DefaultLineLayer;
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
public class CSVLineLayerProvider<T_domain extends Comparable, T_target extends Comparable> implements ILayerProvider
{

  private LayerType m_lt;
  private IDiagramModel m_model;

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;

	 IParameterContainer pc=DiagramFactoryUtilities.createParameterContainer(m_lt.getProvider(), m_lt.getId());

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
	          while ((s=br.readLine())!=null)
	          {
	                String[] cols=s.split( "  *");
	                //YearString
	                if (cols.length>=2)
	                {
	                  m_domainData.add( (T_domain) (Object) (Double.parseDouble( cols[0].trim())));
	                  m_targetData.add(  (T_target) (Object) Double.parseDouble( cols[1].trim()));
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
	 	  data.setInputFile(new File(pc.getParameterValue( "url", m_lt.getId())));
          icl = new DefaultLineLayer( data, domAxis, valAxis );
          icl.setTitle( m_lt.getTitle() );


    return icl;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.ChartView, org.ksp.chart.configuration.LayerType)
   */
  public void init( final IDiagramModel model, final LayerType lt )
  {
    m_lt = lt;
    m_model = model;
  }

}
