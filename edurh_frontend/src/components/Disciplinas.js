import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../config";
import "../styles/Disciplinas.css";
import BotaoVoltar from "./BotaoVoltar";

export default function Disciplinas() {
  const [disciplinas, setDisciplinas] = useState([]);
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editando, setEditando] = useState(null);

  const [nome, setNome] = useState("");
  const [cargaHoraria, setCargaHoraria] = useState("");

  const navigate = useNavigate();

  const tokenHeaders = () => ({
    Authorization: `Bearer ${localStorage.getItem("token")}`,
    "Content-Type": "application/json",
  });

  const carregarDisciplinas = useCallback(async () => {
    try {
      setLoading(true);
      setErro("");
      const res = await fetch(`${API_BASE_URL}/disciplinas`, {
        headers: tokenHeaders(),
      });
      if (!res.ok) {
        setErro("Erro ao carregar disciplinas.");
        return;
      }
      const data = await res.json();
      setDisciplinas(data);
    } catch (e) {
      console.error(e);
      setErro("Erro de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    carregarDisciplinas();
  }, [carregarDisciplinas]);

  const abrirNova = () => {
    setEditando(null);
    setNome("");
    setCargaHoraria("");
    setShowForm(true);
  };

  const abrirEditar = (disciplina) => {
    setEditando(disciplina);
    setNome(disciplina.nome || "");
    setCargaHoraria(disciplina.cargaHoraria || "");
    setShowForm(true);
  };

  const fecharForm = () => {
    setShowForm(false);
    setEditando(null);
    setNome("");
    setCargaHoraria("");
  };

  const salvarDisciplina = async (e) => {
    e.preventDefault();
    setErro("");

    try {
      const body = {
        nome,
        cargaHoraria: cargaHoraria ? parseInt(cargaHoraria, 10) : 0,
      };

      const url = editando
        ? `${API_BASE_URL}/disciplinas/${editando.id}`
        : `${API_BASE_URL}/disciplinas`;

      const method = editando ? "PUT" : "POST";

      const res = await fetch(url, {
        method,
        headers: tokenHeaders(),
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const txt = await res.text();
        alert("Erro ao salvar disciplina: " + (txt || res.status));
        return;
      }

      fecharForm();
      carregarDisciplinas();
    } catch (e) {
      console.error(e);
      alert("Erro de conexão ao salvar disciplina.");
    }
  };

  const excluirDisciplina = async (id) => {
    if (!window.confirm("Tem certeza que deseja excluir esta disciplina?")) {
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/disciplinas/${id}`, {
        method: "DELETE",
        headers: tokenHeaders(),
      });

      if (res.status === 204) {
        carregarDisciplinas();
      } else {
        const txt = await res.text();
        alert("Erro ao excluir disciplina: " + (txt || res.status));
      }
    } catch (e) {
      console.error(e);
      alert("Erro de conexão ao excluir disciplina.");
    }
  };

  const irParaDetalhes = (id) => {
    navigate(`/disciplinas/${id}`);
  };

  return (
    <div className="disciplinas-container">
      <BotaoVoltar destino="/home" />

      <h2 className="disciplinas-titulo">📚 Disciplinas</h2>

      <button className="disciplinas-nova" onClick={abrirNova}>
        ➕ Nova Disciplina
      </button>

      {erro && <p className="disciplinas-erro">{erro}</p>}
      {loading && <p>Carregando disciplinas...</p>}

      {showForm && (
        <div className="disciplinas-overlay">
          <div className="disciplinas-modal">
            <h3 className="disciplinas-modal-titulo">
              {editando ? "Editar Disciplina" : "Nova Disciplina"}
            </h3>
            <form onSubmit={salvarDisciplina}>
              <div className="disciplinas-form-group">
                <label>Nome da disciplina:</label>
                <input
                  className="disciplinas-input"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
              </div>

              <div className="disciplinas-form-group">
                <label>Carga horária (h):</label>
                <input
                  type="number"
                  className="disciplinas-input"
                  value={cargaHoraria}
                  onChange={(e) => setCargaHoraria(e.target.value)}
                  min="0"
                  required
                />
              </div>

              <div className="disciplinas-botoes">
                <button type="submit" className="disciplinas-salvar">
                  💾 Salvar
                </button>
                <button
                  type="button"
                  className="disciplinas-cancelar"
                  onClick={fecharForm}
                >
                  ↩ Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <table className="disciplinas-tabela">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Carga Horária</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {disciplinas
          .filter((d) => d && typeof d === "object")
          .map((d) => (
            <tr key={d.id}>
              <td className="disciplinas-td">{d.nome}</td>
              <td className="disciplinas-td">
                {d.cargaHoraria != null ? `${d.cargaHoraria} h` : "-"}
              </td>
              <td className="disciplinas-td">
                <button
                  className="disciplinas-acao-detalhes"
                  onClick={() => irParaDetalhes(d.id)}
                >
                  📋 Detalhes
                </button>
                <button
                  className="disciplinas-acao-editar"
                  onClick={() => abrirEditar(d)}
                >
                  ✏️ Editar
                </button>
                <button
                  className="disciplinas-acao-excluir"
                  onClick={() => excluirDisciplina(d.id)}
                >
                  🗑 Excluir
                </button>
              </td>
            </tr>
          ))}
          {disciplinas.length === 0 && !loading && (
            <tr>
              <td colSpan="3" className="disciplinas-td">
                Nenhuma disciplina cadastrada ainda.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
