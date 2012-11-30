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
/*
 * Created on 12.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.style.RuleCollection;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author F.Lindemann
 */
public class FilterPatternSymbolizerTabItemBuilder
{
  public FilterPatternSymbolizerTabItemBuilder( final FormToolkit toolkit, final TabFolder tabFolder, final IStyleInput<Symbolizer> input, final RuleCollection ruleCollection, final int symbolizerIndex )
  {
    final TabItem tabItem = new TabItem( tabFolder, SWT.NULL );

    final ISymbolizerComposite< ? > symbolizerLayout = createLayout( toolkit, tabFolder, input, ruleCollection, symbolizerIndex );
    // tabItem.setText( symbolizerLayout.getItemLabel() );
    tabItem.setControl( symbolizerLayout.getControl() );
  }

  private ISymbolizerComposite< ? > createLayout( final FormToolkit toolkit, final Composite parent, final IStyleInput< ? extends Symbolizer> input, final RuleCollection ruleCollection, final int symbolizerIndex )
  {
    final Symbolizer symbolizer = input.getData();
    if( symbolizer instanceof PolygonSymbolizer )
      return new FilterPatternPolygonSymbolizerLayout( toolkit, parent, (IStyleInput<PolygonSymbolizer>)input, ruleCollection, symbolizerIndex );

    if( symbolizer instanceof PointSymbolizer )
      return new FilterPatternPointSymbolizerLayout( toolkit, parent, (IStyleInput<PointSymbolizer>)input, ruleCollection, symbolizerIndex );

    if( symbolizer instanceof LineSymbolizer )
      return new FilterPatternLineSymbolizerLayout( toolkit, parent, (IStyleInput<LineSymbolizer>)input, ruleCollection, symbolizerIndex );

    return new SymbolizerComposite( toolkit, parent, (IStyleInput<Symbolizer>)input );
  }
}