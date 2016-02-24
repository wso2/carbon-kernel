package org.wso2.carbon.datasource.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;
import org.wso2.carbon.datasource.core.spi.DataSourceReader;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.HashMap;
import java.util.Map;

@Component(
        name = "org.wso2.carbon.kernel.datasource.core.internal.DataSourceListenerComponent",
        immediate = true,
        property = {
                "capability-name=org.wso2.carbon.datasource.core.spi.DataSourceReader",
                "component-key=carbon-datasource-service"
        }
)
public class DataSourceListenerComponent implements RequiredCapabilityListener {

    private static final Log log = LogFactory.getLog(DataSourceListenerComponent.class);

    private BundleContext bundleContext;
    private Map<String, DataSourceReader> readers;

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.readers = new HashMap<>();
    }

    @Reference(
            name = "carbon.datasource.DataSourceReader",
            service = DataSourceReader.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterReader"
    )
    protected void registerReader(DataSourceReader reader) {
        if(readers.containsKey(reader.getType())) {
            log.warn("A reader with the type " + reader.getType() + "already exists. "
                    + reader.getClass().toString() + " will be ignored.");
            return;
        }
        readers.put(reader.getType(), reader);
    }

    protected void unregisterReader(DataSourceReader reader) {
        readers.remove(reader);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        log.info("initializing data source bundle");
        try {
            DataSourceManager dsManager = DataSourceManager.getInstance();
            dsManager.addDataSourceProviders(readers);
            dsManager.initDataSources();

            log.info("initializing data source bundle completed");
            DataSourceService dsService = new DataSourceServiceImpl();
            bundleContext.registerService(DataSourceService.class.getName(), dsService, null);
        } catch (DataSourceException e) {
            log.error("error occurred while initializing data sources");
        }
    }
}
