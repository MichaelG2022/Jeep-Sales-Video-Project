package com.promineotech.jeep.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.promineotech.jeep.entity.Color;
import com.promineotech.jeep.entity.Customer;
import com.promineotech.jeep.entity.Engine;
import com.promineotech.jeep.entity.FuelType;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.entity.Option;
import com.promineotech.jeep.entity.OptionType;
import com.promineotech.jeep.entity.Order;
import com.promineotech.jeep.entity.Tire;

@Component
public class DefaultJeepOrderDao implements JeepOrderDao {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Order saveOrder(Customer customer, Jeep jeep, Color color, Engine engine, Tire tire,
      BigDecimal price, List<Option> options) {
    SqlParams params = generateInsertSql(customer, jeep, color, engine, tire, price);

    // keyholder catches orderPK after calling update so we can save the order options.
    KeyHolder keyHolder = new GeneratedKeyHolder();

    // Doing SQL update to save the order.
    jdbcTemplate.update(params.sql, params.source, keyHolder);

    // Extracts the orderPK from keyHolder to use for saving the order options.
    Long orderPK = keyHolder.getKey().longValue();

    saveOptions(options, orderPK);
    
    // @formatter:off
    return Order.builder()
        .orderPK(orderPK)
        .customer(customer)
        .model(jeep)
        .color(color)
        .engine(engine)
        .tire(tire)
        .options(options)
        .price(price)
        .build();
 
    // @formatter:on


  } // end saveOrder

  // Saving the options in the order_options table.
  private void saveOptions(List<Option> options, Long orderPK) {
    for (Option option : options) {
      SqlParams params = generateInsertSql(option, orderPK);
      jdbcTemplate.update(params.sql, params.source);
    } // end FOR
  } // end saveOptions

  // Generating SQL and parameter for saving the order options
  private SqlParams generateInsertSql(Option option, Long orderPK) {
    // Instantiating a new MapSqlParameterSource object
    SqlParams params = new SqlParams();
    
    // @formatter:off
    params.sql = "INSERT INTO order_options (option_fk, order_fk) "
        + "VALUES "
        + "(:option_fk, :order_fk)";
    // @formatter:on
    
    params.source.addValue("option_fk", option.getOptionPK());
    params.source.addValue("order_fk", orderPK);
    
    return params;
  } // end generateInsertSql

  // Generating SQL and parameter for saving most of the order information. 
  private SqlParams generateInsertSql(Customer customer, Jeep jeep, Color color, Engine engine,
      Tire tire, BigDecimal price) {
    // Instantiating a new MapSqlParameterSource object
    SqlParams params = new SqlParams();
    // @formatter:off
    params.sql = ""
        + "INSERT INTO orders (customer_fk, color_fk, engine_fk, tire_fk, model_fk, price) "
        + "VALUES "
        + "(:customer_fk, :color_fk, :engine_fk, :tire_fk, :model_fk, :price)";        
     // @formatter:on

    // Adding PK values for the FK values in the orders table. PK in entity tables is an FK in the
    // orders table.
    params.source.addValue("customer_fk", customer.getCustomerPK());
    params.source.addValue("color_fk", color.getColorPK());
    params.source.addValue("engine_fk", engine.getEnginePK());
    params.source.addValue("tire_fk", tire.getTirePK());
    params.source.addValue("model_fk", jeep.getModelPK());
    params.source.addValue("price", price);

    return params;
  } // end generateInsertSql

