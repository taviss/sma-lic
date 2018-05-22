ALTER TABLE `smartfinder`.`cameras` 
ADD COLUMN `c_type` VARCHAR(45) NULL AFTER `owner_id`;

ALTER TABLE `smartfinder`.`images` 
ADD COLUMN `i_last_seen` VARCHAR(256) NULL AFTER `owner_id`;

ALTER TABLE `smartfinder`.`images` 
ADD COLUMN `i_trainable` TINYINT(1) NULL AFTER `i_last_seen`;