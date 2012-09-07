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
package org.kalypso.zml.ui.table.commands.menu.adjust;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.jface.dialog.EnhancedTitleAreaDialog;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.pager.ElementsComposite;
import org.kalypso.contribs.eclipse.ui.pager.IElementPage;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.AbstractAdjustmentPage;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.ConstantValueAdjustmentPage;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.IAdjustmentPageProvider;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.MultiplyValueAdjustmentPage;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.ShiftDateAdjustmentPage;
import org.kalypso.zml.ui.table.commands.menu.adjust.pages.ShiftValueAdjustmentPage;
import org.kalypso.zml.ui.table.dialogs.input.IZmlEinzelwertCompositeListener;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class ZmlAdjustSelectionDialog extends EnhancedTitleAreaDialog implements IZmlEinzelwertCompositeListener, IAdjustmentPageProvider
{
  private static final String SCREEN_SIZE = "zml.adjust.selection.dialog.screen.size"; //$NON-NLS-1$

  private final IZmlModelColumn m_column;

  private ElementsComposite m_composite;

  private final IZmlTableSelection m_selection;

  public ZmlAdjustSelectionDialog( final Shell shell, final IZmlTableSelection selection, final IZmlModelColumn column )
  {
    super( shell );
    m_selection = selection;
    m_column = column;

    setShellStyle( SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE );

    setHelpAvailable( false );
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected final Control createDialogArea( final Composite parent )
  {
    getShell().setText( Messages.ZmlAdjustSelectionDialog_0 );

    setTitle( String.format( Messages.ZmlAdjustSelectionDialog_1, m_column.getLabel() ) );
    setMessage( Messages.ZmlAdjustSelectionDialog_2 );

    final FormToolkit toolkit = new FormToolkit( parent.getDisplay() );

    final Composite base = toolkit.createComposite( parent, SWT.NULL );
    base.setLayout( new GridLayout() );

    final Point screen = getScreenSize( SCREEN_SIZE );

    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, true );
    data.widthHint = screen.x;
    data.heightHint = screen.y;
    base.setLayoutData( data );

    base.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        setScreenSize( SCREEN_SIZE, base.getSize() );
      }
    } );

    final Set<IElementPage> pages = new LinkedHashSet<>();
    pages.add( new ConstantValueAdjustmentPage( this ) );
    pages.add( new ShiftValueAdjustmentPage( this ) );
    pages.add( new MultiplyValueAdjustmentPage( this ) );
    pages.add( new ShiftDateAdjustmentPage( this ) );

    m_composite = new ElementsComposite( base, toolkit, pages.toArray( new IElementPage[] {} ) );
    m_composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    return super.createDialogArea( parent );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    final AbstractAdjustmentPage page = (AbstractAdjustmentPage) m_composite.getSelectedPage();
    if( !page.isValid() )
    {
      setErrorMessage( Messages.ZmlAdjustSelectionDialog_3 );

      return;
    }

    final ICoreRunnableWithProgress runnable = page.getRunnable();
    final IStatus status = ProgressUtilities.busyCursorWhile( runnable, Messages.ZmlAdjustSelectionDialog_4 );

    if( status.isOK() )
      super.okPressed();
    else
      setErrorMessage( status.getMessage() );
  }

  /**
   * @see org.kalypso.zml.ui.table.dialogs.input.IZmlEinzelwertCompositeListener#inputChanged(boolean)
   */
  @Override
  public void inputChanged( final boolean valid )
  {
    final Button button = getButton( OK );
    if( button != null )
      button.setEnabled( valid );
  }

  @Override
  public IZmlModelColumn getColumn( )
  {
    return m_column;
  }

  @Override
  public IZmlTableSelection getSelectionHandler( )
  {
    return m_selection;
  }
}
