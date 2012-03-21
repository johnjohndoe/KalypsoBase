package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.expressions.PropertyTester;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;

public class ZmlHourCommandsEnablePropertyTester extends PropertyTester
{
  public ZmlHourCommandsEnablePropertyTester( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( receiver instanceof IZmlTableComposite )
    {
      final IZmlTableComposite composite = (IZmlTableComposite) receiver;
      final IZmlTable table = composite.getTable();
      final Period period = HourViewCommands.getTimeStep( table );
      if( Objects.isNull( period ) )
        return false;

      final int hours = period.toStandardHours().getHours();
      return hours == 1;
    }

    return true;
  }
}
