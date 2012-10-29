package org.kalypso.ui.internal.imports;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsHandler;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsWizard;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.imports.ImportWizardsWizard;

public class ImportWizardsHandler extends GenericWizardsHandler
{
  private static final String IMPORT_GML_WIZARDS_EXTENSION_POINT = "importWizards"; //$NON-NLS-1$

  public ImportWizardsHandler( )
  {
    super( KalypsoGisPlugin.getId(), IMPORT_GML_WIZARDS_EXTENSION_POINT );
  }

  @Override
  protected GenericWizardsWizard createWizard( final IStructuredSelection selection, final IWizardRegistry registry )
  {
    return new ImportWizardsWizard( selection, registry );
  }
}