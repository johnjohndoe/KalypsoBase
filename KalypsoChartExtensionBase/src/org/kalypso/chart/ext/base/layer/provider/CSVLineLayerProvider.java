package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.ext.base.data.AbstractDomainValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class CSVLineLayerProvider<T_domain, T_target> extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer icl = null;

    final String domainAxisId = getLayerType().getMapper().getDomainAxisRef().getRef();
    final String targetAxisId = getLayerType().getMapper().getTargetAxisRef().getRef();

    final IAxis domAxis = getChartModel().getMapperRegistry().getAxis( domainAxisId );
    final IAxis valAxis = getChartModel().getMapperRegistry().getAxis( targetAxisId );

    final AbstractDomainValueFileData<T_domain, T_target> data = new AbstractDomainValueFileData<T_domain, T_target>()
    {

      @SuppressWarnings("unchecked")
      @Override
      public boolean openData( )
      {
        try
        {
          FileReader fr;
          fr = new FileReader( getInputFile() );

          BufferedReader br = new BufferedReader( fr );
          String s = "";
          int count = 0;
          List<T_domain> domainValues = new ArrayList<T_domain>();
          List<T_target> targetValues = new ArrayList<T_target>();

          while( (s = br.readLine()) != null )
          {
            String[] cols = s.split( "  *" );
            // YearString
            if( cols.length >= 2 )
            {
              T_domain domainVal = null;
              T_target targetVal = null;
              try
              {
                domainVal = (T_domain) new Double( cols[0].trim() );
                targetVal = (T_target) new Double( cols[1].trim() );
                if( domainVal != null && targetVal != null )
                {

                  domainValues.add( domainVal );
                  targetValues.add( targetVal );
                }
              }
              catch( NumberFormatException e )
              {
                Logger.logWarning( Logger.TOPIC_LOG_DATA, getInputFile().getName() + ": Line " + count + " could not be parsed: \n" + s );
                e.printStackTrace();
              }
            }
            count++;
          }
          setDomainValues( domainValues );
          setTargetValues( targetValues );
        }
        catch( FileNotFoundException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return true;

      }

      public IDataRange getDomainRange( )
      {
        return new ComparableDataRange<T_domain>( getDomainValues() );
      }

      public IDataRange getTargetRange( )
      {
        return new ComparableDataRange<T_target>( getTargetValues() );
      }

    };
    final String url = getParameterContainer().getParameterValue( "url", getLayerType().getId() );
    data.setInputFile( new File( url ) );
    icl = new DefaultLineLayer( data, domAxis, valAxis );
    icl.setTitle( getLayerType().getTitle() );

    return icl;
  }

}
