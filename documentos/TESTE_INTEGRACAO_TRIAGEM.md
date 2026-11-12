# Teste da integracao com Lares-IA

Este roteiro testa primeiro o endpoint de triagem com dados ficticios e depois
o fluxo completo pela API Java no Render. As requisicoes de ocorrencia criam
registros persistentes no banco; use somente os dados sinteticos do Postman ate
confirmar o funcionamento. A configuracao da integracao no Render e feita
previamente pelo responsavel pelo projeto; este roteiro comeca pelos testes.

## Arquivos Postman

- `Lares-Integracao-Triagem.postman_collection.json`: requisicoes e verificacoes.
- `Lares-Render.postman_environment.json`: URLs e variaveis locais do Postman.

## 1. Importar os arquivos no Postman

1. Instale e abra o [Postman](https://www.postman.com/downloads/).
2. Clique em **Import** e selecione os dois arquivos desta pasta.
3. No seletor de ambientes, escolha **Lares - Render**.
4. Abra **Environments** e selecione esse ambiente. Preencha `testPassword`
   com uma senha temporaria forte, com pelo menos 8 caracteres. Salve o valor
   como local/secret, sem exportar o ambiente ou sincronizar esse valor com o
   workspace.
5. Confirme que `apiBaseUrl` e `triagemBaseUrl` apontam para os enderecos
   preenchidos no ambiente importado.

## 2. Acordar os servicos e validar a triagem diretamente

1. Execute **1 - Health da API Java**. A resposta esperada e HTTP `200` com
   `{"status":"UP"}`. No plano gratuito, o primeiro acesso pode demorar enquanto
   o Render desperta a aplicacao; aguarde e tente novamente. Se o servico Java
   ainda nao tiver sido publicado pelo responsavel pelo projeto, continue o
   teste depois que estiver disponivel.
2. Execute **2 - Health do servico Lares-IA**. A resposta esperada e HTTP `200`.
   O primeiro acesso tambem pode levar aproximadamente um minuto.
3. Opcional: execute **3 - Testar endpoint de triagem diretamente**. Essa
   chamada direta exige uma chave de API fornecida pelo responsavel da Lares-IA;
   nao e necessario obter ou compartilhar essa chave para testar o fluxo pela
   API Java. Se nao tiver autorizacao para usar a chave, pule para a proxima
   secao. A requisicao envia
   somente um relato ficticio. O resultado deve ser HTTP `200` e conter:
   `categoria`, `prioridade`, `prazoEstimadoDias`, `confianca` e `origem`.
   Categoria deve corresponder a um codigo do seed; prioridade deve ser
   `BAIXA`, `MEDIA` ou `ALTA`; prazo deve ser positivo. `origem: "llm"` indica
   classificacao pela OpenAI; `origem: "fallback"` indica uso das regras locais.

Se a chamada direta autorizada retornar `401`, o responsavel pelo projeto deve
conferir a chave configurada no servico. `422` indica que a descricao enviada
nao atende ao contrato. Nunca copie a chave para o historico da requisicao ou
para um exemplo.

## 3. Testar a integracao completa atraves da API Java

Execute uma requisicao por vez, nesta ordem:

1. **4 - Registrar cidadao de teste**. Gera automaticamente um e-mail
   `@example.com` novo. Espere HTTP `201` e `papel: "CIDADAO"`.
2. **5 - Login do cidadao de teste**. Espere HTTP `200` e `papel: "CIDADAO"`.
   O script salva o JWT localmente no ambiente Postman.
3. **6 - Criar ocorrencia ficticia**. Envia descricao e bairro sinteticos, sem
   coordenadas ou dados de pessoa real. Espere HTTP `201`, `status: "RECEBIDA"`
   e um `id`. O script salva esse ID no ambiente.
4. Aguarde ate dois minutos para o consumidor RabbitMQ acordar o servico de IA
   e processar o evento.
5. Execute **7 - Verificar resultado da triagem**. Se a resposta ainda indicar
   `RECEBIDA`, aguarde mais um pouco e reenvie somente essa consulta. O resultado
   esperado e `TRIADA`, com `categoria.codigo`, `prioridade`,
   `prazoEstimadoDias` positivo e uma entrada `TRIADA` em `historico`.
6. Execute **8 - Consultar minhas ocorrencias** para confirmar que o registro
   tambem aparece na lista do cidadao autenticado.

O `resumoIa` e a `mensagemCidadao` podem ficar ausentes caso somente a geracao
de texto do Spring AI/OpenAI falhe; isso nao invalida a triagem de categoria,
prioridade e prazo.

## 4. Se a ocorrencia continuar `RECEBIDA`

1. Avise o responsavel pelo projeto para conferir o deploy do `lares-api` e as
   variaveis configuradas no ambiente do servico.
2. No Render, abra o servico `lares-api` e consulte **Logs** no horario do teste.
   A mensagem `Falha ao triar a ocorrencia` registra a causa sem exigir alterar
   os dados da ocorrencia.
3. `401` no log normalmente indica `TRIAGEM_API_KEY` ausente ou incorreta.
4. Timeout pode acontecer no primeiro despertar do plano gratuito. Abra
   `https://lares-ia.onrender.com/health`, aguarde a resposta e crie uma nova
   ocorrencia ficticia para testar novamente.
5. Erro de conexao com RabbitMQ ou Oracle precisa ser resolvido no ambiente do
   Render antes de repetir o fluxo. A ocorrencia criada nao e reprocessada
   automaticamente por este roteiro.

## 5. Passar a dados reais

Somente depois dos testes ficticios passarem, substitua a descricao e o bairro
da requisicao **6 - Criar ocorrencia ficticia** por um caso real autorizado pela
equipe. Evite nomes, telefones, enderecos residenciais e outros dados pessoais
desnecessarios. Lembre que as ocorrencias sao gravadas no banco e podem ser
visiveis a gestores.
