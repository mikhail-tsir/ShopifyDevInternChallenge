-- !Ups
CREATE TABLE images
(
    id SERIAL NOT NULL PRIMARY KEY,
    caption TEXT NOT NULL,
    album_id INTEGER NOT NULL,
    location TEXT NOT NULL,
    CONSTRAINT album_fk FOREIGN KEY (album_id) REFERENCES image_repo.albums (id)
);

-- !Downs
DROP TABLE images;
