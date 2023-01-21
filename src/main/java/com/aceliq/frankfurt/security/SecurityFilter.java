package com.aceliq.frankfurt.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

  private static final String API_KEY_PARAM = "api_key";

  private static final String HEADER_NAME_API_KEY = "X-API-Key";

  private static final String API_KEY_VALUE = "sd3209Sdkl2DF3dfzsDGEsZ8476";

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    LOG.info("init Security filter");
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;
    final String requestUri = request.getRequestURI();

    LOG.debug(">> Request method {} - URI : {}", request.getMethod(), requestUri);

    LOG.debug(String.format(">> Client's IP address: %s, api_key: %s, X-API-Key: %s",
        request.getRemoteAddr(), request.getParameter(API_KEY_PARAM),
        request.getHeader(HEADER_NAME_API_KEY)));
    if (!(verifyApiKey(request))) {
      LOG.error("Either the client's IP address is not allowed, API key is invalid");
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
          "Either the client's IP address is not allowed, API key is invalid");
      return;
    }

    chain.doFilter(req, resp);
  }

  private boolean verifyApiKey(HttpServletRequest request) {
    return API_KEY_VALUE.equals(request.getHeader(HEADER_NAME_API_KEY))
        || API_KEY_VALUE.equals(request.getParameter(API_KEY_PARAM));
  }

  @Override
  public void destroy() {}

}
