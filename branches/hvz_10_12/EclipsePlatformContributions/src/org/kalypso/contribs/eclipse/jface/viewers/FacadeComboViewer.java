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
package org.kalypso.contribs.eclipse.jface.viewers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * Easy to use ComboViewer class<br>
 * <br>
 * ComboViewer has always Input data, an starting selection and selection change event<br>
 * This class helps handling those aspects (example):<br>
 * <br>
 * final INPUT[] wqInput = new INPUT[] { INPUT.eOne, INPUT.eTwo };<br>
 * final FacadeComboViewer fcv = new FacadeComboViewer( new FCVArrayDelegate( wqInput ) );<br>
 * fcv.draw( composite, new GridData(), SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE );<br>
 * fcv.addSelectionChangedListener( new Runnable());<br>
 * <br>
 * IFacadeComboViewerDelegate handles the processing of your input data and default selection of your combobox
 * 
 * @author Dirk Kuch
 */
public class FacadeComboViewer
{
  private final List<Runnable> m_listener = new LinkedList<Runnable>();

  protected ISelection m_selection = new StructuredSelection();

  protected ComboViewer m_comboViewer;

  private Object[] m_inputData;

  protected final IFCVDelegate m_delegate;

  private final boolean m_enableSingleElementSelection;

  public FacadeComboViewer( final IFCVDelegate delegate )
  {
    this( delegate, false );
  }

  public FacadeComboViewer( final IFCVDelegate delegate, final boolean enableSingleElementSelection )
  {
    m_delegate = delegate;
    m_enableSingleElementSelection = enableSingleElementSelection;
    initSelection();
  }

  private void initSelection( )
  {
    m_inputData = m_delegate.getInputData();

    if( m_enableSingleElementSelection )
      if( m_inputData.length == 1 )
      {
        final StructuredSelection selection = new StructuredSelection( m_inputData[0] );
        m_selection = selection;
      }

    if( m_inputData.length > 0 )
      m_selection = m_delegate.getDefaultKey();
  }

  public void addSelectionChangedListener( final Runnable selectionChangedListener )
  {
    m_listener.add( selectionChangedListener );
  }

  public void draw( final Composite parent, final GridData layout, final int style )
  {
    m_comboViewer = new ComboViewer( parent, style );
    m_comboViewer.getCombo().setLayoutData( layout );
    m_comboViewer.setContentProvider( new ArrayContentProvider() );
    m_comboViewer.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        final String label = m_delegate.getValue( element );
        if( label != null )
          return label;

        return super.getText( element );
      }
    } );

    m_comboViewer.setInput( m_inputData );

    if( m_enableSingleElementSelection )
      if( m_inputData.length == 1 )
      {
        final StructuredSelection selection = new StructuredSelection( m_inputData[0] );
        m_comboViewer.setSelection( selection );
        m_selection = selection;
        m_comboViewer.getCombo().setEnabled( false );
      }

    if( m_inputData.length == 0 )
      m_comboViewer.getCombo().setEnabled( false );
    else
    {
      m_comboViewer.setSelection( m_delegate.getDefaultKey() );
      m_selection = m_delegate.getDefaultKey();
    }

    m_comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        m_selection = m_comboViewer.getSelection();
        processListener();
      }
    } );
  }

  public ISelection getSelection( )
  {
    return m_selection;
  }

  protected void processListener( )
  {
    for( final Runnable listener : m_listener )
      listener.run();
  }

  public void setEnabled( final boolean b )
  {
    m_comboViewer.getCombo().setEnabled( b );
  }

  public void setSelection( final ISelection selection )
  {
    m_comboViewer.setSelection( selection );
  }
}
