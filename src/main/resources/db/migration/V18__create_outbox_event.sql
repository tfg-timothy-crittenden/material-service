CREATE TABLE IF NOT EXISTS outbox_event (
                              id UUID PRIMARY KEY,
                              aggregate_type VARCHAR(100) NOT NULL,
                              aggregate_id VARCHAR(100) NOT NULL,
                              event_type VARCHAR(150) NOT NULL,
                              payload JSONB NOT NULL,
                              occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_outbox_event_occurred_at
    ON outbox_event (occurred_at);