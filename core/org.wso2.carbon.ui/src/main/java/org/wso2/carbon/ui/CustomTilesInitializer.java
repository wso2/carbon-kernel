package org.wso2.carbon.ui;

import org.apache.tiles.factory.AbstractTilesContainerFactory;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.apache.tiles.startup.AbstractTilesInitializer;
import org.apache.tiles.startup.TilesInitializer;
import org.apache.tiles.web.startup.AbstractTilesListener;

import javax.servlet.ServletContext;

public class CustomTilesInitializer extends AbstractTilesInitializer {

    @Override
    protected AbstractTilesContainerFactory createContainerFactory(ApplicationContext applicationContext) {

        return new BasicTilesContainerFactory();
    }

    @Override
    protected ApplicationContext createTilesApplicationContext(ApplicationContext context) {

        return new ServletApplicationContext((ServletContext) context.getContext());
    }
}
