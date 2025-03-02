CREATE TABLE "foo"
(
  "id"                     INT AUTO_INCREMENT PRIMARY KEY,
  "name"                   VARCHAR(128)                             NOT NULL,
  "status"                 VARCHAR(32)                              NOT NULL,
  "description"            VARCHAR(255)                             NULL,
  "date"                   DATE                                     NULL,
  "time"                   TIME                                     NULL,
  "timestamp"              TIMESTAMP                                NULL,
  "json"                   JSON                                     NULL,
  "json_varchar"           VARCHAR                                  NULL,
  "json_text"              TEXT                                     NULL,
  "string_json_bean"       CLOB                                     NULL,
  "string_json_bean_array" CHARACTER                                NULL,
  "string_json_bean_list"  CHARACTER                                NULL,
  "string_json_bean_map"   CHARACTER                                NULL,
  "string_json_map"        CHARACTER                                NULL,
  "created_by"             VARCHAR(128)                             NULL,
  "updated_by"             VARCHAR(128)                             NULL,
  "created_at"             DATETIME(3) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
  "updated_at"             DATETIME(3) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
  CONSTRAINT "uk_foo_name" UNIQUE ("name")
);
