/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.binding.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * The feature based implementation of {@link IGeoStatus}.
 * 
 * @author Thomas Jung
 */
public class GeoStatus extends Feature_Impl implements IGeoStatus
{
  private enum SEVERITYTYPE
  {
    ok,
    info,
    warning,
    error,
    cancel
  }

  private final IFeatureBindingCollection<IGeoStatus> m_children = new FeatureBindingCollection<>( this, IGeoStatus.class, QNAME_PROP_STATUS_CHILD_MEMBER );

  public GeoStatus( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public IFeatureBindingCollection<IGeoStatus> getChildrenCollection( )
  {
    return m_children;
  }

  @Override
  public org.eclipse.core.runtime.IStatus[] getChildren( )
  {
    return m_children.toArray( new IGeoStatus[m_children.size()] );
  }

  @Override
  public int getCode( )
  {
    return getProperty( QNAME_PROP_STATUS_CODE, Integer.class );
  }

  @Override
  public Throwable getException( )
  {
    // REMARK: we do deserialize the exception from a byte stream which was obtained as string from the gml

    try
    {
      final String encodedString = (String)getProperty( QNAME_PROP_STATUS_EXCEPTION );
      if( encodedString == null || encodedString.isEmpty() )
        return null;

      final byte[] encodedBytes = encodedString.getBytes( "UTF-8" );

      // REMARK: Although this property is defined as 'string' we decode is as base64
      final byte[] bytes = Base64.decodeBase64( encodedBytes );

      final InputStream bis = new ByteArrayInputStream( bytes );
      final ObjectInputStream ois = new ObjectInputStream( bis );
      final Throwable t = (Throwable)ois.readObject();
      ois.close();

      return t;
    }
    catch( final ClassNotFoundException e )
    {
      // FIXME: always happens, as we would need to bundle class loader to be able ot load custom exception from a plugin that is not in our downstream dependencies.
      return null;
    }
    catch( final Throwable e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoDeegreePlugin.getDefault().getLog().log( status );
      return null;
    }
  }

  @Override
  public String getMessage( )
  {
    return getDescription();
  }

  @Override
  public String getPlugin( )
  {
    return getProperty( QNAME_PROP_STATUS_PLUGIN, String.class );
  }

  @Override
  public int getSeverity( )
  {
    final SEVERITYTYPE severityType = getSeverityType();
    switch( severityType )
    {
      case ok:
        return OK;
      case info:
        return INFO;
      case warning:
        return WARNING;
      case error:
        return ERROR;
      case cancel:
        return CANCEL;

      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public boolean isMultiStatus( )
  {
    return m_children.size() > 0;
  }

  @Override
  public boolean isOK( )
  {
    return getSeverityType() == SEVERITYTYPE.ok;
  }

  @Override
  public boolean matches( final int severityMask )
  {
    return (severityMask & getSeverity()) != 0;
  }

  private SEVERITYTYPE getSeverityType( )
  {
    final String value = (String)getProperty( QNAME_PROP_STATUS_SEVERITY );

    return SEVERITYTYPE.valueOf( value );
  }

  @Override
  public void setCode( final int code )
  {
    setProperty( QNAME_PROP_STATUS_CODE, code );
  }

  @Override
  public void setException( final Throwable t )
  {
    // REMARK: we do serialize the exception as a byte stream and write it as string into the gml
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try( ObjectOutputStream oos = new ObjectOutputStream( bos ) )
    {
      oos.writeObject( t );
      oos.close();

      // REMARK: Although this property is defined as 'string' we decode is as base64
      final byte[] bytes = bos.toByteArray();
      final byte[] encodedBytes = Base64.encodeBase64( bytes );

      final String encodedThrowable = new String( encodedBytes, Charsets.UTF_8 );
      setProperty( QNAME_PROP_STATUS_EXCEPTION, encodedThrowable );
    }
    catch( final IOException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoDeegreePlugin.getDefault().getLog().log( status );
    }
  }

  @Override
  public void setMessage( final String message )
  {
    setDescription( message );
  }

  @Override
  public void setPlugin( final String pluginId )
  {
    setProperty( QNAME_PROP_STATUS_PLUGIN, pluginId );
  }

  @Override
  public void setSeverity( final int severity )
  {
    final SEVERITYTYPE severityType = toSeverityType( severity );
    setSeverityType( severityType );
  }

  private SEVERITYTYPE toSeverityType( final int severity )
  {
    switch( severity )
    {
      case OK:
        return SEVERITYTYPE.ok;

      case INFO:
        return SEVERITYTYPE.info;

      case WARNING:
        return SEVERITYTYPE.warning;

      case ERROR:
        return SEVERITYTYPE.error;

      case CANCEL:
        return SEVERITYTYPE.cancel;

      default:
        throw new IllegalArgumentException( "Unknown severity: " + severity );
    }
  }

  private void setSeverityType( final SEVERITYTYPE severityType )
  {
    setProperty( QNAME_PROP_STATUS_SEVERITY, severityType.name() );
  }

  @Override
  public Date getTime( )
  {
    final XMLGregorianCalendar cal = getProperty( QNAME_PROP_STATUS_TIME, XMLGregorianCalendar.class );
    if( cal != null )
      return DateUtilities.toDate( cal );

    return null;
  }

  @Override
  public void setTime( final Date time )
  {
    if( time != null )
    {
      final XMLGregorianCalendar cal = DateUtilities.toXMLGregorianCalendar( time );
      setProperty( QNAME_PROP_STATUS_TIME, cal );
    }
    else
      setProperty( QNAME_PROP_STATUS_TIME, null );
  }
}