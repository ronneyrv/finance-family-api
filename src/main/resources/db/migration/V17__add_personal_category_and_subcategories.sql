-- ===========================================
-- Add Personal category and subcategories
-- ===========================================

-- Category
INSERT INTO categories (id, name, type)
VALUES (
           '6aa5b3de-8ec9-4c7f-a001-000000000012',
           'Pessoal',
           'EXPENSE'
       );

-- Personal subcategories
INSERT INTO sub_categories (id, name, category_id)
VALUES
       (
           '7bb5b3de-8ec9-4c7f-a001-000000000069',
           'Vestuário',
           '6aa5b3de-8ec9-4c7f-a001-000000000012'
       ),
       (
           '7bb5b3de-8ec9-4c7f-a001-000000000070',
           'Beleza',
           '6aa5b3de-8ec9-4c7f-a001-000000000012'
       ),
       (
           '7bb5b3de-8ec9-4c7f-a001-000000000071',
           'Bens materiais',
           '6aa5b3de-8ec9-4c7f-a001-000000000012'
       ),
       (
           '7bb5b3de-8ec9-4c7f-a001-000000000072',
           'Filho',
           '6aa5b3de-8ec9-4c7f-a001-000000000012'
       ),
       (
           '7bb5b3de-8ec9-4c7f-a001-000000000073',
           'Outros gastos pessoais',
           '6aa5b3de-8ec9-4c7f-a001-000000000012'
       );

-- YouTube subscription
INSERT INTO sub_categories (id, name, category_id)
VALUES (
           '7bb5b3de-8ec9-4c7f-a001-000000000074',
           'YouTube',
           '6aa5b3de-8ec9-4c7f-a001-000000000008'
       );

-- Health subcategory
INSERT INTO sub_categories (id, name, category_id)
VALUES (
           '7bb5b3de-8ec9-4c7f-a001-000000000075',
           'Esporte',
           '6aa5b3de-8ec9-4c7f-a001-000000000005'
       );