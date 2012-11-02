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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.SLDBinding;
import org.kalypso.ui.editor.styleeditor.symbolizer.LinePlacementPerpendicularOffsetValue;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.LinePlacement;

/**
 * @author Gernot Belger
 */
public class LinePlacementComposite extends Composite
{
  private final IDataBinding m_binding;

  private final IStyleInput<LinePlacement> m_input;

  public LinePlacementComposite( final IDataBinding binding, final Composite parent, final IStyleInput<LinePlacement> input )
  {
    super( parent, SWT.NONE );

    m_binding = binding;
    m_input = input;

    getToolkit().adapt( this );
    setLayout( new GridLayout( 2, false ) );

    createPlacementControl( this );
    createPerpendicularOffsetControl( this );
    createGapControl( this );
  }

  private FormToolkit getToolkit( )
  {
    return m_binding.getToolkit();
  }

  private void createPlacementControl( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, Messages.getString( "LinePlacementComposite_0" ) ); //$NON-NLS-1$

    final ComboViewer typeViewer = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    typeViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final LinePlacementControlValue handler = new LinePlacementControlValue( m_input );
    handler.configureViewer( typeViewer );

    final IViewerObservableValue target = ViewerProperties.singleSelection().observe( typeViewer );
    m_binding.bindValue( target, handler );
  }

  private void createPerpendicularOffsetControl( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, Messages.getString( "LinePlacementComposite_1" ) ); //$NON-NLS-1$

    final Text offsetField = toolkit.createText( parent, StringUtils.EMPTY );
    offsetField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ISWTObservableValue target = SWTObservables.observeText( offsetField, SLDBinding.TEXT_DEFAULT_EVENTS );

    m_binding.bindValue( target, new LinePlacementPerpendicularOffsetValue( m_input ) );
  }

  private void createGapControl( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, Messages.getString( "LinePlacementComposite_2" ) ); //$NON-NLS-1$

    final Text gapField = toolkit.createText( parent, StringUtils.EMPTY );
    gapField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ISWTObservableValue target = SWTObservables.observeText( gapField, SLDBinding.TEXT_DEFAULT_EVENTS );

    m_binding.bindValue( target, new LinePlacementGapValue( m_input ) );
  }
}
