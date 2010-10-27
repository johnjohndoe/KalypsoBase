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
package org.kalypso.util.swt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.kalypso.contribs.eclipse.jface.dialog.ListSelectionComposite;
import org.kalypso.core.status.StatusComposite;

/**
 * A wizard page that lets the user select from a list.<br>
 * TODO: add an validation mechanism for the current selection
 * 
 * @author Gernot Belger
 */
public class ListSelectionWizardPage extends WizardPage
{
  private StatusComposite m_statusLabel;

  private boolean m_allowNextIfEmpty;

  private final ListSelectionComposite m_listSelectionPanel;

  public ListSelectionWizardPage( final String pageName, final ILabelProvider labelProvider )
  {
    super( pageName );

    m_listSelectionPanel = new ListSelectionComposite( labelProvider );
  }

  public ListSelectionWizardPage( final String pageName, final IStructuredContentProvider contentProvider, final ILabelProvider labelProvider )
  {
    super( pageName );

    m_listSelectionPanel = new ListSelectionComposite( contentProvider, labelProvider );
  }

  /**
   * Set, if the next button should be allowed an an empty selection.
   */
  public void setAllowNextIfEmpty( final boolean allowNextIfEmpty )
  {
    m_allowNextIfEmpty = allowNextIfEmpty;
  }

  /**
   * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
   */
  @Override
  public boolean canFlipToNextPage( )
  {
    if( m_allowNextIfEmpty )
      return true;

    final Object[] elements = m_listSelectionPanel.getCheckedElements();
    return elements != null && elements.length > 0;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout() );

    m_statusLabel = new StatusComposite( composite, SWT.NONE );
    final GridData statusData = new GridData( SWT.FILL, SWT.TOP, true, false );
    statusData.exclude = true;
    m_statusLabel.setLayoutData( statusData );
    m_statusLabel.setVisible( false );

    new Label( composite, SWT.NONE );

    final Control control = m_listSelectionPanel.createControl( composite, SWT.BORDER );
    final GridData tableGridData = new GridData( SWT.FILL, SWT.FILL, true, true );
    tableGridData.heightHint = 200;
    control.setLayoutData( tableGridData );

    m_listSelectionPanel.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        handleCheckStateChanged();
      }
    } );

    setControl( composite );
  }

  public void setCheckedElements( final Object[] elements )
  {
    m_listSelectionPanel.setCheckedElements( elements );
  }

  public void setInput( final Object input )
  {
    m_listSelectionPanel.setInput( input );
  }

  public void setStatus( final IStatus status )
  {
    // REMARK: we hide the status completely, if the status is null.
    // Makes layout necessary if status changed from/to null.
    final IStatus currentStatus = m_statusLabel.getStatus();
    final boolean layoutNeeded = (status == null && currentStatus != null) || (status != null && currentStatus == null);

    m_statusLabel.setStatus( status );

    m_statusLabel.setVisible( status != null );
    ((GridData) m_statusLabel.getLayoutData()).exclude = status == null;

    if( layoutNeeded )
      m_statusLabel.getParent().layout();
  }

  public Object[] getSelection( )
  {
    return m_listSelectionPanel.getCheckedElements();
  }

  protected void handleCheckStateChanged( )
  {
    getContainer().updateButtons();
  }
}
