package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.springframework.stereotype.Component;


/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    // 清理ThreadLocal中的user
    public void clear() {
        users.remove();
    }

}
