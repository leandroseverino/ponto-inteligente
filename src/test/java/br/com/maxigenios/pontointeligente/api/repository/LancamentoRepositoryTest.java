package br.com.maxigenios.pontointeligente.api.repository;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.maxigenios.pontointeligente.api.domain.Empresa;
import br.com.maxigenios.pontointeligente.api.domain.Funcionario;
import br.com.maxigenios.pontointeligente.api.domain.Lancamento;
import br.com.maxigenios.pontointeligente.api.enums.PerfilEnum;
import br.com.maxigenios.pontointeligente.api.enums.TipoEnum;
import br.com.maxigenios.pontointeligente.api.util.PasswordUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class LancamentoRepositoryTest {
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	private Long funcionarioId;

	@Before
	public void setUp() throws Exception {
		Empresa empresa = this.empresaRepository.save(getEmpresa());
		
		Funcionario funcionario = this.funcionarioRepository.save(getFuncionario(empresa));
		this.funcionarioId = funcionario.getId();
		
		this.lancamentoRepository.save(getLancamento(funcionario));
		this.lancamentoRepository.save(getLancamento(funcionario));
	}

	@After
	public void tearDown() throws Exception {
		this.empresaRepository.deleteAll();
		this.funcionarioRepository.deleteAll();
		this.lancamentoRepository.deleteAll();
	}

	@Test
	public void testFindLancamentosByFuncionarioId() {
		List<Lancamento> lancamentos = this.lancamentoRepository.findByFuncionarioId(funcionarioId);
		
		assertEquals(2, lancamentos.size());
	}
	
	@Test
	public void testFindPageableLancamentosByFuncionarioId() {
		PageRequest page = new PageRequest(0, 10);
		Page<Lancamento> lancamentos = this.lancamentoRepository.findByFuncionarioId(funcionarioId, page);
		
		// assertEquals(2, lancamentos.getTotalElements());
		
		assertEquals(2, lancamentos.getContent().size());
	}
	
	private Lancamento getLancamento(Funcionario funcionario) {
		Lancamento lancameto = new Lancamento();
		lancameto.setData(new Date());
		lancameto.setTipo(TipoEnum.INICIO_ALMOCO);
		lancameto.setFuncionario(funcionario);
		return lancameto;
	}

	private Funcionario getFuncionario(Empresa empresa) throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome("Fulano de Tal");
		funcionario.setPerfil(PerfilEnum.ROLE_USER);
		funcionario.setSenha(PasswordUtils.generateBCrypt("123456"));
		funcionario.setCpf("24291173474");
		funcionario.setEmail("email@email.com");
		funcionario.setEmpresa(empresa);
		return funcionario;
	}

	private Empresa getEmpresa() {
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Empresa de exemplo");
		empresa.setCnpj("51463645000100");
		return empresa;
	}

}
