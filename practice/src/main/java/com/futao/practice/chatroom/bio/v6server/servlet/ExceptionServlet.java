package com.futao.practice.chatroom.bio.v6server.servlet;

/**
 * 模拟异常的Servlet
 *
 * @author futao
 * @date 2020/7/6
 */
public class ExceptionServlet implements Servlet {
    @Override
    public Object service() {
        throw new RuntimeException("发生了异常");
    }
}
