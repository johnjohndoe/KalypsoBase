package org.kalypso.ogc.gml.serialize;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

public final class CsvWriterVisitor implements FeatureVisitor
{
  private final PrintWriter m_writer;

  private final Map<String, String> m_props;

  private final String m_delemiter;

  public CsvWriterVisitor( final PrintWriter writer, final Map<String, String> properties, final String delemiter )
  {
    m_writer = writer;
    m_props = properties;
    m_delemiter = delemiter;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  public boolean visit( final Feature f )
  {
    for( final Iterator<Entry<String, String>> propIt = m_props.entrySet().iterator(); propIt.hasNext(); )
    {
      final Map.Entry<String, String> entry = propIt.next();
      final String prop = entry.getKey();
      final String def = entry.getValue();

      Object property = null;
      try
      {
        property = f.getProperty( prop );
      }
      catch( final IllegalArgumentException e )
      {
        // @hack - if property not exists catch Exception and continue with processing
// e.printStackTrace();
      }

      m_writer.print( property == null ? def : propertyToString( property ) );

      if( propIt.hasNext() )
        m_writer.print( m_delemiter );
    }

    m_writer.println();

    return true;
  }

  private String propertyToString( final Object property )
  {
    if( property instanceof Double )
      return String.format( Locale.US, "%f", property ); //$NON-NLS-1$

    return property.toString();
  }
}