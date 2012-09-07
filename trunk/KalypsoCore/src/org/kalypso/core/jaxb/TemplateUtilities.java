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
package org.kalypso.core.jaxb;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.kalypso.commons.KalypsoCommonsExtensions;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.commons.bind.NamespacePrefixMap;
import org.kalypso.commons.bind.SchemaCache;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.commons.i18n.ITranslatorContext;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.template.gismapview.ObjectFactory;
import org.kalypso.template.gistableview.Gistableview.Layer;
import org.kalypso.template.types.I18NTranslatorType;
import org.kalypsodeegree.filterencoding.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Utility class for handling with the 'template' binding schemata.
 *
 * @author Gernot Belger
 */
public final class TemplateUtilities
{
  private static final String SCHEMA_PATH = "etc/schemas/template/"; //$NON-NLS-1$

  private static final SchemaCache SCHEMA_CACHE = new SchemaCache( KalypsoCorePlugin.getID(), SCHEMA_PATH );

  /* Template Types */
  public static final org.kalypso.template.types.ObjectFactory OF_TEMPLATE_TYPES = new org.kalypso.template.types.ObjectFactory();

  /* GisMapView */
  public static final JAXBContext JC_GISMAPVIEW = JaxbUtilities.createQuiet( ObjectFactory.class );

  public static final ObjectFactory OF_GISMAPVIEW = new ObjectFactory();

  /* GisTableView */
  public static final JAXBContext JC_GISTABLEVIEW = JaxbUtilities.createQuiet( org.kalypso.template.gistableview.ObjectFactory.class );

  public static final org.kalypso.template.gistableview.ObjectFactory OF_GISTABLEVIEW = new org.kalypso.template.gistableview.ObjectFactory();

  private static final NamespacePrefixMapper GISTBALEVIEW_MAPPER = new NamespacePrefixMap( "gistableview.template.kalypso.org" );//$NON-NLS-1$

  /* GisTreeView */
  public static final JAXBContext JC_GISTREEVIEW = JaxbUtilities.createQuiet( org.kalypso.template.gistreeview.ObjectFactory.class );

  public static final org.kalypso.template.gistreeview.ObjectFactory OF_GISTREEVIEW = new org.kalypso.template.gistreeview.ObjectFactory();

  /* Featureview */
  public static final JAXBContext JC_FEATUREVIEW = JaxbUtilities.createQuiet( org.kalypso.template.featureview.ObjectFactory.class );

  public static final org.kalypso.template.featureview.ObjectFactory OF_FEATUREVIEW = new org.kalypso.template.featureview.ObjectFactory();

  /* .gmc */
  public static final JAXBContext JC_GMC = JaxbUtilities.createQuiet( org.kalypso.gml.util.ObjectFactory.class );

  private TemplateUtilities( )
  {
    // do not instantiat, everything is static
  }

  public static Schema getFeatureviewSchema( )
  {
    return SCHEMA_CACHE.getSchema( "featureview.xsd" ); //$NON-NLS-1$
  }

  public static synchronized Schema getGismapviewSchema( )
  {
    return SCHEMA_CACHE.getSchema( "gismapview.xsd" ); //$NON-NLS-1$
  }

  public static synchronized Schema getGistableviewSchema( )
  {
    return SCHEMA_CACHE.getSchema( "gistableview.xsd" ); //$NON-NLS-1$
  }

  public static Marshaller createGismapviewMarshaller( final String encoding ) throws JAXBException
  {
    final Marshaller marshaller = JaxbUtilities.createMarshaller( TemplateUtilities.JC_GISMAPVIEW );
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.setProperty( Marshaller.JAXB_ENCODING, encoding );

    // REMARK: only validate in trace mode, because this leads often to errors
    // because the 'href' attribute of the styledLayers are anyURIs, but its values are often not.
    if( KalypsoCoreDebug.GISMAPVIEW_VALIDATE.isEnabled() )
      marshaller.setSchema( getGismapviewSchema() );

    return marshaller;
  }

