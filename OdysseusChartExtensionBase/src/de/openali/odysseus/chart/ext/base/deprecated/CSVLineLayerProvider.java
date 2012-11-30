package de.openali.odysseus.chart.ext.base.deprecated;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainValueFileData;
import de.openali.odysseus.chart.ext.base.layer.DefaultLineLayer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author alibu
 * @deprecated not used
 */
@Deprecated
public class CSVLineLayerProvider extends AbstractLayerProvider
{
  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new DefaultLineLayer( this, getDataContainer(), getStyleSet());
  }

  public AbstractDomainValueFileData getDataContainer( )
  {
    final AbstractDomainValueFileData data = new AbstractDomainValueFileData()
    {
      @SuppressWarnings("unchecked")
      @Override
      public boolean openData( )
      {
        try
        {

          final InputStream is = getInputURL().openStream();
          final InputStreamReader isr = new InputStreamReader( is );

          final BufferedReader br = new BufferedReader( isr );
          String s = ""; //$NON-NLS-1$
          int count = 0;
          final List<Object> domainValues = new ArrayList<>();
          final List<Object> targetValues = new ArrayList<>();

          while( (s = br.readLine()) != null )
          {
            final String[] cols = s.split( "  *" ); //$NON-NLS-1$
            // YearString
            if( cols.length >= 2 )
            {
              try
              {
                domainValues.add( new Double( cols[0].trim() ) );
                targetValues.add( new Double( cols[1].trim() ) );
              }
              catch( final NumberFormatException e )
              {
                Logger.logWarning( Logger.TOPIC_LOG_DATA, getInputURL().toString() + ": Line " + count + " could not be parsed: \n" + s ); //$NON-NLS-1$ //$NON-NLS-2$
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
        catch( final FileNotFoundException e )
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
        return DataRange.createFromComparable( getDomainValues() );
      }

      @Override
      public IDataRange getTargetRange( )
      {
        return DataRange.createFromComparable( getTargetValues() );
      }

    };
    final String url = getParameterContainer().getParameterValue( "url", getId() ); //$NON-NLS-1$
    try
    {
      data.setInputURL( new URL( url ) );
    }
    catch( final MalformedURLException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return data;
  }
}
