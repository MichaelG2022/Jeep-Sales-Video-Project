package com.promineotech.jeep.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DefaultJeepSalesDao implements JeepSalesDao {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public List<Jeep> fetchJeeps(JeepModel model, String trim) {
    log.debug("DAO: model={}, trim={}", model, trim);
    // @formatter:off
    String sql = ""
        + "SELECT * FROM models "
        + "WHERE model_id = :model_id "
        + "AND trim_level = :trim_level"; 
    // @formatter:on

    Map<String, Object> params = new HashMap<>();
    params.put("model_id", model.toString());
    params.put("trim_level", trim);

    /*
     * RowMapper works with just diamond operator because fetchJeeps return statement implies List
     * of Jeeps.
     * 
     * RowMapper calls mapRow to loop through the result set and return a Jeep object for each row
     * in the result set.
     * 
     * mapRow is an unimplemented method for RowMapper that returns a Jeep object for every row in
     * the result set.
     * 
     * The Builder allows us to get values from the result set for all fields for each Jeep object
     * returned by mapRow.
     * 
     * Use column names instead of column numbers because column order might be changed. It is
     * recommended to use columns in order from left to right.
     */
    // @formatter:off
    return jdbcTemplate.query(sql, params,
      new RowMapper<>() {
        @Override
        public Jeep mapRow(ResultSet rs, int rowNum) throws SQLException {
          return Jeep.builder()
            .modelPK(rs.getInt("model_pk"))
            .modelId(JeepModel.valueOf(rs.getString("model_id")))
            .trimLevel(rs.getString("trim_level"))
            .numDoors(rs.getInt("num_doors"))
            .wheelSize(rs.getInt("wheel_size"))
            .basePrice(new BigDecimal(rs.getString("base_price")))
            .build();
     // @formatter:on
          } // end mapRow
        } // end RowMapper
    ); // end jdbcTemplate.query
  } // end fetchJeeps
} // end CLASS