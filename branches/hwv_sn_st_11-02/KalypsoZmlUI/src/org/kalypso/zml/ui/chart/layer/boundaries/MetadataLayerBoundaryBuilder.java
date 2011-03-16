/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.zml.ui.chart.layer.boundaries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jregex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.metadata.IMetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.ui.core.kod.KodUtils;

import de.openali.odysseus.chart.factory.config.StyleFactory;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chartconfig.x020.LayerType;

/**
 * @author Dirk Kuch
 */
public class MetadataLayerBoundaryBuilder implements ICoreRunnableWithProgress
{
  private final MetadataList m_metaData;

  private final LayerType m_layerType;

  private final Set<IMetadataLayerBoundary> m_boundaries = new HashSet<IMetadataLayerBoundary>();

  private final IParameterContainer m_parameters;

  private final IStyleSet m_styleSet;

  public MetadataLayerBoundaryBuilder( final MetadataList metaData, final LayerType layerType )
  {
    m_metaData = metaData;
    m_layerType = layerType;
    m_parameters = null;
    m_styleSet = StyleFactory.createStyleSet( layerType.getStyles() );
  }

  public MetadataLayerBoundaryBuilder( final MetadataList metadata, final IParameterContainer parameters, final IStyleSet styleSet )
  {
    m_metaData = metadata;
    m_parameters = parameters;
    m_styleSet = styleSet;
    m_layerType = null;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    final String label = getLabel();
    final String labelTokenizer = getLabelTokenizer();

    final String[] keys = MetadataBoundary.findBoundaryKeys( m_metaData, new Pattern( getGrenzwertPattern() ) );
    if( ArrayUtils.isEmpty( keys ) )
      return Status.OK_STATUS;

    final MetadataBoundary[] boundaries = MetadataBoundary.getBoundaries( m_metaData, keys );
    for( final MetadataBoundary boundary : boundaries )
    {
      try
      {
        if( boundary.getValue().doubleValue() == 0.0 )
          continue;

        m_boundaries.add( new KodBoundaryLayer( boundary, label, labelTokenizer, m_styleSet ) );
      }
      catch( final Throwable t )
      {
        final String msg = String.format( "Auswertung des Metadatums: \"%s\" fehlgeschlagen.", boundary.getName() );
        statis.add( StatusUtilities.createExceptionalErrorStatus( msg, t ) );
      }
    }

    return StatusUtilities.createStatus( statis, "Auflösen von Grenzwerten" );
  }

  private String getLabelTokenizer( )
  {
    if( m_layerType != null )
      return KodUtils.getParameter( m_layerType.getProvider(), "labelTokenizer" ); //$NON-NLS-1$

    return m_parameters.getParameterValue( "labelTokenizer", null ); //$NON-NLS-1$
  }

  private String getLabel( )
  {
    if( m_layerType != null )
      return KodUtils.getParameter( m_layerType.getProvider(), "label" ); //$NON-NLS-1$

    return m_parameters.getParameterValue( "label", null ); //$NON-NLS-1$
  }

  private String getGrenzwertPattern( )
  {
    if( m_layerType != null )
      return KodUtils.getParameter( m_layerType.getProvider(), "grenzwert" ); //$NON-NLS-1$

    return m_parameters.getParameterValue( "grenzwert", null ); //$NON-NLS-1$
  }

  public IMetadataLayerBoundary[] getBoundaries( )
  {
    return m_boundaries.toArray( new IMetadataLayerBoundary[] {} );
  }

  public IMetadataLayerBoundary[] getBoundaries( final String type )
  {
    final Set<IMetadataLayerBoundary> boundaryLayers = new LinkedHashSet<IMetadataLayerBoundary>();
    final IMetadataLayerBoundary[] boundaries = getBoundaries();

    for( final IMetadataLayerBoundary boundaryLayer : boundaries )
    {
      final IMetadataBoundary boundary = boundaryLayer.getBoundary();
      if( boundary.getParameterType().equals( type ) )
        boundaryLayers.add( boundaryLayer );
    }

    return boundaryLayers.toArray( new IMetadataLayerBoundary[] {} );
  }
}
