package com.svivianl.dropwizardpac4j;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "corsFilter")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        System.out.println("In CorsFilter, method: " + request.getMethod());

        // Authorize (allow) all domains to consume the content
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:3000");
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET");
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "*");

        // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void destroy() {
    }

}
