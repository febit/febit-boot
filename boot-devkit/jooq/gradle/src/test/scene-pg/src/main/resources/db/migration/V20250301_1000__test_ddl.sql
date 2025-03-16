CREATE TABLE "foo"
(
  "id"                     BIGSERIAL,
  "enabled" BOOLEAN NULL,
  "name"                   VARCHAR(128)                             NOT NULL,
  "status"                 VARCHAR(32)                              NOT NULL,
  "description"            VARCHAR(255)                             NULL,
  "date"                   DATE                                     NULL,
  "time"                   TIME                                     NULL,
  "timestamp"              TIMESTAMP                                NULL,
  "json_varchar"           JSON                                     NULL,
  "json_text"              JSON                                     NULL,
  "string_json_bean"       VARCHAR(255)                             NULL,
  "string_json_bean_array" VARCHAR(255)                             NULL,
  "string_json_bean_list"  VARCHAR(255)                             NULL,
  "string_json_bean_map"   VARCHAR(255)                             NULL,
  "string_json_map"        VARCHAR(255)                             NULL,
  "created_by"             VARCHAR(128)                             NULL,
  "updated_by"             VARCHAR(128)                             NULL,
  "created_at"             TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
  "updated_at"             TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
  CONSTRAINT "pk_foo" PRIMARY KEY ("id")
);
