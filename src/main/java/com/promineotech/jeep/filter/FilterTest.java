package com.promineotech.jeep.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;



public class FilterTest implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    System.out.println(
        "This is a Filter Message test. This application is conducting a test of the"
        + " Filter Message System. Had this been an actual Filter Message, "
        + "it would have said something useful.");

    chain.doFilter(request, response);
  }

}
