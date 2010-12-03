package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.zml.ui.table.IZmlTableComposite;

public class ZmlPropertyTesterHourShiftCommand extends PropertyTester
{
  public ZmlPropertyTesterHourShiftCommand( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( receiver instanceof IZmlTableComposite )
    {
      final IZmlTableComposite table = (IZmlTableComposite) receiver;
      final ZmlViewResolutionFilter filter = AbstractHourViewCommand.resolveFilter( table );

      if( filter.isStuetzstellenMode() )
        return false;
      if( filter.getResulution() == 0 )
        return false;
    }

    return true;
  }

}
