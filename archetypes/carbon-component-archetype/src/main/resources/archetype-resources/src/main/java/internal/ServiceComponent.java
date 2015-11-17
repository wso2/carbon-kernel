package ${package}.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 */
@Component(
        name = "${package}.internal.ServiceComponent",
        immediate = true)
public class ServiceComponent {

    @Reference(
            name = "carbon.runtime",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime")
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().unsetCarbonRuntime();
    }
}
