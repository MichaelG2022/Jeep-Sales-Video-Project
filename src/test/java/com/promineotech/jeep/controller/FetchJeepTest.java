package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import com.promineotech.jeep.Constants;
import com.promineotech.jeep.controller.support.FetchJeepTestSupport;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.service.JeepSalesService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @formatter:off
@Sql(
    scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
               "classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
    config = @SqlConfig(encoding = "utf-8"))
// @formatter:on
class FetchJeepTest {

  /*
   * THROWAWAY TEST TO TEST CONNECTION TO H2 IN-MEMORY DB This, the @Test, and the @Disabled
   * following is the throwaway test we developed to test the connection to the H2 in-memory DB.
   * 
   * I just commented it out so I can keep track of it for future reference.
   *
   * @Autowired private JdbcTemplate jdbcTemplate;
   * 
   * The sysout should show 2 rows as the WRANGLER Sport has 2 choices, 2-door and 4-door
   * 
   * @Test void testDb() { int numRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "customers");
   * System.out.println(numRows); } // end testDb
   * 
   * @Disabled
   */

  // ********************************************************************************************
  // DOESN'T POLLUTE APPLICATION CONTEXT
  // ********************************************************************************************
  @Nested
  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
  @ActiveProfiles("test")
  // @formatter:off
  @Sql(
      scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
                 "classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
      config = @SqlConfig(encoding = "utf-8"))
  // @formatter:on
  class TestsThatDoNotPolluteTheApplicationContext extends FetchJeepTestSupport {
    // ********************************************************************************************
    // VALID MODEL AND TRIM TEST - successful/OK - 200 status code
    // ********************************************************************************************
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

      /*
       * Added the declaration for actual to be able to change the modelPK to 0 so test would pass.
       * 
       * Dr. Rob decided this was not the best method. I left in the code that went with the changes
       * we made but commented them out.
       * 
       * The following line was added and the assertThat modified to match to try to fix issues with
       * modelPK during the test.
       */
      // List<Jeep> actual = response.getBody();

      List<Jeep> expected = buildExpected();

      /*
       * Dr. Rob decided not to use this style to fix the modelPK issue. Instead, we modified the
       * Jeep class itself.
       */
      // actual.forEach(jeep -> jeep.setModelPK(0));

      /*
       * The following line is what we used to fix the modelPK test issue after we used a Lambda
       * expression to change "actual"
       */
      // assertThat(actual).isEqualTo(expected);

      // Sysout added to show what the Lombok toString annotation looks like
      // System.out.println(response.getBody());

      /*
       * This was the original line before we fixed the modelPK test issue by setting the modelPK in
       * "actual" to 0.
       */
      assertThat(response.getBody()).isEqualTo(expected);

    } // end testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied

    // ********************************************************************************************
    // UNKNOWN TRIM TEST - NOT_FOUND - 404 status code
    // ********************************************************************************************
    @Test
    void testThatAnErrorMessageIsReturnedWhenAnUnkownTrimIsSupplied() {
      // Given: a valid model and URI and unknown trim
      JeepModel model = JeepModel.WRANGLER;
      String trim = "Invalid value";
      String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);

      // @formatter:off
      // When: a connection is made to the URI
      ResponseEntity<Map<String, Object>> response =
          getRestTemplate().exchange(uri, HttpMethod.GET,
          null, new ParameterizedTypeReference<>() {});
      // @formatter:on
      // Then: a not found (404) status code is returned
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

      // And: an error message is returned
      Map<String, Object> error = response.getBody();

      // Method made by extracting the assertThat(error) and following steps into a protected method
      assertErrorMessageValid(error, HttpStatus.NOT_FOUND);
    } // end testThatAnErrorMessageIsReturnedWhenAnUnkownTrimIsSupplied

    // ********************************************************************************************
    // INVALID VALUE TEST - BAD_INPUT - 400 status code
    // ********************************************************************************************
    @ParameterizedTest
    @MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
    void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(String model, String trim,
        String reason) {

      // Given: a valid model and URI and unknown trim
      String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);

      // When: a connection is made to the URI
      ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET,
          null, new ParameterizedTypeReference<>() {});

      // Then: a bad request (400) status code is returned
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

      // And: an error message is returned
      Map<String, Object> error = response.getBody();

      // Method made by extracting the assertThat(error) and following steps into a protected method
      assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
    } // end testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied
  } // end Non-polluting CLASS

  // ********************************************************************************************
  // PARAMETERIZED TEST STREAMING SOURCE
  // ********************************************************************************************
  static Stream<Arguments> parametersForInvalidInput() {
    // @formatter:off
    return Stream.of(
        arguments("WRANGLER", "@#$%^&", "Trim contains non-alphanumeric characters"),
        arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim length too long"),
        arguments("PINTO", "Sport", "Model is not enum value")
        );
 // @formatter:on
  } // end parametersForInvalidInput


  // ********************************************************************************************
  // POLLUTES APPLICATION CONTEXT
  // ********************************************************************************************
  @Nested
  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
  @ActiveProfiles("test")
  // @formatter:off
  @Sql(
      scripts = {"classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
                 "classpath:flyway/migrations/V1.1__Jeep_Data.sql"},
      config = @SqlConfig(encoding = "utf-8"))
  // @formatter:on
  class TestsThatPolluteTheApplicationContext extends FetchJeepTestSupport {
    // Creates mock JeepSalesService bean in Bean Registry, replacing any bean of the same name
    @MockBean
    private JeepSalesService jeepSalesService;

    // ********************************************************************************************
    // UNPLANNED ERROR - INTERNAL_SERVER_ERROR - 500 status code
    // ********************************************************************************************
    @Test
    void testThatAnUnplannerErrorResultsInA500Status() {
      // Given: a valid model and URI and unknown trim
      JeepModel model = JeepModel.WRANGLER;
      String trim = "Honda";
      String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);

      // Programming Mock Bean
      // @formatter:off
      doThrow(new RuntimeException("Did I do that?!"))
      .when(jeepSalesService).fetchJeeps(model, trim);

      // When: a connection is made to the URI
      ResponseEntity<Map<String, Object>> response =
          getRestTemplate().exchange(
              uri, 
              HttpMethod.GET, 
              null, 
              new ParameterizedTypeReference<>() {});
      // @formatter:on
      // Then: an internal server error is returned
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

      // And: an error message is returned
      Map<String, Object> error = response.getBody();

      // Method made by extracting the assertThat(error) and following steps into a protected method
      assertErrorMessageValid(error, HttpStatus.INTERNAL_SERVER_ERROR);
    } // end testThatAnErrorMessageIsReturnedWhenAnUnkownTrimIsSupplied

  } // end Polluting CLASS



} // end CLASS
