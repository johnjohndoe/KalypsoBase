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
 * Created on 15.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.rule;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author Gernot Belger
 */
public class AddSymbolizerDialog extends TitleAreaDialog
{
  private final IFeatureType m_featureType;

  private AddSymbolizerComposite m_addComposite;

  private Symbolizer m_symbolizer;

  private final Point m_initialLocation;

  public AddSymbolizerDialog( final Shell shell, final IFeatureType featureType, final Point initialLocation )
  {
    super( shell );

    m_featureType = featureType;
    m_initialLocation = initialLocation;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
   */
  @Override
  protected Point getInitialLocation( final Point initialSize )
  {
    if( m_initialLocation != null )
      return m_initialLocation;

    return super.getInitialLocation( initialSize );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    getShell().setText( "Style Editor" );

    final Composite composite = (Composite) super.createDialogArea( parent );

    m_addComposite = new AddSymbolizerComposite( composite, m_featureType );
    m_addComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return composite;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar( final Composite parent )
  {
    super.createButtonsForButtonBar( parent );

    final Button okButton = getButton( IDialogConstants.OK_ID );
    final boolean choicePossible = m_addComposite.isChoicePossible();

    okButton.setEnabled( choicePossible );
    setTitle( "Create Symbolizer" );
    setMessage( "Please choose geometry and type of the new symbolizer" );

    if( !choicePossible )
      setMessage( "Unable to create a symbolizer for the current feature type: no valid geometry available.", IMessageProvider.WARNING );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    m_symbolizer = m_addComposite.createSymbolizer();

    super.okPressed();
  }

  public Symbolizer getResult( )
  {
    return m_symbolizer;
  }
}