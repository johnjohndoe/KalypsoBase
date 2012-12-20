/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.kml.export.convert;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

/**
 * @author Gernot Belger
 */
public class GraphicImageExporter
{
  private final Map<PointSymbolizer, String> m_pathes = new HashMap<>();

  private final File m_exportDir;

  private final GeoTransform m_transform;

  private final String m_baseName;

  public GraphicImageExporter( final File exportDir, final String baseName, final GeoTransform transform )
  {
    m_exportDir = exportDir;
    m_baseName = baseName;
    m_transform = transform;
  }

  public String getImagePath( final PointSymbolizer symbolizer, final Feature feature ) throws IOException, FilterEvaluationException
  {
    if( m_pathes.containsKey( symbolizer ) )
      return m_pathes.get( symbolizer );

    final UOM uom = symbolizer.getUom();

    final Graphic graphic = symbolizer.getGraphic();

    final BufferedImage image = graphic.getAsImage( feature, uom, m_transform );

    final File imageFile = FileUtilities.createNewUniqueFile( m_baseName, ".png", m_exportDir ); //$NON-NLS-1$

    ImageIO.write( image, "PNG", imageFile ); //$NON-NLS-1$

    final String path = imageFile.getName();

    m_pathes.put( symbolizer, path );

    return path;
  }
}