package org.kalypso.ogc.gml.mapmodel.visitor;

import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;

/**
 * Decides, if a theme is used to calculate the max extent of a map.
 * 
 * @author Gernot Belger
 */
public final class ThemeUsedForMaxExtentPredicate implements IKalypsoThemePredicate
{
  @Override
  public boolean decide( final IKalypsoTheme theme )
  {
    if( !theme.isVisible() )
      return false;

    final String property = theme.getProperty( IKalypsoTheme.PROPERTY_USE_IN_FULL_EXTENT, Boolean.TRUE.toString() );
    return Boolean.parseBoolean( property );
  }
}
