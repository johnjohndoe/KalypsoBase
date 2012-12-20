package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.view.ZmlModelViewportResolutionFilter;
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
      final ZmlModelViewportResolutionFilter filter = AbstractHourViewCommand.resolveFilter( table );
      if( filter == null )
        return false;

      if( filter.isStuetzstellenMode() )
        return false;
// if( filter.getResolution() == 0 )
// return false;

      final Period period = HourViewCommands.getTimeStep( table );
      if( Objects.isNull( period ) )
        return false;

      final int hours = period.toStandardHours().getHours();
      return hours == 1;
    }

    return true;
  }
}
