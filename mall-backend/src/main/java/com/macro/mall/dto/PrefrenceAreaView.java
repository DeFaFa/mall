package com.macro.mall.dto;

import com.macro.mall.entity.PrefrenceArea;
import com.macro.mall.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class PrefrenceAreaView {
    private PrefrenceArea area;
    private List<Product> products;
}
