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
package org.kalypso.ogc.gml.table.command;

import org.kalypso.commons.command.ICommand;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.table.LayerTableStyle;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * TODO: does not yet handle the label/tooltip properties of the template
 * 
 * @author Gernot Belger
 */
public class SetColumnVisibleCommand implements ICommand
{
  private final GMLXPath m_propertyPath;

  private final boolean m_bVisible;

  private final LayerTableViewer m_viewer;

  private final boolean m_wasEditable;

  private final int m_oldWidth;

  private final String m_alignment;

  private final String m_format;

  private final String m_modifier;

  public SetColumnVisibleCommand( final LayerTableViewer viewer, final GMLXPath propertyPath, final String alignment, final String format, final boolean bVisible )
  {
    m_viewer = viewer;
    m_propertyPath = propertyPath;
    m_alignment = alignment;
    m_format = format;
    m_modifier = null;
    m_bVisible = bVisible;
    m_wasEditable = viewer.isEditable( propertyPath );
    m_oldWidth = viewer.getWidth( propertyPath );
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
    doIt( m_viewer, m_propertyPath, m_bVisible, 100, m_alignment, m_format, m_modifier, true );
  }

  @Override
  public void redo( ) throws Exception
  {
    doIt( m_viewer, m_propertyPath, m_bVisible, 100, m_alignment, m_format, m_modifier, true );
  }

  @Override
  public void undo( ) throws Exception
  {
    doIt( m_viewer, m_propertyPath, !m_bVisible, m_oldWidth, m_alignment, m_format, m_modifier, m_wasEditable );
  }

  @Override
  public String getDescription( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.table.command.SetColumnVisibleCommand.0" ) + m_propertyPath + "' " + (m_bVisible ? Messages.getString( "org.kalypso.ogc.gml.table.command.SetColumnVisibleCommand.2" ) : Messages.getString( "org.kalypso.ogc.gml.table.command.SetColumnVisibleCommand.3" )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  private void doIt( final LayerTableViewer viewer, final GMLXPath propertyPath, final boolean bVisible, final int width, final String alignment, final String format, final String modifier, final boolean editable )
  {
    m_viewer.getControl().getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( bVisible )
          viewer.addColumn( propertyPath, null, null, editable, width, alignment, format, modifier, true, new LayerTableStyle( null ) );
        else
          viewer.removeColumn( propertyPath );
      }
    } );
  }
}