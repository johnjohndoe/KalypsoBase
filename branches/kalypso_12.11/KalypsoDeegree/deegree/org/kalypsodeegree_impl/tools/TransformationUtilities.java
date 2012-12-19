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
package org.kalypsodeegree_impl.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.RenderedImage;

import javax.media.jai.TiledImage;

import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.coverage.GridRange;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain.OffsetVector;
import org.kalypsodeegree_impl.model.cv.GridRange_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This class provides functions for transformation purposes.
 * 
 * @author Holger Albert
 */
public class TransformationUtilities
{
  /**
   * The constructor.
   */
  private TransformationUtilities( )
  {
  }

  /**
   * This method transforms an image from a source to a target coordinate system and paints it on the submitted graphic
   * context g.
   * 
   * @param remoteImage
   *          Image to be transformed.
   * @param sourceEnvelope
   *          Bounding box of the remoteMap.
   * @param localCrs
   *          Target coordinate system.
   * @param worldToScreenTransformation
   *          Transformation from target coordinate system to pixel unites.
   * @param g
   *          Graphics context to draw the transformed image to.
   * @throws Exception
   */
  public static void transformImage( final TiledImage remoteImage, final GM_Envelope sourceEnvelope, final String targetCrs, final GeoTransform worldToScreenTransformation, final Graphics g ) throws Exception
  {
    if( remoteImage == null )
      return;
    final int height = remoteImage.getHeight();
    final int width = remoteImage.getWidth();

    final OffsetVector offsetX = new OffsetVector( (sourceEnvelope.getMax().getX() - sourceEnvelope.getMin().getX()) / width, 0.0 );
    final OffsetVector offsetY = new OffsetVector( 0.0, (sourceEnvelope.getMax().getY() - sourceEnvelope.getMin().getY()) / height );

    final GridRange range = new GridRange_Impl( new double[] { 0, 0 }, new double[] { width, height } );

    final GM_Point origin = GeometryFactory.createGM_Point( sourceEnvelope.getMin().getX(), sourceEnvelope.getMin().getY(), sourceEnvelope.getCoordinateSystem() );
    final RectifiedGridDomain gridDomain = new RectifiedGridDomain( origin, offsetX, offsetY, range );

    internalTransformation( (Graphics2D) g, worldToScreenTransformation, remoteImage, gridDomain, targetCrs );
  }

