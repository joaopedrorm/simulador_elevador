package jprm.simulador_elevadores;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 *
 */
public class App {
	
	private static final Logger logger = LoggerFactory.getLogger(Configuracao.class);
	
	public static void main(String[] args) {

		Configuracao config = new Configuracao();
		String conf = config.get("conf", "opcaoDefault");
		logger.info(String.format("Configuração carregada: conf=%s", conf));
		
		
		PessoaLoader loader = new PessoaLoader();
		List<Pessoa> listaPessoas = loader.getListaPessoasResource();
		
		logger.info(String.format("listaPesoas=%s", listaPessoas));
	}
}
