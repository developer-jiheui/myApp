package com.gdu.myapp.dto;

import java.sql.Timestamp;

public class CommentDto {
    private int commentDto, depth, groupNo, blogNo;
    private String contents;
    private Timestamp createDt;

    private UserDto userDto;
}
