-- id-sequence (идемпотентно)
CREATE SEQUENCE IF NOT EXISTS user_id_seq;

-- сама таблица
CREATE TABLE IF NOT EXISTS users (
  id        BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),
  email     VARCHAR(320) NOT NULL UNIQUE,
  password  VARCHAR(100) NOT NULL,
  role      VARCHAR(20)  NOT NULL,
  created_at TIMESTAMP   NOT NULL DEFAULT now()
);

-- назначаем владельца (тоже безопасно)
ALTER SEQUENCE user_id_seq OWNED BY users.id;