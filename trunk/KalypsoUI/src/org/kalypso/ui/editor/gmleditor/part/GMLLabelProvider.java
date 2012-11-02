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

import javax.xml.namespace.QName;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
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
  // IMPORTANT: we are using a static resource manager here that will never be disposed here, because
  // the hasing of the decorated images depends on the base image instances. If create new images for
  // every label provider, the decorated images will add up leading to a resource leak.
  private static LocalResourceManager m_resourceManager;

  private final ILabelProviderListener m_decoratorListener = new ILabelProviderListener()
  {
    @Override
    public void labelProviderChanged( final LabelProviderChangedEvent event )
    {
      handleDecorationChanged( event );
    }
  };

  private final IDecoratorManager m_decoratorManager;

  public GMLLabelProvider( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();

    m_decoratorManager = workbench.getDecoratorManager();

    synchronized( workbench )
    {
      if( m_resourceManager == null )
        m_resourceManager = new LocalResourceManager( JFaceResources.getResources( workbench.getDisplay() ) );
    }

    // TODO: check if this is ok? We change the global decoration context here...
    ((DecorationContext)DecorationContext.DEFAULT_CONTEXT).putProperty( IDecoration.ENABLE_REPLACE, Boolean.TRUE );

    m_decoratorManager.addListener( m_decoratorListener );
  }

  @Override
  public void dispose( )
  {
    m_decoratorManager.removeListener( m_decoratorListener );

    super.dispose();

    // IMPORTANT: not disposes, see above
    // m_resourceManager.dispose();
  }

  @Override
  public Image getImage( final Object element )
  {
    /* Get the descriptor. */
    final ImageDescriptor descriptor = getDescriptor( element );
    if( descriptor == null )
      return null;

    /* If its image is already there, take it. */
    final Image rawImage = m_resourceManager.createImage( descriptor );

    return m_decoratorManager.decorateImage( rawImage, element );
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
      final IValuePropertyType vpt = (IValuePropertyType)element;
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
    final String rawText = getRawText( element );

    // m_decoratorManager.addListener( null );

    return m_decoratorManager.decorateText( rawText, element );
  }

  private String getRawText( final Object element )
  {
    if( element instanceof GMLWorkspace )
      return "GML"; //$NON-NLS-1$

    if( element instanceof Feature )
      return FeatureHelper.getAnnotationValue( (Feature)element, IAnnotation.ANNO_LABEL );

    if( element instanceof FeatureAssociationTypeElement )
    {
      final IAnnotation annotation = ((FeatureAssociationTypeElement)element).getPropertyType().getAnnotation();
      if( annotation != null )
        return annotation.getLabel();
      return "<-> "; //$NON-NLS-1$
    }

    if( element instanceof LinkedFeatureElement )
    {
      final Feature decoratedFeature = ((LinkedFeatureElement)element).getDecoratedFeature();
      return "-> " + getText( decoratedFeature ); //$NON-NLS-1$
    }

    if( element instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType)element;
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
      final IFeatureType featureType = ((Feature)element).getFeatureType();
      return featureType.getQName();
    }
    else if( element instanceof FeatureAssociationTypeElement )
    {
      final FeatureAssociationTypeElement fate = (FeatureAssociationTypeElement)element;
      return fate.getPropertyType().getQName();
    }
    else if( element instanceof LinkedFeatureElement )
    {
      final LinkedFeatureElement linkedFeature = (LinkedFeatureElement)element;
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

  protected void handleDecorationChanged( final LabelProviderChangedEvent event )
  {
    // FIXME: we are reactnig to ANY event in the workbench..., OK?

    fireLabelProviderChanged( event );
  }
}