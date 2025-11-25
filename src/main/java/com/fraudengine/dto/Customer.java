package com.fraudengine.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Customer {

    private String id;
    public Customer(String id) {
        this.id = id;
    }
}
