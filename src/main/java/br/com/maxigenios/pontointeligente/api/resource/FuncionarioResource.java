package br.com.maxigenios.pontointeligente.api.resource;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.maxigenios.pontointeligente.api.domain.Funcionario;
import br.com.maxigenios.pontointeligente.api.dto.FuncionarioDTO;
import br.com.maxigenios.pontointeligente.api.service.FuncionarioService;
import br.com.maxigenios.pontointeligente.api.util.PasswordUtils;
import br.com.maxigenios.pontointeligente.api.util.Response;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioResource {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioResource.class);

	@Autowired
	private FuncionarioService funcionarioService;

	public FuncionarioResource() {
	}

	/**
	 * Atualiza os dados de um funcionário.
	 * 
	 * @param id
	 * @param dto
	 * @param result
	 * @return ResponseEntity<Response<FuncionarioDTO>>
	 * @throws NoSuchAlgorithmException
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<FuncionarioDTO>> updateById(@PathVariable("id") Long id,
			@Valid @RequestBody FuncionarioDTO dto, BindingResult result) throws NoSuchAlgorithmException {
		
		log.info("Atualizando funcionário: {}", dto.toString());
		Response<FuncionarioDTO> response = new Response<FuncionarioDTO>();

		Optional<Funcionario> funcionario = this.funcionarioService.findById(id);
		
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado."));
		}

		this.updateFuncionarioData(funcionario.get(), dto, result);

		if (result.hasErrors()) {
			log.error("Erro validando funcionário: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		this.funcionarioService.save(funcionario.get());
		response.setData(this.convertFuncionarioInDTO(funcionario.get()));

		return ResponseEntity.ok(response);
	}

	/**
	 * Atualiza os dados do funcionário com base nos dados encontrados no DTO.
	 * 
	 * @param funcionario
	 * @param dto
	 * @param result
	 * @throws NoSuchAlgorithmException
	 */
	private void updateFuncionarioData(Funcionario funcionario, FuncionarioDTO dto, BindingResult result)
			throws NoSuchAlgorithmException {
		
		funcionario.setNome(dto.getNome());

		if (!funcionario.getEmail().equals(dto.getEmail())) {
			this.funcionarioService
					.findByEmail(dto.getEmail())
					.ifPresent(func -> result.addError(new ObjectError("email", "Email já existente.")));
			funcionario.setEmail(dto.getEmail());
		}

		funcionario.setQtdHorasAlmoco(null);
		dto.getQtdHorasAlmoco()
				.ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));

		funcionario.setQtdHorasTrabalhoDia(null);
		dto.getQtdHorasTrabalhoDia()
				.ifPresent(qtdHorasTrabDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabDia)));

		funcionario.setValorHora(null);
		dto.getValorHora().
			ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));

		if (dto.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtils.generateBCrypt(dto.getSenha().get()));
		}
	}

	/**
	 * Retorna um DTO com os dados de um funcionário.
	 * 
	 * @param funcionario
	 * @return FuncionarioDTO
	 */
	private FuncionarioDTO convertFuncionarioInDTO(Funcionario funcionario) {
		FuncionarioDTO dto = new FuncionarioDTO();
		dto.setId(funcionario.getId());
		dto.setEmail(funcionario.getEmail());
		dto.setNome(funcionario.getNome());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(
				qtdHorasAlmoco -> dto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
				qtdHorasTrabDia -> dto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabDia))));
		funcionario.getValorHoraOpt()
				.ifPresent(valorHora -> dto.setValorHora(Optional.of(valorHora.toString())));

		return dto;
	}
}
