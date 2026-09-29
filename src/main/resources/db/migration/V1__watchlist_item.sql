CREATE TABLE watchlist_item (
    user_id     INTEGER      NOT NULL,
    content_id  VARCHAR(128) NOT NULL,
    added_at    TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (user_id, content_id)
);

CREATE INDEX idx_watchlist_item_user_added_at ON watchlist_item (user_id, added_at DESC);
