package com.futao.practice.chatroom.bio.v6server.servlet;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 返回当前时间
 *
 * @author futao
 * @date 2020/7/6
 */
public class CurTimeServlet implements Servlet {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object service() {
        return "当前时间为: " + DATE_TIME_FORMATTER.format(LocalDateTime.now(ZoneOffset.ofHours(8)));
    }
}
