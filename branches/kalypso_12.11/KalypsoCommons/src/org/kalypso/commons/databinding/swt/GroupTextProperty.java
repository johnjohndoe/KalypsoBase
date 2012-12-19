/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 256543, 262287
 ******************************************************************************/

package org.kalypso.commons.databinding.swt;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.widgets.Group;

public class GroupTextProperty extends WidgetValueProperty
{
  @Override
  public Object getValueType( )
  {
    return String.class;
  }

  @Override
  protected Object doGetValue( final Object source )
  {
    return ((Group) source).getText();
  }

  @Override
  protected void doSetValue( final Object source, final Object value )
  {
    if( value instanceof String )
      ((Group) source).setText( (String) value );
    else
      ((Group) source).setText( StringUtils.EMPTY );
  }

  @Override
  public String toString( )
  {
    return "Group.text <String>"; //$NON-NLS-1$
  }
}