package br.com.maxigenios.pontointeligente.api.resource;

import java.security.NoSuchAlgorithmException;

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
import br.com.maxigenios.pontointeligente.api.dto.CadastroPJDTO;
import br.com.maxigenios.pontointeligente.api.enums.PerfilEnum;
import br.com.maxigenios.pontointeligente.api.service.EmpresaService;
import br.com.maxigenios.pontointeligente.api.service.FuncionarioService;
import br.com.maxigenios.pontointeligente.api.util.PasswordUtils;
import br.com.maxigenios.pontointeligente.api.util.Response;

@RestController
@RequestMapping("/api/cadastrar-pj")
@CrossOrigin(origins = "*")
public class CadastroPJResource {

	private static final Logger log = LoggerFactory.getLogger(CadastroPJResource.class);

	@Autowired
	private FuncionarioService funcionarioService;

	@Autowired
	private EmpresaService empresaService;

	public CadastroPJResource() {
	}

	/**
	 * Cadastra uma pessoa jurídica no sistema.
	 * 
	 * @param dto
	 * @param result
	 * @return ResponseEntity<Response<CadastroPJDTO>>
	 * @throws NoSuchAlgorithmException
	 */
	@PostMapping
	public ResponseEntity<Response<CadastroPJDTO>> persist(@Valid @RequestBody CadastroPJDTO dto,
			BindingResult result) throws NoSuchAlgorithmException {
		
		log.info("Cadastrando PJ: {}", dto.toString());
		Response<CadastroPJDTO> response = new Response<CadastroPJDTO>();

		validateExistentData(dto, result);
		Empresa empresa = this.convertDTOInEmpresa(dto);
		Funcionario funcionario = this.convertDTOInFuncionario(dto, result);

		if (result.hasErrors()) {
			log.error("Erro validando dados de cadastro PJ: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		this.empresaService.save(empresa);
		funcionario.setEmpresa(empresa);
		this.funcionarioService.save(funcionario);

		response.setData(this.convertFuncionarioToDTO(funcionario));
		return ResponseEntity.ok(response);
	}

	/**
	 * Verifica se a empresa ou funcionário já existem na base de dados.
	 * 
	 * @param dto
	 * @param result
	 */
	private void validateExistentData(CadastroPJDTO dto, BindingResult result) {
		this.empresaService.findByCnpj(dto.getCnpj())
				.ifPresent(emp -> result.addError(new ObjectError("empresa", "Empresa com este CNPJ já existente.")));

		this.funcionarioService.findByCpf(dto.getCpf())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "CPF já existente.")));

		this.funcionarioService.findByEmail(dto.getEmail())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}

	/**
	 * Converte os dados do DTO para empresa.
	 * 
	 * @param dto
	 * @return Empresa
	 */
	private Empresa convertDTOInEmpresa(CadastroPJDTO dto) {
		Empresa empresa = new Empresa();
		empresa.setCnpj(dto.getCnpj());
		empresa.setRazaoSocial(dto.getRazaoSocial());

		return empresa;
	}

	/**
	 * Converte os dados do DTO para funcionário.
	 * 
	 * @param dto
	 * @param result
	 * @return Funcionario
	 * @throws NoSuchAlgorithmException
	 */
	private Funcionario convertDTOInFuncionario(CadastroPJDTO dto, BindingResult result)
			throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(dto.getNome());
		funcionario.setEmail(dto.getEmail());
		funcionario.setCpf(dto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.generateBCrypt(dto.getSenha()));

		return funcionario;
	}

	/**
	 * Popula o DTO de cadastro com os dados do funcionário e empresa.
	 * 
	 * @param funcionario
	 * @return CadastroPJDTO
	 */
	private CadastroPJDTO convertFuncionarioToDTO(Funcionario funcionario) {
		CadastroPJDTO dto = new CadastroPJDTO();
		dto.setId(funcionario.getId());
		dto.setNome(funcionario.getNome());
		dto.setEmail(funcionario.getEmail());
		dto.setCpf(funcionario.getCpf());
		dto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		dto.setCnpj(funcionario.getEmpresa().getCnpj());

		return dto;
	}
}
