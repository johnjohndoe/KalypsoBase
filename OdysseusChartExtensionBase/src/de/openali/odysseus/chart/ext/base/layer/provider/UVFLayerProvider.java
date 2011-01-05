package de.openali.odysseus.chart.ext.base.layer.provider;

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

import de.openali.odysseus.chart.ext.base.data.AbstractDomainValueFileData;
import de.openali.odysseus.chart.ext.base.layer.DefaultLineLayer;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class UVFLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  @SuppressWarnings({ "unused" })
  public IChartLayer getLayer( final URL context ) throws ConfigurationException
  {
    return new DefaultLineLayer( this, getDataContainer(), getStyleSet().getStyle( "line", ILineStyle.class ), getStyleSet().getStyle( "point", IPointStyle.class ) );
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  public AbstractDomainValueFileData<Calendar, Number> getDataContainer( )
  {
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

          final URL url = getInputURL();
          final InputStream is = url.openStream();
          final InputStreamReader isr = new InputStreamReader( is );

          final BufferedReader br = new BufferedReader( isr );
          String s = "";
          int count = 0;
          final List<Calendar> domainValues = new ArrayList<Calendar>();
          final List<Number> targetValues = new ArrayList<Number>();
          m_format = new SimpleDateFormat( "yyMMddHHmm" );

          while( (s = br.readLine()) != null )
          {
            if( count > 4 )
            {
              final String[] cols = s.split( "  *" );
              // YearString
              if( cols.length >= 2 )
              {
                final String ys = cols[0];

                final Date date = m_format.parse( ys );
                final Calendar cal = Calendar.getInstance();
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
        catch( final FileNotFoundException e )
        {
          e.printStackTrace();
        }
        catch( final IOException e )
        {
          e.printStackTrace();
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
        setLoading( false );
        return true;

      }

      @Override
      @SuppressWarnings("cast")
      public IDataRange<Calendar> getDomainRange( )
      {
        /**
         * Geht nicht wg. ClassCastException (Generics - Bug): return new ComparableDataRange<GregorianCalendar>(
         * getDomainValues() ); SuppressWarnings-Annotation ist wichtig, sonst löscht Eclipse Code-CleanUp den cast
         */
        return (IDataRange<Calendar>) new ComparableDataRange( getDomainValues() );
      }

      @Override
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

    final URL url = ChartFactoryUtilities.createURLQuietly( getContext(), getParameterContainer().getParameterValue( "url", getId() ) );
    data.setInputURL( url );
    return data;
  }
}
