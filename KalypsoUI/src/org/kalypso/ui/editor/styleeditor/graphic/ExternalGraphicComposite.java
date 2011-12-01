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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.binding.DatabindingForm;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.SLDBinding;
import org.kalypsodeegree.graphics.sld.ExternalGraphic;

/**
 * @author Gernot Belger
 */
public class ExternalGraphicComposite extends Composite
{
  private final DatabindingForm m_binding;

  public ExternalGraphicComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<ExternalGraphic> input )
  {
    super( parent, SWT.NONE );

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    final ScrolledForm form = toolkit.createScrolledForm( this );

    m_binding = new DatabindingForm( form, toolkit );

    final Composite body = form.getBody();
    body.setLayout( new GridLayout( 2, false ) );

    createUrlControl( toolkit, body, input );

    // TODO: format ignored by ExternalGraphics implementation
    // createFormatControl( toolkit, body, input );
  }

  private void createUrlControl( final FormToolkit toolkit, final Composite parent, final IStyleInput<ExternalGraphic> input )
  {
    toolkit.createLabel( parent, MessageBundle.STYLE_EDITOR_URL );

    final Text urlField = toolkit.createText( parent, StringUtils.EMPTY, SWT.BORDER );
    urlField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ISWTObservableValue target = SWTObservables.observeText( urlField, SLDBinding.TEXT_DEFAULT_EVENTS );
    m_binding.bindValue( target, new OnlineResourceValue( input ), new OnlineResourceValidator( input ) );
  }

  public void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();
  }

// private void createFormatControl( final FormToolkit toolkit, final Composite parent, final ExternalGraphicInput input
// )
// {
// toolkit.createLabel( parent, MessageBundle.STYLE_EDITOR_FORMAT );
//
// final Text formatField = toolkit.createText( parent, StringUtils.EMPTY, SWT.BORDER );
// formatField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
//
// final ISWTObservableValue target = SWTObservables.observeText( formatField, SLDBinding.TEXT_DEFAULT_EVENTS );
// m_binding.bindValue( target, new FormatValue( input ) );
// }
}