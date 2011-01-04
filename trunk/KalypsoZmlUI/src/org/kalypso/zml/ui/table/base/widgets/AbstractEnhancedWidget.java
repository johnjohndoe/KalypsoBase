/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.base.widgets;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.zml.ui.table.base.widgets.rules.IWidgetRule;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractEnhancedWidget<T> extends Composite implements IAbstractEnhancedWidget
{
  protected static final Image IMG_INVALID_PARAMETER = new Image( null, EnhancedTextBox.class.getResourceAsStream( "icons/invalid_parameter.gif" ) );

  private final IWidgetRule<T> m_rule;

  private ImageHyperlink m_validationIcon;

  private final Set<IAbstractEnhancedWidgetChangeListener> m_changeListeners = new LinkedHashSet<IAbstractEnhancedWidgetChangeListener>();

  public AbstractEnhancedWidget( final Composite parent, final FormToolkit toolkit, final IWidgetRule<T> rule )
  {
    super( parent, SWT.NULL );
    m_rule = rule;

    setLayout( LayoutHelper.createGridLayout( 2 ) );
    initWidget( toolkit );
    initValidationIcon( toolkit );

    toolkit.adapt( this );
  }

  public final void addListener( final IAbstractEnhancedWidgetChangeListener listener )
  {
    m_changeListeners.add( listener );
  }

  protected void fireWidgetChanged( )
  {
    final IAbstractEnhancedWidgetChangeListener[] listeners = m_changeListeners.toArray( new IAbstractEnhancedWidgetChangeListener[] {} );
    for( final IAbstractEnhancedWidgetChangeListener listener : listeners )
    {
      listener.widgetChanged( this );
    }
  }

  private void initValidationIcon( final FormToolkit toolkit )
  {
    m_validationIcon = toolkit.createImageHyperlink( this, SWT.READ_ONLY );
    m_validationIcon.setImage( IMG_INVALID_PARAMETER );
    m_validationIcon.setVisible( false );
    m_validationIcon.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );
  }

  protected abstract void initWidget( FormToolkit toolkit );

  protected IWidgetRule<T> getRule( )
  {
    return m_rule;
  }

  protected ImageHyperlink getValidationIcon( )
  {
    return m_validationIcon;
  }

  @Override
  public boolean isValid( )
  {
    if( m_validationIcon.isDisposed() )
      return true;

    return !m_validationIcon.isVisible();
  }
}
