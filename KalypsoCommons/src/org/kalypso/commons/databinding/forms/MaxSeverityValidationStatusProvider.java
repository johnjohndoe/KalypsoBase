/*******************************************************************************
 * Copyright (c) 2009, 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 248877)
 ******************************************************************************/

package org.kalypso.commons.databinding.forms;

import java.util.Collection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.runtime.IStatus;

/*package*/ class MaxSeverityValidationStatusProvider extends ComputedValue {

  private Collection< ? > validationStatusProviders;

  public MaxSeverityValidationStatusProvider(final DataBindingContext dbc) {
    super(ValidationStatusProvider.class);
    this.validationStatusProviders = dbc.getValidationStatusProviders();
  }

  @Override
  protected Object calculate() {
    int maxSeverity = IStatus.OK;
    ValidationStatusProvider maxSeverityProvider = null;
    for( final Object name : validationStatusProviders )
    {
      final ValidationStatusProvider provider = (ValidationStatusProvider) name;
      final IStatus status = (IStatus) provider.getValidationStatus()
      .getValue();
      if (status.getSeverity() > maxSeverity) {
        maxSeverity = status.getSeverity();
        maxSeverityProvider = provider;
      }
    }
    return maxSeverityProvider;
  }

  @Override
  public synchronized void dispose() {
    validationStatusProviders = null;
    super.dispose();
  }
}
