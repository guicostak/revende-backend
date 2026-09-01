-- A coluna passa a dizer o que guarda: uma URL, não a imagem. O campo Java virou
-- `profilePictureUrl` e `ddl-auto: validate` exige que o schema acompanhe.
ALTER TABLE users RENAME COLUMN profile_picture TO profile_picture_url;

-- O cadastro consulta por e-mail antes de inserir, e o login vai consultar a cada
-- tentativa. A constraint UNIQUE de V1 já cria índice implícito no Postgres, então isto
-- aqui é só o comentário registrando que a consulta está coberta — nenhum índice novo é
-- necessário, e criar um duplicado só custaria escrita.
COMMENT ON CONSTRAINT uk_users_email ON users IS
    'Garante unicidade e serve de índice para a busca por e-mail no login e no cadastro.';
