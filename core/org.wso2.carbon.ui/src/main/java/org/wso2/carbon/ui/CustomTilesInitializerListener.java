package org.wso2.carbon.ui;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.apache.tiles.request.servlet.wildcard.WildcardServletApplicationContext;
import org.apache.tiles.startup.TilesInitializer;
import org.apache.tiles.startup.AbstractTilesInitializer;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.request.servlet.ServletUtil;
import org.apache.tiles.web.startup.AbstractTilesListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
        service = ServletContextListener.class,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=tilesContext)"
        }
)
public class CustomTilesInitializerListener extends AbstractTilesListener {

    @Override
    protected TilesInitializer createTilesInitializer() {

        return new CustomTilesInitializer();
    }

//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        ServletApplicationContext application = new ServletApplicationContext(sce.getServletContext());
//        TilesInitializer initializer = new AbstractTilesInitializer() {
//            @Override
//            protected ApplicationContext createTilesApplicationContext(ApplicationContext context) {
//                return new ServletApplicationContext((ServletContext)context.getContext());
//            }
//
////            @Override
////            protected ApplicationContext createApplicationContext(Object context) {
////                return new ServletApplicationContext((javax.servlet.ServletContext) context);
////            }
//
//            @Override
//            protected BasicTilesContainerFactory createContainerFactory(ApplicationContext context) {
//                return new BasicTilesContainerFactory();
//            }
//        };
////        TilesContainer container = initializer.createContainer(application);
////        ServletUtil.setContainer(sce.getServletContext(), container);
//
//        initializer.initialize(application);
////        ServletUtil.setContainer(sce.getServletContext(), container);
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        // Clean up resources if necessary
//    }


}

