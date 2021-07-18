package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索得到帖子放在searchResults
        //下面的page和上面的有冲突，带上包名
        org.springframework.data.domain.Page<DiscussPost> searchResults =
        elasticSearchService.searchDiscussPost(keyword,page.getCurrent() - 1, page.getLimit());

        //数据的聚合
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResults != null){
            for(DiscussPost discussPost: searchResults){
                Map<String,Object> map = new HashMap<>();
                //先把帖子存进map
                map.put("post",discussPost);
                System.out.println(discussPost);
                //再存作者
                map.put("user",userService.findUserById(discussPost.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));

                discussPosts.add(map);
            }
        }
        //把聚合后的数据传给页面 放入model中
        model.addAttribute("discussPosts",discussPosts);
        //把查询的关键词 传回
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setPath("/search/?keyword="+keyword);
        //总的数量可以从results中取；getTotalElements返回long 要强转
        page.setRows(searchResults==null?0:(int)(searchResults.getTotalElements()));

        return "/site/search";
    }


}
