package com.i2i.AuthServer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorModel {

    private int errorCode;
    private String description;

}
