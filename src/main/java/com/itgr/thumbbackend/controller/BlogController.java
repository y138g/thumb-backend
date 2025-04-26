package com.itgr.thumbbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.itgr.thumbbackend.common.BaseResponse;
import com.itgr.thumbbackend.common.ResultUtils;
import com.itgr.thumbbackend.model.empty.Blog;
import com.itgr.thumbbackend.model.vo.BlogVO;
import com.itgr.thumbbackend.service.BlogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    /**
     * 根据 id 获取博客
     *
     * @param blogId 博客 id
     * @return 博客信息
     */
    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId) {
        BlogVO blogVO = blogService.getBlogVOById(blogId);
        return ResultUtils.success(blogVO);
    }

    /**
     * 获取当前用户的博客列表
     *
     * @return 博客列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    public BaseResponse<List<BlogVO>> list() {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogList);
        return ResultUtils.success(blogVOList);
    }
}
