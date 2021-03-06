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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.preview.LinePreview;
import org.kalypso.ui.editor.styleeditor.preview.SymbolizerPreview;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;

/**
 * @author F.Lindemann
 */
public class LineSymbolizerComposite extends AbstractSymbolizerComposite<LineSymbolizer>
{
  private LineSymbolizerStrokeSection m_strokeSection;

  public LineSymbolizerComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<LineSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected Control createContent( final FormToolkit toolkit, final Composite parent )
  {
    m_strokeSection = new LineSymbolizerStrokeSection( toolkit, parent, getInput() );
    return m_strokeSection.getSection();
  }

  @Override
  protected SymbolizerPreview<LineSymbolizer> createPreview( final Composite parent, final Point size, final IStyleInput<LineSymbolizer> input )
  {
    return new LinePreview( parent, size, input );
  }

  @Override
  public void doUpdateControl( )
  {
    m_strokeSection.updateControl();
  }
}