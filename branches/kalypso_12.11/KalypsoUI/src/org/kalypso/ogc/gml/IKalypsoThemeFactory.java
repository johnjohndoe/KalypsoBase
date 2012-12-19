package org.kalypso.ogc.gml;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.types.StyledLayerType;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * A factory that creates different types of themes depending on a link type.
 * 
 * @author Stefan Kurzbach
 */
public interface IKalypsoThemeFactory
{
  IKalypsoTheme createTheme( final I10nString layerName, final StyledLayerType layerType, final URL context, final IMapModell mapModell, final IFeatureSelectionManager selectionManager ) throws CoreException;

  StyledLayerType createLayerType( IKalypsoTheme theme );

  void configureLayer( IKalypsoTheme theme, String id, GM_Envelope bbox, String srsName, StyledLayerType layer, IProgressMonitor monitor ) throws CoreException;
}
