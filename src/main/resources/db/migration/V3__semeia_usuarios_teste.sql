INSERT INTO LARES_USUARIO (NOME, EMAIL, SENHA_HASH, PAPEL, DATA_CADASTRO)
VALUES (
    'Cidadao de Demonstracao',
    'cidadao.teste@example.com',
    '$2a$10$a/2kBfeNXUh1e6KVPeRXQOao2ydlpiOG7Vn8bT2ZD9P0LMRgMiODu',
    'CIDADAO',
    CURRENT_TIMESTAMP
);

INSERT INTO LARES_USUARIO (NOME, EMAIL, SENHA_HASH, PAPEL, DATA_CADASTRO)
VALUES (
    'Gestor de Demonstracao',
    'gestor.teste@example.com',
    '$2a$10$IkurIXA8LRdoSVVZw4GvsOuvOVG23DOAZ9.oOCaROjKW5ub7w/LDO',
    'GESTOR',
    CURRENT_TIMESTAMP
);
