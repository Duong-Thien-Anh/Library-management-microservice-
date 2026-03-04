# CREATE DATABASE IF NOT EXISTS library_auth; # using for local
# USE library_auth; # using for local

# INSERT INTO users (email, enabled, password, role, username)
# VALUES (
#            'admin@gmail.com',
#            1,
#            '$2a$10$JcSjMJIKzeLJwdtQGmlD.uvmmA6McqpOFS/3WrIs7SK.scR3aJDna',
#            'ADMIN',
#            'admin'
#        )
# ON DUPLICATE KEY UPDATE username=username;

INSERT INTO users (email, enabled, password, role, username)
SELECT 'admin@gmail.com', 1, '$2a$10$JcSjMJIKzeLJwdtQGmlD.uvmmA6McqpOFS/3WrIs7SK.scR3aJDna', 'ADMIN', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');