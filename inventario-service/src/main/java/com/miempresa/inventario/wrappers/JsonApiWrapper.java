package com.miempresa.inventario.wrappers;

import lombok.Data;

@Data
public class JsonApiWrapper<T> {
 private JsonApiData<T> data;
}
