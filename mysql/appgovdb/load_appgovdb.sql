-- MySQL dump 10.13  Distrib 8.0.25, for Linux (x86_64)
--
-- Host: localhost    Database: appgovdb
-- ------------------------------------------------------
-- Server version	8.0.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accessrequests`
--

DROP TABLE IF EXISTS `accessrequests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accessrequests` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `project_id` int unsigned NOT NULL,
  `app_id` int unsigned NOT NULL,
  `safe_id` int unsigned NOT NULL,
  `datetime` datetime NOT NULL,
  `approved` tinyint(1) DEFAULT '0',
  `rejected` tinyint(1) DEFAULT '0',
  `provisioned` tinyint(1) DEFAULT '0',
  `revoked` tinyint(1) DEFAULT '0',
  `environment` varchar(30) NOT NULL,
  `lob_name` varchar(30) NOT NULL,
  `requestor` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `project_id` (`project_id`),
  KEY `app_id` (`app_id`),
  KEY `safe_id` (`safe_id`),
  CONSTRAINT `accessrequests_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `accessrequests_ibfk_2` FOREIGN KEY (`app_id`) REFERENCES `appidentities` (`id`),
  CONSTRAINT `accessrequests_ibfk_3` FOREIGN KEY (`safe_id`) REFERENCES `safes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accessrequests`
--

LOCK TABLES `accessrequests` WRITE;
/*!40000 ALTER TABLE `accessrequests` DISABLE KEYS */;
INSERT INTO `accessrequests` VALUES (1,1,1,1,'2021-07-15 18:34:23',1,0,1,0,'dev','CICD','bob'),(2,2,2,2,'2021-07-15 18:35:19',1,0,1,0,'prod','CICD','ted'),(3,3,3,1,'2021-07-15 18:36:35',0,0,0,0,'test','CICD','carol');
/*!40000 ALTER TABLE `accessrequests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appidentities`
--

DROP TABLE IF EXISTS `appidentities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appidentities` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `project_id` int unsigned NOT NULL,
  `name` varchar(30) NOT NULL,
  `authn_method` varchar(30) DEFAULT NULL,
  `authn_attribute1_key` varchar(50) DEFAULT NULL,
  `authn_attribute1_value` varchar(50) DEFAULT NULL,
  `authn_attribute2_key` varchar(50) DEFAULT NULL,
  `authn_attribute2_value` varchar(50) DEFAULT NULL,
  `authn_attribute3_key` varchar(50) DEFAULT NULL,
  `authn_attribute3_value` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_id` (`project_id`,`name`),
  CONSTRAINT `appidentities_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appidentities`
--

LOCK TABLES `appidentities` WRITE;
/*!40000 ALTER TABLE `appidentities` DISABLE KEYS */;
INSERT INTO `appidentities` VALUES (1,1,'devapp1','authn-k8s',NULL,NULL,NULL,NULL,NULL,NULL),(2,2,'prodapp1','authn-k8s',NULL,NULL,NULL,NULL,NULL,NULL),(3,3,'testapp1','authn-k8s',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `appidentities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cybraccounts`
--

DROP TABLE IF EXISTS `cybraccounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cybraccounts` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `safe_id` int unsigned NOT NULL,
  `name` varchar(30) NOT NULL,
  `platform_id` varchar(30) DEFAULT NULL,
  `secret_type` varchar(30) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `address` varchar(80) DEFAULT NULL,
  `resource_type` varchar(30) DEFAULT NULL,
  `resource_name` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `safe_id` (`safe_id`,`name`),
  CONSTRAINT `cybraccounts_ibfk_1` FOREIGN KEY (`safe_id`) REFERENCES `safes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cybraccounts`
--

LOCK TABLES `cybraccounts` WRITE;
/*!40000 ALTER TABLE `cybraccounts` DISABLE KEYS */;
INSERT INTO `cybraccounts` VALUES (1,1,'MySQL','MySQL','password','javauser1','conjurmaster2.northcentralus.cloudapp.azure.com','database','petclinic'),(2,2,'MySQL','MySQL','password','produser1','192.168.99.100','database','petclinic');
/*!40000 ALTER TABLE `cybraccounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `admin_user` varchar(30) DEFAULT NULL,
  `billing_code` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`admin_user`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` VALUES (1,'DevProject','DevProject-admin',NULL),(2,'ProdProject','ProdProject-admin',NULL),(3,'TestProject','TestProject-admin',NULL);
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `safes`
--

DROP TABLE IF EXISTS `safes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `safes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `vault_name` varchar(30) NOT NULL,
  `cpm_name` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`vault_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `safes`
--

LOCK TABLES `safes` WRITE;
/*!40000 ALTER TABLE `safes` DISABLE KEYS */;
INSERT INTO `safes` VALUES (1,'PetClinicDev','DemoVault','PasswordManager'),(2,'PetClinicProd','DemoVault','PasswordManager');
/*!40000 ALTER TABLE `safes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-07-15 18:37:51
