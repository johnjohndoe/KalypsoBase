/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ui.editor.gmleditor.part;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.java.lang.DisposeHelper;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.catalogs.FeatureTypeImageCatalog;
import org.kalypso.ui.catalogs.LinkedFeatureTypeImageCatalog;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * This is a label provider for GML features.
 *
 * @author Christoph Kuepferle
 * @author Holger Albert
 */
public class GMLLabelProvider extends LabelProvider
{
  /**
   * Hash for descriptors and its images.
   */
  private final Map<ImageDescriptor, Image> m_images = new HashMap<>( 20 );

  @Override
  public void dispose( )
  {
    super.dispose();

    /* Helper for disposing objects. */
    final DisposeHelper disposeHelper = new DisposeHelper( m_images.values() );

    /* Dispose the images. */
    disposeHelper.dispose();

    /* Clear the map. */
    m_images.clear();
  }

  @Override
  public Image getImage( final Object element )
  {
    /* Get the descriptor. */
    final ImageDescriptor descriptor = getDescriptor( element );
    if( descriptor == null )
      return null;

    /* If its image is already there, take it. */
    if( m_images.containsKey( descriptor ) )
      return m_images.get( descriptor );

    /* Otherwise create it. */
    final Image createImage = descriptor.createImage();

    /* Store the image. */
    m_images.put( descriptor, createImage );

    return createImage;
  }

  /**
   * This function retrieves the image descriptor from an catalog or other sources.
   *
   * @param element
   *          The element, for which the image descriptor should be obtained.
   * @return The image descriptor.
   */
  private ImageDescriptor getDescriptor( final Object element )
  {
    /* Get the qname. */
    final QName qname = getQName( element );
    if( qname != null )
    {
      /* do we have a registered label provider? */
      // FIXME: we should be able to delegate to a registered label provider

      /* Check the catalogs for this qname. */
      if( element instanceof LinkedFeatureElement )
      {
        final ImageDescriptor catalogImage = LinkedFeatureTypeImageCatalog.getImage( null, qname );
        if( catalogImage != null )
          return catalogImage;
      }

      final ImageDescriptor catalogImage = FeatureTypeImageCatalog.getImage( null, qname );
      if( catalogImage != null )
        return catalogImage;
    }

    /* Take the default images. */
    if( element instanceof Feature )
      return ImageProvider.IMAGE_FEATURE;

    if( element instanceof FeatureAssociationTypeElement )
      return ImageProvider.IMAGE_FEATURE_RELATION_COMPOSITION;

    if( element instanceof LinkedFeatureElement )
      return ImageProvider.IMAGE_FEATURE_LINKED;

    if( element instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) element;
      if( GeometryUtilities.isPointGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_POINT;
      if( GeometryUtilities.isMultiPointGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_MULTIPOINT;
      if( GeometryUtilities.isLineStringGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_LINE;
      if( GeometryUtilities.isMultiLineStringGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_MULTILINE;
      if( GeometryUtilities.isPolygonGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_POLYGON;
      if( GeometryUtilities.isMultiPolygonGeometry( vpt ) )
        return ImageProvider.IMAGE_GEOM_PROP_MULTIPOLYGON;
    }

    if( GeometryUtilities.getPolygonClass().isAssignableFrom( element.getClass() ) )
      return ImageProvider.IMAGE_GEOM_PROP_POLYGON;

    return null;
  }

  @Override
  public String getText( final Object element )
  {
    if( element instanceof GMLWorkspace )
      return "GML"; //$NON-NLS-1$

    if( element instanceof Feature )
      return FeatureHelper.getAnnotationValue( (Feature) element, IAnnotation.ANNO_LABEL );

    if( element instanceof FeatureAssociationTypeElement )
    {
      final IAnnotation annotation = ((FeatureAssociationTypeElement) element).getPropertyType().getAnnotation();
      if( annotation != null )
        return annotation.getLabel();
      return "<-> "; //$NON-NLS-1$
    }

    if( element instanceof LinkedFeatureElement )
    {
      final Feature decoratedFeature = ((LinkedFeatureElement) element).getDecoratedFeature();
      return "-> " + getText( decoratedFeature ); //$NON-NLS-1$
    }

    if( element instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) element;
      return vpt.getValueClass().getName().replaceAll( ".+\\.", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if( element instanceof GM_Object )
      return element.getClass().getName().replaceAll( ".+\\.", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    if( element == null )
      return "null"; //$NON-NLS-1$

    return element.toString();
  }

  /**
   * This function tries to obtain the qname of the element.
   *
   * @param element
   *          The element.
   * @return The qname of the element or null.
   */
  private QName getQName( final Object element )
  {
    if( element instanceof Feature )
    {
      final IFeatureType featureType = ((Feature) element).getFeatureType();
      return featureType.getQName();
    }
    else if( element instanceof FeatureAssociationTypeElement )
    {
      final FeatureAssociationTypeElement fate = (FeatureAssociationTypeElement) element;
      return fate.getPropertyType().getQName();
    }
    else if( element instanceof LinkedFeatureElement )
    {
      final LinkedFeatureElement linkedFeature = (LinkedFeatureElement) element;
      final Feature decoratedFeature = linkedFeature.getDecoratedFeature();
      if( decoratedFeature == null )
        return null;

      final IFeatureType featureType = decoratedFeature.getFeatureType();
      if( featureType == null )
        return null;

      return featureType.getQName();
    }

    return null;
  }
}