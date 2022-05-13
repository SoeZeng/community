package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
//Controller的访问路径可以是空的
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        //在方法调用前，Spring MVC会自动实例化Model和Page，并将Page注入Model
        //所以，在thymeleaf中可以直接访问Page对象中的数据
        //（在Spring MVC中，方法参数都是由DispatchServlet来初始化的(model和page都是，page的数据也是由它注入的)，
        // 除此之外，DispatchServlet还会自动将Page装入model）
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null) {
            for(DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }

        // 这个就是将discussPosts这个List对象作为值存入到model中，然后将他命名成discussPosts，以后从model中取discussPosts这个对象就要通过键名discussPosts来取得
        // 这个model就会被传送到界面，实现不同层级的数据传送
        model.addAttribute("discussPosts",discussPosts);

        // 返回的是模板的路径，也就是主页的路径,就是想要把model对象中存储的数据传给哪一个界面
        // 注意区分下面这个/index和上面那个/index。上面那个是在浏览器访问的时候写index就访问了这个controller，然后这个controller处理了数据后，return给了index.html这个模板，界面就给跳转到了index.html了，下面这个return写的是html模板的名字
        return "index";
    }

    // 服务器/Controller发生异常，统一处理记了日志以后，需要返回出错的页面，那么就要手动地重定向回去
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getError() {
        return "/error/500";
    }

}
