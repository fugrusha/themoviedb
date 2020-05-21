package com.golovko.backend.dto.watchlist;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class WatchlistCreateDTO {

    @NotNull
    @Size(min = 1, max = 128)
    private String name;
}
