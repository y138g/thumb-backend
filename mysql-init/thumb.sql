/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : thumb

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 27/04/2025 02:06:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL COMMENT '用户Id',
  `title` varchar(512) DEFAULT NULL COMMENT '标题',
  `coverImg` varchar(1024) DEFAULT NULL COMMENT '封面',
  `content` text NOT NULL COMMENT '内容',
  `thumbCount` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf16;

-- ----------------------------
-- Table structure for thumb
-- ----------------------------
DROP TABLE IF EXISTS `thumb`;
CREATE TABLE `thumb` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL COMMENT '用户Id',
  `blogId` bigint NOT NULL COMMENT '博客Id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_userId_blogId` (`userId`,`blogId`)
) ENGINE=InnoDB AUTO_INCREMENT=1916180371907567618 DEFAULT CHARSET=utf16;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(128) NOT NULL COMMENT '用户名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf16;

SET FOREIGN_KEY_CHECKS = 1;
