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
package org.kalypso.ui.editor.styleeditor.placement;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.ui.editor.styleeditor.binding.DatabindingForm;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypsodeegree.graphics.sld.LabelPlacement;
import org.kalypsodeegree.graphics.sld.LinePlacement;
import org.kalypsodeegree.graphics.sld.PointPlacement;

/**
 * @author Gernot Belger
 */
public class LabelPlacementComposite extends Composite
{
  private final DatabindingForm m_binding;

  private LinePlacement m_linePlacement;

  private PointPlacement m_pointPlacement;

  private final IStyleInput<LabelPlacement> m_input;

  private final ScrolledForm m_form;

  private final Group m_contentGroup;

  public LabelPlacementComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<LabelPlacement> input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    m_form = toolkit.createScrolledForm( this );
    m_form.setExpandHorizontal( true );
    final Composite body = m_form.getBody();
    body.setLayout( new FillLayout() );

    m_contentGroup = new Group( body, SWT.NONE );
    toolkit.adapt( m_contentGroup );
    m_contentGroup.setLayout( new FillLayout() );

    m_binding = new DatabindingForm( m_form.getForm(), toolkit );

    updateControl();
  }

  private FormToolkit getToolkit( )
  {
    return m_binding.getToolkit();
  }

  /**
   * Call, if style has changed
   */
  public void updateControl( )
  {
    /* check if constellation changed */
    final LabelPlacement data = m_input.getData();
    final LinePlacement linePlacement = data == null ? null : data.getLinePlacement();
    final PointPlacement pointPlacement = data == null ? null : data.getPointPlacement();
    if( linePlacement == m_linePlacement && pointPlacement == m_pointPlacement )
    {
      /* Just update controls */
      m_binding.getBindingContext().updateTargets();
      return;
    }

    m_linePlacement = linePlacement;
    m_pointPlacement = pointPlacement;

    /* Re-create controls */
    m_contentGroup.setText( createContents() );

    m_form.reflow( true );
    m_contentGroup.layout( true, true );
  }

  private String createContents( )
  {
    ControlUtils.disposeChildren( m_contentGroup );

    if( m_pointPlacement != null )
    {
      final IStyleInput<PointPlacement> placementInput = new StyleInput<PointPlacement>( m_pointPlacement, m_input );
      new PointPlacementComposite( m_binding, m_contentGroup, placementInput );
      return "Point Placement";
    }

    if( m_linePlacement != null )
    {
      final IStyleInput<LinePlacement> placementInput = new StyleInput<LinePlacement>( m_linePlacement, m_input );
      new LinePlacementComposite( m_binding, m_contentGroup, placementInput );
      return "Line Placement";
    }

    return createNoPlacement();
  }

  private String createNoPlacement( )
  {

    final Composite composite = getToolkit().createComposite( m_contentGroup );
    GridLayoutFactory.fillDefaults().applyTo( composite );

    final Label label = getToolkit().createLabel( composite, "Placement is not defined" );
    label.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );

    return "No Placement";
  }
}