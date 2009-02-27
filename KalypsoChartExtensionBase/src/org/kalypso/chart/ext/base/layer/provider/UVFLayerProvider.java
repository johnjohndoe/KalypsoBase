package org.kalypso.chart.ext.base.layer.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.kalypso.chart.ext.base.data.AbstractDomainValueFileData;
import org.kalypso.chart.ext.base.layer.DefaultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class UVFLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @SuppressWarnings( { "unchecked", "unused" })
  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer<Calendar, Number> icl = null;

    final String domainAxisId = getLayerType().getMapper().getDomainAxisRef().getRef();
    final String targetAxisId = getLayerType().getMapper().getTargetAxisRef().getRef();

    final IAxis<Calendar> domAxis = (IAxis<Calendar>) getChartModel().getMapperRegistry().getAxis( domainAxisId );
    final IAxis<Number> valAxis = (IAxis<Number>) getChartModel().getMapperRegistry().getAxis( targetAxisId );

    final AbstractDomainValueFileData<Calendar, Number> data = new AbstractDomainValueFileData<Calendar, Number>()
    {

      private SimpleDateFormat m_format;

      @Override
      public boolean openData( )
      {
        setLoading( true );
        try
        {
          FileReader fr = new FileReader( getInputFile() );

          BufferedReader br = new BufferedReader( fr );
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

      public IDataRange<Calendar> getDomainRange( )
      {
        return new ComparableDataRange<Calendar>( getDomainValues() );
      }

      public IDataRange<Number> getTargetRange( )
      {
        return new ComparableDataRange<Number>( getTargetValues() );
      }

    };
    data.setInputFile( new File( getParameterContainer().getParameterValue( "url", getLayerType().getId() ) ) );

    icl = new DefaultLineLayer<Calendar, Number>( data, domAxis, valAxis );
    icl.setTitle( getLayerType().getTitle() );

    return icl;
  }

}
