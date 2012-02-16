package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.zml.core.table.model.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.IZmlTable;

public class ZmlPropertyTesterHourShiftCommand extends PropertyTester
{
  public ZmlPropertyTesterHourShiftCommand( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( receiver instanceof IZmlTable )
    {
      final IZmlTable table = (IZmlTable) receiver;
      final ZmlViewResolutionFilter filter = AbstractHourViewCommand.resolveFilter( table );
      if( filter == null )
        return false;

      if( filter.isStuetzstellenMode() )
        return false;
      if( filter.getResolution() == 0 )
        return false;
    }

    return true;
  }

}
