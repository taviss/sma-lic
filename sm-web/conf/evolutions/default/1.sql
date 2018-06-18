CREATE TABLE users (
    id int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    u_name varchar(64),
    u_mail varchar(45),
    u_pass varchar(256),
    u_token varchar(256),
    u_active int(2)
);