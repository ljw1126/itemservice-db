drop table if exists item CASCADE;
create table item
(
    id bigint generated by default as identity,
    item_name varchar(10),
    price integer,
    quantity integer,
    primary key (id)
);

drop table if exists actor CASCADE;
create table actor
(
    id bigint generated by default as identity,
    first_name varchar(20),
    last_name varchar(20),
    primary key (id)
);