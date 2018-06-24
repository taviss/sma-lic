ALTER TABLE `smartfinder`.`cameras` 
ADD COLUMN `c_type` VARCHAR(45) NULL AFTER `owner_id`;

ALTER TABLE `smartfinder`.`images` 
ADD COLUMN `i_last_seen` VARCHAR(256) NULL AFTER `owner_id`;

ALTER TABLE `smartfinder`.`images` 
ADD COLUMN `i_trainable` TINYINT(1) NULL AFTER `i_last_seen`;

ALTER TABLE `smartfinder`.`images`
ADD COLUMN `i_left` FLOAT NULL AFTER `i_trainable`,
ADD COLUMN `i_top` FLOAT NULL AFTER `i_left`,
ADD COLUMN `i_right` FLOAT NULL AFTER `i_top`,
ADD COLUMN `i_bottom` FLOAT NULL AFTER `i_right`;
