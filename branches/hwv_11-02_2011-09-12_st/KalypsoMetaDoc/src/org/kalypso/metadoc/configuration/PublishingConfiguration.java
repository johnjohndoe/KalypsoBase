/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.metadoc.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;

/**
 * Default implementation of the IPublishingConfiguration interface
 * 
 * @author schlienger
 */
public class PublishingConfiguration implements IPublishingConfiguration
{
  private final Configuration m_conf;

  private final List<IConfigurationListener> m_listeners;

  public PublishingConfiguration( final Configuration conf )
  {
    m_conf = conf;
    m_listeners = new ArrayList<IConfigurationListener>( 10 );
  }

  private void fireConfigurationChanged( final String key )
  {
    final IConfigurationListener[] listeners = m_listeners.toArray( new IConfigurationListener[m_listeners.size()] );
    for( final IConfigurationListener listener : listeners )
    {
      try
      {
        listener.configurationChanged( this, key );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
    }
  }

  /**
   * @see org.kalypso.metadoc.configuration.IPublishingConfiguration#addListener(org.kalypso.metadoc.configuration.IConfigurationListener)
   */
  @Override
  public void addListener( final IConfigurationListener listener )
  {
    m_listeners.add( listener );
  }

  /**
   * @see org.kalypso.metadoc.configuration.IPublishingConfiguration#removeListener(org.kalypso.metadoc.configuration.IConfigurationListener)
   */
  @Override
  public void removeListener( final IConfigurationListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public void addProperty( final String key, final Object arg1 )
  {
    m_conf.addProperty( key, arg1 );

    fireConfigurationChanged( key );
  }

  @Override
  public void clear( )
  {
    m_conf.clear();

    fireConfigurationChanged( null );
  }

  @Override
  public void clearProperty( final String key )
  {
    m_conf.clearProperty( key );

    fireConfigurationChanged( key );
  }

  @Override
  public boolean containsKey( final String arg0 )
  {
    return m_conf.containsKey( arg0 );
  }

  @Override
  public boolean equals( final Object obj )
  {
    return m_conf.equals( obj );
  }

  @Override
  public BigDecimal getBigDecimal( final String arg0 )
  {
    return m_conf.getBigDecimal( arg0 );
  }

  @Override
  public BigDecimal getBigDecimal( final String arg0, final BigDecimal arg1 )
  {
    return m_conf.getBigDecimal( arg0, arg1 );
  }

  @Override
  public BigInteger getBigInteger( final String arg0 )
  {
    return m_conf.getBigInteger( arg0 );
  }

  @Override
  public BigInteger getBigInteger( final String arg0, final BigInteger arg1 )
  {
    return m_conf.getBigInteger( arg0, arg1 );
  }

  @Override
  public boolean getBoolean( final String arg0 )
  {
    return m_conf.getBoolean( arg0 );
  }

  @Override
  public boolean getBoolean( final String arg0, final boolean arg1 )
  {
    return m_conf.getBoolean( arg0, arg1 );
  }

  @Override
  public Boolean getBoolean( final String arg0, final Boolean arg1 ) throws NoClassDefFoundError
  {
    return m_conf.getBoolean( arg0, arg1 );
  }

  @Override
  public byte getByte( final String arg0 )
  {
    return m_conf.getByte( arg0 );
  }

  @Override
  public byte getByte( final String arg0, final byte arg1 )
  {
    return m_conf.getByte( arg0, arg1 );
  }

  @Override
  public Byte getByte( final String arg0, final Byte arg1 )
  {
    return m_conf.getByte( arg0, arg1 );
  }

  @Override
  public double getDouble( final String arg0 )
  {
    return m_conf.getDouble( arg0 );
  }

  @Override
  public double getDouble( final String arg0, final double arg1 )
  {
    return m_conf.getDouble( arg0, arg1 );
  }

  @Override
  public Double getDouble( final String arg0, final Double arg1 )
  {
    return m_conf.getDouble( arg0, arg1 );
  }

  @Override
  public float getFloat( final String arg0 )
  {
    return m_conf.getFloat( arg0 );
  }

  @Override
  public float getFloat( final String arg0, final float arg1 )
  {
    return m_conf.getFloat( arg0, arg1 );
  }

  @Override
  public Float getFloat( final String arg0, final Float arg1 )
  {
    return m_conf.getFloat( arg0, arg1 );
  }

  @Override
  public int getInt( final String arg0 )
  {
    return m_conf.getInt( arg0 );
  }

  @Override
  public int getInt( final String arg0, final int arg1 )
  {
    return m_conf.getInt( arg0, arg1 );
  }

  @Override
  public Integer getInteger( final String arg0, final Integer arg1 )
  {
    return m_conf.getInteger( arg0, arg1 );
  }

  @Override
  public Iterator< ? > getKeys( )
  {
    return m_conf.getKeys();
  }

  @Override
  public Iterator< ? > getKeys( final String arg0 )
  {
    return m_conf.getKeys( arg0 );
  }

  @Override
  public List< ? > getList( final String arg0 )
  {
    return m_conf.getList( arg0 );
  }

  @Override
  public List< ? > getList( final String arg0, @SuppressWarnings("rawtypes") final List arg1 )
  {
    return m_conf.getList( arg0, arg1 );
  }

  @Override
  public long getLong( final String arg0 )
  {
    return m_conf.getLong( arg0 );
  }

  @Override
  public Long getLong( final String arg0, final Long arg1 )
  {
    return m_conf.getLong( arg0, arg1 );
  }

  @Override
  public long getLong( final String arg0, final long arg1 )
  {
    return m_conf.getLong( arg0, arg1 );
  }

  @Override
  public Properties getProperties( final String arg0 )
  {
    return m_conf.getProperties( arg0 );
  }

  @Override
  public Object getProperty( final String arg0 )
  {
    return m_conf.getProperty( arg0 );
  }

  @Override
  public short getShort( final String arg0 )
  {
    return m_conf.getShort( arg0 );
  }

  @Override
  public Short getShort( final String arg0, final Short arg1 )
  {
    return m_conf.getShort( arg0, arg1 );
  }

  @Override
  public short getShort( final String arg0, final short arg1 )
  {
    return m_conf.getShort( arg0, arg1 );
  }

  @Override
  public String getString( final String arg0 )
  {
    return m_conf.getString( arg0 );
  }

  @Override
  public String getString( final String arg0, final String arg1 )
  {
    return m_conf.getString( arg0, arg1 );
  }

  @Override
  public String[] getStringArray( final String arg0 )
  {
    return m_conf.getStringArray( arg0 );
  }

  @Override
  public int hashCode( )
  {
    return m_conf.hashCode();
  }

  @Override
  public boolean isEmpty( )
  {
    return m_conf.isEmpty();
  }

  @Override
  public void setProperty( final String key, final Object arg1 )
  {
    m_conf.setProperty( key, arg1 );

    fireConfigurationChanged( key );
  }

  @Override
  public Configuration subset( final String prefix )
  {
    return m_conf.subset( prefix );
  }

  @Override
  public String toString( )
  {
    return m_conf.toString();
  }
}
