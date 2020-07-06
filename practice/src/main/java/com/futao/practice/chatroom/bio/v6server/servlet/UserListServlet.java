package com.futao.practice.chatroom.bio.v6server.servlet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * 返回list集合
 *
 * @author futao
 * @date 2020/7/6
 */
public class UserListServlet implements Servlet {
    @Override
    public Object service() {

        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(
                    User.builder()
                            .name("喜欢天文的pony站长")
                            .age(i)
                            .address("浙江杭州")
                            .build()
            );
        }
        return users;
    }


    @Getter
    @Setter
    @Builder
    static class User {
        private String name;
        private int age;
        private String address;
    }
}
