package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.ext.base.data.AbstractDomainValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author alibu
 */
public class CSVLineLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( URL context ) throws ConfigurationException
  {
    return new DefaultLineLayer();
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  public IDataContainer getDataContainer( ) throws ConfigurationException
  {
    final AbstractDomainValueFileData data = new AbstractDomainValueFileData()
    {

      @SuppressWarnings("unchecked")
      @Override
      public boolean openData( )
      {
        try
        {

          InputStream is = getInputURL().openStream();
          InputStreamReader isr = new InputStreamReader( is );

          BufferedReader br = new BufferedReader( isr );
          String s = "";
          int count = 0;
          List<Object> domainValues = new ArrayList<Object>();
          List<Object> targetValues = new ArrayList<Object>();

          while( (s = br.readLine()) != null )
          {
            String[] cols = s.split( "  *" );
            // YearString
            if( cols.length >= 2 )
            {
              Object domainVal = null;
              Object targetVal = null;
              try
              {
                domainVal = new Double( cols[0].trim() );
                targetVal = new Double( cols[1].trim() );
                if( domainVal != null && targetVal != null )
                {

                  domainValues.add( domainVal );
                  targetValues.add( targetVal );
                }
              }
              catch( NumberFormatException e )
              {
                Logger.logWarning( Logger.TOPIC_LOG_DATA, getInputURL().toString() + ": Line " + count + " could not be parsed: \n" + s );
                e.printStackTrace();
              }
            }
            count++;
          }

          br.close();
          isr.close();
          is.close();

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
        return new ComparableDataRange<Object>( getDomainValues() );
      }

      public IDataRange getTargetRange( )
      {
        return new ComparableDataRange<Object>( getTargetValues() );
      }

    };
    final String url = getParameterContainer().getParameterValue( "url", getId() );
    try
    {
      data.setInputURL( new URL( url ) );
    }
    catch( MalformedURLException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return data;
  }
}
