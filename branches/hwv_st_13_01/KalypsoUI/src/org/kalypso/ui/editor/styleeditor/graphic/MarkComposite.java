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
package org.kalypso.ui.editor.styleeditor.graphic;

import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.DatabindingForm;
import org.kalypso.ui.editor.styleeditor.binding.IDataBinding;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.graphics.sld.Mark;

/**
 * @author Gernot Belger
 */
public class MarkComposite extends Composite
{
  private ComboViewer m_wknChooser;

  private final IStyleInput<Mark> m_input;

  private final IDataBinding m_binding;

  private MarkFillSection m_fillSection;

  private MarkStrokeSection m_strokeSection;

  public MarkComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<Mark> input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    final Form form = toolkit.createForm( this );

    m_binding = new DatabindingForm( form, toolkit );

    final Composite body = form.getBody();
    GridLayoutFactory.swtDefaults().numColumns( 2 ).equalWidth( true ).applyTo( body );

    createWellKnownName( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    createStroke( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    createFill( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private Control createWellKnownName( final FormToolkit toolkit, final Composite parent )
  {
    final Composite panel = toolkit.createComposite( parent );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).applyTo( panel );

    toolkit.createLabel( panel, "Symbol" );

    final ComboViewer wknChooser = new ComboViewer( panel, SWT.READ_ONLY | SWT.DROP_DOWN );
    m_wknChooser = wknChooser;
    toolkit.adapt( m_wknChooser.getControl(), true, true );
    m_wknChooser.getControl().setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false ) );

    final IViewerObservableValue target = ViewerProperties.singleSelection().observe( wknChooser );
    final WellKnownNameValue model = new WellKnownNameValue( m_input );

    model.configureViewer( wknChooser );

    m_binding.bindValue( target, model );

    return panel;
  }

  private Control createFill( final FormToolkit toolkit, final Composite parent )
  {
    m_fillSection = new MarkFillSection( toolkit, parent, m_input );
    return m_fillSection.getSection();
  }

  private Control createStroke( final FormToolkit toolkit, final Composite parent )
  {
    m_strokeSection = new MarkStrokeSection( toolkit, parent, m_input );
    return m_strokeSection.getSection();
  }

  public void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();

    m_strokeSection.updateControl();
    m_fillSection.updateControl();
  }
}