package com.promineotech.jeep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.promineotech.ComponentScanMarker;

//scanBasePackageClasses tells Spring Boot to start looking for interesting classes and methods from the ComponentScanMarker class and and all packages under it.
@SpringBootApplication(scanBasePackageClasses = {ComponentScanMarker.class})
public class JeepSales {

  public static void main(String[] args) {
    SpringApplication.run(JeepSales.class, args);

  } // end MAIN

} // end CLASS
