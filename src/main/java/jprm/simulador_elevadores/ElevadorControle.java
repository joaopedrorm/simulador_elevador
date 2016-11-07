package jprm.simulador_elevadores;

import java.time.LocalDateTime;
import java.util.List;

public interface ElevadorControle {
	public void inicializar(List<Elevador> elevadores);

	public Elevador decisao(Pessoa p, LocalDateTime instanteAtual);
	
	public String getNome();
}
