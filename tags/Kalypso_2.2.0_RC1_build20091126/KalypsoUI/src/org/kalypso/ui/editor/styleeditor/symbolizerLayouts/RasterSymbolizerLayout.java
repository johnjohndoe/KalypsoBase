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
package org.kalypso.ui.editor.styleeditor.symbolizerLayouts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.colorMapEntryTable.ColorMapEntryTable;
import org.kalypso.ui.editor.styleeditor.panels.ModeSelectionComboPanel;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author F.Lindemann
 */

public class RasterSymbolizerLayout extends AbstractSymbolizerLayout
{

  public RasterSymbolizerLayout( final Composite composite, final Symbolizer symbolizer, final IKalypsoUserStyle userStyle )
  {
    super( composite, symbolizer, userStyle );
  }

  @Override
  public void draw( )
  {
    final RasterSymbolizer rasterSymbolizer = (RasterSymbolizer) m_symbolizer;

    final GridLayout compositeLayout = new GridLayout();
    compositeLayout.marginHeight = 2;

    // ***** ColorMap Group
    final Group colorMapGroup = new Group( m_composite, SWT.NULL );
    final GridData colorMapGroupData = new GridData();
    colorMapGroupData.widthHint = 210;
    colorMapGroupData.heightHint = 246;
    colorMapGroup.setLayoutData( colorMapGroupData );
    colorMapGroup.setLayout( compositeLayout );
    colorMapGroup.layout();
    colorMapGroup.setText( MessageBundle.STYLE_EDITOR_COLORMAP );

    // ***** ComboBox Mode Panel

    final ModeSelectionComboPanel modeComboPanel = new ModeSelectionComboPanel( colorMapGroup, Messages.getString("org.kalypso.ui.editor.styleeditor.symbolizerLayouts.RasterSymbolizerLayout.0"), 0 ); //$NON-NLS-1$
    modeComboPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        m_userStyle.fireStyleChanged();
      }
    } );

    // ***** Table
    final Composite tableComposite = new Composite( colorMapGroup, SWT.NULL );
    new ColorMapEntryTable( tableComposite, m_userStyle, rasterSymbolizer );
  }
}