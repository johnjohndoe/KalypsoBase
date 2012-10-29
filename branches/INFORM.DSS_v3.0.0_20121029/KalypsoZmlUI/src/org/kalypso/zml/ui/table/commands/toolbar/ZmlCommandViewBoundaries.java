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
package org.kalypso.zml.ui.table.commands.toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.table.binding.ZmlRuleResolver;
import org.kalypso.zml.core.table.binding.rule.AbstractZmlRule;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnEvent;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.schema.RuleRefernceType;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;

import com.google.common.base.Splitter;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandViewBoundaries extends AbstractHandler implements IElementUpdater
{

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final AbstractZmlRule[] rules = findRules( event.getParameters() );
    for( final AbstractZmlRule rule : rules )
    {
      rule.setEnabled( HandlerUtils.isSelected( event ) );
    }

    final IZmlTable table = ZmlHandlerUtil.getTable( event );
    final ZmlModelViewport viewport = table.getModelViewport();
    viewport.fireModelChanged( IZmlModelColumnEvent.COLUMN_RULES_CHANGED );

    return Status.OK_STATUS;
  }

  private AbstractZmlRule[] findRules( @SuppressWarnings("rawtypes") final Map parameters )
  {
    final String linkedRules = (String) parameters.get( "rules" ); //$NON-NLS-1$

    final List<AbstractZmlRule> myRules = new ArrayList<>();
    final ZmlRuleResolver resolver = ZmlRuleResolver.getInstance();

    final Iterable<String> rules = Splitter.on( ';' ).split( linkedRules ); //$NON-NLS-1$
    for( final String lnkRule : rules )
    {
      try
      {
        final RuleRefernceType reference = new RuleRefernceType();
        reference.setUrl( lnkRule );

        final AbstractZmlRule rule = resolver.findRule( null, reference );
        if( Objects.isNotNull( rule ) )
          myRules.add( rule );
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return myRules.toArray( new AbstractZmlRule[] {} );
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    final AbstractZmlRule[] rules = findRules( parameters );
    for( final AbstractZmlRule rule : rules )
    {
      element.setChecked( rule.isEnabled() );
    }
  }

}