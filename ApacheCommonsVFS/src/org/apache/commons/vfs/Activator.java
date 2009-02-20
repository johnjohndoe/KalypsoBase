package org.apache.commons.vfs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	private static final String EXTENSION_POINT_ID = "org.apache.commons.vfs.provider";
	private static HashMap<String, IConfigurationElement> THE_PROVIDER_LOCATIONS;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		readExtensions();
		final DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
				.getManager();
		for (final Map.Entry<String, IConfigurationElement> entry : THE_PROVIDER_LOCATIONS
				.entrySet()) {
			final IConfigurationElement element = (IConfigurationElement) entry
					.getValue();

			final String scheme = element.getAttribute("scheme");
			final VFSProviderExtension provider = (VFSProviderExtension) element
					.createExecutableExtension("class");
			fsManager.addProvider(scheme, provider.getProvider());
			provider.init(fsManager);
		}
	}

	private static void readExtensions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();

		if (THE_PROVIDER_LOCATIONS == null) {
			final IExtensionPoint extensionPoint = registry
					.getExtensionPoint(EXTENSION_POINT_ID);
			final IConfigurationElement[] configurationElements = extensionPoint
					.getConfigurationElements();
			THE_PROVIDER_LOCATIONS = new HashMap<String, IConfigurationElement>(
					configurationElements.length);
			for (int i = 0; i < configurationElements.length; i++) {
				final IConfigurationElement element = configurationElements[i];
				final String bundleName = element.getContributor().getName();
				THE_PROVIDER_LOCATIONS.put(bundleName, element);
			}
		}
	}
}
