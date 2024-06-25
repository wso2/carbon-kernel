package org.wso2.carbon.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterServletAdaptor implements Servlet {
    ServletConfig config;
    Servlet delegate;
    Filter filter;
    Properties filterInitParameters;
    private FilterChain filterChain;

    public FilterServletAdaptor(Filter filter, Properties filterInitParameters, Servlet delegate) {
        this.delegate = delegate;
        this.filter = filter;
        this.filterInitParameters = filterInitParameters == null ? new Properties() : filterInitParameters;
        this.filterChain = new FilterServletAdaptor.FilterChainImpl();
    }

    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.filter.init(new FilterServletAdaptor.FilterConfigImpl());
        this.delegate.init(config);
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        this.filter.doFilter(request, response, this.filterChain);
    }

    public void destroy() {
        this.delegate.destroy();
        this.filter.destroy();
        this.config = null;
    }

    public ServletConfig getServletConfig() {
        return this.config;
    }

    public String getServletInfo() {
        return "";
    }

    class FilterConfigImpl implements FilterConfig {
        FilterConfigImpl() {
        }

        public String getFilterName() {
            String filterName = FilterServletAdaptor.this.filterInitParameters.getProperty("filter-name");
            if (filterName == null) {
                filterName = FilterServletAdaptor.this.filter.getClass().getName();
            }

            return filterName;
        }

        public String getInitParameter(String name) {
            return FilterServletAdaptor.this.filterInitParameters.getProperty(name);
        }

        public Enumeration getInitParameterNames() {
            return FilterServletAdaptor.this.filterInitParameters.propertyNames();
        }

        public ServletContext getServletContext() {
            return FilterServletAdaptor.this.config.getServletContext();
        }
    }

    public class FilterChainImpl implements FilterChain {
        public FilterChainImpl() {
        }

        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            FilterServletAdaptor.this.delegate.service(request, response);
        }
    }
}

