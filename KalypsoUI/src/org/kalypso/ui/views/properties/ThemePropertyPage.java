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
package org.kalypso.ui.views.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ogc.gml.command.CompositeCommand;
import org.kalypso.ogc.gml.command.EnableThemeCommand;
import org.kalypso.ogc.gml.command.RenameThemeCommand;

/**
 * This is a page for showing some properties of a theme.
 *
 * @author Gernot Belger
 * @author Holger Albert
 */
public class ThemePropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
  private String m_themeName = null;

  private Boolean m_themeVisibility = null;

  @Override
  protected Control createContents( final Composite parent )
  {
    /* Get the theme. */
    final IKalypsoTheme theme = getTheme();

    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 2, false ) );

    if( theme == null )
    {
      // todo: show some error message
      return composite;
    }

    final boolean themeEditable = ThemeUtilities.isDeletable( theme );

    /* Current Status */
    final Label statusLabel = new Label( composite, SWT.NONE );
    statusLabel.setText( Messages.getString("ThemePropertyPage.0") ); //$NON-NLS-1$

    final StatusComposite statusComposite = new StatusComposite( composite, StatusComposite.DETAILS );
    statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    statusComposite.setStatus( theme.getStatus() );

    /* Theme name */
    final Label nameLabel = new Label( composite, SWT.NONE );
    nameLabel.setText( Messages.getString( "org.kalypso.ui.views.properties.ThemePropertyPage.0" ) ); //$NON-NLS-1$

    final Text nameText = new Text( composite, SWT.BORDER );
    nameText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    nameText.setText( theme.getLabel() );
    nameText.setEditable( themeEditable );
    nameText.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        final String name = nameText.getText();
        setThemeName( name );
      }
    } );

    /* Theme visibility */
    final Label visibilityLabel = new Label( composite, SWT.NONE );
    visibilityLabel.setText( Messages.getString( "org.kalypso.ui.views.properties.ThemePropertyPage.2" ) ); //$NON-NLS-1$

    final Button visibilityButton = new Button( composite, SWT.CHECK );
    visibilityButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    visibilityButton.setSelection( theme.isVisible() );
    visibilityButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        setVisibility( visibilityButton.getSelection() );
      }
    } );

    return composite;
  }

  /**
   * This function returns the theme.
   *
   * @return The theme.
   */
  private IKalypsoTheme getTheme( )
  {
    final IAdaptable element = getElement();
    final IKalypsoTheme theme = (IKalypsoTheme) (element instanceof IKalypsoTheme ? element : element.getAdapter( IKalypsoTheme.class ));

    return theme;
  }

  @Override
  public boolean performOk( )
  {
    final IKalypsoTheme theme = getTheme();
    if( theme == null )
      return false;

    final CompositeCommand allCommands = new CompositeCommand( Messages.getString( "org.kalypso.ui.views.properties.ThemePropertyPage.1" ) ); //$NON-NLS-1$
    if( m_themeName != null )
      allCommands.addCommand( new RenameThemeCommand( theme, new I10nString( m_themeName ) ) );

    if( m_themeVisibility != null )
      allCommands.addCommand( new EnableThemeCommand( theme, m_themeVisibility.booleanValue() ) );

    // TODO: find mapPanel!
    try
    {
      allCommands.process();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return super.performOk();
  }

  private void checkValid( )
  {
    /* clear error */
    setErrorMessage( null );

    /* Revalidate and set message accoring to result */
    final String msg = validate();
    if( msg == null )
      setValid( true );
    else if( msg.length() == 0 )
      setValid( false );
    else
    {
      setValid( false );
      setErrorMessage( msg );
    }
  }

  private String validate( )
  {
    // If nothing changed, not valid, but also no mesage
    if( m_themeName == null && m_themeVisibility == null )
      return ""; //$NON-NLS-1$

    /* No empty 'name' allowed */
    if( m_themeName != null && m_themeName.trim().isEmpty() )
      return Messages.getString( "org.kalypso.ui.views.properties.ThemePropertyPage.3" ); //$NON-NLS-1$

    // everything else is valid
    return null;
  }

  /**
   * This function sets the name of the theme.
   *
   * @param name
   *          The name of the theme.
   */
  protected void setThemeName( final String name )
  {
    m_themeName = name;

    checkValid();
  }

  protected void setVisibility( final boolean visibility )
  {
    m_themeVisibility = visibility;

    checkValid();
  }
}
