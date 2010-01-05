package org.kalypso.ogc.gml.painter;

import java.awt.Graphics;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Gernot Belger
 */
public final class FeatureThemePaintable implements IStylePaintable
{
  private final Graphics m_graphics;

  private final Boolean m_paintSelected;

  private final IFeatureSelectionManager m_selectionManager;

  private final Double m_scale;

  private final GM_Envelope m_boundingBox;

  private final GeoTransform m_p;

  public FeatureThemePaintable( final GeoTransform worldToScreen, final Graphics graphics, final IFeatureSelectionManager selectionManager, final Boolean paintSelected )
  {
    m_p = worldToScreen;
    m_graphics = graphics;
    m_selectionManager = selectionManager;
    m_paintSelected = paintSelected;

    // Performance: get scale and bbox once, else they will be costly recalculated very often
    m_scale = worldToScreen.getScale();
    m_boundingBox = worldToScreen.getSourceRect();
  }

  public void paint( final DisplayElement displayElement, final IProgressMonitor paintMonitor ) throws CoreException
  {
    displayElement.paint( m_graphics, m_p, paintMonitor );

    // DEBUG output to show feature envelope: TODO: put into tracing option
    // final GM_Envelope envelope = displayElement.getFeature().getEnvelope();
    // if( envelope != null )
    // {
    // GM_Position destPointMin = projection.getDestPoint( envelope.getMin() );
    // GM_Position destPointMax = projection.getDestPoint( envelope.getMax() );
    //
    // GM_Envelope_Impl env = new GM_Envelope_Impl( destPointMin, destPointMax, null );
    //
    // graphics.drawRect( (int) env.getMin().getX(), (int) env.getMin().getY(), (int) env.getWidth(), (int)
    // env.getHeight()
    // );
    // }
  }

  @Override
  public Double getScale( )
  {
    return m_scale;
  }

  /**
   * @see org.kalypso.ogc.gml.IPaintDelegate#getBoundingBox()
   */
  @Override
  public GM_Envelope getBoundingBox( )
  {
    return m_boundingBox;
  }

  /**
   * @see org.kalypso.ogc.gml.painter.IStylePaintable#shouldPaintFeature(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public boolean shouldPaintFeature( final Feature feature )
  {
    /* Is selected/unselected ? */
    if( m_paintSelected == null )
      return true;

    final boolean featureIsSelected = m_selectionManager.isSelected( feature );

    if( featureIsSelected && m_paintSelected )
      return true;

    if( !featureIsSelected && !m_paintSelected )
      return true;

    return false;
  }
}