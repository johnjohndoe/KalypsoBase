/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gmlschema;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.lang.MultiException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class finds and loads a GML schema, keeping a local schema cache.
 * 
 * @author Andreas von Doemming
 * @author Felipe Maximino - Refaktoring
 */
public class GMLSchemaLoaderWithLocalCache
{
  private final Map<String, URL> m_schemaLocations;

  /**
   * Performance: used to speedup lookup of gml-schemata; we assume, that schemata do not change while loading one GML.
   * Speedup is approximately 15%.
   */
  private final Map<String, IGMLSchema> m_localSchemaCache;

  /**
   * version of the GML schema being loaded.
   */
  private String m_version;

  public GMLSchemaLoaderWithLocalCache( )
  {
    this( null, null );
  }

  public GMLSchemaLoaderWithLocalCache( final Map<String, URL> schemaLocations, final Map<String, IGMLSchema> preFetchedSchemas )
  {
    m_schemaLocations = new HashMap<String, URL>();
    if( schemaLocations != null )
    {
      m_schemaLocations.putAll( schemaLocations );
    }

    m_localSchemaCache = new HashMap<String, IGMLSchema>();
    if( preFetchedSchemas != null )
    {
      m_localSchemaCache.putAll( preFetchedSchemas );
    }

    m_version = null;
  }

  /**
   * Finds a schema with the given namespace.
   * <p>
   * This method will look in the local schema cache at first.
   * 
   * @param namespace
   *          - the schema namespace.
   */
  public IGMLSchema findSchema( final String namespace ) throws SAXParseException
  {
    final IGMLSchema locallyCachedSchema = m_localSchemaCache.get( namespace );
    if( locallyCachedSchema != null )
      return locallyCachedSchema;

    try
    {
      GMLSchema schema = loadSchemaFromCatalog( namespace );
      if( schema == null )
      {
        final URL locationHint = m_schemaLocations.get( namespace );
        schema = GMLSchemaFactory.createGMLSchema( m_version, locationHint );
      }

      if( m_version == null )
      {
        m_version = schema.getGMLVersion();
      }

      m_localSchemaCache.put( namespace, schema );

      return schema;
    }
    catch( final GMLSchemaException e )
    {
      throw new SAXParseException( "Unknown schema for namespace: " + namespace, null );
    }
  }

  private GMLSchema loadSchemaFromCatalog( final String namespace ) throws GMLSchemaException
  {
    return loadSchemaFromCatalog( namespace, m_version, null );
  }

  private GMLSchema loadSchemaFromCatalog( final String gmlVersion, final URL schemaLocation )
  {
    final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
    final GMLSchema schema = schemaCatalog.getSchema( gmlVersion, schemaLocation );

    return schema;
  }

  private GMLSchema loadSchemaFromCatalog( final String namespace, final String gmlVersion, final URL schemaLocation ) throws GMLSchemaException
  {
    final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();

    return schemaCatalog.getSchema( namespace, gmlVersion, schemaLocation );
  }

  /**
   * Loads a main schema and also all (via xmlns) references schemas.
   * <p>
   * All schema loaded in this method are stored into a map (namespace -> schema), .
   */
  public IGMLSchema loadMainSchema( final String uri, final Attributes atts, final String schemaLocationString, final URL locationHint, final URL context ) throws SAXException
  {
    // the main schema is the schema defining the root elements namespace
    // REMARK: schemaLocationHint only used for main schema
    final GMLSchema gmlSchema = loadSchema( uri, null, schemaLocationString, locationHint, context );
    final String version = gmlSchema == null ? null : gmlSchema.getGMLVersion();
    if( gmlSchema != null )
      m_localSchemaCache.put( uri, gmlSchema );

    // Also force all dependent schemas (i.e. for which xmlns entries exist) as dependency into
    // the main schema.
    // This allows to introduce necessary schemata (for example which introduce new elements
    // via substitution).
    final int attLength = atts.getLength();
    for( int i = 0; i < attLength; i++ )
    {
      // STRANGE: shouldn't it work like this?
      // if( NS.XML_PREFIX_DEFINITION_XMLNS.equals( atts.getURI( i ) ) )
      // But atts.getURI gives empty string for xmlns entries.
      // so we ask for the qname
      final String qname = atts.getQName( i );
      if( qname != null && qname.startsWith( "xmlns:" ) )
      {
        final String xmlnsUri = atts.getValue( i );
        // HM: are there any other possible namespaces we do NOT want to load?
        if( !xmlnsUri.equals( NS.XSD ) )
        {
          // make sure that all dependent schemas are loaded
          final GMLSchema additionalSchema = loadSchema( xmlnsUri, version, schemaLocationString, locationHint, context );
          if( gmlSchema != null )
          {
            gmlSchema.addAdditionalSchema( additionalSchema );
            m_localSchemaCache.put( xmlnsUri, gmlSchema );
          }
        }
      }
    }

    return gmlSchema;
  }

  private GMLSchema loadSchema( final String uri, final String gmlVersion, final String schemaLocationString, final URL schemaLocationHint, final URL context ) throws SAXException
  {
    final MultiException schemaNotFoundExceptions = new MultiException();

    GMLSchema schema = null;
    try
    {
      // 1. try : use hint
      if( schemaLocationHint != null )
      {
        schema = loadSchemaFromCatalog( null, schemaLocationHint );
      }

      // 2. try : from schema cache: we only use uri here, so locally loaded schemas will not be stored in the cache.
      // This is necessary for WFS
      if( schema == null )
      {
        schema = loadSchemaFromCatalog( uri, gmlVersion, null );
      }
    }
    catch( final GMLSchemaException e )
    {
      /*
       * throw it, because the following SaxException eats the inner exception and we need to log this exception
       */
      if( schema == null )
      {
        schemaNotFoundExceptions.addException( new SAXException( "Schema unknown. Could not load schema with namespace: " + uri + " (schemaLocationHint was " + schemaLocationHint
            + ") (schemaLocation was " + schemaLocationString + "): ", e ) );
      }
    }

    // 3. try: if we have a schemaLocation, load from there bt: do not put into cache!
    if( schema == null )
    {
      schema = loadFromSchemaLocation( uri, schemaLocationString, gmlVersion, context );
    }

    if( schema == null )
    {
      if( schemaNotFoundExceptions.isEmpty() )
        throw new SAXException( "Schema unknown. Could not load schema with namespace: " + uri + " (schemaLocationHint was " + schemaLocationHint + ") (schemaLocation was " + schemaLocationString
            + ")" );
      else
        throw new SAXException( schemaNotFoundExceptions );
    }

    return schema;
  }

  private GMLSchema loadFromSchemaLocation( final String uri, final String schemaLocationString, final String gmlVersion, final URL context ) throws SAXException
  {
    final Map<String, URL> namespaces = GMLSchemaUtilities.parseSchemaLocation( schemaLocationString, context );
    final URL schemaLocation = namespaces.get( uri );
    if( schemaLocation != null )
    {
      try
      {
        return GMLSchemaFactory.createGMLSchema( gmlVersion, schemaLocation );
      }
      catch( final GMLSchemaException e )
      {
        final String msg = String.format( "Could not load schema (namespace=%s) from it's schemaLocation: %s", uri, schemaLocation );
        throw new SAXException( msg, e );
      }
    }

    return null;
  }

  public void setSchemaLocation( final Map<String, URL> schemaLocations )
  {
    if( schemaLocations != null )
      m_schemaLocations.putAll( schemaLocations );
  }
}
