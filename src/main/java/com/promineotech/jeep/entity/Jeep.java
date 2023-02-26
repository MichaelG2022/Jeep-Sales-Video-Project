package com.promineotech.jeep.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// @formatter:off
/*
 * @Data does the following (simplified):
 *      @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode
 *      Since no args are required, this generates a no-args constructor.
 * 
 * @Builder does the following: (In order of the outline)
 *      In the annotated class, generates an all-args constructor.
 *          In this case, it changed the no-args constructor created by @Data so we
 *          had to add @NoArgsConstructor annotation.
 *      In the annotated class, generates a builder() method to create a new instance of builder.
 *      In the annotated class, generates the JeepBuilder class as an inner class.
 *      In the Builder class, generates private non-static, non-final fields
 *          for each parameter of the annotated class.
 *      In the Builder class, generates a package private no-args empty constructor.
 *      In the Builder class, generates a setter-like method for 
 *          each field (ex: this.modelPK = modelPK). The "this" is returned for each field 
 *          which allows the "setter" calls to be chained as shown in FetchJeepTestSupport.
 *      In the Builder class, generates a build() method which calls the setter-like methods,
 *          passing in each field.
 *      In the Builder class, generates a toString() override method.

 * @NoArgsConstructor generates a no-args constructor to replace the one that @Builder changed.
 *      Adding the no-args constructor breaks @Builder so we have to add an all-args
 *      constructor to fix @Builder.
 *      We added the no-args constructor for possible future use.
 *      
 * @AllArgsConstructor generates an all-args constructor to replace the one that got changed
 *      when we added the @NoArgsConstructor. This fixes @Builder.
 */
//@formatter:on
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jeep {
  // He set modelPK to Long in the video. I set it to int to match the table.
  private int modelPK;
  private String modelId;
  private String trimLevel;
  private int numDoors;
  private int wheelSize;
  private BigDecimal basePrice;

} // end CLASS
