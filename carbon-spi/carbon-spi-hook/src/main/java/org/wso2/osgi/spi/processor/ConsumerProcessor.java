package org.wso2.osgi.spi.processor;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.wso2.osgi.spi.Constants;
import org.wso2.osgi.spi.processor.asm.ConsumerClassVisitor;

import java.util.List;

public class ConsumerProcessor implements WeavingHook {


    public void weave(WovenClass wovenClass) {

        boolean isConsumer = false;
        Bundle consumerBundle = wovenClass.getBundleWiring().getBundle();
        BundleWiring bundleWiring = consumerBundle.adapt(BundleWiring.class);
        List<BundleWire> requiredWires = bundleWiring.getRequiredWires(Constants.EXTENDER_CAPABILITY_NAMESPACE);

        BundleCapability processorCapability;
        for (BundleWire requiredWire : requiredWires) {
            if (requiredWire.getCapability().getAttributes().containsKey(Constants.EXTENDER_CAPABILITY_NAMESPACE)) {
                String extenderCapabilityType = requiredWire.getCapability().getAttributes()
                        .get(Constants.EXTENDER_CAPABILITY_NAMESPACE).toString();

                if (extenderCapabilityType.equals(Constants.PROCESSOR_EXTENDER_NAME)) {
                    processorCapability = requiredWire.getCapability();
                    List<BundleRequirement> requirements = bundleWiring.getRequirements(Constants.EXTENDER_CAPABILITY_NAMESPACE);
                    for (BundleRequirement requirement : requirements) {
                        if (requirement.matches(processorCapability)) {
                            isConsumer = true;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (isConsumer) {
            ClassReader classReader = new ClassReader(wovenClass.getBytes());
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ConsumerClassVisitor consumerClassVisitor = new ConsumerClassVisitor(classWriter, wovenClass.getClassName());
            classReader.accept(consumerClassVisitor, ClassReader.SKIP_FRAMES);
            if (consumerClassVisitor.isModified()) {
                wovenClass.setBytes(classWriter.toByteArray());
                wovenClass.getDynamicImports().add(Constants.DYNAMIC_INJECT_PACKAGE_NAME);
            }

        }
    }
}
