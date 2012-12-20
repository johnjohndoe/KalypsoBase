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
package org.kalypso.ui.editor.styleeditor.halo;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.forms.DatabindingForm;
import org.kalypso.ui.editor.styleeditor.IStyleContext;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * @author Gernot Belger
 */
public class HaloComposite extends Composite
{
  private Halo m_halo;

  private IStyleContext m_context;

  private final IStyleInput<Halo> m_input;

  private final IDataBinding m_binding;

  private HaloStrokeSection m_strokeSection;

  private HaloFillSection m_fillSection;

  public HaloComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<Halo> input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    final Form form = toolkit.createForm( this );
    final Composite body = form.getBody();
    GridLayoutFactory.fillDefaults().numColumns( 2 ).equalWidth( true ).spacing( 0, 0 ).applyTo( body );

    m_binding = new DatabindingForm( form, toolkit );

    createRadiusControl( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    createStrokeControl( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    createFillControl( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private Control createRadiusControl( final FormToolkit toolkit, final Composite parent )
  {
    final Composite radiusComposite = toolkit.createComposite( parent );
    radiusComposite.setLayout( new GridLayout( 2, false ) );

    toolkit.createLabel( radiusComposite, Messages.getString( "HaloComposite_0" ) ); //$NON-NLS-1$

    final Spinner spinner = new Spinner( radiusComposite, SWT.BORDER );
    toolkit.adapt( spinner, true, true );

    final IObservableValue target = SWTObservables.observeSelection( spinner );
    final HaloRadiusValue model = new HaloRadiusValue( m_input );
    model.configureSpinner( spinner );

    m_binding.bindValue( target, model );

    return radiusComposite;
  }

  private Control createStrokeControl( final FormToolkit toolkit, final Composite parent )
  {
    m_strokeSection = new HaloStrokeSection( toolkit, parent, m_input );
    return m_strokeSection.getSection();
  }

  private Control createFillControl( final FormToolkit toolkit, final Composite parent )
  {
    m_fillSection = new HaloFillSection( toolkit, parent, m_input );
    return m_fillSection.getSection();
  }

  protected void handleStrokeChanged( final Stroke stroke )
  {
    if( m_halo != null )
      m_halo.setStroke( stroke );

    fireStyleChanged();
  }

  private void fireStyleChanged( )
  {
    if( m_context != null )
      m_context.fireStyleChanged();
  }

  public void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();

    m_strokeSection.updateControl();
    m_fillSection.updateControl();
  }
}
