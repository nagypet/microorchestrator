-- Creating only the role table, because we have to add some initial data into it. The rest will be created by Hibernate
-- automatioally.

CREATE USER IF NOT EXISTS "SA" SALT '3afedbb597746d01' HASH '6d1710db13e3cd788a0fe5d897001c2d75bac269ca55583ce116e59187ca55ef' ADMIN;
CREATE SCHEMA IF NOT EXISTS "DBO" AUTHORIZATION "SA";
