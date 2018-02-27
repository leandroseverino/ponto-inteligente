package br.com.maxigenios.pontointeligente.api.service;

import java.util.Optional;

import br.com.maxigenios.pontointeligente.api.domain.Empresa;

public interface EmpresaService {

	/**
	 * Retorna uma empresa dado um CNPJ.
	 * 
	 * @param cnpj
	 * @return Optional<Empresa>
	 */
	Optional<Empresa> findByCnpj(String cnpj);
	
	/**
	 * Cadastra uma nova empresa na base de dados.
	 * 
	 * @param empresa
	 * @return Empresa
	 */
	Empresa save(Empresa empresa);
	
}
