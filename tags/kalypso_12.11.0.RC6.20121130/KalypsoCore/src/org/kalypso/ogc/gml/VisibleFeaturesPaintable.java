package org.kalypso.ogc.gml;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.ogc.gml.painter.IStylePaintable;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.IncompatibleGeometryTypeException;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.displayelements.ILabelPlacementStrategy;

/**
 * Paintable that collects all features that are within a given extent.
 *
 * @author Gernot Belger
 */
final class VisibleFeaturesPaintable implements IStylePaintable
{
  private final Set<Feature> m_features = new LinkedHashSet<>();

  private final GM_Envelope m_env;

  VisibleFeaturesPaintable( final GM_Envelope env )
  {
    m_env = env;
  }

  /**
   * Returns the (unmodifiable) collection of found features.
   */
  Collection<Feature> getVisibleFeatures( )
  {
    return Collections.unmodifiableCollection( m_features );
  }

  @Override
  public void paint( final Feature feature, final Symbolizer symbolizer, final IProgressMonitor newChild )
  {
    try
    {
      final GM_Object[] geometries = DisplayElementFactory.findGeometries( feature, symbolizer );

      for( final GM_Object gm : geometries )
      {
        // final GM_Envelope envelope = feature.getEnvelope();
        final GM_Envelope envelope = gm.getEnvelope();
        if( envelope != null && m_env.intersects( envelope ) )
        {
          m_features.add( feature );
        }
      }
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
    catch( final IncompatibleGeometryTypeException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public Double getScale( )
  {
    return null;
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    return m_env;
  }

  @Override
  public boolean shouldPaintFeature( final Feature feature )
  {
    return true;
  }

  @Override
  public ILabelPlacementStrategy createLabelStrategy( )
  {
    return null;
  }

  @Override
  public void paintLabels( final ILabelPlacementStrategy strategy )
  {
  }
}