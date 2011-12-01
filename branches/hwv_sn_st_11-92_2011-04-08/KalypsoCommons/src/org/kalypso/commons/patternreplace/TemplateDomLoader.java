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
package org.kalypso.commons.patternreplace;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.commons.resources.FileUtilities;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.xml.XMLHelper;
import org.w3c.dom.Document;

/**
 * Helper class that reads template files (like .kod, .ort, etc.) into a DOM.</br> The speciality is, that it allows to
 * search/replace certain tokens in the template to be loaded. Several ways are possible to do that:
 * <ul>
 * <li>add additional pattern replacements via {@link #addReplacementPattern(IPatternInput)}</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class TemplateDomLoader
{
  private static final String PROP_TEMPLATE_URI = "templateUri";

  private final Properties m_properties = new Properties();

  private final TemplateInputReplacer m_replacer = new TemplateInputReplacer( m_properties );

  private IFile m_templateFile = null;

  private URL m_templateLocation = null;

  /* Location of the template file -> use this one to resolve refs inside the real template file. */
  private URL m_realTemplateLocation;

  private String m_templateCharset;


  public TemplateDomLoader( final IFile templateFile )
  {
    Assert.isNotNull( templateFile );

    m_templateFile = templateFile;
  }

  public TemplateDomLoader( final URL templateLocation )
  {
    Assert.isNotNull( templateLocation );

    m_templateLocation = templateLocation;
  }

  public void addReplacementPattern( final IPatternInput<Object> pattern )
  {
    m_replacer.addReplacer( pattern );
  }

  private InputStream openStream( ) throws CoreException, IOException
  {
    if( m_templateFile != null )
      return m_templateFile.getContents();

    if( m_templateLocation != null )
      return m_templateLocation.openStream();

    throw new IllegalStateException();
  }

  private URL asUrl( ) throws MalformedURLException
  {
    if( m_templateLocation != null )
      return m_templateLocation;

    if( m_templateFile != null )
      return ResourceUtilities.createURL( m_templateFile );

    throw new IllegalStateException();
  }

  public Document loadTemplate( ) throws CoreException
  {
    final String templateContent = readAsString();

    /* Pattern replace contents according to the previously loaded properties */
    final String replacedContent = patternReplace( templateContent );

    try
    {
      /* Read string as dom */
      final InputStream is = IOUtils.toInputStream( replacedContent, m_templateCharset );
      return XMLHelper.getAsDOM( is, true );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String message = String.format( "Fehler beim Lesen der Vorlage %s", getFilename() );
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), message, e ) );
    }
  }

  private String getFilename( )
  {
    if( m_templateFile != null )
      return m_templateFile.getName();

    if( m_templateLocation != null )
      return m_templateLocation.getFile();

    throw new IllegalStateException();
  }

  private String readAsString( ) throws CoreException
  {
    final String tryOne = readAsProperties();
    if( tryOne != null )
      return tryOne;

    /* Second try: directly read the document, it is not behind the template-proxy */
    final InputStream inputStream = null;
    try
    {
      m_realTemplateLocation = asUrl();
      final IFile templateFile = ResourceUtilities.findFileFromURL( m_realTemplateLocation );
      m_templateCharset = templateFile.getCharset();
      return UrlUtilities.toString( m_realTemplateLocation, m_templateCharset );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String message = String.format( "Fehler beim Laden der Vorlagendatei: %s", getFilename() );
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), message, e ) );
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
    }
  }

  /**
   * TODO: check if this is really a good idea. Try to read the chart as properties file that describes the real chart
   * location + the replacement properties.
   */
  private String readAsProperties( ) throws CoreException
  {
    InputStream inputStream = null;
    try
    {
      inputStream = openStream();
      m_properties.loadFromXML( inputStream );

      // OK, it WAS a properties file -> load the real .kod and search/replace all tokens

      /* Resolve url and read file into string */
      final String templateUriProp = m_properties.getProperty( PROP_TEMPLATE_URI );
      final URL context = asUrl();

      m_realTemplateLocation = new URL( context, templateUriProp );
      final IFile kodFile = ResourceUtilities.findFileFromURL( m_realTemplateLocation );
      m_templateCharset = kodFile.getCharset();
      return FileUtilities.toString( kodFile );
    }
    catch( final InvalidPropertiesFormatException e )
    {
      // ignore: it is not a properties file -> proceed to load normally
      // TODO: we should keep the exception, in case that we also cannot load the chart normally, maybe it was a bad
      // properties file
      return null;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), "Fehler beim Laden der Diagrammvorlage", e ) );
    }
    finally
    {
      IOUtils.closeQuietly( inputStream );
    }
  }

  public String patternReplace( final String kodContent )
  {
    return m_replacer.replaceTokens( kodContent, null );
  }

  /**
   * Returns the real location of the loaded template
   */
  public URL getRealLocation( )
  {
    return m_realTemplateLocation;
  }

}
