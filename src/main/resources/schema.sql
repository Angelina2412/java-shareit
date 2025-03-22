CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  available BOOLEAN NOT NULL,
  owner_id BIGINT NOT NULL,
  item_request_id BIGINT,
  CONSTRAINT pk_item PRIMARY KEY (id),
  CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT fk_item_request FOREIGN KEY (item_request_id) REFERENCES item_requests(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    booker_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id SERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS item_requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description TEXT NOT NULL,
    requester_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_item_request PRIMARY KEY (id),
    CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE
);



