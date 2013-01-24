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
package org.kalypso.contribs.eclipse.jface.viewers;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Shows the list of available charset to the user.
 * 
 * @author Gernot Belger
 */
public class CharsetViewer extends ComboViewer
{
  private Charset m_charset;

  /** Charset-Name -> Charset-Label */
  final Map<String, String> m_labelMappings = new HashMap<String, String>();

  public CharsetViewer( final Composite parent )
  {
    super( parent, SWT.DROP_DOWN | SWT.READ_ONLY );

    setContentProvider( new ArrayContentProvider() );
    setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final Charset charset = (Charset) element;
        if( m_labelMappings.containsKey( charset.name() ) )
          return m_labelMappings.get( charset.name() );

        return charset.displayName();
      }
    } );

    final SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
    final Collection<Charset> charsets = availableCharsets.values();
    setInput( charsets );
    
    addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleSelectionChanged( (IStructuredSelection) event.getSelection() );
      }
    } );
    
    final Charset defaultCharset = Charset.defaultCharset();
    final String defaultCharsetLabel = String.format( "%s (platform default)", defaultCharset.displayName());
    addLabelMapping( defaultCharset, defaultCharsetLabel );
  }

  public void addLabelMapping( final Charset charset, final String label )
  {
    m_labelMappings.put( charset.name(), label );
    update( charset, null );
  }

  protected void handleSelectionChanged( final IStructuredSelection selection )
  {
    m_charset = (Charset) selection.getFirstElement();
  }

  public Charset getCharset( )
  {
    return m_charset;
  }

  public void setCharset( final Charset charset )
  {
    m_charset = charset;

    setSelection( new StructuredSelection( charset ) );
  }
}
