/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.core.internal.layoutwizard.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.core.layoutwizard.ILayoutPart;

/**
 * @author Gernot Belger
 */
public class SashContainer extends AbstractLayoutContainer
{
  public static final String PROP_SASH_STYLE = "sashStyle";

  public static final String PROP_SASH_WEIGHT = "sashWeight";

  public static final String PROP_SASH_MAXIMIZED = "sashMaximized";

  private final SashConfiguration m_configuration;

  public SashContainer( final String id, final SashConfiguration configuration )
  {
    super( id );

    m_configuration = configuration;
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#createControl(org.eclipse.ui.forms.widgets.FormToolkit,
   *      org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final SashForm sashForm = new SashForm( parent, m_configuration.m_style );
    toolkit.adapt( sashForm );
    sashForm.setBackground( parent.getDisplay().getSystemColor( SWT.COLOR_GRAY ) );

    final ILayoutPart[] children = getChildren();

    final String maximizedId = m_configuration.m_maxmizedChildId;

    for( final ILayoutPart child : children )
    {
      final Control childControl = child.createControl( sashForm, toolkit );
      if( maximizedId != null && maximizedId.equals( child.getId() ) )
        sashForm.setMaximizedControl( childControl );
    }

    sashForm.setWeights( m_configuration.m_weights );

    return sashForm;
  }
}
