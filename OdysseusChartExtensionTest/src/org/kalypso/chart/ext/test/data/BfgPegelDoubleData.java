package org.kalypso.chart.ext.test.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import de.openali.odysseus.chart.ext.base.data.AbstractDomainValueData;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;

/**
 * data class for loading gauge time series from hochwasser-nrw server
 * 
 * @author alibu
 * @param <Calendar>
 * @param <Number>
 */

public class BfgPegelDoubleData extends AbstractDomainValueData<Double, Double>
{

  private final URL m_url;

  private final SimpleDateFormat m_dateFormat;

  /**
   * parameter names are similar to those in HTTP-GET-request
   * 
   * @param url
   *            URL der Pegeldatei
   * 
   */
  public BfgPegelDoubleData( URL url )
  {
    m_url = url;

    m_dateFormat = new SimpleDateFormat( "dd.MM.yyyy;HH:mm" );
  }

  @Override
  public boolean openData( )
  {
    setLoading( true );
    InputStream inputStream = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try
    {
      inputStream = m_url.openStream();

      isr = new InputStreamReader( inputStream );

      br = new BufferedReader( isr );
      String s = "";
      int count = 0;
      final List<Double> domainValues = new ArrayList<Double>();
      final List<Double> targetValues = new ArrayList<Double>();

      while( (s = br.readLine()) != null && !s.trim().equals( "" ) )
      {
        if( count >= 1 )
        {
          final String[] cols = s.split( ";" );
          // YearString
          if( cols.length >= 3 )
          {
            final String ys = cols[0] + ";" + cols[1];
            final Date date = m_dateFormat.parse( ys );
            domainValues.add( new Double( date.getTime() ) );
            targetValues.add( Double.parseDouble( cols[2] ) );
          }
        }
        count++;
      }
      setDomainValues( domainValues );
      setTargetValues( targetValues );
    }
    catch( final FileNotFoundException e )
    {
      e.printStackTrace();
    }
    catch( final ConnectException e )
    {
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "cannot connect to " + m_url.getHost() + "; please check proxy settings" );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      // Streams und Reader schliessen
      if( br != null )
      {
        try
        {
          br.close();
        }
        catch( final IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      if( isr != null )
      {
        try
        {
          isr.close();
        }
        catch( final IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      if( inputStream != null )
      {
        try
        {
          inputStream.close();
        }
        catch( final IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    setLoading( false );
    return true;
  }

  public IDataRange<Double> getDomainRange( )
  {
    return new ComparableDataRange<Double>( getDomainValues() );
  }

  public IDataRange<Double> getTargetRange( )
  {
    Double[] targetValues = getTargetValues();
    return new ComparableDataRange<Double>( targetValues );
  }

  @Override
  public Double[] getTargetValues( )
  {
    Object[] vals = super.getTargetValues();
    List<Double> list = new ArrayList<Double>();
    for( Object val : vals )
    {
      list.add( (Double) val );
    }
    return (Double[]) list.toArray( new Double[] {} );
  }

  @Override
  public Double[] getDomainValues( )
  {
    Object[] vals = super.getDomainValues();
    List<Double> list = new ArrayList<Double>();
    for( Object val : vals )
    {
      list.add( (Double) val );
    }
    return (Double[]) list.toArray( new Double[] {} );
  }
}
