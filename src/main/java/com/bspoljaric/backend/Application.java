package com.bspoljaric.backend;

import com.bspoljaric.backend.service.DatabaseService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Application {

    @Inject
    DatabaseService     databaseService;
    // init Logger
    final static Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) throws Exception {
        new Application();
    }

    public Application() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages("com.bspoljaric.backend");
        resourceConfig.register(new ApplicationBinder());
        // DB init
        try {
            databaseService.initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Server

        final ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(jerseyServlet, "/*");

        jerseyServlet.setInitOrder(0);

        final Server jettyServer = new Server(20000);
        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jettyServer.destroy();
        }
    }
}
