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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.core.i18n.Messages;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeWriter;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.shape.deegree.Shape2GML;
import org.kalypso.shape.shp.SHPException;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;

import com.google.common.base.Charsets;

/**
 * Helper-Klasse zum lesen und schreiben von GML <br>
 * TODO: Problem: reading/writing a shape will change the precision/size of the columns!
 * 
 * @author Gernot Belger
 */
public final class ShapeSerializer
{
  /** The default charset of a shape (really the .dbf) is IBM850. */
  private static final String SHAPE_DEFAULT_CHARSET_IBM850 = "IBM850"; //$NON-NLS-1$

  private ShapeSerializer( )
  {
    // wird nicht instantiiert
  }

  /**
   * @deprecated Use {@link org.kalypso.shape.ShapeWriter} and {@link org.kalypso.shape.deegree.GenericShapeDataFactory} instead.
   */
  @Deprecated
  public static void serialize( final GMLWorkspace workspace, final String filenameBase, final String targetSrs ) throws GmlSerializeException
  {
    final ShapeCollection collection = (ShapeCollection)workspace.getRootFeature();
    serialize( collection, filenameBase, targetSrs );
  }

  /**
   * Saves the given shape into a shape file, using the kalypso coordinate system.
   */
  public static void serialize( final ShapeCollection collection, final String filenameBase ) throws GmlSerializeException
  {
    final String defaultSrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    serialize( collection, filenameBase, defaultSrs );
  }

  /**
   * Saves the given shape into a shape file.
   */
  public static void serialize( final ShapeCollection collection, final String filenameBase, final String targetSrs ) throws GmlSerializeException
  {
    try
    {
      final IShapeData data = GenericShapeDataFactory.createDefaultData( collection, getShapeDefaultCharset(), targetSrs );
      final ShapeWriter shapeWriter = new ShapeWriter( data );

      final IProgressMonitor monitor = new NullProgressMonitor();
      shapeWriter.write( filenameBase, monitor );
    }
    catch( IOException | DBaseException | SHPException | ShapeDataException e )
    {
      e.printStackTrace();
      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.7" ), e ); //$NON-NLS-1$
    }
  }

  /**
   * Same as {@link #deserialize(String, String, new NullProgressMonitor())}
   */
  public static ShapeCollection deserialize( final String fileBase, final String sourceCrs ) throws GmlSerializeException
  {
    return deserialize( fileBase, sourceCrs, new NullProgressMonitor() );
  }

  public static ShapeCollection deserialize( final String fileBase, final String sourceCrs, final IProgressMonitor monitor ) throws GmlSerializeException
  {
    final Charset charset = getShapeDefaultCharset();
    return deserialize( fileBase, sourceCrs, charset, monitor );
  }

  // FIXME:...
  // The shape default charset if IBM850. We use this if it exists on this platform.
  public static Charset getShapeDefaultCharset( )
  {
    try
    {
      return Charset.forName( SHAPE_DEFAULT_CHARSET_IBM850 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    /* If the shape default charset is not available on this platform, we use the platforms default. */
    return Charset.defaultCharset();
  }

  public static ShapeCollection deserialize( final String fileBase, final String sourceCrs, final Charset charset, final IProgressMonitor monitor ) throws GmlSerializeException
  {
    final String taskName = Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.2", fileBase ); //$NON-NLS-1$
    final SubMonitor moni = SubMonitor.convert( monitor, taskName, 100 );

    try( ShapeFile sf = new ShapeFile( fileBase, charset, FileMode.READ ) )
    {
      // TODO: as before, but still ugly
      final String key = Integer.toString( fileBase.hashCode() );

      return Shape2GML.convertShp2Gml( key, sf, sourceCrs, moni );
    }
    catch( final OperationCanceledException e )
    {
      throw new GmlSerializeException( Messages.getString( "ShapeSerializer.0" ), e ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.19" ), e ); //$NON-NLS-1$
    }
  }

  /**
   * This function tries to load a prj file, which contains the coordinate system. If it exists and is a valid one, this
   * coordinate system is returned. If it is not found, the source coordinate system is returned (this should be the one
   * in the gmt). If it does also not exist, null will be returned.
   * 
   * @param prjLocation
   *          Location of the .prj file.
   * @param defaultSrs
   *          Will be returned, if the .prj file could not be read.
   * @return The coordinate system, which should be used to load the shape.
   */
  public static String loadCrs( final URL prjLocation, final String defaultSrs )
  {
    try
    {
      // TODO: Should in the first instance interpret the prj content ...
      // Does not work now because we must create a coordinate system instance then, but we use string codes right now
      final String prjString = IOUtils.toString( prjLocation, Charsets.UTF_8.name() );
      if( prjString.startsWith( "EPSG:" ) ) //$NON-NLS-1$
        return prjString;

      return defaultSrs;
    }
    catch( final IOException ex )
    {
      System.out.println( "No prj file found for: " + prjLocation.toString() ); //$NON-NLS-1$
      return defaultSrs;
    }
  }
}