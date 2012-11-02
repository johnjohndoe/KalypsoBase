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
package org.kalypso.ui.editor.mapeditor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * <p>
 * Eclipse-Editor zum Editieren der GML-Gis-Templates.
 * </p>
 * 
 * @author Gernot Belger
 * @author Stefan Kurzbach
 */
public class GisMapEditor extends AbstractMapPart implements IEditorPart
{
  public static final String ID = "org.kalypso.ui.editor.mapeditor.GisMapEditor"; //$NON-NLS-1$

  private static final String OUTLINE_URI_TOOLBAR = "toolbar:org.kalypso.map.outline.GisMapEditor"; //$NON-NLS-1$

  private static final String OUTLINE_URI_MENU = "menu:org.kalypso.map.outline.GisMapEditor"; //$NON-NLS-1$

  private static final String OUTLINE_URI_POPUP = "popup:org.kalypso.map.outline.GisMapEditor"; //$NON-NLS-1$

  @Override
  public synchronized void createPartControl( final Composite parent )
  {
    super.createPartControl( parent );

    /* Add drop support */
    final Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };

    final JobExclusiveCommandTarget commandTarget = getCommandTarget();
    final GisTemplateMapModell mapModell = getMapModell();

    final Viewer mapViewer = new MapViewer( parent, getMapPanel() );

    final DropTargetListener dropListener = new GisMapDropAdapter( mapViewer, commandTarget, mapModell );

    final DropTarget dropTarget = new DropTarget( parent, DND.DROP_LINK );
    dropTarget.setTransfer( transfers );
    dropTarget.addDropListener( dropListener );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
    {
      final GisMapOutlinePage page = new GisMapOutlinePage( getCommandTarget() );
      page.addActionURI( OUTLINE_URI_TOOLBAR );
      page.addActionURI( OUTLINE_URI_MENU );
      // TODO: check we do not need the other popup-uri any more and should probably remove it
      page.addActionURI( OUTLINE_URI_POPUP );
      page.addActionURI( "popup:org.eclipse.ui.popup.any" ); //$NON-NLS-1$

      page.setMapPanel( getMapPanel() );
      return page;
    }

    return super.getAdapter( adapter );
  }
}