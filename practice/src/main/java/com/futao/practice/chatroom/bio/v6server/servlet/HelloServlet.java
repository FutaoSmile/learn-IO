package com.futao.practice.chatroom.bio.v6server.servlet;

/**
 * 返回字符串
 *
 * @author futao
 * @date 2020/7/6
 */
public class HelloServlet implements Servlet {
    @Override
    public Object service() {
        return "greet from dynamic server...";
    }
}
