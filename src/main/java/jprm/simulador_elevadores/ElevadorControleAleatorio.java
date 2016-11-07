package jprm.simulador_elevadores;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ElevadorControleAleatorio implements ElevadorControle {

	private List<Elevador> elevadores;
	
	@Override
	public void inicializar(List<Elevador> elevadores) {
		this.elevadores = elevadores;
	}

	@Override
	public Elevador decisao(Pessoa p, LocalDateTime instanteAtual) {
		Integer index = ThreadLocalRandom.current().nextInt(0, this.elevadores.size());
		return this.elevadores.get(index);
	}

	@Override
	public String getNome() {
		return "Controle Aleatório";
	}

}
