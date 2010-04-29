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
package org.kalypso.ui.editor.gmleditor.util.command;

import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.command.FeatureChangeModellEvent;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;


public class AddRelationCommand implements ICommand
{
  private final Feature m_srcFE;

  private int m_pos = 0;

  private final IRelationType m_propName;

  private final Feature m_linkFeature;

  public AddRelationCommand( final Feature srcFE, final IRelationType propertyName, final int pos, final Feature destFE )
  {
    m_srcFE = srcFE;
    m_propName = propertyName;
    m_pos = pos;
    m_linkFeature = destFE;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#isUndoable()
   */
  public boolean isUndoable( )
  {
    return true;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#process()
   */
  public void process( ) throws Exception
  {
    final GMLWorkspace workspace = m_srcFE.getWorkspace();
    workspace.addFeatureAsAggregation( m_srcFE, m_propName, m_pos, m_linkFeature.getId() );
    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, m_srcFE, m_linkFeature, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );
    workspace.fireModellEvent( new FeatureChangeModellEvent( workspace, new FeatureChange[] { new FeatureChange( m_srcFE, m_propName, null ) } ) );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  public void redo( ) throws Exception
  {
    process();
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  public void undo( ) throws Exception
  {
    if( m_linkFeature == null )
      return;

    final GMLWorkspace workspace = m_srcFE.getWorkspace();
    workspace.removeLinkedAsAggregationFeature( m_srcFE, m_propName, m_linkFeature.getId() );
    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, m_srcFE, m_linkFeature, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE ) );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  public String getDescription( )
  {
    return null;
  }
}