  @Override
  public List<Option> fetchOptions(List<String> optionIds) {
    // Checks for empty optionIds (still not checking for null optionIds)
    // Returns an empty list if optionIds is empty
    if (optionIds.isEmpty()) {
      return new LinkedList<>();
    } // end IF

    Map<String, Object> params = new HashMap<>();

    // @formatter:off
    String sql = ""
        + "SELECT * "
        + "FROM options "
        + "WHERE option_id IN(";
    // @formatter:on

    /*
     * The following code adds to the sql statement after the IN(
     * 
     * Now that we know options aren't empty, we will cycle through the options to create the
     * :option key value to add to the sql statement, then will add the actual value into the map
     * with the key.
     * 
     * We create the key without colons so it doesn't interfere with the Map<K,V>.
     * 
     * We add a colon to the key to create a named parameter for use in the sql statement. We also
     * add a comma and a space to the end of the key for proper sql inside the IN parameter area.
     * 
     * The sql statement will then look like this: SELECT * FROM options WHERE option_id
     * IN(:option_0, :option_1, etc.)
     * 
     * Then we use the key without colons to get the option value to put into the params Map.
     */
    for (int index = 0; index < optionIds.size(); index++) {
      // Creating the key for the Map. Key will look like "option_0"
      String key = "option_" + index;
      // Adding the colon to the key for named parameter sql statement and a comma and space at the
      // end.
      sql += ":" + key + ", ";
      params.put(key, optionIds.get(index));
    } // end FOR

    /*
     * Since we added a comma and space to the end of every option, we have to remove the comma and
     * space from the last option.
     * 
     * We do this by getting the length of sql and subtracting 2 from it to drop the last 2
     * characters.
     */
    sql = sql.substring(0, sql.length() - 2);

    /*
     * Then we have to add a closing parenthesis to finish the sql IN(
     */
    sql += ")";

    return jdbcTemplate.query(sql, params, new RowMapper<Option>() {

      @Override
      public Option mapRow(ResultSet rs, int rowNum) throws SQLException {
        // @formatter:off
        return Option.builder()
            .category(OptionType.valueOf(rs.getString("category")))
            .manufacturer(rs.getString("manufacturer"))
            .name(rs.getString("name"))
            .optionId(rs.getString("option_id"))
            .optionPK(rs.getLong("option_pk"))
            .price(rs.getBigDecimal("price"))
            .build();
        // @formatter:on
      } // end mapRow
    } // end RowMapper
    ); // end jdbcTemplate.query
  } // end fetchOptions

  @Override
  public Optional<Customer> fetchCustomer(String customerId) {
    // @formatter:off
    String sql = ""
        + "SELECT * "
        + "FROM customers "
        + "WHERE customer_id = :customer_id";
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("customer_id", customerId);


    return Optional.ofNullable(jdbcTemplate.query(sql, params, new CustomerResultSetExtractor()));

  } // end fetchCustomer

  class CustomerResultSetExtractor implements ResultSetExtractor<Customer> {

    // Unimplemented method of ResultSetExtractor interface
    @Override
    public Customer extractData(ResultSet rs) throws SQLException, DataAccessException {
      // Result set comes to us in initialized state which means the result set pointer is pointing
      // before the first line of the result set (as in index -1) so we have to use rs.next() to get
      // it to move to the first line of the result set.
      rs.next();

    // @formatter:off
    return Customer.builder()
        .customerId(rs.getString("customer_id"))
        .customerPK(rs.getLong("customer_pk"))
        .firstName(rs.getString("first_name"))
        .lastName(rs.getString("last_name"))
        .phone(rs.getString("phone"))          
        .build();
    // @formatter:on
    } // end extractData
  } // end INNER CLASS CustomerResultSetExtractor

  @Override
  public Optional<Jeep> fetchModel(JeepModel model, String trim, int doors) {
 // @formatter:off
    String sql = "" 
        + "SELECT * " 
        + "FROM models "
        + "WHERE model_id = :model_id "
        + "AND trim_level = :trim_level "
        + "AND num_doors = :num_doors";
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("model_id", model.toString());
    params.put("trim_level", trim);
    params.put("num_doors", doors);

    return Optional.ofNullable(jdbcTemplate.query(sql, params, new ModelResultSetExtractor()));
  } // end fetchModel

