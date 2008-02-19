package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultBarLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.impl.model.data.ComparableDataRange;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;

/**
 * @author alibu
 */
public class CSVBarLayerProvider<T_domain, T_target> extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @SuppressWarnings( { "unused", "unchecked" })
  public IChartLayer< ? , ? > getLayer( URL context ) throws LayerProviderException
  {
    return new DefaultBarLayer<T_domain, T_target>();
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
  public IDataContainer getDataContainer( ) throws LayerProviderException
  {
    final AbstractDomainIntervalValueFileData<T_domain, T_target> data = new AbstractDomainIntervalValueFileData<T_domain, T_target>()
    {

      @SuppressWarnings("cast")
      @Override
      public boolean openData( )
      {
        // TODO: umschreiben, damit auch urls verwendet werden können
        FileReader fr;
        try
        {
          fr = new FileReader( getInputFile() );

          List<T_domain> domainValues = new ArrayList<T_domain>();
          List<T_domain> domainIntervalStartValues = new ArrayList<T_domain>();
          List<T_domain> domainIntervalEndValues = new ArrayList<T_domain>();
          List<T_target> targetValues = new ArrayList<T_target>();

          BufferedReader br = new BufferedReader( fr );
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
                T_domain domStart = null;
                T_domain domEnd = null;
                T_domain domVal = null;
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

                  domVal = (T_domain) calVal;
                  domStart = (T_domain) calStart;
                  domEnd = (T_domain) calEnd;

                }
                else
                {
                  Number numVal = createNumber( cols[0] );
                  domVal = (T_domain) numVal;
                  domStart = (T_domain) (new Double( numVal.doubleValue() - 0.5 ));
                  domEnd = (T_domain) (new Double( numVal.doubleValue() + 0.5 ));
                }

                domainValues.add( (T_domain) domVal );
                domainIntervalStartValues.add( (T_domain) domStart );
                domainIntervalEndValues.add( (T_domain) domEnd );
                targetValues.add( (T_target) createNumber( cols[1] ) );

              }
              setDomainIntervalEndValues( domainIntervalEndValues );
              setDomainIntervalStartValues( domainIntervalStartValues );
              setDomainValues( domainValues );
              setTargetValues( targetValues );
            }
            count++;

          }
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

      public IDataRange<T_domain> getDomainRange( )
      {
        T_domain[] domainStart = getDomainDataIntervalStart();
        T_domain[] domainEnd = getDomainDataIntervalEnd();
        Object[] merged = new Object[domainStart.length + domainEnd.length];
        for( int i = 0; i < domainStart.length; i++ )
        {
          merged[i] = domainStart[i];
          merged[i + domainStart.length] = domainEnd[i];
        }
        return new ComparableDataRange<T_domain>( (T_domain[]) merged );
      }

      public IDataRange<T_target> getTargetRange( )
      {
        return new ComparableDataRange<T_target>( getTargetValues() );
      }

    };
    data.setInputFile( new File( getParameterContainer().getParameterValue( "url", getLayerType().getId() ) ) );

    return data;
  }

}
