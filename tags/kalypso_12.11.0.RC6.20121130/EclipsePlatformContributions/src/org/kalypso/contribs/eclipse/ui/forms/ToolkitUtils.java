/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.forms;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Helper class for {@link org.eclipse.ui.forms.widgets.FormToolkit} related stuff.
 * 
 * @author Gernot Belger
 */
public final class ToolkitUtils
{
  private ToolkitUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Create a {@link FormToolkit} on the display of a control.<br/>
   * The toolkit will be automatically disposed when the control is disposed.
   */
  public static FormToolkit createToolkit( final Control control )
  {
    final boolean isWindows = Platform.getOS().equals( Platform.OS_WIN32 );
    // FIXME: how to decide this?! maybe use system property?
    final boolean classicWindows = false;

    final FormToolkit toolkit = new FormToolkit( control.getDisplay() )
    {
      @Override
      public Button createButton( final Composite buttonParent, final String text, final int style )
      {
        if( !isWindows )
          return super.createButton( buttonParent, text, style );

        // Suppress flat style: this works for all windowses
        final Button button = new Button( buttonParent, style | Window.getDefaultOrientation() );
        if( text != null )
          button.setText( text );
        adapt( button, true, true );
        return button;
      }
    };

    if( classicWindows )
      toolkit.setBackground( null );

    control.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        toolkit.dispose();
      }
    } );

    return toolkit;
  }

  /**
   * Same as {@link FormToolkit#createScrolledForm(Composite)}, but allows to specify which style to use.<br/>
   * {@link ScrolledForm#setExpandXXXX} is called depending on which style bits are set.
   */
  public static ScrolledForm createScrolledForm( final FormToolkit toolkit, final Composite parent, final int style )
  {
    final ScrolledForm form = new ScrolledForm( parent, SWT.V_SCROLL | SWT.H_SCROLL | toolkit.getOrientation() );
    form.setExpandHorizontal( (style & SWT.H_SCROLL) != 0 );
    form.setExpandVertical( (style & SWT.V_SCROLL) != 0 );
    form.setBackground( toolkit.getColors().getBackground() );
    form.setForeground( toolkit.getColors().getColor( IFormColors.TITLE ) );
    form.setFont( JFaceResources.getHeaderFont() );
    return form;
  }

  public static void adapt( final FormToolkit toolkit, final Composite composite )
  {
    if( toolkit == null )
      return;

    toolkit.adapt( composite );
  }
}
