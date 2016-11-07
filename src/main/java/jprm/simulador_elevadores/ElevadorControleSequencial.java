package jprm.simulador_elevadores;

import java.time.LocalDateTime;
import java.util.List;

public class ElevadorControleSequencial implements ElevadorControle {
	
	private List<Elevador> elevadores;
	private Integer index;
	
	@Override
	public void inicializar(List<Elevador> elevadores) {
		this.elevadores = elevadores;
		this.index = 0;
	}

	@Override
	public Elevador decisao(Pessoa p, LocalDateTime instanteAtual) {
		index += 1;
		return this.elevadores.get((index) % this.elevadores.size());
	}

	@Override
	public String getNome() {
		return "Controle Sequencial";
	}
}
