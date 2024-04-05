package com.gdu.myapp.dto;

import lombok.*;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BlogDto {
    private int blogNo , hit;
    private String title, contents;
    private Timestamp createDt, modifyDt;
    private UserDto user;
}
