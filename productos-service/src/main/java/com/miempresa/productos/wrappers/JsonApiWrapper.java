package com.miempresa.productos.wrappers;

import lombok.Data;

//JsonApiWrapper.java
@Data
public class JsonApiWrapper<T> {
 private JsonApiData<T> data;
}
