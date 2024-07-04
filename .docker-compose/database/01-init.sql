create user lobby_user with password 'lobby';
create user lobby_flyway with password 'lobby_flyway';
create database lobby_db with owner lobby_flyway;
grant all privileges on database lobby_db to lobby_user;

