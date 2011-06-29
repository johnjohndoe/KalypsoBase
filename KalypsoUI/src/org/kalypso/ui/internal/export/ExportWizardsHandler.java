package org.kalypso.ui.internal.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsHandler;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsWizard;
import org.kalypso.ui.KalypsoGisPlugin;

public class ExportWizardsHandler extends GenericWizardsHandler
{
  private static final String EXPORT_GML_WIZARDS_EXTENSION_POINT = "exportWizards"; //$NON-NLS-1$

  public ExportWizardsHandler( )
  {
    super( KalypsoGisPlugin.getId(), EXPORT_GML_WIZARDS_EXTENSION_POINT );
  }

  @Override
  protected GenericWizardsWizard createWizard( final IStructuredSelection selection, final IWizardRegistry registry )
  {
    return new ExportWizardsWizard( selection, registry );
  }
}