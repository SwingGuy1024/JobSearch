-- I used this to create my tables the first time. Here's the build sequence:
-- 
-- 1. Write this script
-- 2. Open sqlite3
-- 3. Open the generating database file in sqlite3. This is at src/main/resources/sql/generateFromJobs.db
-- 4. Run this script, which will create the table in generateFromJobs.db.
-- 5. Paste the create statement from this script into the SQLiteRecordDao class, replacing the "create table" script that's already there.
-- 6. Run the generator: mvn jooq-codegen:generate
--
-- The code generator generates classes from the gen table. The gen table remains empty, and is only for development.
-- When you run the application the first time, it will create the real table from the statement pasted into 
-- SQLiteRecordDao.class.
 
drop table "lead";
CREATE TABLE IF NOT EXISTS lead (
  id             INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
  company        VARCHAR(512) NOT NULL collate noCase,
  contact_name   VARCHAR(512) NOT NULL collate noCase,
  dice_posn      VARCHAR(512) NOT NULL collate noCase,
  dice_id        VARCHAR(512) NOT NULL collate noCase,
  email          VARCHAR(512) NOT NULL collate noCase,
  phone1         VARCHAR(512) NOT NULL collate noCase,
  phone2         VARCHAR(512) NOT NULL collate noCase,
  fax            VARCHAR(512) NOT NULL collate noCase,
  website        VARCHAR(512) NOT NULL collate noCase,
  skype          VARCHAR(512) NOT NULL collate noCase,
  description    VARCHAR      NOT NULL collate noCase,
  history        VARCHAR      NOT NULL collate noCase,
  createdOn      DATETIME     NOT NULL DEFAULT (DATETIME('now'))
);

-- CREATE TABLE IF NOT EXISTS lead2 (
--   id             INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
--   company        VARCHAR(512) NOT NULL collate noCase,
--   contact_name   VARCHAR(512) NOT NULL collate noCase,
--   dice_posn      VARCHAR(512) NOT NULL collate noCase,
--   dice_id        VARCHAR(512) NOT NULL collate noCase,
--   email          VARCHAR(512) NOT NULL collate noCase,
--   phone1         VARCHAR(512) NOT NULL collate noCase,
--   phone2         VARCHAR(512) NOT NULL collate noCase,
--   fax            VARCHAR(512) NOT NULL collate noCase,
--   website        VARCHAR(512) NOT NULL collate noCase,
--   skype          VARCHAR(512) NOT NULL collate noCase,
--   description    VARCHAR      NOT NULL collate noCase,
--   history        VARCHAR      NOT NULL collate noCase,
--   created_on     DATETIME     NOT NULL DEFAULT (DATETIME('now'))
-- );

-- FOREIGN KEY CONSTRAINTS SHOULD BE TURNED ON when making a database connection, using
-- SQLITE_DEFAULT_FOREIGN_KEYS=1

-- drop table "history_event";
-- CREATE TABLE IF NOT EXISTS history_event (
--   id         INTEGER      NOT NULL PRIMARY KEY,
--   event_time  VARCHAR(40) NOT NULL,
--   info       VARCHAR      NOT NULL collate noCase,
--   lead_id    INTEGER      NOT NULL,
--   FOREIGN KEY(lead_id) REFERENCES lead(id)
-- );