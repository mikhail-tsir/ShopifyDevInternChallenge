-- !Ups

CREATE TABLE image_repo.albums
(
    id          SERIAL  NOT NULL PRIMARY KEY,
    user_id     INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    description TEXT    NOT NULL,
    is_public   BOOLEAN NOT NULL,
    CONSTRAINT owner_fk FOREIGN KEY (user_id) REFERENCES image_repo.users (id)
);

-- !Downs

DROP TABLE image_repo.albums;