  class ModelResultSetExtractor implements ResultSetExtractor<Jeep> {
    @Override
    public Jeep extractData(ResultSet rs) throws SQLException {
      rs.next();

      // @formatter:off
      return Jeep.builder()
          .basePrice(rs.getBigDecimal("base_price"))
          .modelId(JeepModel.valueOf(rs.getString("model_id")))
          .modelPK(rs.getLong("model_pk"))
          .numDoors(rs.getInt("num_doors"))
          .trimLevel(rs.getString("trim_level"))
          .wheelSize(rs.getInt("wheel_size"))
          .build();
      // @formatter:on
    } // end extractData
  } // end INNER CLASS ModelResultSetExtractor

  @Override
  public Optional<Color> fetchColor(String colorId) {
 // @formatter:off
    String sql = "" 
        + "SELECT * " 
        + "FROM colors " 
        + "WHERE color_id = :color_id";
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("color_id", colorId);

    return Optional.ofNullable(jdbcTemplate.query(sql, params, new ColorResultSetExtractor()));
  } // end fetchColor

  class ColorResultSetExtractor implements ResultSetExtractor<Color> {
    @Override
    public Color extractData(ResultSet rs) throws SQLException {
      rs.next();

      // @formatter:off
      return Color.builder()
          .color(rs.getString("color"))
          .colorId(rs.getString("color_id"))
          .colorPK(rs.getLong("color_pk"))
          .isExterior(rs.getBoolean("is_exterior"))
          .price(rs.getBigDecimal("price"))
          .build();
      // @formatter:on
    } // end extractData
  } // end INNER CLASS ColorResultSetExtractor

  @Override
  public Optional<Engine> fetchEngine(String engineId) {
 // @formatter:off
    String sql = "" 
        + "SELECT * " 
        + "FROM engines " 
        + "WHERE engine_id = :engine_id";
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("engine_id", engineId);

    return Optional.ofNullable(jdbcTemplate.query(sql, params, new EngineResultSetExtractor()));
  } // end fetchEngine

  class EngineResultSetExtractor implements ResultSetExtractor<Engine> {
    @Override
    public Engine extractData(ResultSet rs) throws SQLException {
      rs.next();

      // @formatter:off
      return Engine.builder()
          .description(rs.getString("description"))
          .engineId(rs.getString("engine_id"))
          .enginePK(rs.getLong("engine_pk"))
          .fuelType(FuelType.valueOf(rs.getString("fuel_type")))
          .hasStartStop(rs.getBoolean("has_start_stop"))
          .mpgCity(rs.getFloat("mpg_city"))
          .mpgHwy(rs.getFloat("mpg_hwy"))
          .name(rs.getString("name"))
          .price(rs.getBigDecimal("price"))
          .sizeInLiters(rs.getFloat("size_in_liters"))
          .build();
      // @formatter:on
    } // end extractData
  } // end INNER CLASS EngineResultSetExtractor

  @Override
  public Optional<Tire> fetchTire(String tireId) {
 // @formatter:off
    String sql = "" 
        + "SELECT * " 
        + "FROM tires " 
        + "WHERE tire_id = :tire_id";
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("tire_id", tireId);

    return Optional.ofNullable(jdbcTemplate.query(sql, params, new TireResultSetExtractor()));
  } // end fetchTire

  class TireResultSetExtractor implements ResultSetExtractor<Tire> {
    @Override
    public Tire extractData(ResultSet rs) throws SQLException {
      rs.next();

      // @formatter:off
      return Tire.builder()
          .manufacturer(rs.getString("manufacturer"))
          .price(rs.getBigDecimal("price"))
          .tireId(rs.getString("tire_id"))
          .tirePK(rs.getLong("tire_pk"))
          .tireSize(rs.getString("tire_size"))
          .warrantyMiles(rs.getInt("warranty_miles"))
          .build();
      // @formatter:on
    }
  } // end INNER CLASS TireResultSetExtractor

  // MapSqlParameterSource passes in a simple map of parameter values to the methods of the
  // NamedParameterJdbcemplate class.
  class SqlParams {
    String sql;
    MapSqlParameterSource source = new MapSqlParameterSource();
  } // end SqlParams

} // end CLASS
