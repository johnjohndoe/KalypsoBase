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
package org.kalypso.contribs.eclipse.core.commands.operations;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * @author Gernot Belger
 */
public class CompositeOperation extends AbstractOperation implements ICompositeOperation
{
  private final Collection<IUndoableOperation> m_children = new LinkedHashSet<IUndoableOperation>();

  public CompositeOperation( final String label )
  {
    super( label );
  }

  /**
   * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor, final IAdaptable info ) throws ExecutionException
  {
    final String taskMessage = String.format( "Executing: %s", getLabel() );
    monitor.beginTask( taskMessage, m_children.size() );

    final MultiStatus result = new MultiStatus( EclipseRCPContributionsPlugin.ID, -1, taskMessage, null );

    for( final IUndoableOperation child : m_children )
    {
      monitor.subTask( child.getLabel() );

      final IStatus staus = child.execute( new SubProgressMonitor( monitor, 1 ), info );
      result.add( staus );
    }

    return result;
  }

  /**
   * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
   */
  @Override
  public IStatus redo( final IProgressMonitor monitor, final IAdaptable info ) throws ExecutionException
  {
    final String taskMessage = String.format( "Redoing: %s", getLabel() );
    monitor.beginTask( taskMessage, m_children.size() );

    final MultiStatus result = new MultiStatus( EclipseRCPContributionsPlugin.ID, -1, taskMessage, null );

    for( final IUndoableOperation child : m_children )
    {
      monitor.subTask( child.getLabel() );

      final IStatus staus = child.redo( new SubProgressMonitor( monitor, 1 ), info );
      result.add( staus );
    }

    return result;
  }

  /**
   * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
   */
  @Override
  public IStatus undo( final IProgressMonitor monitor, final IAdaptable info ) throws ExecutionException
  {
    final String taskMessage = String.format( "Undoing: %s", getLabel() );
    monitor.beginTask( taskMessage, m_children.size() );

    final MultiStatus result = new MultiStatus( EclipseRCPContributionsPlugin.ID, -1, taskMessage, null );

    for( final IUndoableOperation child : m_children )
    {
      monitor.subTask( child.getLabel() );

      final IStatus staus = child.undo( new SubProgressMonitor( monitor, 1 ), info );
      result.add( staus );
    }

    return result;
  }

  /**
   * @see org.eclipse.core.commands.operations.ICompositeOperation#add(org.eclipse.core.commands.operations.IUndoableOperation)
   */
  @Override
  public void add( final IUndoableOperation operation )
  {
    m_children.add( operation );

    final IUndoContext[] contexts = operation.getContexts();
    for( final IUndoContext childContext : contexts )
      addContext( childContext );
  }

  /**
   * @see org.eclipse.core.commands.operations.ICompositeOperation#remove(org.eclipse.core.commands.operations.IUndoableOperation)
   */
  @Override
  public void remove( final IUndoableOperation operation )
  {
    final boolean remove = m_children.remove( operation );
    if( remove )
      operation.dispose();
  }

}
