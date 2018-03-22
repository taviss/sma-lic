CREATE TABLE `images` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `i_class` varchar(128) DEFAULT NULL,
  `i_path` varchar(256) DEFAULT NULL,
  `owner_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ImageOwner_idx` (`owner_id`),
  CONSTRAINT `ImageOwner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `cameras` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `c_address` varchar(64) DEFAULT NULL,
  `c_user` varchar(64) DEFAULT NULL,
  `c_password` varchar(256) DEFAULT NULL,
  `owner_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_id_idx` (`owner_id`),
  CONSTRAINT `owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

