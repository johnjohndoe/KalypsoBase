package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.zml.core.table.model.view.ZmlViewResolutionFilter;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;

public class ZmlHourShiftCommandsEnablePropertyTester extends PropertyTester
{
  public ZmlHourShiftCommandsEnablePropertyTester( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( receiver instanceof IZmlTableComposite )
    {
      final IZmlTableComposite composite = (IZmlTableComposite) receiver;
      final IZmlTable table = composite.getTable();
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
