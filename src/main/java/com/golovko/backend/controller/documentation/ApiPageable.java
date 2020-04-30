package com.golovko.backend.controller.documentation;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", type = "int", paramType = "query",
                value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
        @ApiImplicitParam(name = "size", type = "int", paramType = "query",
                value = "Number of records per page. Max value is 500", defaultValue = "50"),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). "
                        + "Default order is ascending. Multiple sort criteria is supported")
})
public @interface ApiPageable {
}