  public static Unmarshaller createGismapviewUnmarshaller( ) throws JAXBException
  {
    final Unmarshaller unmarshaller = TemplateUtilities.JC_GISMAPVIEW.createUnmarshaller();

    // REMARK: only validate in trace mode, because this lead often to errors
    // because the 'href' attribute of the styledLayers are anyURIs, but its values are often not.
    if( KalypsoCoreDebug.GISMAPVIEW_VALIDATE.isEnabled() )
      unmarshaller.setSchema( getGismapviewSchema() );

    return unmarshaller;
  }

  public static Unmarshaller createFeatureviewUnmarshaller( ) throws JAXBException
  {
    final Unmarshaller unmarshaller = JC_FEATUREVIEW.createUnmarshaller();

    if( KalypsoCoreDebug.FEATUREVIEW_VALIDATE.isEnabled() )
      unmarshaller.setSchema( getFeatureviewSchema() );

    return unmarshaller;
  }

  public static Marshaller createGistableviewMarshaller( final String encoding ) throws JAXBException
  {
    final Marshaller marshaller = JaxbUtilities.createMarshaller( JC_GISTABLEVIEW, true, encoding, GISTBALEVIEW_MAPPER );

    // REMARK: only validate in trace mode, because this lead often to errors
    // because the 'href' attribute of the styledLayers are anyURIs, but its values are often not.
    if( KalypsoCoreDebug.GISMAPVIEW_VALIDATE.isEnabled() )
      marshaller.setSchema( getGistableviewSchema() );

    return marshaller;
  }

  public static Unmarshaller createGistableviewUnmarshaller( ) throws JAXBException
  {
    final Unmarshaller unmarshaller = JC_GISTABLEVIEW.createUnmarshaller();

    if( KalypsoCoreDebug.FEATUREVIEW_VALIDATE.isEnabled() )
      unmarshaller.setSchema( getGistableviewSchema() );

    return unmarshaller;
  }

  /**
   * Converts the given ogc-filter to an dom-element suitable for the Gistableview and set its to the layer.
   */
  public static void setFilter( final Layer layer, final Filter ogcFilter ) throws IOException, SAXException
  {
    final Element filterElement = ogcFilter.toDom();
    final Document doc = filterElement.getOwnerDocument();
    final Element rootElement = doc.createElementNS( "gistableview.template.kalypso.org", "filter" ); //$NON-NLS-1$ //$NON-NLS-2$
    rootElement.appendChild( filterElement );
    layer.setFilter( rootElement );
  }

  public static Marshaller createGistreeviewMarshaller( final String encoding ) throws JAXBException
  {
    final Marshaller marshaller = JaxbUtilities.createMarshaller( TemplateUtilities.JC_GISTREEVIEW );
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    marshaller.setProperty( Marshaller.JAXB_ENCODING, encoding );
    return marshaller;
  }

  public static Unmarshaller createGistreeviewUnmarshaller( ) throws JAXBException
  {
    final Unmarshaller unmarshaller = TemplateUtilities.JC_GISTREEVIEW.createUnmarshaller();
    return unmarshaller;
  }

  public static ITranslator createTranslator( final I18NTranslatorType translatorElement, final URL context )
  {
    if( translatorElement == null )
      return null;

    final ITranslator translator = KalypsoCommonsExtensions.createTranslator( translatorElement.getId() );
    if( translator != null )
    {
      final ITranslatorContext translatorContext = new ITranslatorContext()
      {
        @Override
        public Object getAdapter( final Class adapter )
        {
          return null;
        }

        @Override
        public URL getContext( )
        {
          return context;
        }
      };

      translator.configure( translatorContext, translatorElement.getAny() );
    }

    return translator;
  }

  public static I18NTranslatorType createTranslatorType( final ITranslator i10nTranslator )
  {
    if( i10nTranslator == null )
      return null;

    final String id = i10nTranslator.getId();
    /* Fake translator, return nothing */
    if( id == null )
      return null;

    final I18NTranslatorType translator = OF_TEMPLATE_TYPES.createI18NTranslatorType();
    translator.setId( id );
    final List<Element> configuration = i10nTranslator.getConfiguration();
    if( configuration != null )
      translator.getAny().addAll( configuration );
    return translator;
  }
}