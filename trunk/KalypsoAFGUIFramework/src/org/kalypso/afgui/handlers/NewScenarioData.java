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
package org.kalypso.afgui.handlers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOCase;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.commons.java.util.AbstractModelObject;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;

/**
 * @author Gernot Belger
 */
public class NewScenarioData extends AbstractModelObject
{
  public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

  public static final String PROPERTY_COMMENT = "comment"; //$NON-NLS-1$

  private final IScenario m_parentScenario;

  private String m_name;

  private String m_comment;

  public NewScenarioData( final IScenario parentScenario )
  {
    m_parentScenario = parentScenario;
  }

  public String getParentScenarioPath( )
  {
    if( m_parentScenario == null )
      return Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.6" ); //$NON-NLS-1$

    final StringBuilder path = new StringBuilder();

    IScenario segment = m_parentScenario;
    while( segment != null )
    {
      path.insert( 0, '/' );
      path.insert( 0, segment.getName() );

      final IScenario parentSegment = segment.getParentScenario();
      if( parentSegment == null )
      {
        path.insert( 0, '/' );
        path.insert( 0, segment.getProject().getName() );
      }

      segment = parentSegment;
    }

    return path.toString();
  }

  public String getName( )
  {
    return m_name;
  }

  public void setName( final String name )
  {
    final String oldValue = m_name;

    m_name = name;

    firePropertyChange( PROPERTY_NAME, oldValue, name );
  }

  public String getComment( )
  {
    return m_comment;
  }

  public void setComment( final String comment )
  {
    final String oldValue = m_comment;

    m_comment = comment;

    firePropertyChange( PROPERTY_COMMENT, oldValue, comment );
  }

  /**
   * Returns names of all sub scenarios that already exist. The names cannot be used for a new scenario.
   */
  public Set<String> getExistingNames( )
  {
    final Comparator<String> ignoreCaseComparator = new Comparator<String>()
    {
      @Override
      public int compare( final String o1, final String o2 )
      {
        return o1.compareToIgnoreCase( o2 );
      }
    };

    final Set<String> names = new TreeSet<>( ignoreCaseComparator );

    final IScenarioList derivedScenarios = m_parentScenario.getDerivedScenarios();

    final List<IScenario> scenarios = derivedScenarios.getScenarios();
    for( final IScenario scenario : scenarios )
      names.add( scenario.getName() );

    return Collections.unmodifiableSet( names );
  }

  /**
   * Returns names of all sub folders of the parent scenario. These folder (either sb scenarios or ordinary data
   * folders) cannot be used as new scenario.
   */
  public Set<String> getExistingFolders( )
  {
    final Comparator<String> ignoreCaseComparator = new Comparator<String>()
    {
      @Override
      public int compare( final String o1, final String o2 )
      {
        // REMARK: using same case sensitive rules as the current file system.
        return IOCase.SYSTEM.checkCompareTo( o1, o2 );
      }
    };

    final Set<String> folders = new TreeSet<>( ignoreCaseComparator );

    final IFolder folder = m_parentScenario.getFolder();

    try
    {
      final IResource[] members = folder.members();
      for( final IResource member : members )
      {
        if( member instanceof IFolder )
          folders.add( member.getName() );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      // error handling?
    }

    return Collections.unmodifiableSet( folders );
  }

  public IProject getProject( )
  {
    return m_parentScenario.getProject();
  }

  public IScenario getParentScenario( )
  {
    return m_parentScenario;
  }
}