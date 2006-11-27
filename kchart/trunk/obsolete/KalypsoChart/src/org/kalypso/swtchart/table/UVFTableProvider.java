package org.kalypso.swtchart.table;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.kalypso.observation.result.Component;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.ksp.chart.tableconfiguration.ComponentType;
import org.ksp.chart.tableconfiguration.TableType;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class UVFTableProvider extends ObservationTableProvider implements ITableProvider
{


  @Override
  public ITable getTable( )
  {
    return tableFromTupleResult( createObservation() );
  }


  
  public TupleResult createObservation()
  {
    String url = m_tableType.getDataSource().getUrl();
    try
    {

        //IComponents neu erzeugen
        Component comp_date=new Component("date", "Datum", "das Datum", "", "frame_datum", new QName( "Datum" ), null);
        Component comp_pegel=new Component("pegel", "Wasserstand", "der Wasserstand", "cmNN", "frame_pegel",  new QName( "Wasserstand" ), null);
        TupleResult result = new TupleResult(new Component[] {comp_date, comp_pegel});
        
        //Config-Components auswerten
        List<JAXBElement< ? extends ComponentType>> configComponents = m_tableType.getComponent();
        for( JAXBElement< ? extends ComponentType> configComponent : configComponents )
        {
          ComponentType comp= configComponent.getValue();
          comp.getName();
        }
        
        
//      TODO: umschreiben, damit auch urls verwendet werden können
        FileReader fr=new FileReader(url);
        
        BufferedReader br=new BufferedReader(fr);
        String s="";
        int linecount=0;
        while ((s=br.readLine())!=null)
        {
            if (linecount>4)
            {
              IRecord record = result.createRecord();
              String[] cols=s.split( "  *");
              //YearString
              if (cols.length>=2)
              {
                String ys=cols[0];
                //Datum zerpflücken (Bsp: 0510190530)
                //TODO: Auslagern in Toolbox-ähnliche Klasse
                int year=2000+Integer.parseInt( ys.substring( 0, 2 )) ;
                int month=Integer.parseInt( ys.substring( 2, 4 ))-1;
                int date=Integer.parseInt( ys.substring( 4, 6 )) ;
                int hour=Integer.parseInt( ys.substring( 6, 8 )) ;
                int minute=Integer.parseInt( ys.substring( 8, 10 )) ;
                Calendar cal=GregorianCalendar.getInstance();
                cal.set( year, month, date, hour, minute );
                XMLGregorianCalendar xmlcal=new XMLGregorianCalendarImpl((GregorianCalendar) cal);
                
                record.setValue( comp_date, xmlcal);
                record.setValue( comp_pegel, Integer.parseInt( cols[1]));
                result.add( record );
              }
            }
            linecount++;
        }
        return result;
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
    return null;
    
    
  }

}
