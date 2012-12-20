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
package org.kalypso.gml.ui.internal.shape;

import java.util.Iterator;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class RemoveFieldAction extends Action
{
  private final IObservableList m_fieldList;

  private final TableViewer m_viewer;

  public RemoveFieldAction( final TableViewer viewer, final IObservableList fieldList )
  {
    super( Messages.getString( "RemoveFieldAction_0" ) ); //$NON-NLS-1$

    final ImageDescriptor image = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.SHAPE_FILE_NEW_REMOVE_FIELD );
    setImageDescriptor( image );

    setToolTipText( Messages.getString( "RemoveFieldAction_1" ) ); //$NON-NLS-1$

    m_viewer = viewer;
    m_fieldList = fieldList;
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();

    final Shell shell = m_viewer.getControl().getShell();
    if( selection.isEmpty() )
    {
      MessageDialog.openInformation( shell, getText(), Messages.getString( "RemoveFieldAction_2" ) ); //$NON-NLS-1$
      return;
    }
    else if( !MessageDialog.openConfirm( shell, getText(), Messages.getString( "RemoveFieldAction_3" ) ) ) //$NON-NLS-1$
      return;

    for( final Iterator< ? > iterator = selection.iterator(); iterator.hasNext(); )
    {
      final Object element = iterator.next();
      m_fieldList.remove( element );
    }
  }
}
