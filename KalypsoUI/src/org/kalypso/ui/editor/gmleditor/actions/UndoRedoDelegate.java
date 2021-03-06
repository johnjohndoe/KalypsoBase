/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.gmleditor.actions;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.editor.AbstractGisEditorActionDelegate;
import org.kalypso.ui.editor.gmleditor.part.GmlEditor;
import org.kalypso.ui.editor.gmleditor.part.GmlTreeView;
import org.kalypso.ui.editor.mapeditor.WidgetActionPart;
import org.kalypso.util.command.CommandJob;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * @author belger
 */
public class UndoRedoDelegate extends AbstractGisEditorActionDelegate
{
  private final boolean m_undo;

  public UndoRedoDelegate( final boolean undo )
  {
    m_undo = undo;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run( final IAction action )
  {
    final WidgetActionPart part = getPart();
    if( part == null )
      return;

    // WARNING: Because of the following cast, we can only use
    // this delegate with the GMLEditor.
    final GmlEditor editor = (GmlEditor)part.getPart();
    if( editor == null )
      return;

    final CommandableWorkspace workspace = editor.getTreeView().getWorkspace();
    final JobExclusiveCommandTarget target = new JobExclusiveCommandTarget( workspace, null );
    final ISchedulingRule schedulingRule = target.getSchedulingRule();

    if( m_undo && workspace.canUndo() || !m_undo && workspace.canRedo() )
      new CommandJob( null, workspace, schedulingRule, null, m_undo ? CommandJob.UNDO : CommandJob.REDO );

    refreshAction( action, getSelection() );
  }

  @Override
  protected void refreshAction( final IAction action, final ISelection selection )
  {
    boolean bEnabled = false;

    final WidgetActionPart part = getPart();
    if( part == null )
      return;

    // WARNING: Because of the following cast, we can only use
    // this delegate with the GMLEditor.
    final GmlEditor editor = (GmlEditor)part.getPart();
    if( editor != null )
    {
      final GmlTreeView treeView = editor.getTreeView();

      if( treeView != null )
      {
        final CommandableWorkspace workspace = treeView.getWorkspace();
        if( workspace != null )
          bEnabled = m_undo ? workspace.canUndo() : workspace.canRedo();
      }
    }

    action.setEnabled( bEnabled );
  }
}