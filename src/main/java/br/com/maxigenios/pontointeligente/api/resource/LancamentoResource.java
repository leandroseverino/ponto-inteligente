package br.com.maxigenios.pontointeligente.api.resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.maxigenios.pontointeligente.api.domain.Funcionario;
import br.com.maxigenios.pontointeligente.api.domain.Lancamento;
import br.com.maxigenios.pontointeligente.api.dto.LancamentoDTO;
import br.com.maxigenios.pontointeligente.api.enums.TipoEnum;
import br.com.maxigenios.pontointeligente.api.service.FuncionarioService;
import br.com.maxigenios.pontointeligente.api.service.LancamentoService;
import br.com.maxigenios.pontointeligente.api.util.Response;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoResource {

	private static final Logger log = LoggerFactory.getLogger(LancamentoResource.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private LancamentoService lancamentoService;

	@Autowired
	private FuncionarioService funcionarioService;
	
	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;

	public LancamentoResource() {
	}

	/**
	 * Retorna a listagem de lançamentos de um funcionário.
	 * 
	 * @param funcionarioId
	 * @return ResponseEntity<Response<LancamentoDTO>>
	 */
	@GetMapping(value = "/funcionario/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDTO>>> listByFuncionarioId(
			@PathVariable("funcionarioId") Long funcionarioId,
			@RequestParam(value = "pag", defaultValue = "0") int pag,
			@RequestParam(value = "ord", defaultValue = "id") String ord,
			@RequestParam(value = "dir", defaultValue = "DESC") String dir) {
		
		log.info("Buscando lançamentos por ID do funcionário: {}, página: {}", funcionarioId, pag);
		Response<Page<LancamentoDTO>> response = new Response<Page<LancamentoDTO>>();

		PageRequest pageRequest = new PageRequest(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> listLancamentos = this.lancamentoService.findByFuncionarioId(funcionarioId, pageRequest);
		Page<LancamentoDTO> listLancamentosDTO = listLancamentos.map(lancamento -> this.convertLancamentoInDTO(lancamento));

		response.setData(listLancamentosDTO);
		return ResponseEntity.ok(response);
	}

	/**
	 * Retorna um lançamento por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<LancamentoDTO>>
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDTO>> listById(@PathVariable("id") Long id) {
		
		log.info("Buscando lançamento por ID: {}", id);
		
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		Optional<Lancamento> lancamento = this.lancamentoService.findById(id);

		if (!lancamento.isPresent()) {
		
			log.info("Lançamento não encontrado para o ID: {}", id);
			response.getErrors().add("Lançamento não encontrado para o id " + id);
			
			return ResponseEntity.badRequest().body(response);
		}

		response.setData(this.convertLancamentoInDTO(lancamento.get()));
		return ResponseEntity.ok(response);
	}

	/**
	 * Adiciona um novo lançamento.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<LancamentoDTO>>
	 * @throws ParseException 
	 */
	@PostMapping
	public ResponseEntity<Response<LancamentoDTO>> add(@Valid @RequestBody LancamentoDTO dto,
			BindingResult result) throws ParseException {
		
		log.info("Adicionando lançamento: {}", dto.toString());
		
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		validateFuncionario(dto, result);
		
		Lancamento lancamento = this.convertDTOInLancamento(dto, result);

		if (result.hasErrors()) {
			
			log.error("Erro validando lançamento: {}", result.getAllErrors());
			
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		lancamento = this.lancamentoService.save(lancamento);
		response.setData(this.convertLancamentoInDTO(lancamento));
		
		return ResponseEntity.ok(response);
	}

	/**
	 * Atualiza os dados de um lançamento.
	 * 
	 * @param id
	 * @param dto
	 * @return ResponseEntity<Response<Lancamento>>
	 * @throws ParseException 
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDTO>> update(
			@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDTO dto,
			BindingResult result) throws ParseException {
		
		log.info("Atualizando lançamento: {}", dto.toString());
		
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		
		validateFuncionario(dto, result);
		
		dto.setId(Optional.of(id));
		
		Lancamento lancamento = this.convertDTOInLancamento(dto, result);

		if (result.hasErrors()) {
			
			log.error("Erro validando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		lancamento = this.lancamentoService.save(lancamento);
		response.setData(this.convertLancamentoInDTO(lancamento));
		return ResponseEntity.ok(response);
	}

	/**
	 * Remove um lançamento por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<Lancamento>>
	 */
	@DeleteMapping(value = "/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> remove(@PathVariable("id") Long id) {
		
		log.info("Removendo lançamento: {}", id);
		Response<String> response = new Response<String>();
		
		Optional<Lancamento> lancamento = this.lancamentoService.findById(id);

		if (!lancamento.isPresent()) {
			log.info("Erro ao remover devido ao lançamento ID: {} ser inválido.", id);
			response.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o id " + id);
			return ResponseEntity.badRequest().body(response);
		}

		this.lancamentoService.remove(id);
		return ResponseEntity.ok(new Response<String>());
	}

	/**
	 * Valida um funcionário, verificando se ele é existente e válido no
	 * sistema.
	 * 
	 * @param dto
	 * @param result
	 */
	private void validateFuncionario(LancamentoDTO dto, BindingResult result) {
		
		if (dto.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcionário não informado."));
			return;
		}

		log.info("Validando funcionário id {}: ", dto.getFuncionarioId());
		
		Optional<Funcionario> funcionario = this.funcionarioService.findById(dto.getFuncionarioId());
		
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado. ID inexistente."));
		}
	}

	/**
	 * Converte uma entidade lançamento para seu respectivo DTO.
	 * 
	 * @param lancamento
	 * @return LancamentoDTO
	 */
	private LancamentoDTO convertLancamentoInDTO(Lancamento lancamento) {
		LancamentoDTO lancamentoDto = new LancamentoDTO();
		lancamentoDto.setId(Optional.of(lancamento.getId()));
		lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDto.setTipo(lancamento.getTipo().toString());
		lancamentoDto.setDescricao(lancamento.getDescricao());
		lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());

		return lancamentoDto;
	}

	/**
	 * Converte um LancamentoDTO para uma entidade Lancamento.
	 * 
	 * @param dto
	 * @param result
	 * @return Lancamento
	 * @throws ParseException 
	 */
	private Lancamento convertDTOInLancamento(LancamentoDTO dto, BindingResult result) throws ParseException {
		Lancamento lancamento = new Lancamento();

		if (dto.getId().isPresent()) {
			
			Optional<Lancamento> lancamentoOptional = this.lancamentoService.findById(dto.getId().get());
			
			if (lancamentoOptional.isPresent()) {
				lancamento = lancamentoOptional.get();
				
			} else {
				result.addError(new ObjectError("lancamento", "Lançamento não encontrado."));
			}
			
		} else {
			
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(dto.getFuncionarioId());
		}

		lancamento.setDescricao(dto.getDescricao());
		lancamento.setLocalizacao(dto.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(dto.getData()));

		if (EnumUtils.isValidEnum(TipoEnum.class, dto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(dto.getTipo()));
		} else {
			result.addError(new ObjectError("tipo", "Tipo inválido."));
		}

		return lancamento;
	}
}
