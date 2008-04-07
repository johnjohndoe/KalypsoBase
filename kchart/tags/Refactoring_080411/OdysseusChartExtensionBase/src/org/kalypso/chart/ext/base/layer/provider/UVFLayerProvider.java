package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class UVFLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @SuppressWarnings( { "unchecked", "unused" })
  public IChartLayer getLayer( URL context )
  {
    return new DefaultLineLayer();
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  public IDataContainer getDataContainer( ) throws ConfigurationException
  {
    final String domainAxisId = getDomainAxisId();
    final String targetAxisId = getTargetAxisId();

    final IAxis domAxis = getChartModel().getMapperRegistry().getAxis( domainAxisId );
    final IAxis valAxis = getChartModel().getMapperRegistry().getAxis( targetAxisId );

    final AbstractDomainValueFileData<Calendar, Number> data = new AbstractDomainValueFileData<Calendar, Number>()
    {

      private SimpleDateFormat m_format;

      @Override
      public boolean openData( )
      {
        setLoading( true );
        try
        {
          // FileReader fr = new FileReader( getInputURL() );

          URL url = getInputURL();
          InputStream is = url.openStream();
          InputStreamReader isr = new InputStreamReader( is );

          BufferedReader br = new BufferedReader( isr );
          String s = "";
          int count = 0;
          List<Calendar> domainValues = new ArrayList<Calendar>();
          List<Number> targetValues = new ArrayList<Number>();
          m_format = new SimpleDateFormat( "yyMMddHHmm" );

          while( (s = br.readLine()) != null )
          {
            if( count > 4 )
            {
              String[] cols = s.split( "  *" );
              // YearString
              if( cols.length >= 2 )
              {
                String ys = cols[0];

                Date date = m_format.parse( ys );
                Calendar cal = Calendar.getInstance();
                cal.setTime( date );

                // wichtig, damit die Zeiten richtig sind
                cal.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
                domainValues.add( cal );
                targetValues.add( Double.parseDouble( cols[1] ) );
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
          e.printStackTrace();
        }
        catch( IOException e )
        {
          e.printStackTrace();
        }
        catch( Exception e )
        {
          e.printStackTrace();
        }
        setLoading( false );
        return true;

      }

      @SuppressWarnings("cast")
      public IDataRange<Calendar> getDomainRange( )
      {
        /**
         * Geht nicht wg. ClassCastException (Generics - Bug):
         * 
         * return new ComparableDataRange<GregorianCalendar>( getDomainValues() );
         * 
         * SuppressWarnings-Annotation ist wichtig, sonst löscht Eclipse Code-CleanUp den cast
         */
        return (IDataRange<Calendar>) new ComparableDataRange( getDomainValues() );
      }

      @SuppressWarnings("cast")
      public IDataRange<Number> getTargetRange( )
      {
        /*
         * Geht nicht wg. ClassCastException (Generics - Bug): return (IDataRange<Number>) new ComparableDataRange(
         * targetValues );
         */
        return (IDataRange<Number>) new ComparableDataRange( getTargetValues() );
      }

    };

    URL url = ChartFactoryUtilities.createURLQuietly( getContext(), getParameterContainer().getParameterValue( "url", getId() ) );
    data.setInputURL( url );
    return data;
  }
}
