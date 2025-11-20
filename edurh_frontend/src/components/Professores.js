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
  const [mensagem, setMensagem] = useState("");

  // Carregar lista
  useEffect(() => {
    carregarProfessores();
  }, []);

  const navigate = useNavigate();

  const carregarProfessores = async () => {
    const token = localStorage.getItem("token");

    try {
      const res = await fetch(`${API_BASE_URL}/professores`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Erro ao carregar professores.");

      const data = await res.json();
      setProfessores(data);
    } catch (err) {
      setMensagem("Erro ao carregar professores.");
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
                const chAtual = calcularCargaAtual(prof);   // calcula CH atual
                const excedeu = chAtual > (prof.cargaHoraria || 0); // verifica estouro

                return (
                  <tr
                    key={prof.id}
                    className={excedeu ? "professor-ultrapassado" : ""}
                  >
                    <td>{prof.id}</td>
                    <td>{prof.nome}</td>
                    <td>
                      {chAtual} per. / {prof.cargaHoraria} per.
                    </td>
                    <td>{prof.turno}</td>
                    <td>
                      <button onClick={() => editarProfessor(prof)}>Editar</button>
                      <button onClick={() => deletarProfessor(prof.id)}>Excluir</button>
                      <button onClick={() => navigate(`/professores/${prof.id}`)}>
                        Detalhes
                      </button>
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
