create user lobby_user with password 'lobby';
create user lobby_flyway with password 'lobby_flyway';
create database lobby_db with owner lobby_flyway;
grant all privileges on database lobby_db to lobby_user;

-- create user error_report_user password 'error_report';
-- create database error_report owner error_report_user;

