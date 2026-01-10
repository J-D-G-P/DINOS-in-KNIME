/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package cu.edu.cujae.daf.knime;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Collection of KNIME extensions for for subgroup discovery
 * 
 * Original DINOS algorithm described in:
 *      Revista Cubana de Ciencias Informáticas  
 *      Vol. 14, No. 3, Mes Julio-Septiembre, 2020  
 *      ISSN: 2227-1899 | RNPS: 2301 
 *      http://rcci.uci.cu 
 *      Pág. 18-40 
 * 
 * Creators of Original DINOS algorithm:
 *      Lisandra Bravo Ilisastigui1 https://orcid.org/0000-0002-8209-4121 lbravo@ceis.cujae.edu.cu 
 *      Diana Martín Rodriguez https://orcid.org/ 0000-0001-9188-3926 dianamartin85@ceis.cujae.edu.cu 
 *      Milton García Borroto https://orcid.org/0000-0002-3154-177X mgarcia@ceis.cujae.edu.cu 
 *
 * Original Creator of the DAF library:
 *      Alejandro Tomé de Armas atomeda@ceis.cujae.edu.cu
 *      
 * Creator of this KNIME plugin and DAF library numeric and survival extensions:
 *      Jonathan David González Pereda
 *
 * @author Jonathan David González Pereda, CUJAE
 */
public class DinosNodePlugin extends Plugin {
    // The shared instance.
    private static DinosNodePlugin plugin;

    /**
     * The default constructor.
     */
    public DinosNodePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be started
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static DinosNodePlugin getDefault() {
        return plugin;
    }

}

