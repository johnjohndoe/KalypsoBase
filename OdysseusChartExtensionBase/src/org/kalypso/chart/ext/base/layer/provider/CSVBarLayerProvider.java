package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultBarLayer;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author alibu
 */
public class CSVBarLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @SuppressWarnings( { "unused", "unchecked" })
  public IChartLayer getLayer( URL context ) throws ConfigurationException
  {
    return new DefaultBarLayer();
  }

  private Calendar createDate( String s )
  {
    // Datum zerpflücken (Bsp: 0510190530)
    // TODO: Auslagern in Toolbox-ähnliche Klasse
    int year = 2000 + Integer.parseInt( s.substring( 0, 2 ) );
    int month = Integer.parseInt( s.substring( 2, 4 ) ) - 1;
    int day = Integer.parseInt( s.substring( 4, 6 ) );
    int hour = Integer.parseInt( s.substring( 6, 8 ) );
    int minute = Integer.parseInt( s.substring( 8, 10 ) );

    // echte Daten aus EiongabeDatei
    Calendar calData = Calendar.getInstance();
    calData.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
    calData.set( Calendar.YEAR, year );
    calData.set( Calendar.MONTH, month );
    calData.set( Calendar.DAY_OF_MONTH, day );
    calData.set( Calendar.HOUR_OF_DAY, hour );
    calData.set( Calendar.MINUTE, minute );
    calData.set( Calendar.SECOND, 0 );
    calData.set( Calendar.MILLISECOND, 0 );
    return calData;
  }

  private Number createNumber( String s )
  {
    return Double.parseDouble( s );
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  public IDataContainer getDataContainer( ) throws ConfigurationException
  {
    final AbstractDomainIntervalValueFileData data = new AbstractDomainIntervalValueFileData()
    {

      @SuppressWarnings( { "cast", "unchecked" })
      @Override
      public boolean openData( )
      {
        // TODO: umschreiben, damit auch urls verwendet werden können
        FileReader fr;
        try
        {
          URL url = getInputURL();
          InputStream is = url.openStream();
          InputStreamReader isr = new InputStreamReader( is );

          List<Object> domainValues = new ArrayList<Object>();
          List<Object> domainIntervalStartValues = new ArrayList<Object>();
          List<Object> domainIntervalEndValues = new ArrayList<Object>();
          List<Object> targetValues = new ArrayList<Object>();

          BufferedReader br = new BufferedReader( isr );
          String s = "";
          int count = 0;
          String domType = null;
          String targetType = null;

          while( (s = br.readLine()) != null && s.trim() != "" )
          {
            String[] cols = s.split( "  *" );
            // erste Zeile: Überschrift
            if( count == 0 )
            {
              domType = cols[0];
              targetType = cols[1];
            }
            else
            {

              if( cols.length >= 2 )
              {
                Object domStart = null;
                Object domEnd = null;
                Object domVal = null;
                if( domType.equals( "DATE" ) )
                {
                  Calendar calVal = createDate( cols[0] );

                  // Startwert für Interval
                  Calendar calStart = (Calendar) calVal.clone();
                  calStart.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
                  calStart.set( Calendar.MINUTE, 0 );
                  calStart.set( Calendar.HOUR_OF_DAY, 0 );

                  // Endwert für Interval
                  Calendar calEnd = (Calendar) calStart.clone();
                  calEnd.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
                  // wichtig, damit die Zeiten richtig sind

                  calEnd.add( Calendar.DAY_OF_MONTH, +1 );

                  domVal = (Object) calVal;
                  domStart = (Object) calStart;
                  domEnd = (Object) calEnd;

                }
                else
                {
                  Number numVal = createNumber( cols[0] );
                  domVal = numVal;
                  domStart = new Double( numVal.doubleValue() - 0.5 );
                  domEnd = new Double( numVal.doubleValue() + 0.5 );
                }

                domainValues.add( (Object) domVal );
                domainIntervalStartValues.add( domStart );
                domainIntervalEndValues.add( domEnd );
                targetValues.add( createNumber( cols[1] ) );

              }
              setDomainIntervalEndValues( domainIntervalEndValues );
              setDomainIntervalStartValues( domainIntervalStartValues );
              setDomainValues( domainValues );
              setTargetValues( targetValues );
            }
            count++;

          }
          br.close();
          isr.close();
          is.close();
        }
        catch( FileNotFoundException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( NumberFormatException e )
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
        Object[] domainStart = getDomainDataIntervalStart();
        Object[] domainEnd = getDomainDataIntervalEnd();
        Object[] merged = new Object[domainStart.length + domainEnd.length];
        for( int i = 0; i < domainStart.length; i++ )
        {
          merged[i] = domainStart[i];
          merged[i + domainStart.length] = domainEnd[i];
        }
        return new ComparableDataRange<Object>( merged );
      }

      public IDataRange<Object> getTargetRange( )
      {
        return new ComparableDataRange<Object>( getTargetValues() );
      }

    };

    URL url = ChartFactoryUtilities.createURLQuietly( getContext(), getParameterContainer().getParameterValue( "url", getId() ) );
    data.setInputURL( url );

    return data;
  }

}
