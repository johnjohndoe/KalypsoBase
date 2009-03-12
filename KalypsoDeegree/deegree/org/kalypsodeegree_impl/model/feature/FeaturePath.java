package org.kalypsodeegree_impl.model.feature;

/**
 * @author belger
 */
public class FeaturePath
{
  private final String m_fid;
  
  private final String[] m_segments;
  
  private final String m_typename;

  public FeaturePath( final String path )
  {
    if( path.startsWith( "#fid#" ) )
    {
      m_fid = path.substring( "#fid#".length() );
      m_segments = new String[0];
      m_typename = null;
      
      return;
    }

    m_fid = null;
    
    // TODO: match against a regexp
    
    final int i1 = path.indexOf( '[' );
    final int i2 = path.indexOf( ']' );

    if( i1 != -1 && i2 != -1 )
      m_typename = path.substring( i1 + 1, i2 );
    else
      m_typename = null;

    if( path.trim().length() == 0 )
      m_segments = new String[] {};
    else if( i1 != -1 )
      m_segments = path.substring( 0, i1 ).split( "/" );
    else
      m_segments = path.split( "/" );
  }
  
  public int getLength()
  {
    return m_segments.length;
  }
  
  public String getSegment( final int index )
  {
    return m_segments[index];
  }
  
  public String getTypename()
  {
    return m_typename;
  }
  
  public boolean isID()
  {
    return m_fid != null;
  }
  
  public String getID()
  {
    return m_fid;
  }
}
