ALTER TABLE person MODIFY COLUMN EMAIL_CAPACITY INT(11) DEFAULT 0;
ALTER TABLE file_sort ADD COLUMN DEL_USER TEXT AFTER OWNER;