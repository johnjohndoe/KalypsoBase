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
package org.kalypso.ui.editor.diagrameditor.actions;

import java.util.TimeZone;

import org.kalypso.commons.command.ICommand;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.template.obsdiagview.Obsdiagview.TitleFormat;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author schlienger
 */
public class ChangeDiagPropsCommand implements ICommand
{
  private final String m_diagramTitle;

  private final boolean m_showLegend;

  private final String m_legendTitle;

  private final String m_timezoneName;

  private final DiagView m_diag;

  private final String m_orgDiagramTitle;

  private final boolean m_orgShowLegend;

  private final String m_orgLegendTitle;

  private final String m_orgTimezoneName;

  private final TitleFormat m_orgDiagramTitleFormat;

  public ChangeDiagPropsCommand( final DiagView diag, final String diagramTitle, final boolean showLegend, final String legendTitle, final String timezoneName )
  {
    m_orgDiagramTitle = diag.getTitle();
    m_orgDiagramTitleFormat = diag.getTitleFormat();
    m_orgShowLegend = diag.isShowLegend();
    m_orgLegendTitle = diag.getLegendName();
    final TimeZone timezone = diag.getTimezone();
    m_orgTimezoneName = timezone == null ? null : timezone.getID();

    m_diag = diag;

    m_diagramTitle = diagramTitle;
    m_showLegend = showLegend;
    m_legendTitle = legendTitle;
    m_timezoneName = timezoneName;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#isUndoable()
   */
  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#process()
   */
  @Override
  public void process( ) throws Exception
  {
    m_diag.setTitle( m_diagramTitle, m_orgDiagramTitleFormat );
    m_diag.setShowLegend( m_showLegend );
    m_diag.setLegendName( m_legendTitle );

    final TimeZone tz = m_timezoneName == null ? null : TimeZone.getTimeZone( m_timezoneName );
    m_diag.setTimezone( tz );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  @Override
  public void redo( ) throws Exception
  {
    process();
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  @Override
  public void undo( ) throws Exception
  {
    m_diag.setTitle( m_orgDiagramTitle, m_orgDiagramTitleFormat );
    m_diag.setShowLegend( m_orgShowLegend );
    m_diag.setLegendName( m_orgLegendTitle );

    if( m_orgTimezoneName == null )
      m_diag.setTimezone( null );
    else
      m_diag.setTimezone( TimeZone.getTimeZone( m_orgTimezoneName ) );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.ChangeDiagPropsCommand.0" ); //$NON-NLS-1$
  }
}
