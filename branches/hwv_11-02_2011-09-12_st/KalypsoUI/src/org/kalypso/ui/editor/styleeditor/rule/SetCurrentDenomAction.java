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
package org.kalypso.ui.editor.styleeditor.rule;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;

/**
 * @author Gernot Belger
 */
public class SetCurrentDenomAction extends Action
{
  private final Text m_denomField;

  private final IMapPanel m_mapPanel;

  public SetCurrentDenomAction( final Text denomField )
  {
    m_denomField = denomField;

    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_GET_SCALE );
    setToolTipText( MessageBundle.STYLE_EDITOR_SCALE );

    m_mapPanel = findPanel();
    setEnabled( m_mapPanel != null );
  }

  private static IMapPanel findPanel( )
  {
    final IEvaluationService service = (IEvaluationService) PlatformUI.getWorkbench().getService( IEvaluationService.class );
    final IEvaluationContext state = service.getCurrentState();
    return MapHandlerUtils.getMapPanel( state );

// final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
// final IEditorPart editor = window.getActivePage().getActiveEditor();
// if( editor instanceof GisMapEditor )
// {
// // TODO: get from current context instead!
// final GisMapEditor gisMapEditor = (GisMapEditor) editor;
// return gisMapEditor.getMapPanel().getCurrentScale();
// }
//
// // TODO Auto-generated method stub
// return null;
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Double currentScale = getCurrentScale();
    if( currentScale == null )
      return;

    final String newText = String.format( "%.1f", currentScale );//$NON-NLS-1$
    m_denomField.setText( newText );
    // Need to set/un-set focus, because the value will only be committed on focus loss
    m_denomField.setFocus();

    if( event.widget instanceof Control )
      ((Control) event.widget).setFocus();
  }

  public Double getCurrentScale( )
  {
    if( m_mapPanel == null )
      return null;

    return m_mapPanel.getCurrentScale();
  }
}