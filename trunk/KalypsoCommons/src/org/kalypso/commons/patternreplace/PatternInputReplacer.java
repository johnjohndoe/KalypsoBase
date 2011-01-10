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
package org.kalypso.commons.patternreplace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

/**
 * @author Gernot Belger
 */
public class PatternInputReplacer<T>
{
  private final Map<String, IPatternInput<T>> m_replacers = new LinkedHashMap<String, IPatternInput<T>>();

  private final char m_patternStart;

  private final char m_patternStop;

  private Pattern m_tokenPattern;

  public PatternInputReplacer( )
  {
    this( '<', '>' );
  }

  public PatternInputReplacer( final char patternStart, final char patternStop )
  {
    m_patternStart = patternStart;
    m_patternStop = patternStop;

    final String pattern = String.format( "\\%s(.*?)(:(.*?))*\\%s", m_patternStart, m_patternStop );

    m_tokenPattern = Pattern.compile( pattern );
  }

  public void addReplacer( final IPatternInput<T> replacer )
  {
    final String token = replacer.getToken();
    m_replacers.put( token, replacer );
  }

  public String getMessage( )
  {
    final Formatter formatter = new Formatter();

    // FIXME: how to show params?
    for( final IPatternInput< ? extends T> token : m_replacers.values() )
      formatter.format( "%s%s%s: %s%n", m_patternStart, token.getToken(), m_patternStop, token.getLabel() ); //$NON-NLS-1$

    return formatter.toString();
  }

  public String replaceTokens( final String pattern, final T context )
  {
    if( pattern == null )
      return null;

    final StringBuffer result = new StringBuffer();

    final Matcher matcher = m_tokenPattern.matcher( pattern );
    while( matcher.find() )
    {
      final String token = matcher.group( 1 );
      final String params = matcher.group( 3 );

      final IPatternInput<T> tokenReplacer = getReplacer( token );

      final String replacement = tokenReplacer.getReplacement( context, params );
      matcher.appendReplacement( result, replacement );
    }

    return result.toString();
  }

  private IPatternInput<T> getReplacer( final String token )
  {
    return m_replacers.get( token );
  }

  public IContributionItem[] asContributionItems( final Text text )
  {
    final Collection<IContributionItem> items = new ArrayList<IContributionItem>();

    for( final IPatternInput<T> pattern : m_replacers.values() )
    {
      final String label = pattern.getLabel();
      final String token = pattern.getToken();
      final String replacement = String.format( "%s%s%s", m_patternStart, token, m_patternStop ); //$NON-NLS-1$
      items.add( new ActionContributionItem( new PatternAction( label, replacement, text ) ) );
    }

    return items.toArray( new IContributionItem[items.size()] );
  }

  private MenuManager createPatternMenu( final Text text )
  {
    final MenuManager menuManager = new MenuManager();

    final IContributionItem[] items = asContributionItems( text );
    for( final IContributionItem item : items )
      menuManager.add( item );

    return menuManager;
  }

  /**
   * Creates a menu-button that insert pattern into the given text field.
   */
  public Button createPatternButton( final Composite parent, final Text text )
  {
    final MenuManager menuManager = createPatternMenu( text );
    menuManager.setRemoveAllWhenShown( false );

    final Button button = new Button( parent, SWT.ARROW | SWT.LEFT );
    button.setToolTipText( "Insert a token from the list of available patterns" );
    final Menu menu = menuManager.createContextMenu( button );

    button.setMenu( menu );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Point displayLocation = button.toDisplay( e.x, e.y );
        menu.setLocation( displayLocation );
        menu.setVisible( true );
      }
    } );

    button.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        menu.dispose();
        menuManager.dispose();
      }
    } );

    return button;
  }

}
