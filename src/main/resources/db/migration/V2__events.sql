CREATE SEQUENCE SYSTEM_SEQUENCE_EVENTS;
CREATE TABLE EVENTS (
	ID INTEGER DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_EVENTS) NOT NULL,
	DATE TIMESTAMP NOT NULL,
	TITLE VARCHAR(300) NOT NULL,
	"FROM" VARCHAR(50) NOT NULL,
	PRIMARY KEY (ID)
);

