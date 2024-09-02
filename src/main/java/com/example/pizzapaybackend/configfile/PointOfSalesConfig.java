package com.example.pizzapaybackend.configfile;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "pointsofsales", ignoreInvalidFields = false)
public class PointOfSalesConfig {
    

    /**
     * List of all terminals available for usage.
     * One application, in general, can support multiple point of sales.
     * All terminals in all  points of sales selected are available
     * 
     * This can be dynamic using a table in a database
     */
    private List<AvailablePointOfSale> available;

}
