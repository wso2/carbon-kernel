# Accessing the Carbon Configurations
> The process of accessing the Carbon configurations in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The instructions given below explain how you can programmatically access the Carbon configurations in an OSGi environment. Carbon configurations are stored in the `carbon.yml` file stored in the `<PRODUCT_HOME>/conf` directory. In order to acquire the Carbon configurations in the OSGi environment, the program first needs to acquire the `CarbonRuntime` service.
Follow the steps given below.

## Step 1: Accessing the `CarbonRuntime` service
There are two ways to acquire the `CarbonRuntime` service reference: You can either define a declarative service and acquire the service dynamically or else you can use the OSGi service registry lookup and directly access the service. The following samples provide a comprehensive guide for both approaches:

* Shown below is a sample implementation for accessing the CarbonRuntime via the OSGi service component.

   ```
@Component(
        name = "org.wso2.carbon.SampleDSComponent",
        immediate = true
)
class SampleDSComponent {

    @Activate
    protected void start(BundleContext bundleContext) {
    }

    /**
     * Register the carbon runtime instance.
     *
     * @param carbonRuntime - runtime instance
     */

    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRregisterCarbonRuntime"
    )
    protected void registerCarbonRuntime(CarbonRuntime runtime) {
        System.out.println(runtime.getConfiguration().getName());
    }

    protected void unRegisterCarbonRuntime(CarbonRuntime runtime) {
        System.out.println("unregistered");
    }
}
```

* Shown below is a sample implementation for accessing the `CarbonRuntime` service via OSGi service registry lookup.

   ```
ServiceReference reference = bundleContext.getServiceReference(CarbonRuntime.class.getName());
CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
CarbonConfiguration carbonConfiguration = carbonRuntime.getConfiguration();
```

## Step 2: Accessing the Carbon configurations
After acquiring the `CarbonRuntime` service, the Carbon configurations can be acquired as shown below.

```
CarbonConfiguration carbonConfiguration = carbonRuntime.getConfiguration();
```

