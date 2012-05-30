/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.ProgressInputStream;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.contribs.org.xml.sax.DelegateXmlReader;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.GMLException;
import org.kalypso.gml.GMLWorkspaceInputSource;
import org.kalypso.gml.GMLWorkspaceReader;
import org.kalypso.gml.GMLorExceptionContentHandler;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Helper - Klasse, um Gml zu lesen und zu schreiben.
 * 
 * @author Gernot Belger
 */
public final class GmlSerializer
{
  private static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

  public static final IFeatureProviderFactory DEFAULT_FACTORY = new GmlSerializerFeatureProviderFactory();

  public static final String[] GZ_EXTENSIONS = new String[] { "gz", "gmlz" }; //$NON-NLS-1$ //$NON-NLS-2$

  private GmlSerializer( )
  {
    // do not instantiate this class
  }

  public static void serializeWorkspace( final OutputStreamWriter writer, final GMLWorkspace workspace ) throws GmlSerializeException
  {
    serializeWorkspace( writer, workspace, writer.getEncoding() );
  }

  /**
   * Writes a {@link GMLWorkspace} into an {@link File}. If the filename ends with <code>.gz</code>, the output will be
   * compressed with GZip.
   */
  public static void serializeWorkspace( final File gmlFile, final GMLWorkspace gmlWorkspace, final String encoding ) throws IOException, GmlSerializeException
  {
    OutputStream os = null;
    try
    {
      final BufferedOutputStream bs = new BufferedOutputStream( new FileOutputStream( gmlFile ) );

      if( isGZ( gmlFile.getName() ) )
        os = new GZIPOutputStream( bs );
      else
        os = bs;

      GmlSerializer.serializeWorkspace( os, gmlWorkspace, encoding );
      os.close();
    }
    finally
    {
      IOUtils.closeQuietly( os );
    }
  }

  /**
   * REMARK: This method closes the given writer, which is VERY bad. Every caller should close the write on its own
   * 
   * @deprecated Because this method closes it writer. Change to
   *             {@link #serializeWorkspace(Writer, GMLWorkspace, String, false)}, rewrite your code, then we can get
   *             rid of this method and the flag.
   */
  @Deprecated
  public static void serializeWorkspace( final Writer writer, final GMLWorkspace gmlWorkspace, final String charsetEncoding ) throws GmlSerializeException
  {
    serializeWorkspace( writer, gmlWorkspace, charsetEncoding, true );
  }

  /**
   * @deprecated Do not usae writer to write xml. The caller(!) always must close the writer. Use
   *             {@link #serializeWorkspace(OutputStreamWriter, GMLWorkspace)}
   */
  @Deprecated
  public static void serializeWorkspace( final Writer writer, final GMLWorkspace gmlWorkspace, final String charsetEncoding, final boolean closeWriter ) throws GmlSerializeException
  {
    final GMLWorkspaceInputSource inputSource = new GMLWorkspaceInputSource( gmlWorkspace );
    final StreamResult result = new StreamResult( writer );

    try
    {
      serializeWorkspace( inputSource, result, charsetEncoding );
    }
    finally
    {
      if( closeWriter )
      {
        IOUtils.closeQuietly( writer );
      }
    }
  }

  public static void serializeWorkspace( final OutputStream is, final GMLWorkspace gmlWorkspace, final String charsetEncoding ) throws GmlSerializeException
  {
    final GMLWorkspaceInputSource inputSource = new GMLWorkspaceInputSource( gmlWorkspace );
    final StreamResult result = new StreamResult( is );

    serializeWorkspace( inputSource, result, charsetEncoding );
  }

