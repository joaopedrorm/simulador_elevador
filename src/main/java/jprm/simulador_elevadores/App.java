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

		Configuracao config = new Configuracao();
		String conf = config.get("conf", "opcaoDefault");
		logger.info(String.format("Configuração carregada: conf=%s", conf));
		
		List<Pessoa> listaPessoas = (new PessoaLoader()).getListaPessoasResource();
		
		logger.info(String.format("listaPesoas=%s", listaPessoas));
	}
}
