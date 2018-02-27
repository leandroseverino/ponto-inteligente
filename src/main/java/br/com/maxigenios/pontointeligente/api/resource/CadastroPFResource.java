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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.maxigenios.pontointeligente.api.domain.Empresa;
import br.com.maxigenios.pontointeligente.api.domain.Funcionario;
import br.com.maxigenios.pontointeligente.api.dto.CadastroPFDTO;
import br.com.maxigenios.pontointeligente.api.enums.PerfilEnum;
import br.com.maxigenios.pontointeligente.api.service.EmpresaService;
import br.com.maxigenios.pontointeligente.api.service.FuncionarioService;
import br.com.maxigenios.pontointeligente.api.util.PasswordUtils;
import br.com.maxigenios.pontointeligente.api.util.Response;

@RestController
@RequestMapping("/api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFResource {

private static final Logger log = LoggerFactory.getLogger(CadastroPFResource.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private FuncionarioService funcionarioService;

	public CadastroPFResource() {
	}

	/**
	 * Cadastra um funcionário pessoa física no sistema.
	 * 
	 * @param dto
	 * @param result
	 * @return ResponseEntity<Response<CadastroPFDTO>>
	 * @throws NoSuchAlgorithmException
	 */
	@PostMapping
	public ResponseEntity<Response<CadastroPFDTO>> persist(@Valid @RequestBody CadastroPFDTO dto,
			BindingResult result) throws NoSuchAlgorithmException {
		log.info("Cadastrando PF: {}", dto.toString());
		Response<CadastroPFDTO> response = new Response<CadastroPFDTO>();

		validateExistentData(dto, result);
		Funcionario funcionario = this.convertDTOInFuncionario(dto, result);

		if (result.hasErrors()) {
			log.error("Erro validando dados de cadastro PF: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		Optional<Empresa> empresa = this.empresaService.findByCnpj(dto.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.save(funcionario);

		response.setData(this.convertFuncionarioInDTO(funcionario));
		return ResponseEntity.ok(response);
	}

	/**
	 * Verifica se a empresa está cadastrada e se o funcionário não existe na base de dados.
	 * 
	 * @param dto
	 * @param result
	 */
	private void validateExistentData(CadastroPFDTO dto, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.findByCnpj(dto.getCnpj());
		if (!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa não cadastrada."));
		}
		
		this.funcionarioService.findByCpf(dto.getCpf())
			.ifPresent(func -> result.addError(new ObjectError("funcionario", "CPF já existente.")));

		this.funcionarioService.findByEmail(dto.getEmail())
			.ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}

	/**
	 * Converte os dados do DTO para funcionário.
	 * 
	 * @param dto
	 * @param result
	 * @return Funcionario
	 * @throws NoSuchAlgorithmException
	 */
	private Funcionario convertDTOInFuncionario(CadastroPFDTO dto, BindingResult result)
			throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(dto.getNome());
		funcionario.setEmail(dto.getEmail());
		funcionario.setCpf(dto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_USER);
		funcionario.setSenha(PasswordUtils.generateBCrypt(dto.getSenha()));
		dto.getQtdHorasAlmoco()
				.ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		dto.getQtdHorasTrabalhoDia()
				.ifPresent(qtdHorasTrabDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabDia)));
		dto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));

		return funcionario;
	}

	/**
	 * Popula o DTO de cadastro com os dados do funcionário e empresa.
	 * 
	 * @param funcionario
	 * @return CadastroPFDTO
	 */
	private CadastroPFDTO convertFuncionarioInDTO(Funcionario funcionario) {
		CadastroPFDTO dto = new CadastroPFDTO();
		dto.setId(funcionario.getId());
		dto.setNome(funcionario.getNome());
		dto.setEmail(funcionario.getEmail());
		dto.setCpf(funcionario.getCpf());
		dto.setCnpj(funcionario.getEmpresa().getCnpj());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(qtdHorasAlmoco -> dto
				.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
				qtdHorasTrabDia -> dto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabDia))));
		funcionario.getValorHoraOpt()
				.ifPresent(valorHora -> dto.setValorHora(Optional.of(valorHora.toString())));

		return dto;
	}
}
