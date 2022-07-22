package com.bing.elk.document;

import lombok.Data;

/**
 * @author EDZ
 * @date 2022/07/02
 */
@Data
public class UserDocument {
    private String id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 城市
     */
    private String city;
}