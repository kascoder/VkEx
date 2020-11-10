package org.kascoder.vkex.core.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class JobProgress {
    Integer step;
    Integer total;
    String caption;
    BigDecimal remainingSizeLimit;
}
