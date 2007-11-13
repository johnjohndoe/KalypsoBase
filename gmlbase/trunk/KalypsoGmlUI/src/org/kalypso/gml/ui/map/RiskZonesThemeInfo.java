package org.kalypso.gml.ui.map;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeInfo;
import org.kalypsodeegree.model.geometry.GM_Position;

public class RiskZonesThemeInfo extends CoverageThemeInfo implements IKalypsoThemeInfo
{
  public static final String DEFAULT_FORMAT_STRING = "Zone: %s";

  private Map<Double, String> m_riskZonesMap;

  /**
   * @see org.kalypso.gml.ui.map.CoverageThemeInfo#init(org.kalypso.ogc.gml.IKalypsoTheme, java.util.Properties)
   */
  @Override
  public void init( IKalypsoTheme theme, Properties props )
  {
    super.init( theme, props );
    m_formatString = props.getProperty( PROP_FORMAT, DEFAULT_FORMAT_STRING );
    m_riskZonesMap = new HashMap<Double, String>();
    m_riskZonesMap.put( 1.0, "m‰ﬂige Betroffenheit, bebaute Fl‰chen" );
    m_riskZonesMap.put( 2.0, "mittlere Betroffenheit, bebaute Fl‰chen" );
    m_riskZonesMap.put( 3.0, "hohe Betroffenheit, bebaute Fl‰chen" );
    m_riskZonesMap.put( 4.0, "m‰ﬂige Betroffenheit, Freifl‰chen" );
    m_riskZonesMap.put( 5.0, "mittlere Betroffenheit, Freifl‰chen" );
    m_riskZonesMap.put( 6.0, "hohe Betroffenheit, Freifl‰chen" );
  }

  /**
   * @see org.kalypso.gml.ui.map.CoverageThemeInfo#appendQuickInfo(java.util.Formatter,
   *      org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  public void appendQuickInfo( final Formatter formatter, final GM_Position pos )
  {
    try
    {
      final Double value = getValue( pos );
      if( value == null )
        return;
      formatter.format( getFormatString(), m_riskZonesMap.get( value ) );
    }
    catch( Exception e )
    {
      e.printStackTrace();
      formatter.format( "Fehler: %s%n", e.toString() );
    }
  }
  
}
