package jprm.simulador_elevadores;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

public class PessoaLoader {

	private String filename;

	private ClassLoader classLoader;

	private DateTimeFormatter dateTimeFormat;

	private static final Logger logger = LoggerFactory.getLogger(PessoaLoader.class);

	private static final String filenameDefault = "elevadores.csv";

	private static final String dateTimeFormatDefault = "yyyy-MM-dd HH:mm:ss";

	public PessoaLoader() {
		this.filename = filenameDefault;
		// https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
		this.dateTimeFormat = DateTimeFormatter.ofPattern(dateTimeFormatDefault);
		this.classLoader = this.getClass().getClassLoader();
	}

	public PessoaLoader(String arquivo, DateTimeFormatter padrao) {
		this.filename = arquivo;
		this.dateTimeFormat = padrao;
		this.classLoader = this.getClass().getClassLoader();
	}

	public PessoaLoader(String arquivo) {
		this.filename = arquivo;
		this.dateTimeFormat = DateTimeFormatter.ofPattern(dateTimeFormatDefault);
		this.classLoader = this.getClass().getClassLoader();
	}

	public List<Pessoa> getListaPessoas() {
		try (InputStream in = Files.newInputStream(Paths.get(this.filename))){
		return getListaPessoas(in);
		} catch (IOException e) {
			logger.error(String.format("Erro ao carregar %s", this.filename), e);
			return Collections.emptyList();
		}
	}

	public List<Pessoa> getListaPessoasResource() {
		try (InputStream in = this.classLoader.getResource(this.filename).openStream()){
			return getListaPessoas(in);	
		} catch (IOException e) {
			logger.error(String.format("Erro ao carregar %s", this.filename), e);
			return Collections.emptyList();
		}
		
	}

	private List<Pessoa> getListaPessoas(InputStream in) {
		return lerCSV(in).stream().filter(Objects::nonNull).filter(l -> l.size() >= 3).map(this::mapper)
				.collect(Collectors.toList());
	}

	private Pessoa mapper(List<String> tokens) {
		// nome, instante, andar
		String nome = tokens.get(0);
		Integer andar = Integer.parseInt(tokens.get(2));
		LocalDateTime instante = LocalDateTime.parse(tokens.get(1), this.dateTimeFormat);
		return new Pessoa(nome, andar, instante);
	}

	private List<List<String>> lerCSV(InputStream in) {
		try (ICsvListReader listReader = new CsvListReader(new InputStreamReader(in),
				CsvPreference.STANDARD_PREFERENCE)) {

			List<List<String>> listaLinhasCSV = new LinkedList<>();
			List<String> tokens;
			while ((tokens = listReader.read()) != null) {
				listaLinhasCSV.add(tokens);
			}
			return listaLinhasCSV;

		} catch (IOException e) {
			logger.error(String.format("Erro ao carregar %s", this.filename), e);
			return Collections.emptyList();
		}
	}

}
