package com.promineotech.jeep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import com.promineotech.ComponentScanMarker;
import com.promineotech.jeep.filter.FilterTest;

// scanBasePackageClasses tells Spring Boot to start looking for interesting classes and methods
// from the ComponentScanMarker class and and all packages under it.
@SpringBootApplication(scanBasePackageClasses = {ComponentScanMarker.class})
public class JeepSales {

  public static void main(String[] args) {
    SpringApplication.run(JeepSales.class, args);
    
    

  } // end MAIN
  @Bean
  public FilterRegistrationBean<FilterTest> filterTest() {
    FilterRegistrationBean<FilterTest> filterReg = new FilterRegistrationBean<>();
    filterReg.setFilter(new FilterTest());
    filterReg.addUrlPatterns("/jeeps");
    return filterReg;
  }

} // end CLASS
