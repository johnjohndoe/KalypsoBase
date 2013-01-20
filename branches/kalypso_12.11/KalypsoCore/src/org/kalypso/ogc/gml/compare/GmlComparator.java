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
package org.kalypso.ogc.gml.compare;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollectorWithTime;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;

/**
 * Compares two {@link org.kalypsodeegree.model.feature.GMLWorkspace}s.
 * 
 * @author Holger Albert
 * @author Gernot Belger
 */
public class GmlComparator
{
  private final GMLXPath[] m_listPaths;

  private final QName[] m_uniqueProperties;

  public GmlComparator( final GMLXPath[] listPaths, final QName[] uniqueProperties )
  {
    m_listPaths = listPaths;
    m_uniqueProperties = uniqueProperties;
  }

  // FIXME: unsufficient exception handling!
  public IStatus compare( final GMLWorkspace referenceWorkspace, final GMLWorkspace selectedWorkspace ) throws GMLXPathException
  {
    /* The status collector. */
    final IStatusCollector log = new StatusCollectorWithTime( KalypsoCorePlugin.getID() );

    /* Get the models. */
    final Feature referenceModel = referenceWorkspace.getRootFeature();
    final Feature selectedModel = selectedWorkspace.getRootFeature();

    // FIXME: properties of root features are not compared :-(
    // TODO: we need a better abstraction for the lists/properties

    /* Compare each configured list. */
    for( int i = 0; i < m_listPaths.length; i++ )
    {
      final GMLXPath listPath = m_listPaths[i];
      final QName uniqueProperty = m_uniqueProperties[i];

      final IStatus listStatus = compareList( referenceModel, selectedModel, listPath, uniqueProperty );
      log.add( listStatus );
    }

    return log.asMultiStatus( "Comparison" );
  }

  private IStatus compareList( final Feature referenceFeature, final Feature selectedFeature, final GMLXPath listPath, final QName uniqueProperty ) throws GMLXPathException
  {
    final FeatureListComparator comparator = new FeatureListComparator( referenceFeature, selectedFeature, listPath, uniqueProperty );
    return comparator.compareList();
  }
}