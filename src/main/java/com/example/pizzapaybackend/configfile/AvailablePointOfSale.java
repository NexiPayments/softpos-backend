package com.example.pizzapaybackend.configfile;

import java.util.List;
import lombok.Data;

@Data
public class AvailablePointOfSale {

    /**
     * ID of the point of sales allowed
     */
    private String pointOfSale;

    /**
     * List of terminals available
     */
    private List<AvailablePointOfSaleTerminals> terminals;

}
