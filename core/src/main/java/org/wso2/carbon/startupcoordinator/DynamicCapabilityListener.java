package org.wso2.carbon.startupcoordinator;

/**
 * The DynamicCapabilityListener is the way to inform startup coordinator about the capabilities that can be
 * registered dynamically, where as @see RequireCapabilityListener is the way to statically inform startup
 * coordinator about the capabilities. DynamicCapabilityListener interface may be implemented by a carbon
 * component developer who wants to have some dependency onto some capabilities until there are available.
 * <p>
 * A DynamicCapabilityListener must be registered as an OSGi service. A bundle which registers an implementation of this
 * interface should provide the DynamicCapabilityName and DynamicCapabilityCount
 * <p>
 * e.g  DynamicCapabilityName = "org.wso2.carbon.transports.CarbonTransport"
 *      DynamicCapabilityCount = 2
 *
 *      The above will inform startup coordinator that "org.wso2.carbon.transports.CarbonTransport" is a capability
 *      that other RequireCapabilityListener can listen on and there will be "two" service instances of the capability
 *      that the startup coordinator should wait before calling the onAllRequiredCapabilitiesAvailable callback
 *      method of an interested listener.
 * <p>
 */
public interface DynamicCapabilityListener {

    /**
     * This method should return the full qualified name of the dynamic capability. This will be called by the
     * RequireCapabilityCoordinator, which is the startup coordinator, when an implementation of this is registered
     * as an OSGi service. The capability name will be used as the key to store the count of the dynamically
     * registered capabilities.
     *
     * @return the full qualified name of the capability. Eg : "org.wso2.carbon.transports.CarbonTransport"
     */
    String getDynamicCapabilityName();

    /**
     * This method should return count of the dynamic capability. This will be called by the
     * RequireCapabilityCoordinator, which is the startup coordinator, when an implementation of this is
     * registered as an OSGi service. The capability count will be used against the key that will be decremented
     * and checked when the capability is registered.
     *
     * @return the integer value of the capability count that startup coordinator should wait before calling the
     * onAllRequiredCapabilitiesAvailable callback method of an interested listener
     */
    int getDynamicCapabilityCount();
}
