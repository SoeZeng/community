package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    // 之前实现Mapper都是在resource下新建一个配置文件写sql
    // 也可以在Mapper类中写注解，声明此方法对应的sql

    // 多个字符串会被拼接成一条sql语句
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ", //好习惯->每个字符串后添加一个空格
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    // 自动生成主键，并将其值注入Bean(LoginTicket)的id属性
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //动态sql示例（实际此处update不需要动态处理）
    @Update({
            "<script>",
            "update login_ticket set status=#{status} ",
            "where ticket=#{ticket}",
            "<if test=\"ticket!=null\">",
            "and 1=1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);

}
