package jprm.simulador_elevadores;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		// Configuração usada somente durante testes
		//
		// Configuracao config = new Configuracao();
		// String conf = config.get("conf", "opcaoDefault");
		// logger.info(String.format("Configuração carregada: conf=%s", conf));

		// Capturar a lista 3 vezes para evitar problemas de referencia para a
		// mesma lista
		List<Pessoa> listaPessoas1 = (new PessoaLoader()).getListaPessoasResource();
		List<Pessoa> listaPessoas2 = (new PessoaLoader()).getListaPessoasResource();
		List<Pessoa> listaPessoas3 = (new PessoaLoader()).getListaPessoasResource();

		/**
		 * Simulação com controle sequencial
		 */
		ElevadorControle ctrlS = new ElevadorControleSequencial();
		Simulador sim1 = new Simulador(ctrlS);
		logger.info("Simulação com " + sim1.getElevadorControle().getNome());
		sim1.inicializar(listaPessoas1);

		while (sim1.processarInstante() == SimulacaoStatus.PROCESSANDO) {
			sim1.incrementarInstanteAtual();
		}
		logger.info("Simulação Finalizada");

		/**
		 * Simulação com controle Aleatório
		 */
		ElevadorControle ctrlA = new ElevadorControleAleatorio();
		Simulador sim2 = new Simulador(ctrlA);
		logger.info("Simulação com " + sim2.getElevadorControle().getNome());
		sim2.inicializar(listaPessoas2);

		while (sim2.processarInstante() == SimulacaoStatus.PROCESSANDO) {
			sim2.incrementarInstanteAtual();
		}
		logger.info("Simulação Finalizada");

		/**
		 * Simulação com controle menor tempo imediato
		 */
		ElevadorControle ctrlMTI = new ElevadorControleMenorTempoImediato();
		Simulador sim3 = new Simulador(ctrlMTI);
		logger.info("Simulação com " + sim3.getElevadorControle().getNome());
		sim3.inicializar(listaPessoas3);

		while (sim3.processarInstante() == SimulacaoStatus.PROCESSANDO) {
			sim3.incrementarInstanteAtual();
		}
		logger.info("Simulação Finalizada");

		/**
		 * Imprime estatisticas
		 */
		sim1.imprimeEstatisticasSimulacao();
		sim2.imprimeEstatisticasSimulacao();
		sim3.imprimeEstatisticasSimulacao();

		logger.info("Processo Finalizado");

	}

}
