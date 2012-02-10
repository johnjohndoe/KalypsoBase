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

import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IDataBinding;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.graphics.sld.PointPlacement;

/**
 * @author Gernot Belger
 */
public class PointPlacementComposite extends Composite
{
  private Button m_autoButton;

  private final IStyleInput<PointPlacement> m_input;

  private final IDataBinding m_binding;

  public PointPlacementComposite( final IDataBinding binding, final Composite parent, final IStyleInput<PointPlacement> input )
  {
    super( parent, SWT.NONE );

    m_binding = binding;
    m_input = input;

    getToolkit().adapt( this );
    setLayout( new GridLayout( 3, false ) );

    createOffsetField( this );
    createAnchorField( this );
    createDisplacementField( this );
    createAutoField( this );
  }

  private FormToolkit getToolkit( )
  {
    return m_binding.getToolkit();
  }

  private void createAutoField( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    final Button autoButton = toolkit.createButton( parent, "Auto", SWT.CHECK );
    m_autoButton = autoButton;
    m_autoButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    final ISWTObservableValue target = SWTObservables.observeSelection( autoButton );
    m_binding.bindValue( target, new PointPlacementAutoValue( m_input ) );
  }

  private void createDisplacementField( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, "Displacement" );

    final Spinner displacementXField = new Spinner( parent, SWT.BORDER );
    displacementXField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.adapt( displacementXField, true, true );
    displacementXField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    displacementXField.setValues( 0, -1000, 1000, 0, 1, 10 );

    final Spinner displacementYField = new Spinner( parent, SWT.BORDER );
    displacementYField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.adapt( displacementYField, true, true );
    displacementYField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    displacementYField.setValues( 0, -1000, 1000, 0, 1, 10 );

    final ISWTObservableValue targetX = SWTObservables.observeSelection( displacementXField );
    final ISWTObservableValue targetY = SWTObservables.observeSelection( displacementYField );

    m_binding.bindValue( targetX, new PointPlacementDisplacementXValue( m_input ) );
    m_binding.bindValue( targetY, new PointPlacementDisplacementYValue( m_input ) );
  }

  private void createAnchorField( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, "Anchor" );

    final Spinner anchorXField = new Spinner( parent, SWT.BORDER );
    anchorXField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.adapt( anchorXField, true, true );
    anchorXField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    anchorXField.setValues( 0, 0, 100, 0, 1, 10 );

    final Spinner anchorYField = new Spinner( parent, SWT.BORDER );
    anchorYField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.adapt( anchorYField, true, true );
    anchorYField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    anchorYField.setValues( 0, 0, 100, 0, 1, 10 );

    final ISWTObservableValue targetX = SWTObservables.observeSelection( anchorXField );
    final ISWTObservableValue targetY = SWTObservables.observeSelection( anchorYField );

    m_binding.bindValue( targetX, new PointPlacementAnchorXValue( m_input ) );
    m_binding.bindValue( targetY, new PointPlacementAnchorYValue( m_input ) );
  }

  private void createOffsetField( final Composite parent )
  {
    final FormToolkit toolkit = getToolkit();

    toolkit.createLabel( parent, "Rotation" );

    final Spinner rotationField = new Spinner( parent, SWT.BORDER );
    toolkit.adapt( rotationField, true, true );
    rotationField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    rotationField.setValues( 0, 0, 359, 0, 1, 45 );

    final ISWTObservableValue target = SWTObservables.observeSelection( rotationField );
    m_binding.bindValue( target, new PointPlacementRotationValue( m_input ) );
  }
}
