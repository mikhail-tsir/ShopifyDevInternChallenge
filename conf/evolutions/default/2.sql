-- !Ups
CREATE TABLE image_repo.users
(
    id       SERIAL NOT NULL PRIMARY KEY,
    username TEXT   NOT NULL,
    name     TEXT   NOT NULL,
    password TEXT   NOT NULL
);

-- !Downs

DROP TABLE image_repo.users;