  /**
   * Most general way to serialize the workspace. All other serialize methods should eventually cal this method.
   */
  public static void serializeWorkspace( final GMLWorkspaceInputSource inputSource, final StreamResult result, final String charsetEncoding ) throws GmlSerializeException
  {
    try
    {
      // TODO: error handling
      final XMLReader reader = new GMLWorkspaceReader();
      // TODO: add an error handler that logs everything into a status
      // reader.setErrorHandler( null );
      // TODO: write gml to a temporary location and replace the target file later,
      // in order to preserve the old version if anything goes wrong

      reader.setFeature( "http://xml.org/sax/features/namespaces", true ); //$NON-NLS-1$
      reader.setFeature( "http://xml.org/sax/features/namespace-prefixes", true ); //$NON-NLS-1$

      final Source source = new SAXSource( reader, inputSource );

      final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
      transformer.setOutputProperty( OutputKeys.ENCODING, charsetEncoding );
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); //$NON-NLS-1$
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "1" ); //$NON-NLS-1$ //$NON-NLS-2$
      transformer.setOutputProperty( OutputKeys.METHOD, "xml" ); //$NON-NLS-1$
      // TODO: maybe also use OutputKeys.CDATA_SECTION_ELEMENTS ? See the marshallMethod of the XSDBaseTypeHandlerString
      // TODO put new QName( NS.OM, "result" ) here instead inside the GMLSaxFactory
      // Problem: must now know the prefix of NS.OM
      transformer.transform( source, result );
    }
    catch( final Exception e )
    {
      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.4" ), e ); //$NON-NLS-1$
    }
  }

  /**
   * Same as {@link #createGMLWorkspace(URL, IFeatureProviderFactory, null )

   */
  public static GMLWorkspace createGMLWorkspace( final URL gmlURL, final IFeatureProviderFactory factory ) throws Exception
  {
    return createGMLWorkspace( gmlURL, factory, null );
  }

  /**
   * Reads a {@link GMLWorkspace} from the contents of an {@link URL}.
   */
  public static GMLWorkspace createGMLWorkspace( final URL gmlURL, final IFeatureProviderFactory factory, final IProgressMonitor monitor ) throws Exception
  {
    return createGMLWorkspace( gmlURL, null, factory, monitor );
  }

  /**
   * @param context
   *          If set, this context is used instead of the gmlUrl where the workspace is loaded from.
   */
  public static GMLWorkspace createGMLWorkspace( final URL gmlURL, final URL context, final IFeatureProviderFactory factory, final IProgressMonitor monitor ) throws Exception
  {
    InputStream is = null;
    InputStream urlStream = null;
    InputStream bis = null;
    try
    {
      urlStream = gmlURL.openStream();

      if( monitor == null )
      {
        bis = new BufferedInputStream( urlStream );
      }
      else
      {
        final long contentLength = UrlUtilities.getContentLength( gmlURL );
        final String tskMsg = Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.3", gmlURL ); //$NON-NLS-1$
        monitor.beginTask( tskMsg, (int) contentLength );
        bis = new ProgressInputStream( urlStream, contentLength, monitor );
      }

      if( isGZ( gmlURL.toExternalForm() ) )
        is = new GZIPInputStream( bis );
      else
        is = bis;

      final URL usedContext = context == null ? gmlURL : context;

      final GMLWorkspace workspace = createGMLWorkspace( new InputSource( is ), null, usedContext, factory );
      is.close();
      return workspace;
    }
    catch( final IOException e )
    {
      // Handle cancel of progress monitor: ProgressInputStreams throws IOException with a CoreException as cause
      if( e == ProgressInputStream.CANCEL_EXCEPTION )
        throw new CoreException( StatusUtilities.createStatus( IStatus.CANCEL, Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.5" ), e ) ); //$NON-NLS-1$

      throw e;
    }
    finally
    {
      // also close <code>bis</code> separately, as GZipInputStream throws exception in constructor
      IOUtils.closeQuietly( bis );
      IOUtils.closeQuietly( is );
      IOUtils.closeQuietly( urlStream );
      ProgressUtilities.done( monitor );
    }
  }

  public static boolean isGZ( final String filename )
  {
    // REMARK: this is a quite crude way to decide, if to compress or not. But how should we decide it anyway?
    final String extension = FilenameUtils.getExtension( filename );
    return ArrayUtils.contains( GZ_EXTENSIONS, extension );
  }

  public static GMLWorkspace createGMLWorkspace( final InputSource inputSource, final URL schemaLocationHint, final URL context, final IFeatureProviderFactory factory ) throws ParserConfigurationException, SAXException, IOException, GMLException
  {
    TimeLogger perfLogger = null;
    if( KalypsoCoreDebug.PERF_SERIALIZE_GML.isEnabled() )
      perfLogger = new TimeLogger( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.7" ) ); //$NON-NLS-1$

    final IFeatureProviderFactory providerFactory = factory == null ? DEFAULT_FACTORY : factory;
    final XMLReader xmlReader = createXMLReader();

    // TODO: also set an error handler here

    final GMLorExceptionContentHandler exceptionHandler = new GMLorExceptionContentHandler( xmlReader, schemaLocationHint, context, providerFactory );
    xmlReader.setContentHandler( exceptionHandler );

    xmlReader.parse( inputSource );

    final GMLWorkspace workspace = exceptionHandler.getWorkspace();

    if( perfLogger != null )
    {
      perfLogger.takeInterimTime();
      perfLogger.printCurrentTotal( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.8" ) ); //$NON-NLS-1$
    }

    return workspace;
  }

  private static XMLReader createXMLReader( ) throws ParserConfigurationException, SAXException
  {
    final SAXParserFactory saxFac = SAXParserFactory.newInstance();
    saxFac.setNamespaceAware( true );

    final SAXParser saxParser = saxFac.newSAXParser();
    // make namespace-prefixes visible to content handler
    // used to allow necessary schemas from gml document
    final XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setFeature( "http://xml.org/sax/features/namespace-prefixes", Boolean.TRUE ); //$NON-NLS-1$
    return xmlReader;
  }

  public static GMLWorkspace createGMLWorkspace( final Node gmlNode, final URL context, final IFeatureProviderFactory factory ) throws TransformerException, GMLException
  {
    final IFeatureProviderFactory providerFactory = factory == null ? DEFAULT_FACTORY : factory;

    /* REMARK: we need to simulate a XMLReader in order to make the gml parsing work. */
    final DelegateXmlReader delegateReader = new DelegateXmlReader();
    final GMLorExceptionContentHandler gmlContentHandler = new GMLorExceptionContentHandler( delegateReader, null, context, providerFactory );
    delegateReader.setContentHandler( gmlContentHandler );

    final Transformer t = TransformerFactory.newInstance().newTransformer();
    t.transform( new DOMSource( gmlNode ), new SAXResult( delegateReader ) );

    return gmlContentHandler.getWorkspace();
  }

  public static GMLWorkspace createGMLWorkspace( final File file, final IFeatureProviderFactory factory ) throws Exception
  {
    InputStream is = null;
    BufferedInputStream bis = null;

    try
    {
      bis = new BufferedInputStream( new FileInputStream( file ) );

      if( file.getName().endsWith( ".gz" ) ) //$NON-NLS-1$
      {
        is = new GZIPInputStream( bis );
      }
      else
      {
        is = bis;
      }

      final URL context = file.toURI().toURL();
      final GMLWorkspace workspace = createGMLWorkspace( new InputSource( is ), null, context, factory );
      is.close();
      return workspace;
    }
    finally
    {
      IOUtils.closeQuietly( is );
      // Close <code>bis<code> separately, as GZipInputStream throws exception constructor
      IOUtils.closeQuietly( bis );
    }

  }

  public static GMLWorkspace createGMLWorkspace( final InputStream inputStream, final URL schemaURLHint, final IFeatureProviderFactory factory ) throws Exception
  {
    return createGMLWorkspace( new InputSource( inputStream ), schemaURLHint, null, factory );
  }

  /**
   * Creates an (empty) gml file, containing only one root feature of a given type.
   */
  public static void createGmlFile( final IFeatureType rootFeatureType, final IFile targetFile, final IProgressMonitor monitor, final IFeatureProviderFactory factory ) throws CoreException
  {
    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.10" ), 2 ); //$NON-NLS-1$
      final IFeatureProviderFactory providerFactory = factory == null ? DEFAULT_FACTORY : factory;
      final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( rootFeatureType, ResourceUtilities.createURL( targetFile ), providerFactory );
      monitor.worked( 1 );

      final SetContentHelper contentHelper = new SetContentHelper()
      {
        @Override
        protected void write( final OutputStreamWriter writer ) throws Throwable
        {
          GmlSerializer.serializeWorkspace( writer, workspace );
        }
      };
      contentHelper.setFileContents( targetFile, false, true, new SubProgressMonitor( monitor, 1 ) );
      monitor.worked( 1 );
    }
    catch( final MalformedURLException e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  public static void createGmlFile( final QName rootFeatureQName, final String[] introduceNamespaces, final IFile targetFile, final IProgressMonitor monitor, final IFeatureProviderFactory factory ) throws CoreException, GMLSchemaException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.10" ), 2 ); //$NON-NLS-1$

    final IFeatureProviderFactory providerFactory = factory == null ? DEFAULT_FACTORY : factory;

    URL context = null;
    try
    {
      context = targetFile.getLocationURI().toURL();
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
    }

    final GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( rootFeatureQName, context, providerFactory );

    // introduce further schemata into workspace
    final IGMLSchema schema = workspace.getGMLSchema();
    if( introduceNamespaces != null && schema instanceof GMLSchema )
    {
      final GMLSchema gmlSchema = (GMLSchema) schema;
      for( final String namespaceUri : introduceNamespaces )
      {
        try
        {
          gmlSchema.getGMLSchemaForNamespaceURI( namespaceUri );
        }
        catch( final GMLSchemaException e )
        {
          // probably not a vital error, just log it
          final IStatus status = StatusUtilities.statusFromThrowable( e );
          KalypsoCorePlugin.getDefault().getLog().log( status );
        }
      }
    }

    monitor.worked( 1 );

    final SetContentHelper contentHelper = new SetContentHelper()
    {
      @Override
      protected void write( final OutputStreamWriter writer ) throws Throwable
      {
        GmlSerializer.serializeWorkspace( writer, workspace );
      }
    };
    contentHelper.setFileContents( targetFile, false, true, new SubProgressMonitor( monitor, 1 ) );
    monitor.worked( 1 );
  }

  /**
   * This function loads a workspace from a {@link IFile}.
   * 
   * @param file
   *          The file of the workspace.
   * @return The workspace of the file.
   */
  public static GMLWorkspace createGMLWorkspace( final IFile file ) throws Exception
  {
    return createGMLWorkspace( file, new NullProgressMonitor() );
  }

  /**
   * This function loads a workspace from a {@link IFile}.
   * 
   * @param file
   *          The file of the workspace.
   * @return The workspace of the file.
   */
  public static GMLWorkspace createGMLWorkspace( final IFile file, final IProgressMonitor monitor ) throws Exception
  {
    return createGMLWorkspace( file, null, null, monitor );
  }

  /**
   * This function loads a workspace from a {@link IFile}.
   * 
   * @param file
   *          The file of the workspace.
   * @return The workspace of the file.
   */
  public static GMLWorkspace createGMLWorkspace( final IFile file, final URL context, final IFeatureProviderFactory factory, final IProgressMonitor monitor ) throws Exception
  {
    /* Create the url of the workspace. */
    final URL url = ResourceUtilities.createURL( file );

    /* Load the workspace and return it. */
    return GmlSerializer.createGMLWorkspace( url, context, factory, monitor );
  }

  /**
   * This function saves a given workspace to a file. Don't forget to set your charset to the file you are about to
   * create. It will be used by this function.
   * 
   * @param workspace
   *          The workspace to save.
   * @param file
   *          The file to save the workspace to. <strong>Note:</strong> The file must point to a real file.
   */
  public static void saveWorkspace( final GMLWorkspace workspace, final IFile file ) throws Exception
  {
    if( workspace == null || file == null )
      throw new Exception( Messages.getString( "org.kalypso.ogc.gml.serialize.GmlSerializer.2" ) ); //$NON-NLS-1$

    /* The default encoding is that of the file. */
    final String encoding = file.getCharset();

    /* Create a writer. */
    final File javaFile = file.getLocation().toFile();

    /* Save the workspace. */
    serializeWorkspace( javaFile, workspace, encoding );

    /* Refresh the file. */
    file.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
  }

  /**
   * serializes a workspace into a zipfile
   */
  public static void serializeWorkspaceToZipFile( final File gmlZipResultFile, final GMLWorkspace resultWorkspace, final String zipEntryName ) throws FileNotFoundException
  {
    final ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( gmlZipResultFile ) ) );
    try
    {
      final ZipEntry newEntry = new ZipEntry( zipEntryName );
      zos.putNextEntry( newEntry );

      serializeWorkspace( zos, resultWorkspace, "CP1252" ); //$NON-NLS-1$

      zos.closeEntry();
      zos.close();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      IOUtils.closeQuietly( zos );
    }
  }

  public static void serializeWorkspace( final IFile targetFile, final GMLWorkspace workspace, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Writing gml", 100 );

    String charset;
    if( targetFile.exists() )
      charset = targetFile.getCharset();
    else
    {
      // check: is there a better way to set this default encoding?
      charset = "UTF-8"; //$NON-NLS-1$
    }
    monitor.worked( 5 );

    try
    {
      final File file = targetFile.getLocation().toFile();
      serializeWorkspace( file, workspace, charset );
      monitor.worked( 90 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus error = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Failed to write gml workspace", e );
      throw new CoreException( error );
    }
    finally
    {
      targetFile.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 5 ) );
    }
  }
}