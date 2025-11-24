import React, { useEffect, useState } from "react";
import { API_BASE_URL } from "../config";
import "../styles/Professores.css";
import { useNavigate } from "react-router-dom";
import BotaoVoltar from "./BotaoVoltar";


export default function Professores() {
  const [professores, setProfessores] = useState([]);
  const [busca, setBusca] = useState("");
  const [nome, setNome] = useState("");
  const [cargaHoraria, setCargaHoraria] = useState("");
  const [turno, setTurno] = useState("MANHA");
  const [editando, setEditando] = useState(null);
  // Mapa para armazenar a CH total em turmas por ID de professor
  const [chTotalEmTurmas, setChTotalEmTurmas] = useState({});
  const [mensagem, setMensagem] = useState("");

  // Carregar lista
  useEffect(() => {
    carregarProfessores();
  }, []);

  const navigate = useNavigate();

  const carregarProfessores = async () => {
    const token = localStorage.getItem("token");
    setMensagem(""); // Limpa mensagem de erro antes de buscar

    try {
      // 1. Busca a lista básica de professores
      const [resProf, resRelatorio] = await Promise.all([
        fetch(`${API_BASE_URL}/professores`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }),
        // 2. Busca o relatório detalhado de distribuição de CH
        fetch(`${API_BASE_URL}/professores/relatorio/distribuicao-ch-turno`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (!resProf.ok) throw new Error("Erro ao carregar professores.");
      if (!resRelatorio.ok) throw new Error("Erro ao carregar relatório de CH.");

      const dataProf = await resProf.json();
      const dataRelatorio = await resRelatorio.json();

      setProfessores(dataProf);

      // Mapeia o resultado do relatório: professorId -> soma totalPeriodos
      const mapChTotal = dataRelatorio.reduce((acc, item) => {
        // Assume que o backend retorna o professorId em algum lugar do objeto.
        // O backend atual não retorna o ID, apenas o nome! 
        // 🚨 Por enquanto, vamos usar o NOME do professor como chave, 
        // mas o ideal seria usar o ID (e modificar o backend para incluí-lo)
        
        // Se o professor já estiver no mapa, atualiza a CH TOTAL
        // (necessário porque o relatório tem uma linha para cada turno/período)
        const currentTotal = acc[item.professor] || 0;
        acc[item.professor] = currentTotal + (item.totalPeriodos || 0);

        return acc;
      }, {});
      
      // Armazena o mapa (Nome do Professor -> CH Total em Turmas)
      setChTotalEmTurmas(mapChTotal); 

    } catch (err) {
      console.error(err);
      setMensagem(err.message || "Erro ao carregar dados.");
    }
  };

  const salvarProfessor = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");

    const metodo = editando ? "PUT" : "POST";
    const url = editando
      ? `${API_BASE_URL}/professores/${editando}`
      : `${API_BASE_URL}/professores`;

    try {
      const res = await fetch(url, {
        method: metodo,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ nome, cargaHoraria, turno }),
      });

      if (res.ok) {
        setMensagem(editando ? "Professor atualizado!" : "Professor cadastrado!");
        setNome("");
        setCargaHoraria("");
        setTurno("MANHA");
        setEditando(null);
        carregarProfessores();
      } else if (res.status === 403) {
        setMensagem("Acesso negado. Faça login novamente.");
      } else {
        setMensagem("Erro ao salvar professor.");
      }
    } catch {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  const editarProfessor = (prof) => {
    setEditando(prof.id);
    setNome(prof.nome);
    setCargaHoraria(prof.cargaHoraria);
    setTurno(prof.turno);
  };

  const deletarProfessor = async (id) => {
    const token = localStorage.getItem("token");

    if (window.confirm("Tem certeza que deseja excluir este professor?")) {
      try {
        const res = await fetch(`${API_BASE_URL}/professores/${id}`, {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (res.ok) {
          carregarProfessores();
        } else {
          setMensagem("Erro ao excluir professor.");
        }
      } catch {
        setMensagem("Erro ao conectar com o servidor.");
      }
    }
  };

  // Soma a carga horária das disciplinas do professor
  const calcularCargaAtual = (prof) => {
    if (!prof.disciplinas || !Array.isArray(prof.disciplinas)) return 0;

    return prof.disciplinas
      .filter((d) => d && typeof d === "object")
      .reduce((total, d) => total + (d.cargaHoraria || 0), 0);
  };

  // Calcula o máximo de períodos permitidos com base na RT
  const calcularMaxPeriodosRT = (rt) => {
    if (!rt || rt <= 0) return 0;

    switch (rt) {
      case 20:
        return 16;
      case 30:
        return 24;
      case 40:
        return 32;
      default:
        return Math.round(rt * 0.8);
    }
  };


  return (
    <div className="prof-container">
        <BotaoVoltar destino="/home" />
      <h2>👩‍🏫 Gerenciar Professores</h2>

      <input
        type="text"
        placeholder="🔍 Buscar professor por nome..."
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        style={{
          width: "100%",
          padding: "10px",
          marginBottom: "20px",
          borderRadius: "8px",
          border: "1px solid #ccc",
          boxSizing: "border-box",
        }}
      />

      <form className="prof-form" onSubmit={salvarProfessor}>
        <input
          type="text"
          placeholder="Nome do professor"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          required
        />
        <input
          type="number"
          placeholder="Carga Horária Máxima (períodos)"
          value={cargaHoraria}
          onChange={(e) => setCargaHoraria(e.target.value)}
          required
        />
        <select value={turno} onChange={(e) => setTurno(e.target.value)}>
          <option value="INTEGRAL">Integral</option>
          <option value="MANHA">Manhã</option>
          <option value="TARDE">Tarde</option>
          <option value="NOTURNO">Noturno</option>
        </select>
        <button type="submit">{editando ? "Atualizar" : "Cadastrar"}</button>
      </form>

      {mensagem && <p className="mensagem">{mensagem}</p>}

      <table className="prof-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Carga Horária (Períodos)</th>
            <th>Turno</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {professores.length === 0 ? (
            <tr>
              <td colSpan="5">Nenhum professor cadastrado.</td>
            </tr>
          ) : (
            professores
              .filter((prof) => prof && typeof prof === "object")
              .filter((prof) =>
                prof.nome.toLowerCase().includes(busca.toLowerCase())
              )
              .map((prof) => {
                // A CH que o professor tem vinculada diretamente a ele (calculado localmente)
                // const chAtual = calcularCargaAtual(prof); 
                
                //NOVO VALOR: CH total em turmas, buscado do relatório
                const chEmTurmas = chTotalEmTurmas[prof.nome] || 0;

                const maxPeriodos = calcularMaxPeriodosRT(prof.cargaHoraria || 0);
                
                // Agora, usamos a CH em turmas para verificar se excedeu
                const excedeu = chEmTurmas > maxPeriodos;

                return (
                  <tr
                    key={prof.id}
                    className={excedeu ? "professor-ultrapassado" : ""}
                  >
                    <td>{prof.id}</td>
                    <td>{prof.nome}</td>
                    <td>
                      {/* Mostra a CH em Turmas / Limite */}
                      {chEmTurmas} períodos / {maxPeriodos} períodos{" "} 
                      {prof.cargaHoraria != null && (
                        <span style={{ fontSize: "0.85rem" }}>
                          (RT {prof.cargaHoraria}h)
                        </span>
                      )}
                    </td>
                    <td>{prof.turno}</td>
                    <td>
                      <button className="btn-editar" onClick={() => editarProfessor(prof)}>Editar</button>
                      <button className="btn-excluir" onClick={() => deletarProfessor(prof.id)}>Excluir</button>
                      <button className="btn-detalhes" onClick={() => navigate(`/professores/${prof.id}`)}>Detalhes</button>
                    </td>
                  </tr>
                );
              })
          )}
        </tbody>
      </table>
    </div>
  );
}
