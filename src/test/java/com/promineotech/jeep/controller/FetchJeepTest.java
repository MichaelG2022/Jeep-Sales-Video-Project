package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import com.promineotech.jeep.controller.support.FetchJeepTestSupport;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @formatter:off
@Sql(
    scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
               "classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
    config = @SqlConfig(encoding = "utf-8"))
// @formatter:on
class FetchJeepTest extends FetchJeepTestSupport {

  /*
   * This, the @Test, and the @Disabled following is the throwaway test we developed to test the
   * connection to the H2 in-memory DB.
   * 
   * I just commented it out so I can keep track of it for future reference.
   *
   * @Autowired
   * private JdbcTemplate jdbcTemplate;
   * 
   * @Test
   * void testDb() {
   * int numRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "customers");
   * System.out.println(numRows);
   *  } // end testDb
   *  
   *  @Disabled
   */
  @Test
  void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
    // Given: a valid model, trim, and URI
    JeepModel model = JeepModel.WRANGLER;
    String trim = "Sport";
    String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);

    // Added only for testing
    // System.out.println(uri);

    // When: a connection is made to the URI
    ResponseEntity<List<Jeep>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null,
        new ParameterizedTypeReference<>() {});

    // Then: a success (OK - 200) status code is returned
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // And: the actual list returns is the same as the expected list
    List<Jeep> expected = buildExpected();
    // Sysout added to show what the Lombok toString annotation looks like
    System.out.println(expected);
    assertThat(response.getBody()).isEqualTo(expected);

  } // end testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied

} // end CLASS
