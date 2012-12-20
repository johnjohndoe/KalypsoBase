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
package org.kalypso.ogc.gml.command;

import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureProvider;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree_impl.model.feature.FeatureLinkUtils;
import org.kalypsodeegree_impl.model.feature.FeatureProvider;

public class AddLinkCommand implements ICommand
{
  private final IFeatureProvider m_srcFE;

  private int m_pos = 0;

  private final IRelationType m_propName;

  private final String m_href;

  public AddLinkCommand( final Feature srcFE, final IRelationType propertyName, final int pos, final String href )
  {
    this( new FeatureProvider( srcFE ), propertyName, pos, href );
  }

  public AddLinkCommand( final IFeatureProvider source, final IRelationType propertyName, final int pos, final String href )
  {
    m_srcFE = source;
    m_propName = propertyName;
    m_pos = pos;
    m_href = href;
  }

  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  @Override
  public void process( ) throws Exception
  {
    final Feature source = m_srcFE.getFeature();
    final GMLWorkspace workspace = source.getWorkspace();

    FeatureLinkUtils.insertLink( source, m_propName, m_pos, m_href );

    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, source, (Feature) null, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

    workspace.fireModellEvent( new FeatureChangeModellEvent( workspace, new FeatureChange[] { new FeatureChange( source, m_propName, null ) } ) );
  }

  @Override
  public void redo( ) throws Exception
  {
    process();
  }

  @Override
  public void undo( ) throws Exception
  {
    final Feature source = m_srcFE.getFeature();
    final GMLWorkspace workspace = source.getWorkspace();

    // FIXME:
    // workspace.removeLinkedAsAggregationFeature( source, m_propName, m_linkFeature.getId() );

    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, source, (Feature) null, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE ) );
  }

  @Override
  public String getDescription( )
  {
    return null;
  }
}