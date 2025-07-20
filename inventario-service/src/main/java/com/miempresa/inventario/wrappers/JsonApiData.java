package com.miempresa.inventario.wrappers;

import lombok.Data;

@Data
public class JsonApiData<T> {
    private String type;
    private String id;
    private T attributes;
}