  /**
   * Transforms.
   * 
   * @param g2d
   *          Empty graphics context.
   * @param projection
   *          World to screen projection (passed from MapPanel).
   * @param rasterImage
   *          Image from server.
   * @param gridDomain
   *          Image domain from server with geospatial ( real world ) context. CS from server and Envelope from server
   *          (all layers).
   * @param targetCS
   *          Target coordinate system (local CS from client).
   */
  private static void internalTransformation( final Graphics2D g2d, final GeoTransform projection, final TiledImage rasterImage, final RectifiedGridDomain gridDomain, final String targetCS ) throws Exception
  {
    /* Get the Screen extent in real world coordiantes. */
    final GM_Envelope sourceScreenRect = projection.getSourceRect();

    /* Create a surface and transform it in the coordinate system of the. */
    final GM_Polygon sourceScreenSurface = GeometryFactory.createGM_Surface( sourceScreenRect, targetCS );

    GM_Polygon destScreenSurface;
    if( !targetCS.equals( gridDomain.getOrigin( null ).getCoordinateSystem() ) )
    {
      final IGeoTransformer geoTrans1 = GeoTransformerFactory.getGeoTransformer( gridDomain.getOrigin( null ).getCoordinateSystem() );
      destScreenSurface = (GM_Polygon) geoTrans1.transform( sourceScreenSurface );
    }
    else
      destScreenSurface = sourceScreenSurface;

    /* Get the gridExtent for the envelope of the surface. */
    final int[] gridExtent = gridDomain.getGridExtent( destScreenSurface.getEnvelope(), gridDomain.getOrigin( null ).getCoordinateSystem() );
    // Make it a bit larger in order to avoid undrawn border
    final int lowX = gridExtent[0] - 2;
    final int lowY = gridExtent[1] - 2;
    final int highX = gridExtent[2] + 2;
    final int highY = gridExtent[3] + 2;

    /* Calculate imageExtent from gridExtent. */
    final int minX = lowX;
    final int minY = rasterImage.getHeight() - highY;
    final int width = highX - lowX;
    final int height = highY - lowY;

    /* Get the required subImage according to the gridExtent (size of the screen). */
    final TiledImage image = rasterImage.getSubImage( minX, minY, width, height );

    /* If the requested sub image is not on the screen (map panel) nothing to display. */
    if( image == null )
      return;

    final RenderedImage paintImage = derivePaintImage( image );

    /* Get the destinationSurface in target coordinates. */
    final GM_Polygon destSurface = gridDomain.getGM_Surface( lowX, lowY, highX, highY, targetCS );
    final GM_Ring destExtRing = destSurface.getSurfaceBoundary().getExteriorRing();
    final GM_Position llCorner = destExtRing.getPositions()[0];
    final GM_Position lrCorner = destExtRing.getPositions()[1];
    final GM_Position urCorner = destExtRing.getPositions()[2];
    final GM_Position ulCorner = destExtRing.getPositions()[3];

    /* Calculate the Corners in screen coordinates. */
    final GM_Position pixel_llCorner = projection.getDestPoint( llCorner );
    final GM_Position pixel_lrCorner = projection.getDestPoint( lrCorner );
    final GM_Position pixel_urCorner = projection.getDestPoint( urCorner );
    final GM_Position pixel_ulCorner = projection.getDestPoint( ulCorner );

    /* Calculate the height and width of the image on screen. */
    final double destImageWidth = pixel_lrCorner.getX() - pixel_llCorner.getX();
    final double destImageHeight = pixel_llCorner.getY() - pixel_ulCorner.getY();

    /* If one of the values is <=0, there could nothing displayed. */
    if( destImageHeight <= 0 || destImageWidth <= 0 )
      return;

    /* Calculate the scaling factors for the transformation. */
    final double scaleX = destImageWidth / paintImage.getWidth();
    final double scaleY = destImageHeight / paintImage.getHeight();

    /* Calculate the shear parameters for the transformation. */
    final double shearX = pixel_llCorner.getX() - pixel_ulCorner.getX();
    final double shearY = pixel_lrCorner.getY() - pixel_llCorner.getY();

    /* Calculate the required extent of the bufferedImage. */
    final GM_Position scaledImage_min = pixel_ulCorner;
    final GM_Position scaledImage_max = GeometryFactory.createGM_Position( pixel_urCorner.getX(), pixel_llCorner.getY() );

    final GM_Position buffImage_min = GeometryFactory.createGM_Position( scaledImage_min.getX() - Math.abs( shearX ), scaledImage_min.getY() - Math.abs( shearY ) );
    final GM_Position buffImage_max = GeometryFactory.createGM_Position( scaledImage_max.getX() + Math.abs( shearX ), scaledImage_max.getY() + Math.abs( shearY ) );
    final GM_Envelope buffImageEnv = GeometryFactory.createGM_Envelope( buffImage_min, buffImage_max, targetCS );

    final AffineTransform trafo = new AffineTransform();
    trafo.translate( (int) buffImageEnv.getMin().getX(), (int) buffImageEnv.getMin().getY() );

    /* Translate the image, so that the subImage is at the right position. */
    trafo.translate( -paintImage.getMinX() * scaleX, -paintImage.getMinY() * scaleY );

    /* Scale the image. */
    trafo.scale( scaleX, scaleY );

    /* Translate the image to compensate the shearing. */
    trafo.translate( Math.abs( shearX ) / Math.abs( scaleX ), Math.abs( shearY ) / Math.abs( scaleY ) );

    /* Shear the image. */
    trafo.shear( shearX / destImageWidth, shearY / destImageHeight );

    /* We cannot draw, if the image would have one or both side with 0 pixels. */
    final int width2 = (int) buffImageEnv.getWidth();
    final int height2 = (int) buffImageEnv.getHeight();
    if( width2 <= 0 || height2 <= 0 )
      return;

    g2d.drawRenderedImage( paintImage, trafo );
  }

  private static RenderedImage derivePaintImage( final TiledImage image )
  {
    return image;

    // TODO: example code that turns all white pixels transparent (nice for tiffs without transparent color)

    // This is probably heavy and should only be used as an option on the theme -> introduce option for 'transparent
    // color'

// final BufferedImage asBufferedImage = image.getAsBufferedImage();
// final Image transparentImage = makeColorTransparent( asBufferedImage, new Color( 255, 255, 255 ) );
// final BufferedImage paintImage = imageToBufferedImage( transparentImage );
//
// return paintImage;
  }

  static BufferedImage imageToBufferedImage( final Image image )
  {
    final BufferedImage bufferedImage = new BufferedImage( image.getWidth( null ), image.getHeight( null ), BufferedImage.TYPE_INT_ARGB );
    final Graphics2D g2 = bufferedImage.createGraphics();
    g2.drawImage( image, 0, 0, null );
    g2.dispose();

    return bufferedImage;
  }

  static Image makeColorTransparent( final BufferedImage im, final Color color )
  {
    final ImageFilter filter = new RGBImageFilter()
    {
      // the color we are looking for... Alpha bits are set to opaque
      public int markerRGB = color.getRGB() | 0xFF000000;

      @Override
      public final int filterRGB( final int x, final int y, final int rgb )
      {
        if( (rgb | 0xFF000000) == markerRGB )
        {
          // Mark the alpha bits as zero - transparent
          return 0x00FFFFFF & rgb;
        }
        else
        {
          // nothing to do
          return rgb;
        }
      }
    };

    final ImageProducer ip = new FilteredImageSource( im.getSource(), filter );
    return Toolkit.getDefaultToolkit().createImage( ip );
  }
}