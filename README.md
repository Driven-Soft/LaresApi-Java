# Lares — API Java

**Projeto:** Lares, zeladoria colaborativa · Global Solution 2026/2 · CitizenTech
**Equipe:** Driven Soft
**Disciplina:** Java Advanced
**Repositórios integrados:** [Lares-Mobile](https://github.com/Driven-Soft/Lares-Mobile) · [Lares-IA](https://github.com/Driven-Soft/Lares-IA) · [LaresApi-NET](https://github.com/Driven-Soft/LaresApi-NET)
> **Repositório GitHub:** [https://github.com/Driven-Soft/LaresApi-Java](https://github.com/Driven-Soft/LaresApi-Java)

## Links da entrega

> - **Deploy:** [LaresApi-Java](https://laresapi-java.onrender.com)
> - **Vídeo pitch:** [Demonstração da solução LaresApi - Driven Soft (Global Solution 2 | 2026)](https://www.youtube.com/watch?v=Uh9tMj9W-fk)

## Integrantes

| Nome | RM |
| --- | --- |
| Felipe Bezerra Beatrici | 564723 |
| Max Hayashi Batista | 563717 |
| Henrique Cunha Torres | 565119 |
| Lucas da Silva Lima | 562118 |
| Yasmin Nathalin Miranda dos Santos | 561365 |

## Sumário

- [Links da entrega](#links-da-entrega)
1. [O problema e a solução](#1-o-problema-e-a-solução)
2. [Arquitetura e fluxo](#2-arquitetura-e-fluxo)
3. [Tecnologias e decisões](#3-tecnologias-e-decisões)
4. [Modelo de dados](#4-modelo-de-dados)
5. [Estrutura do repositório](#5-estrutura-do-repositório)
6. [Como executar localmente](#6-como-executar-localmente)
7. [Configuração de ambiente](#7-configuração-de-ambiente)
8. [Autenticação e papéis](#8-autenticação-e-papéis)
9. [Contrato da API](#9-contrato-da-api)
10. [Swagger / OpenAPI](#10-swagger--openapi)
11. [Deploy](#11-deploy)
12. [Limitações e próximos passos](#12-limitações-e-próximos-passos)
13. [Uso responsável e uso de IA](#13-uso-responsável-e-uso-de-ia)

---

## 1. O problema e a solução

Moradores encontram problemas urbanos — como buracos, postes apagados, lixo
acumulado ou calçadas danificadas — mas nem sempre sabem como encaminhá-los ou
acompanhar a resposta. A prefeitura, por sua vez, precisa organizar relatos e
priorizar o atendimento.

O Lares conecta cidadãos e gestores públicos. Pelo aplicativo, o cidadão
registra uma ocorrência e acompanha seu histórico. A API registra o relato e
inicia uma triagem assíncrona; o gestor consulta a fila, atualiza o andamento e
pode corrigir a categoria sugerida pela IA.

- **Público beneficiado:** cidadãos e equipes municipais.
- **CitizenTech:** tecnologia cívica para melhorar a comunicação e a zeladoria
  urbana.
- **ODS relacionados:** ODS 11 (Cidades e Comunidades Sustentáveis) e ODS 10
  (Redução das Desigualdades).
- **Impacto esperado:** acompanhar ocorrências e comparar atendimento entre
  bairros.

## 2. Arquitetura e fluxo

```mermaid
flowchart LR
    APP[Lares Mobile<br/>React Native + Expo] -->|REST / JSON| API[Lares API<br/>Spring Boot]
    API --> DB[(Oracle<br/>JPA + Flyway)]
    API -->|ocorrencia.criada<br/>após commit| MQ[(RabbitMQ)]
    MQ --> CONSUMER[Consumidor na API Java]
    CONSUMER -->|POST /triagem| TRIAGE[Lares-IA<br/>Python + FastAPI]
    TRIAGE -->|categoria, prioridade,<br/>prazo estimado| CONSUMER
    CONSUMER -->|Spring AI| LLM[OpenAI]
    CONSUMER -->|grava triagem,<br/>textos e histórico| DB
    MANAGER[Gestor municipal] -->|REST + JWT| API
    API2[Lares API .NET] -. API paralela<br/>mesmo contrato .-> DB2[(Azure SQL)]
```

1. O cidadão envia uma ocorrência para `POST /ocorrencias`.
2. A API grava a ocorrência como `RECEBIDA` e publica `ocorrencia.criada`
   somente depois do commit da transação.
3. O consumidor RabbitMQ consulta a ocorrência e chama o serviço Lares-IA para
   classificar categoria e prioridade e estimar prazo.
4. O Spring AI gera um resumo para a equipe municipal e uma mensagem ao
   cidadão.
5. A API grava os resultados e o histórico e muda o status para `TRIADA`.
6. O gestor atualiza o andamento até `RESOLVIDA`.

Se a triagem externa falhar, a ocorrência permanece `RECEBIDA`. Se somente a
geração de textos do Spring AI falhar, a triagem é salva sem `resumoIa` e
`mensagemCidadao`.

### Componentes integrados

| Componente | Tecnologia | Repositório / responsabilidade |
| --- | --- | --- |
| Aplicativo | React Native + Expo | [Lares-Mobile](https://github.com/Driven-Soft/Lares-Mobile), integração usada pelo cidadão e gestor |
| API principal | Spring Boot, Java | Este repositório; API REST consumida pelo app |
| Serviço de triagem | Python + FastAPI | [Lares-IA](https://github.com/Driven-Soft/Lares-IA), classificação e prazo estimado |
| Mensageria | RabbitMQ / CloudAMQP | Processamento assíncrono de novas ocorrências |
| API paralela | .NET | [LaresApi-NET](https://github.com/Driven-Soft/LaresApi-NET), contrato paralelo para DevOps |
| Banco Java | Oracle | Persistência da API Java, com migrations Flyway |

## 3. Tecnologias e decisões

| Área | Tecnologia | Uso |
| --- | --- | --- |
| Linguagem e framework | Java 17, Spring Boot 4 | Aplicação REST |
| Persistência | Spring Data JPA, Oracle | Entidades, relacionamentos e repositórios |
| Migrações | Flyway | DDL e carga inicial das categorias |
| Segurança | Spring Security, JWT, BCrypt | Autenticação stateless e papéis |
| Validação | Bean Validation | Validar dados recebidos nos DTOs |
| Erros | `@RestControllerAdvice` | Respostas de erro JSON padronizadas |
| Mensageria | RabbitMQ | Triagem assíncrona após criação |
| IA generativa | Spring AI, OpenAI | Resumo para equipe e mensagem ao cidadão |
| Integração HTTP | `RestClient` | Chamada ao serviço externo de triagem |
| Documentação | Springdoc OpenAPI | Swagger UI e contrato OpenAPI |
| Empacotamento | Docker, Docker Compose | API e RabbitMQ em ambiente local |
| Deploy planejado | Render, CloudAMQP | Hospedagem da API e broker gerenciado |

A publicação do evento é feita após o commit para evitar que o consumidor
receba um identificador ainda não persistido. O cadastro público sempre cria
um `CIDADAO`; o papel não é aceito no corpo da requisição. As operações de
negócio e as transições de status ficam no service.

### Requisito 5 — opção escolhida

Foi escolhida a **opção A: arquitetura distribuída com RabbitMQ**. A API publica
o evento `ocorrencia.criada` após salvar a ocorrência, e um consumidor processa
a triagem de forma assíncrona. A integração HTTP com o serviço Lares-IA é usada
nesse fluxo; o projeto não adota a opção B com Eureka.

## 4. Modelo de dados

O banco da API Java possui quatro entidades relacionadas:

| Entidade | Responsabilidade e relacionamentos |
| --- | --- |
| `LARES_USUARIO` | Cidadãos e gestores; relaciona-se às ocorrências abertas e às mudanças de status |
| `LARES_CATEGORIA` | Categorias e seus prazos padrão; uma categoria pode classificar várias ocorrências |
| `LARES_OCORRENCIA` | Descrição, bairro, coordenadas opcionais, status, prioridade e resultados da IA |
| `LARES_HISTORICO_STATUS` | Linha do tempo de cada ocorrência, com status anterior/novo, comentário, autor e data |

Relacionamentos: usuário 1:N ocorrência; categoria 1:N ocorrência; ocorrência
1:N histórico; usuário 1:N mudanças de histórico. A categoria da ocorrência e
o usuário responsável por uma mudança de histórico podem ser nulos quando
apropriado.

Categorias carregadas pelo seed: `BURACO`, `ILUMINACAO`, `LIXO`, `CALCADA`,
`ARVORE`, `SINALIZACAO` e `OUTROS`.

### Ciclo de status

```text
RECEBIDA -> TRIADA -> EM_ANDAMENTO -> RESOLVIDA
      \----------------> EM_ANDAMENTO
```

O gestor pode avançar diretamente de `RECEBIDA` para `EM_ANDAMENTO`. Outras
transições inválidas retornam `409 Conflict`. O cidadão só pode editar ou
excluir a própria ocorrência enquanto ela estiver `RECEBIDA` ou `TRIADA`.

## 5. Estrutura do repositório

```text
.
├── .mvn/wrapper/                 # configuração do Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/br/com/fiap/laresapi/
│   │   │   ├── config/           # JWT, segurança e OpenAPI
│   │   │   ├── controller/       # endpoints REST
│   │   │   ├── dto/              # contratos de entrada e saída
│   │   │   ├── entity/           # entidades e enums JPA
│   │   │   ├── exception/        # tratamento global de erros
│   │   │   ├── integration/      # cliente HTTP para Lares-IA
│   │   │   ├── mapper/           # conversão de entidades para DTOs
│   │   │   ├── messaging/        # eventos, RabbitMQ e consumidor
│   │   │   ├── repository/       # repositórios Spring Data
│   │   │   └── service/          # regras de negócio e Spring AI
│   │   └── resources/
│   │       ├── db/migration/     # migrations Flyway
│   │       └── application.properties
│   └── test/                     # testes automatizados
├── .env.example                 # nomes das variáveis, sem credenciais
├── docker-compose.yml           # API e RabbitMQ locais
├── Dockerfile                   # imagem multi-stage
├── mvnw / mvnw.cmd              # Maven Wrapper para Linux/macOS e Windows
├── pom.xml                      # dependências e build Maven
└── render.yaml                  # blueprint de deploy
```

## 6. Como executar localmente

### Pré-requisitos

- JDK 17.
- Docker Desktop com Docker Compose.
- Acesso a um Oracle compatível com as migrations do projeto.
- Uma chave OpenAI e uma senha para o RabbitMQ local.

O Compose sobe a API e o RabbitMQ, mas **não** provisiona um Oracle local.

1. Copie o modelo de ambiente:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Edite o `.env` local e configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
   `JWT_SECRET`, `OPENAI_API_KEY` e `RABBITMQ_DEFAULT_PASS`.
3. Configure `TRIAGEM_BASE_URL` e `TRIAGEM_API_KEY` quando o serviço Lares-IA
   estiver disponível. Até lá, a conexão ponta a ponta com a triagem não pode
   ser validada.
4. Inicie os serviços:

   ```powershell
   docker compose up --build
   ```

A API ficará em `http://localhost:8080`; o painel RabbitMQ, em
`http://localhost:15672`. O Flyway aplica as migrations na inicialização.

Para executar os testes sem iniciar a API:

```powershell
.\mvnw.cmd test
```

O Maven Wrapper baixa a distribuição do Maven definida no próprio projeto.

### Executar a API diretamente pelo Maven

O Spring Boot não carrega `.env` automaticamente quando executado diretamente.
Defina as variáveis no terminal primeiro. No PowerShell, por exemplo:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@//host:1521/service"
$env:DB_USERNAME = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
$env:JWT_SECRET = "chave_base64_de_pelo_menos_32_bytes"
$env:OPENAI_API_KEY = "sua_chave"
$env:RABBITMQ_URL = "amqps://usuario:senha@host/vhost"
.\mvnw.cmd spring-boot:run
```

Use valores locais; nunca coloque credenciais reais em comandos salvos,
exemplos versionados ou mensagens.

## 7. Configuração de ambiente

O `.env.example` documenta as variáveis aceitas:

| Variável | Finalidade |
| --- | --- |
| `DB_URL` | URL JDBC do Oracle |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciais do Oracle |
| `JWT_SECRET` | Chave Base64 para assinar/verificar JWT; mínimo de 32 bytes |
| `RABBITMQ_URL` | URI AMQPS do CloudAMQP, com credenciais e virtual host |
| `RABBITMQ_DEFAULT_USER`, `RABBITMQ_DEFAULT_PASS` | Usuário e senha do RabbitMQ local no Compose |
| `OPENAI_API_KEY` | Chave usada pelo Spring AI |
| `OPENAI_MODEL` | Modelo configurável; padrão `gpt-4o-mini` |
| `TRIAGEM_BASE_URL` | URL base do serviço Lares-IA |
| `TRIAGEM_API_KEY` | Chave enviada no header `X-API-Key` à triagem |
| `PORT` | Porta HTTP, fornecida pela plataforma de deploy |

No Compose local, a conexão usa o serviço `rabbitmq` e as credenciais
`RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS`; nesse modo, `RABBITMQ_URL`
fica vazio. No CloudAMQP, configure `RABBITMQ_URL`; a URI já inclui usuário,
senha, host, TLS e virtual host.

O `.env` está no `.gitignore`. Não compartilhe esse arquivo nem adicione
segredos ao repositório. Para produção, configure os secrets no painel do
provedor de hospedagem.

## 8. Autenticação e papéis

1. Cadastre um cidadão com `POST /auth/registro`.
2. Faça login em `POST /auth/login`.
3. Envie o JWT retornado nas rotas protegidas:

   ```http
   Authorization: Bearer <token>
   ```

O token tem validade de 24 horas. O cadastro público sempre define o papel
`CIDADAO`; o papel não pode ser escolhido pelo cliente. Usuários `GESTOR`
devem ser provisionados administrativamente.

Respostas de falha de autenticação e autorização são JSON com os campos
`status`, `erro`, `mensagem` e `campos`. O health check e a documentação Swagger
são públicos.

### Usuários de teste

Não há usuários de teste nem credenciais padrão criados pelas migrations.
Para testar o papel `CIDADAO`, cadastre uma conta em `POST /auth/registro` com
um e-mail e uma senha próprios e autentique-se em `POST /auth/login`. O cadastro
público não permite escolher o papel. Uma conta `GESTOR` precisa ser criada ou
provisionada administrativamente; não existe uma credencial de gestor pública
para este projeto.

## 9. Contrato da API

| Método | Rota | Acesso | Descrição |
| --- | --- | --- | --- |
| `POST` | `/auth/registro` | Público | Registrar cidadão |
| `POST` | `/auth/login` | Público | Autenticar e obter token |
| `GET` | `/usuarios/me` | Autenticado | Consultar usuário autenticado |
| `GET` | `/categorias` | Autenticado | Listar categorias |
| `POST` | `/ocorrencias` | `CIDADAO` | Registrar ocorrência |
| `GET` | `/ocorrencias/minhas?page=0&size=10` | `CIDADAO` | Listar ocorrências próprias |
| `GET` | `/ocorrencias?bairro=Centro&status=TRIADA&sort=prioridade&page=0&size=10` | Autenticado | Pesquisar ocorrências |
| `GET` | `/ocorrencias/{id}` | Autenticado | Consultar ocorrência e histórico |
| `PUT` | `/ocorrencias/{id}` | `CIDADAO`, titular | Editar enquanto `RECEBIDA` ou `TRIADA` |
| `DELETE` | `/ocorrencias/{id}` | `CIDADAO`, titular | Excluir enquanto `RECEBIDA` ou `TRIADA` |
| `PATCH` | `/ocorrencias/{id}/status` | `GESTOR` | Atualizar status e, opcionalmente, categoria |
| `GET` | `/actuator/health` | Público | Verificar saúde da aplicação |

Listas paginadas usam `page` iniciado em zero e `size` até 100, no formato
`{ itens, pagina, tamanho, totalItens, totalPaginas }`. `sort=prioridade`
ordena `ALTA`, `MEDIA`, `BAIXA` e, em seguida, pela abertura mais antiga.

### Exemplos de requisição

Cadastro:

```json
{
  "nome": "Ana Souza",
  "email": "ana@example.com",
  "senha": "senha-segura"
}
```

Nova ocorrência:

```json
{
  "descricao": "Buraco na via em frente à escola",
  "bairro": "Vila Mariana",
  "latitude": -23.588700,
  "longitude": -46.632300
}
```

Atualização de status:

```json
{
  "status": "EM_ANDAMENTO",
  "comentario": "Equipe encaminhada",
  "categoriaId": 1
}
```

Erros de validação, autenticação e autorização seguem a resposta padronizada da
API. Transições inválidas retornam `409 Conflict`.

## 10. Swagger / OpenAPI

Com a API em execução:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI do deploy: `https://laresapi-java.onrender.com/swagger-ui/index.html`

Use **Authorize** no Swagger UI e informe `Bearer <token>` para testar rotas
protegidas.

## 11. Deploy

O serviço está publicado em [https://laresapi-java.onrender.com](https://laresapi-java.onrender.com).
O blueprint `render.yaml` configura a imagem Docker, o deploy da branch `main` e
o health check em `/actuator/health`. Os valores secretos são configurados no
painel do Render.

O deploy e o teste integrado da triagem dependem de o serviço Lares-IA estar
publicado e de a conectividade do Oracle estar confirmada. A URL do serviço e
sua chave devem ser configuradas como variáveis do Render, nunca no repositório.

## 12. Limitações e próximos passos

- A integração de ponta a ponta com Lares-IA depende do deploy do serviço
  externo. Quando estiver disponível, validar contrato, autenticação,
  timeouts, categorias e comportamento de fallback.
- A conexão do Render com o Oracle da FIAP precisa ser confirmada. Se o acesso
  externo não for permitido, será necessário usar o banco alternativo definido
  pela equipe.
- Usuários de teste e gestores precisam ser provisionados no banco; não há
  credenciais de teste embutidas neste README.
- O fluxo completo com Oracle, RabbitMQ/CloudAMQP, OpenAI e aplicação Mobile
  ainda requer validação integrada.

## 13. Uso responsável e declaração de uso de IA

- O sistema armazena relatos e dados de localização fornecidos pelo cidadão;
  envie somente informações necessárias para registrar o problema urbano.
- A IA oferece apoio à triagem e à comunicação, não substitui a decisão do
  gestor. O gestor pode corrigir a categoria e atualizar o status.
- Resumos e mensagens podem conter erros. A equipe deve revisar decisões e não
  tratar o prazo estimado como garantia de atendimento.
- As chaves de OpenAI, Oracle, JWT e RabbitMQ devem ficar em variáveis de
  ambiente e secrets do provedor, não no código nem em logs.
- Spring AI / OpenAI é usado no fluxo de ocorrência para gerar um resumo à
  equipe e uma mensagem ao cidadão. A triagem de categoria, prioridade e prazo
  pertence ao serviço Lares-IA.
- **Declaração:** ferramentas de IA generativa foram utilizadas como apoio
  durante o desenvolvimento e o Spring AI/OpenAI é usado na funcionalidade
  descrita acima. As alterações foram revisadas e testadas pela equipe.
