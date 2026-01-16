CREATE TABLE "foo"
(
  "id"                     BIGSERIAL                                NOT NULL,
  "enabled"                BOOLEAN,
  "name"                   VARCHAR(128)                             NOT NULL,
  "status"                 VARCHAR(32)                              NOT NULL,
  "description"            VARCHAR(255),
  "date"                   DATE,
  "time"                   TIME,
  "timestamp"              TIMESTAMP,
  "json_varchar"           JSON,
  "json_text"              JSON,
  "string_json_bean"       VARCHAR(255),
  "string_json_bean_array" VARCHAR(255),
  "string_json_bean_list"  VARCHAR(255),
  "string_json_bean_map"   VARCHAR(255),
  "string_json_map"        VARCHAR(255),
  "created_by"             VARCHAR(128),
  "updated_by"             VARCHAR(128),
  "created_at"             TIMESTAMPTZ DEFAULT current_timestamp(6) NOT NULL,
  "updated_at"             TIMESTAMPTZ DEFAULT current_timestamp(6) NOT NULL,
  CONSTRAINT "pk_foo" PRIMARY KEY ("id")
);

CREATE TABLE "bar"
(
  "id"      UUID         NOT NULL,
  "enabled" BOOLEAN      NOT NULL,
  "name"    VARCHAR(128) NOT NULL,
  CONSTRAINT "pk_bar" PRIMARY KEY ("id")
)
