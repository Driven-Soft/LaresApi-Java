package br.com.fiap.laresapi.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import br.com.fiap.laresapi.dto.ai.ConteudoOcorrenciaIa;

@Service
public class ConteudoOcorrenciaAiService {

	private static final String SYSTEM_PROMPT = """
			Você auxilia uma prefeitura a comunicar ocorrências de serviços urbanos.
			Gere um resumo curto e objetivo para a equipe municipal e uma mensagem clara,
			acolhedora e respeitosa para o cidadão.
			Não invente prazos, ações, protocolos ou fatos que não estejam nos dados.
			Responda somente com os campos resumoIa e mensagemCidadao.
			""";

	private final ChatClient chatClient;

	public ConteudoOcorrenciaAiService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	public ConteudoOcorrenciaIa gerar(
			String descricao,
			String bairro,
			String categoria,
			String prioridade,
			Integer prazoEstimadoDias) {
		ConteudoOcorrenciaIa conteudo = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(user -> user.text("""
						Prepare os textos para esta ocorrência urbana:
						Descrição: {descricao}
						Bairro: {bairro}
						Categoria: {categoria}
						Prioridade: {prioridade}
						Prazo estimado em dias: {prazo}
						""")
						.param("descricao", descricao)
						.param("bairro", bairro)
						.param("categoria", categoria)
						.param("prioridade", prioridade)
						.param("prazo", prazoEstimadoDias))
				.call()
				.entity(ConteudoOcorrenciaIa.class);

		if (conteudo == null
				|| conteudo.resumoIa() == null
				|| conteudo.resumoIa().isBlank()
				|| conteudo.mensagemCidadao() == null
				|| conteudo.mensagemCidadao().isBlank()
				|| conteudo.resumoIa().length() > 1000
				|| conteudo.mensagemCidadao().length() > 1000) {
			throw new IllegalStateException("A IA retornou textos vazios ou acima do limite permitido");
		}

		return new ConteudoOcorrenciaIa(conteudo.resumoIa().trim(), conteudo.mensagemCidadao().trim());
	}
}
