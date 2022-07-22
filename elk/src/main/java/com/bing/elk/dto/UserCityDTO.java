package com.bing.elk.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 城市聚合dto
 *
 * @author EDZ
 * @date 2022/07/06
 */
@Data
@ToString
public class UserCityDTO {
    /**
     * 城市
     */
    private String city;
    /**
     * 用户数
     */
    private Long count;
    /**
     * 平均年龄
     */
    private Double avgAge;
}