# Builds a flyway migration for inserting sample data
FROM flyway/flyway

RUN mkdir /flyway/sql
COPY R__lobby_db_sample_data.sql /flyway/sql/
