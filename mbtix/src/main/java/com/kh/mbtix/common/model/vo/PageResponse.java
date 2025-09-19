package com.kh.mbtix.common.model.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private PageInfo pi;
    private List<T> list;
}