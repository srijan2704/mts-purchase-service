CREATE INDEX idx_po_date ON purchase_orders(order_date);
CREATE INDEX idx_po_seller ON purchase_orders(seller_id);
CREATE INDEX idx_oi_order ON order_items(order_id);
CREATE INDEX idx_oi_variant ON order_items(variant_id);
CREATE INDEX idx_var_product ON product_variants(product_id, is_active);
