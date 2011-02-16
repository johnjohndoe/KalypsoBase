/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package de.openali.odysseus.chart.factory.util;

import jregex.Pattern;
import jregex.RETokenizer;

import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chartconfig.x020.ChildLayerType;
import de.openali.odysseus.chartconfig.x020.DerivedLayerType;
import de.openali.odysseus.chartconfig.x020.LayerRefernceType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayersType;

/**
 * @author Dirk Kuch
 */
public final class DerivedLayerTypeHelper
{
  private DerivedLayerTypeHelper( )
  {
  }

  public static LayerType buildDerivedLayerType( final DerivedLayerType derivedLayerType, final LayerType baseLayerType )
  {
    final LayerType clonedLayerType = (LayerType) baseLayerType.copy();
    DerivedLayerTypeHelper.updateLayerTypeSetttings( clonedLayerType, derivedLayerType );

    // replace "overwritten" / modified child layer instances
    final ChildLayerType[] childLayerTypes = derivedLayerType.getChildLayerArray();
    for( final ChildLayerType childLayerType : childLayerTypes )
    {
      final LayerType child = findChildLayerType( clonedLayerType, childLayerType.getRef() );
      DerivedLayerTypeHelper.updateLayerTypeSettings( child, childLayerType );
    }

    return clonedLayerType;
  }

  private static LayerType findChildLayerType( final LayerType parent, final String identifier )
  {
    final LayersType layersType = parent.getLayers();
    if( Objects.isNull( layersType ) )
      throw new IllegalStateException( String.format( "parent layer '%s' doesn't contains child layer '%s'", parent.getId(), identifier ) );

    final LayerType[] layers = layersType.getLayerArray();
    for( final LayerType layerType : layers )
    {
      if( identifier.equals( layerType.getId() ) )
        return layerType;
    }

    final LayerRefernceType[] references = layersType.getLayerReferenceArray();
    for( final LayerRefernceType reference : references )
    {
      final RETokenizer tokenizer = new RETokenizer( new Pattern( ".*#" ), reference.getUrl() );
      final String referencedLayerIdentifier = tokenizer.nextToken();

      if( identifier.equals( referencedLayerIdentifier ) )
        throw new IllegalStateException( "Updating of derived child layer references is not possible." );
    }

    final DerivedLayerType[] derivedLayers = layersType.getDerivedLayerArray();
    for( final DerivedLayerType derivedLayer : derivedLayers )
    {
      final LayerRefernceType reference = derivedLayer.getLayerReference();
      final RETokenizer tokenizer = new RETokenizer( new Pattern( ".*#" ), reference.getUrl() );
      final String referencedLayerIdentifier = tokenizer.nextToken();

      if( identifier.equals( referencedLayerIdentifier ) )
        throw new IllegalStateException( "Updating of derived child layers is not possible." );
    }

    return null;
  }

  private static void updateLayerTypeSettings( final LayerType layer, final ChildLayerType update )
  {
    layer.setLegend( update.getLegend() );

    if( update.isSetTitle() )
      layer.setTitle( update.getTitle() );

    if( update.isSetDescription() )
      layer.setDescription( update.getDescription() );

    if( update.isSetStyles() )
      layer.setStyles( update.getStyles() );

    if( update.isSetParameters() )
      LayerTypeHelper.appendParameters( layer, update.getParameters() );
  }

  private static void updateLayerTypeSetttings( final LayerType layer, final DerivedLayerType update )
  {
    layer.setId( update.getId() );
    layer.setLegend( update.getLegend() );

    if( update.isSetTitle() )
      layer.setTitle( update.getTitle() );

    if( update.isSetDescription() )
      layer.setDescription( update.getDescription() );

    if( update.isSetVisible() )
      layer.setVisible( update.getVisible() );

    if( update.isSetStyles() )
      layer.setStyles( update.getStyles() );

    if( update.isSetParameters() )
      LayerTypeHelper.appendParameters( layer, update.getParameters() );
  }

}
