# Using the CarbonContext API
> The usage of the `CarbonContext` API for development in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The `CarbonContext` API is used for the purpose of storing and retrieving data that is thread local. This API implements the two classes named `CarbonContext` and `PrivilegedCarbonContext`.

* **[CarbonContext](#carboncontext)**
* **[PrivilegedCarbonContext](#privilegedcarboncontext)**

## CarbonContext

This is the `ReadOnly` API, which is basically the user-level API. Shown below is a sample use case of the `CarbonContext` API.

    CarbonContext carbonContext = CarbonContext.getCurrentContext();
    String tenant = carbonContext.getTenant();
    Principal principal = carbonContext.getUserPrincipal();
    Object propertyValue = carbonContext.getProperty("PROPERTY_KEY");

As shown above, the `CarbonContext` class is used to get the following information:

* The name of the tenant dedicated for the server.
  
  > **Retrieving tenant information:**
  Note that from Carbon 5 onwards, a server is dedicated to one tenant. Therefore, we do not have a separate API for setting the tenant name. The tenant name will be taken from the `carbon.yml` file or it can be set as a system/environment variable. 
  
* The `User Principal` value, which is the JAAS principal for authorization that is applicable to the currently logged in user. 
* The properties that help you set values that can be used later in the thread flow.

## PrivilegedCarbonContext

This is the `ReadWrite` API, which is secured using java security permission. This is the final class extending from the `CarbonContext` API. Shown below is a sample use case of the `PrivilegedCarbonContext` API.

    PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
    privilegedCarbonContext.setUserPrincipal(userPrincipal);
    privilegedCarbonContext.setProperty("PROPERTY_KEY", propertyValue);

As shown above, the `PrivilegedCarbonContext` class is used to set the following information:
* The User Principal value.
* Property values.
