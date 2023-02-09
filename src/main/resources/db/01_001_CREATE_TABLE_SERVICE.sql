--liquibase formatted sql

--changeset id:01_001_01_CREATE_TABLE_SERVICE author:aisypchenko
CREATE TABLE IF NOT EXISTS service
(
    id    bigint constraint service_pk primary key,
    name  text,
    price int
        CONSTRAINT positive_price CHECK (price > 0)
);
--rollback drop table service