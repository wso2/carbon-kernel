package org.wso2.carbon.kernel.startupresolver;

/**
 * The CapabilityProvider is the way to inform startup coordinator about the capabilities that can be
 * registered dynamically, where as @see RequireCapabilityListener is the way to statically inform startup
 * coordinator about the capabilities. CapabilityProvider interface may be implemented by a carbon
 * component developer who wants to have some dependency onto some capabilities until there are available.
 * <p>
 * A CapabilityProvider must be registered as an OSGi service. A bundle which registers an implementation of this
 * interface should provide the CapabilityName and CapabilityCount. The CapabilityName should be given using the
 * "provided-capability-interface" OSGi service registration property and the count should be given by implementing
 * the getCount method of CapabilityProvider interface.
 * <p>
 * e.g  capability-name = "org.wso2.carbon.transports.CarbonTransport"
 *      CapabilityCount = 2
 *
 *      The above will inform startup coordinator that "org.wso2.carbon.transports.CarbonTransport" is a capability
 *      that other RequireCapabilityListener can listen on and there will be "two" service instances of the capability
 *      that the startup coordinator should wait before calling the onAllRequiredCapabilitiesAvailable callback
 *      method of an interested listener.
 *
 * @since 5.0.0
 */
public interface CapabilityProvider {

    /**
     * This method should return count of the provided capabilities. This will be called by the
     * RequireCapabilityCoordinator, which is the startup coordinator, when an implementation of this is
     * registered as an OSGi service. The capability count will be used against the key that will be decremented
     * and checked when the capability is registered.
     *
     * @return the integer value of the capability count that startup coordinator should wait before calling the
     * onAllRequiredCapabilitiesAvailable callback method of an interested listener
     */
    int getCount();
}
