package org.kalypso.ogc.gml.convert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.IKalypsoCoreConstants;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.gml.util.ChangeSourceType;
import org.kalypso.gml.util.CsvSourceType;
import org.kalypso.gml.util.CsvTargetType;
import org.kalypso.gml.util.Excelsource;
import org.kalypso.gml.util.FeaturemappingSourceType;
import org.kalypso.gml.util.GmlSourceType;
import org.kalypso.gml.util.GmlTargetType;
import org.kalypso.gml.util.Gmlconvert;
import org.kalypso.gml.util.Gmlnew;
import org.kalypso.gml.util.Refsource;
import org.kalypso.gml.util.RegisterSourceType;
import org.kalypso.gml.util.ShpSourceType;
import org.kalypso.gml.util.SourceType;
import org.kalypso.gml.util.TargetType;
import org.kalypso.ogc.gml.convert.source.ChangeSourceTypeHandler;
import org.kalypso.ogc.gml.convert.source.CsvSourceHandler;
import org.kalypso.ogc.gml.convert.source.ExcelSourceHandler;
import org.kalypso.ogc.gml.convert.source.FeaturemappingSourceHandler;
import org.kalypso.ogc.gml.convert.source.GmlNewHandler;
import org.kalypso.ogc.gml.convert.source.GmlSourceHandler;
import org.kalypso.ogc.gml.convert.source.ISourceHandler;
import org.kalypso.ogc.gml.convert.source.RefSourceHandler;
import org.kalypso.ogc.gml.convert.source.RegisterSourceHandler;
import org.kalypso.ogc.gml.convert.source.ShpSourceHandler;
import org.kalypso.ogc.gml.convert.target.GmlTargetHandler;
import org.kalypso.ogc.gml.convert.target.ITargetHandler;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.xml.sax.InputSource;

/**
 * Dreh- und Angelpunkt des GML-Konvertierens.
 * 
 * @author Gernot Belger
 */
public final class GmlConvertFactory
{
  private GmlConvertFactory( )
  {
    // wird nicht instantiiert
  }

  /**
   * Genau wie {@link #convertXml(URL, IUrlResolver, URL, Map)}. Die URL wird selbst als Kontext gesetzt.
   * 
   * @throws IOException
   * @throws GmlConvertException
   * @throws GmlConvertException
   * @throws JAXBException
   */
  public static IStatus convertXml( final URL url, final IUrlResolver resolver, final Map< ? , ? > externData ) throws IOException, JAXBException, GmlConvertException
  {
    return convertXml( url, resolver, url, externData );
  }

  /**
   * Genau wie {@link #convertXml(InputSource, IUrlResolver, URL, Map)}. K�mmert sich aber um die URL und Stream
   * Details.
   * 
   * @throws IOException
   * @throws GmlConvertException
   * @throws GmlConvertException
   * @throws JAXBException
   */
  public static IStatus convertXml( final URL url, final IUrlResolver resolver, final URL context, final Map< ? , ? > externData ) throws IOException, JAXBException, GmlConvertException
  {
    final URLConnection connection = url.openConnection();
    final String contentEncoding = connection.getContentEncoding();
    final InputStream inputStream = connection.getInputStream();
    final InputSource source = new InputSource( inputStream );
    if( contentEncoding != null )
      source.setEncoding( contentEncoding );
    return convertXml( source, resolver, context, externData );
  }

  /**
   * F�r die in einem XML (gmc) gespeicherte Konvertierung durch.
   * 
   * @param resolver
   *          Wird f�r im XMl Referenzierte Dokumente gebraucht.
   * @param context
   *          Gegen diesen Kontext werdenim XML definierte Dokumente aufgel�st.
   * @throws JAXBException
   * @throws GmlConvertException
   */
  public static IStatus convertXml( final InputSource inputSource, final IUrlResolver resolver, final URL context, final Map< ? , ? > externData ) throws JAXBException, GmlConvertException
  {
    final Unmarshaller unmarshaller = TemplateUtilities.JC_GMC.createUnmarshaller();
    final Gmlconvert convert = (Gmlconvert) unmarshaller.unmarshal( inputSource );
    final GMLWorkspace gml = GmlConvertFactory.loadSource( resolver, context, convert.getSource().getValue(), externData );
    final TargetType target = convert.getTarget().getValue();
    GmlConvertFactory.writeIntoTarget( resolver, context, gml, target );
    final String message = Messages.getString( "org.kalypso.ogc.gml.convert.GmlConvertFactory.0" ) + " " + convert.getTarget().getValue().getHref() + Messages.getString( "org.kalypso.ogc.gml.convert.GmlConvertFactory.1" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return new Status( IStatus.OK, IKalypsoCoreConstants.PLUGIN_ID, 0, message, null );
  }

  /**
   * L�dt das GML aus einer Source. Sorgt intern daf�r, dass die richtigen Source-Handler benutzt werden.
   * 
   * @throws GmlConvertException
   */
  public static GMLWorkspace loadSource( final IUrlResolver resolver, final URL context, final SourceType source, final Map< ? , ? > externData ) throws GmlConvertException
  {
    // switch over source-type
    final ISourceHandler handler = createSourceHandler( resolver, context, source, externData );
    return handler.getWorkspace();
  }

  private static ISourceHandler createSourceHandler( final IUrlResolver resolver, final URL context, final SourceType source, final Map< ? , ? > externData ) throws GmlConvertException
  {
    if( source instanceof FeaturemappingSourceType )
      return new FeaturemappingSourceHandler( resolver, context, (FeaturemappingSourceType) source, externData );

    if( source instanceof ChangeSourceType )
      return new ChangeSourceTypeHandler( resolver, context, (ChangeSourceType) source, externData );

    if( source instanceof CsvSourceType )
      return new CsvSourceHandler( resolver, context, (CsvSourceType) source );

    if( source instanceof Excelsource )
      return new ExcelSourceHandler( resolver, context, (Excelsource) source );

    if( source instanceof ShpSourceType )
      return new ShpSourceHandler( resolver, context, (ShpSourceType) source );

    if( source instanceof Refsource )
      return new RefSourceHandler( resolver, context, (Refsource) source, externData );

    if( source instanceof GmlSourceType )
      return new GmlSourceHandler( resolver, context, (GmlSourceType) source );

    if( source instanceof Gmlnew )
      return new GmlNewHandler( context, (Gmlnew) source );

    if( source instanceof RegisterSourceType )
      return new RegisterSourceHandler( (RegisterSourceType) source, externData );

    throw new GmlConvertException( Messages.getString( "org.kalypso.ogc.gml.convert.GmlConvertFactory.2" ) + source.getClass().getName() ); //$NON-NLS-1$
  }

  /**
   * Schreibt das GML ins angegebene Target.
   * 
   * @throws GmlConvertException
   */
  public static void writeIntoTarget( final IUrlResolver resolver, final URL context, final GMLWorkspace gml, final TargetType target ) throws GmlConvertException
  {
    final ITargetHandler handler;
    if( target instanceof CsvTargetType )
      handler = new CsvTargetHandler( resolver, context, (CsvTargetType) target );
    else if( target instanceof GmlTargetType )
      handler = new GmlTargetHandler( resolver, context, (GmlTargetType) target );
    else
      throw new GmlConvertException( Messages.getString( "org.kalypso.ogc.gml.convert.GmlConvertFactory.3" ) + target.getClass().getName() ); //$NON-NLS-1$
    handler.saveWorkspace( gml );
  }
}
