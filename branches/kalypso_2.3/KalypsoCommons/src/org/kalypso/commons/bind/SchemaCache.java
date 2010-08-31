/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.commons.bind;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.xml.sax.SAXException;

/**
 * Helper class for caching jaxb schemas. Used for validation while reading bound xml.
 * 
 * @author Gernot Belger
 */
public final class SchemaCache
{
  private final SchemaFactory m_factory = SchemaFactory.newInstance( W3C_XML_SCHEMA_NS_URI );

  private final Map<String, Schema> m_cache = new HashMap<String, Schema>();

  private final String m_pluginId;

  private final String m_baseResourcePath;

  public SchemaCache( final String pluginId, final String baseResourcePath )
  {
    m_pluginId = pluginId;
    m_baseResourcePath = baseResourcePath;
  }

  public Schema getSchema( final String relativeSchemaPath )
  {
    final String schemaResourcePath = m_baseResourcePath + relativeSchemaPath;
    final URL schemaUrl = PluginUtilities.findResource( m_pluginId, schemaResourcePath );
    if( schemaUrl == null )
      return null;

    synchronized( m_cache )
    {
      if( !m_cache.containsKey( relativeSchemaPath ) )
      {
        final Schema schema = loadSchema( schemaUrl );
        m_cache.put( relativeSchemaPath, schema );
      }

      return m_cache.get( relativeSchemaPath );
    }
  }

  private Schema loadSchema( final URL schemaUrl )
  {
    try
    {
      return m_factory.newSchema( schemaUrl );
    }
    catch( final SAXException e )
    {
      KalypsoCommonsPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return null;
    }
  }

}
