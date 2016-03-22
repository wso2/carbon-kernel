package org.wso2.osgi.spi;

public class Constants {

    public static final String EXTENDER_CAPABILITY_NAMESPACE = "osgi.extender";

    // ServiceLoader capability and related directive
    public static final String SERVICELOADER_NAMESPACE = "osgi.serviceloader";
    public static final String CAPABILITY_REGISTER_DIRECTIVE = "register";

    // Service registration property
    public static final String SERVICELOADER_MEDIATOR_PROPERTY = "serviceloader.mediator";

    // The names of the extenders involved
    public static final String PROCESSOR_EXTENDER_NAME = "osgi.serviceloader.processor";
    public static final String REGISTRAR_EXTENDER_NAME = "osgi.serviceloader.registrar";

    public static final String METAINF_SERVICES = "META-INF/services";

    public static final String DYNAMIC_INJECT_CLASS_NAME = "Lorg/wso2/osgi/spi/processor/DynamicInject;";
    public static final String DYNAMIC_INJECT_PACKAGE_NAME = "org.wso2.osgi.spi.processor";

}
