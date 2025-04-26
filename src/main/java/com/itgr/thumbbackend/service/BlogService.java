package com.itgr.thumbbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itgr.thumbbackend.model.empty.Blog;
import com.itgr.thumbbackend.model.vo.BlogVO;

import java.util.List;

/**
 * @author ygking
 * @description 针对表【blog】的数据库操作Service
 * @createDate 2025-04-26 18:22:16
 */
public interface BlogService extends IService<Blog> {

    /**
     * 根据 id 获取博客详情
     *
     * @param blogId 博客 id
     * @return 博客详情
     */
    BlogVO getBlogVOById(long blogId);

    /**
     * 获取当前用户的博客列表
     *
     * @param blogList 博客列表
     * @return 博客列表
     */
    List<BlogVO> getBlogVOList(List<Blog> blogList);

}
