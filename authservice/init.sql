USE library_auth;

INSERT INTO users (email, enabled, password, role, username)
VALUES (
           'admin@gmail.com  ',
           1,
           '$2a$10$JcSjMJIKzeLJwdtQGmlD.uvmmA6McqpOFS/3WrIs7SK.scR3aJDna',
           'ADMIN',
           'admin'
       )
ON DUPLICATE KEY UPDATE username=username;