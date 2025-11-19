import React, { useEffect, useState } from "react";
import { API_BASE_URL } from "../config";
import "../styles/Matrizes.css";
import BotaoVoltar from "./BotaoVoltar";

export default function Matrizes() {
  const [matrizes, setMatrizes] = useState([]);
  const [tipo, setTipo] = useState("");
  const [cargaHoraria, setCargaHoraria] = useState("");
  const [turno, setTurno] = useState("INTEGRAL");
  const [editando, setEditando] = useState(null);
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    carregarMatrizes();
  }, []);

  const carregarMatrizes = async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${API_BASE_URL}/matrizes`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setMatrizes(data);
      } else {
        setMensagem("Erro ao carregar matrizes.");
      }
    } catch {
      setMensagem("Erro de conexão com o servidor.");
    }
  };

  const salvarMatriz = async (e) => {
    e.preventDefault();

    const metodo = editando ? "PUT" : "POST";
    const url = editando
      ? `${API_BASE_URL}/matrizes/${editando}`
      : `${API_BASE_URL}/matrizes`;

    try {
      const token = localStorage.getItem("token");
      const res = await fetch(url, {
        method: metodo,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ tipo, cargaHoraria, turno }),
      });

      if (res.ok) {
        setMensagem(editando ? "Matriz atualizada!" : "Matriz cadastrada!");
        setTipo("");
        setCargaHoraria("");
        setTurno("INTEGRAL");
        setEditando(null);
        carregarMatrizes();
      } else {
        setMensagem("Erro ao salvar matriz.");
      }
    } catch {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  const editarMatriz = (m) => {
    setEditando(m.id);
    setTipo(m.tipo);
    setCargaHoraria(m.cargaHoraria);
    setTurno(m.turno);
  };

  const deletarMatriz = async (id) => {
    if (window.confirm("Tem certeza que deseja excluir esta matriz?")) {
      try {
        const token = localStorage.getItem("token");
        const res = await fetch(`${API_BASE_URL}/matrizes/${id}`, {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) {
          setMensagem("Matriz excluída com sucesso!");
          carregarMatrizes();
        } else {
          setMensagem("Erro ao excluir matriz.");
        }
      } catch {
        setMensagem("Erro de conexão com o servidor.");
      }
    }
  };

  return (
    <div className="matrizes-container">
      <BotaoVoltar destino="/home" />
      <h2>📚 Gerenciar Matrizes</h2>

      <form className="matrizes-form" onSubmit={salvarMatriz}>
        <input
          type="text"
          placeholder="Tipo (ex: 6º ao 9º ano)"
          value={tipo}
          onChange={(e) => setTipo(e.target.value)}
          required
        />
        <input
          type="number"
          placeholder="Carga Horária"
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
        <button type="submit">
          {editando ? "Atualizar" : "Cadastrar"}
        </button>
      </form>

      {mensagem && <p className="mensagem">{mensagem}</p>}

      <table className="matrizes-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tipo</th>
            <th>Carga Horária</th>
            <th>Turno</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {matrizes.length === 0 ? (
            <tr>
              <td colSpan="5">Nenhuma matriz cadastrada.</td>
            </tr>
          ) : (
            matrizes
            .filter((m) => m && typeof m === "object")
            .map((m) => (
              <tr key={m.id}>
                <td>{m.id}</td>
                <td>{m.tipo}</td>
                <td>{m.cargaHoraria}h</td>
                <td>{m.turno}</td>
                <td>
                  <button onClick={() => editarMatriz(m)}>Editar</button>
                  <button onClick={() => deletarMatriz(m.id)}>Excluir</button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
