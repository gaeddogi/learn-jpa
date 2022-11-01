package com.jpa.jpashop.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@AllArgsConstructor
public class Address {

    /*jpa 스펙상 생성한 생성자*/
    protected Address() {
    }

    private String city;
    private String street;
    private String zipcode;

}
