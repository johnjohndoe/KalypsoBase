package org.kalypso.swtchart.table;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.kalypso.observation.result.Component;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class UVFTableProvider extends ObservationTableProvider implements ITableProvider
{

  @Override
  public ITable getTable( )
  {
    return tableFromTupleResult( createObservation() );
  }

  public TupleResult createObservation( )
  {
    final String id = m_tableType.getName();

    final ParameterHelper pm = new ParameterHelper();
    pm.addParameters( m_tableType.getParameters(), id );

    final String url = pm.getParameterValue( "url", id, "" );
    try
    {

      // IComponents neu erzeugen
      final Component comp_date = new Component( "date", "Datum", "das Datum", "", "frame_datum", new QName( "Datum" ), null, null );
      final Component comp_pegel = new Component( "pegel", "Wasserstand", "der Wasserstand", "cmNN", "frame_pegel", new QName( "Wasserstand" ), null, null );
      final TupleResult result = new TupleResult( new Component[] { comp_date, comp_pegel } );

      // TODO: umschreiben, damit auch urls verwendet werden können
      final FileReader fr = new FileReader( url );

      final BufferedReader br = new BufferedReader( fr );
      String s = "";
      int linecount = 0;
      while( (s = br.readLine()) != null )
      {
        if( linecount > 4 )
        {
          final IRecord record = result.createRecord();
          final String[] cols = s.split( "  *" );
          // YearString
          if( cols.length >= 2 )
          {
            final String ys = cols[0];
            // Datum zerpflücken (Bsp: 0510190530)
            // TODO: Auslagern in Toolbox-ähnliche Klasse
            final int year = 2000 + Integer.parseInt( ys.substring( 0, 2 ) );
            final int month = Integer.parseInt( ys.substring( 2, 4 ) ) - 1;
            final int date = Integer.parseInt( ys.substring( 4, 6 ) );
            final int hour = Integer.parseInt( ys.substring( 6, 8 ) );
            final int minute = Integer.parseInt( ys.substring( 8, 10 ) );
            final Calendar cal = Calendar.getInstance();
            cal.set( year, month, date, hour, minute );
            final XMLGregorianCalendar xmlcal = new XMLGregorianCalendarImpl( (GregorianCalendar) cal );

            record.setValue( comp_date, xmlcal );
            record.setValue( comp_pegel, Integer.parseInt( cols[1] ) );
            result.add( record );
          }
        }
        linecount++;
      }
      return result;
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
    return null;
  }

}
