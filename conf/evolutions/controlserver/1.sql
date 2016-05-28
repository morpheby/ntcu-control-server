# Users schema

# --- !Ups

CREATE TABLE User (
    name     VARCHAR(255),
    password VARCHAR(255),
    otpKey   VARCHAR(511),
    PRIMARY KEY (name)
);

# --- !Downs

DROP TABLE User;
