CREATE OR REPLACE FUNCTION trigger_set_timestamp ()
  RETURNS TRIGGER
  AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- ACCOUNTS
CREATE TABLE "accounts" (
  "id" text NOT NULL,
  "name" text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW()
);

ALTER TABLE "accounts"
  ADD CONSTRAINT "accounts_id" PRIMARY KEY ("id");

CREATE TRIGGER accounts_set_timestamp
  BEFORE UPDATE ON accounts
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

-- USERS
CREATE TABLE "users" (
  "id" text NOT NULL,
  "login" text NOT NULL,
  "login_lowercase" text NOT NULL,
  "email_lowercase" text NOT NULL,
  "password" text NOT NULL,
  "account_id" text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW()
);

CREATE TRIGGER users_set_timestamp
  BEFORE UPDATE ON users
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

ALTER TABLE "users"
  ADD CONSTRAINT "users_id" PRIMARY KEY ("id");

CREATE UNIQUE INDEX "users_login_lowercase" ON "users" ("login_lowercase", "account_id");

CREATE UNIQUE INDEX "users_email_lowercase" ON "users" ("email_lowercase", "account_id");

ALTER TABLE "users"
  ADD CONSTRAINT "users_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- API KEYS
CREATE TABLE "api_keys" (
  "id" text NOT NULL,
  "account_id" text NOT NULL,
  "user_id" text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW(),
  "valid_until" timestamptz NOT NULL
);

CREATE TRIGGER api_keys_set_timestamp
  BEFORE UPDATE ON api_keys
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

ALTER TABLE "api_keys"
  ADD CONSTRAINT "api_keys_id" PRIMARY KEY ("id");

ALTER TABLE "api_keys"
  ADD CONSTRAINT "api_keys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "api_keys"
  ADD CONSTRAINT "api_keys_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- PASSWORD RESET CODES
CREATE TABLE "password_reset_codes" (
  "id" text NOT NULL,
  "user_id" text NOT NULL,
  "valid_until" timestamptz NOT NULL
);

ALTER TABLE "password_reset_codes"
  ADD CONSTRAINT "password_reset_codes_id" PRIMARY KEY ("id");

ALTER TABLE "password_reset_codes"
  ADD CONSTRAINT "password_reset_codes_user_fk" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- EMAILS
CREATE TABLE "scheduled_emails" (
  "id" text NOT NULL,
  "recipient" text NOT NULL,
  "subject" text NOT NULL,
  "content" text NOT NULL
);

ALTER TABLE "scheduled_emails"
  ADD CONSTRAINT "scheduled_emails_id" PRIMARY KEY ("id");

-- SHOPS
CREATE TABLE "shops" (
  "id" text NOT NULL,
  "external_id" text NOT NULL,
  "account_id" text NOT NULL,
  "from_email" text,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW()
);

ALTER TABLE "shops"
  ADD CONSTRAINT "shops_id" PRIMARY KEY ("id");

ALTER TABLE "shops"
  ADD CONSTRAINT "shops_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE UNIQUE INDEX "shops_external_id" ON "shops" ("account_id", "external_id");

CREATE TRIGGER shops_set_timestamp
  BEFORE UPDATE ON shops
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

-- PRODUCTS
CREATE TABLE "products" (
  "id" text NOT NULL,
  "external_id" text NOT NULL,
  "account_id" text NOT NULL,
  "shop_id" text NOT NULL,
  "name" text NOT NULL,
  "image_urls" text[],
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW()
);

ALTER TABLE "products"
  ADD CONSTRAINT "products_id" PRIMARY KEY ("id");

ALTER TABLE "products"
  ADD CONSTRAINT "products_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "products"
  ADD CONSTRAINT "products_shop_id_fk" FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE UNIQUE INDEX "products_external_id" ON "products" ("account_id", "external_id", "shop_id");

CREATE TRIGGER products_set_timestamp
  BEFORE UPDATE ON products
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

-- VARIANTS
CREATE TABLE "variants" (
  "id" text NOT NULL,
  "external_id" text NOT NULL,
  "product_id" text NOT NULL,
  "account_id" text NOT NULL,
  "shop_id" text NOT NULL,
  "name" text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW()
);

ALTER TABLE "variants"
  ADD CONSTRAINT "variants_id" PRIMARY KEY ("id");

ALTER TABLE "variants"
  ADD CONSTRAINT "variants_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "variants"
  ADD CONSTRAINT "variants_shop_id_fk" FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE UNIQUE INDEX "variants_external_id" ON "variants" ("account_id", "external_id", "shop_id");

CREATE TRIGGER variants_set_timestamp
  BEFORE UPDATE ON variants
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

-- ORDERS
CREATE TABLE "orders" (
  "id" text NOT NULL,
  "external_id" text NOT NULL,
  "account_id" text NOT NULL,
  "shop_id" text NOT NULL,
  "customer_id" text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW(),
  "updated_at" timestamptz NOT NULL DEFAULT NOW(),
  "proccessed_at" timestamptz,
  "received_at" timestamptz,
  "completed_at" timestamptz
);

ALTER TABLE "orders"
  ADD CONSTRAINT "orders_id" PRIMARY KEY ("id");

ALTER TABLE "orders"
  ADD CONSTRAINT "orders_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "orders"
  ADD CONSTRAINT "orders_shop_id_fk" FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "orders"
  ADD CONSTRAINT "orders_customer_id_fk" FOREIGN KEY ("customer_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE UNIQUE INDEX "orders_external_id" ON "orders" ("account_id", "external_id", "shop_id");

CREATE TRIGGER orders_set_timestamp
  BEFORE UPDATE ON orders
  FOR EACH ROW
  EXECUTE PROCEDURE trigger_set_timestamp ();

-- LINE ITEMS
CREATE TABLE "line_items" (
  id text NOT NULL,
  product_id text NOT NULL,
  variant_id text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW()
);

ALTER TABLE "line_items"
  ADD CONSTRAINT "line_items_id" PRIMARY KEY ("id");

ALTER TABLE "line_items"
  ADD CONSTRAINT "line_items_product_id_fk" FOREIGN KEY ("product_id") REFERENCES "products" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "line_items"
  ADD CONSTRAINT "line_items_variant_id_fk" FOREIGN KEY ("variant_id") REFERENCES "variants" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- REVIEWS
CREATE TABLE "reviews" (
  id text NOT NULL,
  title text,
  body text,
  rating int,
  order_id text NOT NULL,
  customer_id text NOT NULL,
  account_id text NOT NULL,
  shop_id text NOT NULL,
  product_id text NOT NULL,
  "created_at" timestamptz NOT NULL DEFAULT NOW()
);
ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_id" PRIMARY KEY (
  "id");

ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_account_id_fk" FOREIGN KEY ("account_id") REFERENCES "accounts" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_shop_id_fk" FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_customer_id_fk" FOREIGN KEY ("customer_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_order_id_fk" FOREIGN KEY ("order_id") REFERENCES "orders" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "reviews"
  ADD CONSTRAINT "reviews_product_id_fk" FOREIGN KEY ("product_id") REFERENCES "products" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

