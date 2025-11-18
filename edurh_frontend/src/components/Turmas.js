import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../config";
import "../styles/Turmas.css";
import BotaoVoltar from "./BotaoVoltar";

export default function Turmas() {
  const [turmas, setTurmas] = useState([]);
  const [matrizes, setMatrizes] = useState([]);
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editando, setEditando] = useState(null);

  const [nome, setNome] = useState("");
  const [matrizId, setMatrizId] = useState("");

  const navigate = useNavigate();

  const tokenHeaders = () => ({
    Authorization: `Bearer ${localStorage.getItem("token")}`,
    "Content-Type": "application/json",
  });

  const carregarTurmas = useCallback( async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}/turmas`, {
        headers: tokenHeaders(),
      });
      if (!res.ok) {
        setErro("Erro ao carregar turmas.");
        return;
      }
      const data = await res.json();
      setTurmas(data);
    } catch (e) {
      console.error(e);
      setErro("Erro de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  }, []);

  const carregarMatrizes = useCallback( async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/matrizes`, {
        headers: tokenHeaders(),
      });
      if (res.ok) {
        const data = await res.json();
        setMatrizes(data);
      }
    } catch (e) {
      console.error("Erro ao carregar matrizes:", e);
    }
  }, []);

  useEffect(() => {
    carregarTurmas();
    carregarMatrizes();
  }, [carregarTurmas, carregarMatrizes]);

  const abrirNovo = () => {
    setEditando(null);
    setNome("");
    setMatrizId("");
    setShowForm(true);
  };

  const abrirEditar = (turma) => {
    setEditando(turma);
    setNome(turma.nome || "");
    setMatrizId(turma.matriz ? turma.matriz.id : "");
    setShowForm(true);
  };

  const fecharForm = () => {
    setShowForm(false);
    setEditando(null);
    setNome("");
    setMatrizId("");
  };

  const salvarTurma = async (e) => {
    e.preventDefault();
    setErro("");

    try {
      const body = {
        nome,
        matriz: matrizId ? { id: parseInt(matrizId, 10) } : null,
      };

      const url = editando
        ? `${API_BASE_URL}/turmas/${editando.id}`
        : `${API_BASE_URL}/turmas`;

      const method = editando ? "PUT" : "POST";

      const res = await fetch(url, {
        method,
        headers: tokenHeaders(),
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || `HTTP ${res.status}`);
      }

      await carregarTurmas();
      fecharForm();
    } catch (e) {
      console.error(e);
      setErro(`Erro ao salvar turma: ${e.message}`);
    }
  };

  const excluirTurma = async (id) => {
    if (!window.confirm("Tem certeza que deseja excluir esta turma?")) return;

    try {
      const res = await fetch(`${API_BASE_URL}/turmas/${id}`, {
        method: "DELETE",
        headers: tokenHeaders(),
      });
      if (res.ok || res.status === 204) {
        setTurmas((prev) => prev.filter((t) => t.id !== id));
      } else {
        const txt = await res.text();
        alert("Erro ao excluir turma: " + (txt || res.status));
      }
    } catch (e) {
      alert("Erro de conexão ao excluir turma.");
    }
  };

  const irParaDetalhes = (id) => {
    navigate(`/turmas/${id}`);
  };

  return (
    <div className="turmas-container">
      <BotaoVoltar destino="/home" />

      <h2 className="turmas-titulo">🏫 Turmas</h2>

      <div className="turmas-top-actions">
        <button className="turmas-nova-btn" onClick={abrirNovo}>
          ➕ Nova Turma
        </button>
      </div>

      {erro && <p className="turmas-erro">{erro}</p>}
      {loading && <p>Carregando turmas...</p>}

      {showForm && (
        <div className="turmas-overlay">
          <div className="turmas-modal">
            <h3 className="turmas-modal-titulo">
              {editando ? "Editar Turma" : "Nova Turma"}
            </h3>
            <form onSubmit={salvarTurma}>
              <div className="turmas-form-group">
                <label>Nome da turma:</label>
                <input
                  className="turmas-input"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
              </div>

              <div className="turmas-form-group">
                <label>Matriz:</label>
                <select
                  className="turmas-input"
                  value={matrizId}
                  onChange={(e) => setMatrizId(e.target.value)}
                >
                  <option value="">Selecione uma matriz</option>
                  {matrizes.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.tipo} ({m.turno})
                    </option>
                  ))}
                </select>
              </div>

              <div className="turmas-modal-actions">
                <button type="submit" className="turmas-salvar-btn">
                  💾 Salvar
                </button>
                <button
                  type="button"
                  className="turmas-cancelar-btn"
                  onClick={fecharForm}
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <table className="turmas-table">
        <thead>
          <tr>
            <th className="turmas-th">ID</th>
            <th className="turmas-th">Nome</th>
            <th className="turmas-th">Matriz</th>
            <th className="turmas-th">Ações</th>
          </tr>
        </thead>
        <tbody>
          {turmas.map((t) => (
            <tr key={t.id}>
              <td className="turmas-td">{t.id}</td>
              <td className="turmas-td">{t.nome}</td>
              <td className="turmas-td">{t.matriz ? t.matriz.tipo : "-"}</td>
              <td className="turmas-td">
                <button
                  className="turmas-acao-detalhes"
                  onClick={() => irParaDetalhes(t.id)}
                >
                  📋 Detalhes
                </button>
                <button
                  className="turmas-acao-editar"
                  onClick={() => abrirEditar(t)}
                >
                  ✏️ Editar
                </button>
                <button
                  className="turmas-acao-excluir"
                  onClick={() => excluirTurma(t.id)}
                >
                  🗑️ Excluir
                </button>
              </td>
            </tr>
          ))}
          {turmas.length === 0 && !loading && (
            <tr>
              <td colSpan="4" className="turmas-td">
                Nenhuma turma cadastrada ainda.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
