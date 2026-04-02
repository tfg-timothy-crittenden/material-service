INSERT INTO exam_family (id, code, name, description)
VALUES
  (1, 'TOEFL', 'TOEFL', 'Test of English as a Foreign Language'),
  (2, 'CAE', 'CAE', 'Cambridge English: Advanced'),
  (3, 'FCE', 'FCE', 'Cambridge English: First')
ON CONFLICT (id) DO NOTHING;

