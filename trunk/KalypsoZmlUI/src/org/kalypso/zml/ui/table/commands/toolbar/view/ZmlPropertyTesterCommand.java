package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;

public class ZmlPropertyTesterCommand extends PropertyTester
{
  public ZmlPropertyTesterCommand( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    return true;
  }

}
