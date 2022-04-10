-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: apijson.cn    Database: sys
-- ------------------------------------------------------
-- Server version	5.7.34-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `GlobalConfig`
--

DROP TABLE IF EXISTS `GlobalConfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GlobalConfig` (
  `id` bigint(15) NOT NULL COMMENT '唯一标识',
  `projectId` bigint(15) NOT NULL DEFAULT '0' COMMENT '项目 id',
  `requestHeader` varchar(1000) DEFAULT NULL COMMENT '请求头',
  `responseHeader` varchar(1000) DEFAULT NULL COMMENT '响应头',
  `requestRandom` varchar(1000) DEFAULT NULL COMMENT '请求参数注入配置',
  `responseRandom` varchar(1000) DEFAULT NULL COMMENT '响应参数注入配置',
  `breforeScript` varchar(5000) DEFAULT NULL COMMENT '前置执行脚本，一般用于初始化数据',
  `afterScript` varchar(5000) DEFAULT NULL COMMENT '后置执行脚本，一般用于断言和还原数据',
  `date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='全局配置  // TODO 是否改成把内容分散到原来的各个表，然后通过字段 is_global = 1 及 is_before = 1 等 标识？ 另外需要单独拆分出 Header 表，为了实现和 Random 一样与 Document 多对一，批量覆盖不同场景，也必须拆分出 Header';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `GlobalConfig`
--

LOCK TABLES `GlobalConfig` WRITE;
/*!40000 ALTER TABLE `GlobalConfig` DISABLE KEYS */;
/*!40000 ALTER TABLE `GlobalConfig` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-04-11  1:21:19
