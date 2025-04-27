package com.itgr.thumbbackend.mapper;

import com.itgr.thumbbackend.model.empty.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author ygking
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2025-04-26 18:22:16
* @Entity generator.domain.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




