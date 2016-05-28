# Default admin user

# --- !Ups

INSERT INTO User VALUES ('admin', NULL, NULL)

# --- !Downs

DELETE FROM User WHERE name = 'admin';
