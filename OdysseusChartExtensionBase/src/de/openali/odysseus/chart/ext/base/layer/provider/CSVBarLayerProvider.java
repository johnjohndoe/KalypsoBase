package de.openali.odysseus.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueFileData;
import de.openali.odysseus.chart.ext.base.layer.DefaultBarLayer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public class CSVBarLayerProvider extends AbstractLayerProvider
{

  private final String ROLE_BAR_STYLE = "bar";

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new DefaultBarLayer( this, getDataContainer(), getStyleSet().getStyle( ROLE_BAR_STYLE, IAreaStyle.class ) );
  }

  private Calendar createDate( final String s )
  {
    // Datum zerpflücken (Bsp: 0510190530)
    // TODO: Auslagern in Toolbox-ähnliche Klasse
    final int year = 2000 + Integer.parseInt( s.substring( 0, 2 ) );
    final int month = Integer.parseInt( s.substring( 2, 4 ) ) - 1;
    final int day = Integer.parseInt( s.substring( 4, 6 ) );
    final int hour = Integer.parseInt( s.substring( 6, 8 ) );
    final int minute = Integer.parseInt( s.substring( 8, 10 ) );

    // echte Daten aus EiongabeDatei
    final Calendar calData = Calendar.getInstance();
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

  private Number createNumber( final String s )
  {
    return Double.parseDouble( s );
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  protected AbstractDomainIntervalValueFileData getDataContainer( )
  {
    final AbstractDomainIntervalValueFileData data = new AbstractDomainIntervalValueFileData()
    {

      @SuppressWarnings({ "cast", "unchecked" })
      @Override
      public boolean openData( )
      {
        // TODO: umschreiben, damit auch urls verwendet werden können
        try
        {
          final URL url = getInputURL();
          final InputStream is = url.openStream();
          final InputStreamReader isr = new InputStreamReader( is );

          final List<Object> domainValues = new ArrayList<Object>();
          final List<Object> domainIntervalStartValues = new ArrayList<Object>();
          final List<Object> domainIntervalEndValues = new ArrayList<Object>();
          final List<Object> targetValues = new ArrayList<Object>();

          final BufferedReader br = new BufferedReader( isr );
          String s = "";
          int count = 0;
          String domType = null;

          while( (s = br.readLine()) != null && s.trim() != "" )
          {
            final String[] cols = s.split( "  *" );
            // erste Zeile: Überschrift
            if( count == 0 )
            {
              domType = cols[0];
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
                  final Calendar calVal = createDate( cols[0] );

                  // Startwert für Interval
                  final Calendar calStart = (Calendar) calVal.clone();
                  calStart.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
                  calStart.set( Calendar.MINUTE, 0 );
                  calStart.set( Calendar.HOUR_OF_DAY, 0 );

                  // Endwert für Interval
                  final Calendar calEnd = (Calendar) calStart.clone();
                  calEnd.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
                  // wichtig, damit die Zeiten richtig sind

                  calEnd.add( Calendar.DAY_OF_MONTH, +1 );

                  domVal = (Object) calVal;
                  domStart = (Object) calStart;
                  domEnd = (Object) calEnd;

                }
                else
                {
                  final Number numVal = createNumber( cols[0] );
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
        catch( final FileNotFoundException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final NumberFormatException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        return true;

      }

      @Override
      public IDataRange getDomainRange( )
      {
        final Object[] domainStart = getDomainDataIntervalStart();
        final Object[] domainEnd = getDomainDataIntervalEnd();
        final Object[] merged = new Object[domainStart.length + domainEnd.length];
        for( int i = 0; i < domainStart.length; i++ )
        {
          merged[i] = domainStart[i];
          merged[i + domainStart.length] = domainEnd[i];
        }
        return new ComparableDataRange<Object>( merged );
      }

      @Override
      public IDataRange<Object> getTargetRange( )
      {
        return new ComparableDataRange<Object>( getTargetValues() );
      }

    };

    final URL url = ChartFactoryUtilities.createURLQuietly( getContext(), getParameterContainer().getParameterValue( "url", getId() ) );
    data.setInputURL( url );

    return data;
  }

}
